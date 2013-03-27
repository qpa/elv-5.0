package elv.server.step;

import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Node;
import elv.common.params.Param;
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
public class PopulationPreparation extends AbstractStep {
  private List<Gender> genders;
  private List<Integer> years;
  private List<Interval> ageIntervals;
  private List<TerritoryNode> baseRangeNodes;

  @Override
  public int initParams(Process process) {
    genders = Params.getGenders(process);
    years = Params.getYears(process);
    ageIntervals = Params.getAgeIntervals(process);
    baseRangeNodes = Params.getBaseRangeNodes(process);
    return years.size() * genders.size() * ageIntervals.size() * baseRangeNodes.size();
  }

  @Override
  public void doCompute(Process process) {
    // Prepare multithreading arguments
    Map<Param, Object> arguments = new HashMap<>();

    for(Integer iYear : years) {
      arguments.put(Param.year, iYear);

      for(Gender iGender : genders) {
        arguments.put(Param.gender, iGender);

        for(Interval iAgeInterval : ageIntervals) {
          arguments.put(Param.ageInterval, iAgeInterval);

          for(TerritoryNode iRangeNode : baseRangeNodes) {
            Key key = new Key.Builder().setYear(iYear).setGender(iGender).setAgeInterval(iAgeInterval).setTerritory(iRangeNode.territory).build();
            Value value = results.get(key);
            if(value == null) {
              int population = Process.EXECUTOR.invoke(new PopulationCounter(iRangeNode.getChildren(), arguments));
              value = new Value.Builder().setPopulation(population).build();
              results.put(key, value);
              Process.LOG.info(key + ":" + value);
            }
            process.incrementProgress();
          }
        }
      }
    }
    process.getResultDb().commit();
  }

  private static class PopulationCounter extends RecursiveTask<Integer> {
    private final List<Node> settlementNodes;
    private final Map<Param, Object> arguments;

    public PopulationCounter(List<Node> settlementNodes, Map<Param, Object> arguments) {
      this.settlementNodes = settlementNodes;
      this.arguments = arguments;
    }

    @Override
    public Integer compute() {
      int population = 0;
      if(settlementNodes.size() == 1) {
        Territory settlement = ((TerritoryNode)settlementNodes.get(0)).territory;
        Integer year = (Integer)arguments.get(Param.year);
        Gender gender = (Gender)arguments.get(Param.gender);
        Interval ageInterval = (Interval)arguments.get(Param.ageInterval);

        String sqlString = Sqls.AND.join(POPULATION_SQL + Sqls.createClause(year), Sqls.createClause(gender),
          Sqls.createClause(ageInterval), Sqls.createClause(settlement));
        population = select(sqlString);
      } else {
        List<ForkJoinTask<Integer>> tasks = new ArrayList<>();
        int halfIdx = settlementNodes.size() / 2;
        tasks.add(new PopulationCounter(settlementNodes.subList(0, halfIdx), arguments));
        tasks.add(new PopulationCounter(settlementNodes.subList(halfIdx, settlementNodes.size()), arguments));
        for(ForkJoinTask<Integer> task : invokeAll(tasks)) {
          population += task.join();
        }
      }
      return population;
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
