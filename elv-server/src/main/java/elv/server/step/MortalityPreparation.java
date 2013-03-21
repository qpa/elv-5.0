package elv.server.step;

import elv.common.params.Diagnosis;
import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Node;
import elv.common.params.Param;
import elv.common.params.Resolution;
import elv.common.params.Territory;
import elv.common.params.TerritoryNode;
import elv.common.step.Progress;
import elv.common.step.Progresses;
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
public class MortalityPreparation implements Step {
  public static final String SQL = "SELECT COUNT(*) FROM mortality WHERE ";

  @Override
  public void compute(Process process) {
    Progresses progresses = process.getProgresses();

    Map<Key, Value> stepResults = process.getResults().get(this.getClass().getSimpleName());

    List<Gender> genders = (List<Gender>)process.getParams().get(Param.genders);

    Resolution resolution = (Resolution)process.getParams().get(Param.resolution);

    List<Diagnosis> diseaseDiagnoses = (List<Diagnosis>)process.getParams().get(Param.diseaseDiagnoses);
    List<Diagnosis> mortalityDiagnoses = (List<Diagnosis>)process.getParams().get(Param.mortalityDiagnoses);
    String diagnosesClause = Sqls.createClause(diseaseDiagnoses, mortalityDiagnoses);

    List<Interval> yearIntervals = (List<Interval>)process.getParams().get(Param.yearIntervals);
    List<Interval> ageIntervals = (List<Interval>)process.getParams().get(Param.ageIntervals);
    List<TerritoryNode> rangeNodes = (List<TerritoryNode>)process.getParams().get(Param.baseRanges);

    Map<String, Object> arguments = new HashMap<>();
    arguments.put(RangeCasesCounter.RESOLUTION, resolution);
    arguments.put(RangeCasesCounter.DIAGNOSES_CLAUSE, diagnosesClause);

    progresses.push(new Progress("YearIntervals", 0, yearIntervals.size()));
    for(int yearIntervalCount = 0; yearIntervalCount < yearIntervals.size(); yearIntervalCount++) {
      Interval iYearInterval = yearIntervals.get(yearIntervalCount);
      arguments.put(RangeCasesCounter.YEAR_INTERVAL, iYearInterval);

      int years = resolution == Resolution.YEAR_INTERVALY ? 1 : iYearInterval.to - iYearInterval.from + 1;
      progresses.push(new Progress("Years", 0, years));
      for(int yearCount = 0; yearCount < years; yearCount++) {
        Integer iYear = resolution == Resolution.YEAR_INTERVALY ? null : iYearInterval.from + yearCount;
        arguments.put(RangeCasesCounter.YEAR, iYear);

        int months = resolution == Resolution.MONTHLY ? 12 : 1;
        progresses.push(new Progress("Months", 0, months));
        for(int monthCount = 0; monthCount < months; monthCount++) {
          Integer iMonth = resolution == Resolution.MONTHLY ? monthCount + 1 : monthCount;
          arguments.put(RangeCasesCounter.MONTH, iMonth);

          progresses.push(new Progress("Genders", 0, genders.size()));
          for(int genderCount = 0; genderCount < genders.size(); genderCount++) {
            Gender iGender = genders.get(genderCount);
            arguments.put(RangeCasesCounter.GENDER, iGender);

            progresses.push(new Progress("AgeIntervals", 0, ageIntervals.size()));
            for(int ageIntervalCount = 0; ageIntervalCount < ageIntervals.size(); ageIntervalCount++) {
              Interval iAgeInterval = ageIntervals.get(ageIntervalCount);
              arguments.put(RangeCasesCounter.AGE_INTERVAL, iAgeInterval);

              progresses.push(new Progress("Ranges", 0, rangeNodes.size()));
              for(int rangeCount = 0; rangeCount < rangeNodes.size(); rangeCount++) {
                TerritoryNode iRangeNode = rangeNodes.get(rangeCount);

                Key key = new Key.Builder().setYearInterval(iYearInterval).setYear(iYear)
                  .setMonth(iMonth).setGender(iGender).setAgeInterval(iAgeInterval)
                  .setRange(iRangeNode.territory).build();
                Value value = stepResults.get(key);
                if(value == null) {
                  int observedCases = Process.EXECUTOR.invoke(new RangeCasesCounter(iRangeNode.getChildren(), arguments));
                  value = new Value.Builder().setObservedCases(observedCases).build();
                  stepResults.put(key, value);
                  Process.LOG.info(key + ":" + value);
                }
                progresses.setPeekValue(rangeCount + 1);
              }
              progresses.pop();
              progresses.setPeekValue(ageIntervalCount + 1);
            }
            progresses.pop();
            progresses.setPeekValue(genderCount + 1);
          }
          progresses.pop();
          progresses.setPeekValue(monthCount + 1);
        }
        progresses.pop();
        progresses.setPeekValue(yearCount + 1);
      }
      progresses.pop();
      progresses.setPeekValue(yearIntervalCount + 1);
    }
    progresses.pop();
    process.getResultDb().commit();
  }

  private static class RangeCasesCounter extends RecursiveTask<Integer> {
    public static final String RESOLUTION = "resolution";
    public static final String YEAR_INTERVAL = "yearInterval";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String GENDER = "gender";
    public static final String AGE_INTERVAL = "ageInterval";
    public static final String DIAGNOSES_CLAUSE = "diagnosesClause";
    final List<Node> settlements;
    final Map<String, Object> arguments;

    public RangeCasesCounter(List<Node> settlements, Map<String, Object> arguments) {
      this.settlements = settlements;
      this.arguments = arguments;
    }

    @Override
    public Integer compute() {
      int observedCases = 0;
      if(settlements.size() == 1) {
        Territory settlement = ((TerritoryNode)settlements.get(0)).territory;
        Resolution resolution = (Resolution)arguments.get(RESOLUTION);
        Interval yearInterval = (Interval)arguments.get(YEAR_INTERVAL);
        Integer year = (Integer)arguments.get(YEAR);
        Integer month = (Integer)arguments.get(MONTH);
        Gender gender = (Gender)arguments.get(GENDER);
        Interval ageInterval = (Interval)arguments.get(AGE_INTERVAL);
        String diagnosesClause = (String)arguments.get(DIAGNOSES_CLAUSE);

        String sqlString = Sqls.AND.join(SQL + Sqls.createClause(resolution, yearInterval, year, month),
          Sqls.createClause(gender), Sqls.createClause(ageInterval), Sqls.createClause(settlement), diagnosesClause);
        try(Connection connection = Process.DATA_DB.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sqlString)) {
          resultSet.next();
          observedCases = resultSet.getInt(1);
        } catch(SQLException exc) {
          throw new RuntimeException(exc);
        }
      } else {
        List<ForkJoinTask<Integer>> tasks = new ArrayList<>();
        int halfIdx = settlements.size() / 2;
        tasks.add(new RangeCasesCounter(settlements.subList(0, halfIdx), arguments));
        tasks.add(new RangeCasesCounter(settlements.subList(halfIdx, settlements.size()), arguments));
        for(ForkJoinTask<Integer> task : invokeAll(tasks)) {
          observedCases += task.join();
        }
      }
      return observedCases;
    }
  }
}
