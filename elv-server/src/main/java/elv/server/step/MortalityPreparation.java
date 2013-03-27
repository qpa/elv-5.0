package elv.server.step;

import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Node;
import elv.common.params.Param;
import elv.common.params.Resolution;
import elv.common.params.Territory;
import elv.common.params.TerritoryNode;
import elv.server.proc.Params;
import elv.server.proc.Process;
import elv.server.result.Key;
import elv.server.result.Sqls;
import elv.server.result.Value;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Mortality preparation.
 */
public class MortalityPreparation extends AbstractStep {
  private List<Gender> genders;
  private Resolution resolution;
  private List<Interval> yearIntervals;
  private List<Interval> ageIntervals;
  private List<TerritoryNode> baseRangeNodes;
  private String diagnosesClause;

  @Override
  public int initParams(Process process) {
    genders = Params.getGenders(process);
    resolution = Params.getResolution(process);
    yearIntervals = Params.getYearIntervals(process);
    ageIntervals = Params.getAgeIntervals(process);
    baseRangeNodes = Params.getBaseRangeNodes(process);
    diagnosesClause = Sqls.createClause(Params.getDiseaseDiagnoses(process), Params.getMortalityDiagnoses(process));
    int months = (resolution == Resolution.MONTHLY ? 12 : 1);
    int yearOrIntervalCount = (resolution == Resolution.YEAR_INTERVALY ? yearIntervals.size() : Params.getYears(process).size());
    return yearOrIntervalCount * months * genders.size() * ageIntervals.size() * baseRangeNodes.size();
  }

  @Override
  public void doCompute(Process process) {
    // Prepare multithreading arguments
    Map<Param, Object> arguments = new HashMap<>();
    arguments.put(Param.resolution, resolution);
    arguments.put(Param.diagnosesClause, diagnosesClause);

    for(Interval iYearInterval : yearIntervals) {
      arguments.put(Param.yearInterval, iYearInterval);

      int years = resolution == Resolution.YEAR_INTERVALY ? 1 : iYearInterval.to - iYearInterval.from + 1;
      for(int yearCount = 0; yearCount < years; yearCount++) {
        Integer iYear = (resolution == Resolution.YEAR_INTERVALY ? null : iYearInterval.from + yearCount);
        arguments.put(Param.year, iYear);

        int months = (resolution == Resolution.MONTHLY ? 12 : 1);
        for(int monthCount = 0; monthCount < months; monthCount++) {
          Integer iMonth = (resolution == Resolution.MONTHLY ? monthCount + 1 : null);
          arguments.put(Param.month, iMonth);

          for(Gender iGender : genders) {
            arguments.put(Param.gender, iGender);

            for(Interval iAgeInterval : ageIntervals) {
              arguments.put(Param.ageInterval, iAgeInterval);

              for(TerritoryNode iRangeNode : baseRangeNodes) {
                Key key = new Key.Builder().setYearInterval(iYearInterval).setYear(iYear).setMonth(iMonth)
                  .setGender(iGender).setAgeInterval(iAgeInterval).setTerritory(iRangeNode.territory).build();
                Value value = results.get(key);
                if(value == null) {
                  int observedCases = Process.EXECUTOR.invoke(new RangeCasesCounter(iRangeNode.getChildren(), arguments));
                  value = new Value.Builder().setObservedCases(observedCases).build();
                  results.put(key, value);
                  Process.LOG.info(key + ":" + value);
                }
                process.incrementProgress();
              }
            }
          }
        }
      }
    }
    process.getResultDb().commit();
  }

  private static class RangeCasesCounter extends RecursiveTask<Integer> {
    private final List<Node> settlementNodes;
    private final Map<Param, Object> arguments;

    public RangeCasesCounter(List<Node> settlementNodes, Map<Param, Object> arguments) {
      this.settlementNodes = settlementNodes;
      this.arguments = arguments;
    }

    @Override
    public Integer compute() {
      int observedCases = 0;
      if(settlementNodes.size() == 1) {
        Territory settlement = ((TerritoryNode)settlementNodes.get(0)).territory;
        Resolution resolution = (Resolution)arguments.get(Param.resolution);
        Interval yearInterval = (Interval)arguments.get(Param.yearInterval);
        Integer year = (Integer)arguments.get(Param.year);
        Integer month = (Integer)arguments.get(Param.month);
        Gender gender = (Gender)arguments.get(Param.gender);
        Interval ageInterval = (Interval)arguments.get(Param.ageInterval);
        String diagnosesClause = (String)arguments.get(Param.diagnosesClause);

        String sqlString = Sqls.AND.join(MORTALITY_SQL + Sqls.createClause(resolution, yearInterval, year, month),
          Sqls.createClause(gender), Sqls.createClause(ageInterval), Sqls.createClause(settlement), diagnosesClause);
        observedCases = select(sqlString);
      } else {
        List<ForkJoinTask<Integer>> tasks = new ArrayList<>();
        int halfIdx = settlementNodes.size() / 2;
        tasks.add(new RangeCasesCounter(settlementNodes.subList(0, halfIdx), arguments));
        tasks.add(new RangeCasesCounter(settlementNodes.subList(halfIdx, settlementNodes.size()), arguments));
        for(ForkJoinTask<Integer> task : invokeAll(tasks)) {
          observedCases += task.join();
        }
      }
      return observedCases;
    }

    private int select(String sqlString) {
      try(Connection connection = Process.DATA_DB.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sqlString)) {
        resultSet.next();
        return resultSet.getInt(1);
      } catch(SQLException exc) {
        throw new RuntimeException(exc);
      }
    }
  }
}
