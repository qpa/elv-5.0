package elv.server.step;

import com.google.common.base.Joiner;
import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Param;
import elv.common.params.Territory;
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
import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;

/**
 * Mortality preparation for standardization.
 */
public class MortalityStandardPreparation extends AbstractStep {
  private Gender gender;
  private List<Integer> years;
  private List<Interval> ageIntervals;
  private List<Territory> settlements;
  private String diagnosesClause;

  @Override
  public int initParams(Process process) {
    gender = Params.getGender(process);
    years = Params.getYears(process);
    ageIntervals = Params.getAgeIntervals(process);
    settlements = Params.getSettlements(process);
    diagnosesClause = Sqls.createClause(Params.getDiseaseDiagnoses(process), Params.getMortalityDiagnoses(process));
    return years.size() * ageIntervals.size() * settlements.size();
  }

  @Override
  public void doCompute(Process process) {
    // Prepare multithreading arguments
    Map<Param, Object> arguments = new HashMap<>();
    arguments.put(Param.gender, gender);
    arguments.put(Param.diagnosesClause, diagnosesClause);

    for(int iYear : years) {
      arguments.put(Param.year, iYear);
      for(Interval iAgeInterval : ageIntervals) {
        arguments.put(Param.ageInterval, iAgeInterval);
        Process.EXECUTOR.invoke(new SettlementCasesCounter(process, settlements, arguments, results));
      }
    }
    process.getResultDb().commit();
  }

  private static class SettlementCasesCounter extends RecursiveAction {
    private final Process process;
    private final List<Territory> settlements;
    private final Map<Param, Object> arguments;
    private final Map<Key, Value> stepResults;

    public SettlementCasesCounter(Process process, List<Territory> settlements, Map<Param, Object> arguments, Map<Key, Value> stepResults) {
      this.process = process;
      this.settlements = settlements;
      this.arguments = arguments;
      this.stepResults = stepResults;
    }

    @Override
    public void compute() {
      if(settlements.size() == 1) {
        Territory settlement = settlements.get(0);
        Integer year = (Integer)arguments.get(Param.year);
        Gender gender = (Gender)arguments.get(Param.gender);
        Interval ageInterval = (Interval)arguments.get(Param.ageInterval);
        String diagnosesClause = (String)arguments.get(Param.diagnosesClause);

        Key key = new Key.Builder().setYear(year).setGender(gender).setAgeInterval(ageInterval).setTerritory(settlement).build();
        Value value = stepResults.get(key);
        if(value == null) {
          String populationSqlString = Sqls.AND.join(POPULATION_SQL + Sqls.createClause(year), Sqls.createClause(gender),
            Sqls.createClause(ageInterval), Sqls.createClause(settlement));
          String totalCasesSqlString = Sqls.AND.join(MORTALITY_SQL + Sqls.createClause(year), Sqls.createClause(gender),
            Sqls.createClause(ageInterval), Sqls.createClause(settlement));
          String observedCasesSqlString = Sqls.AND.join(totalCasesSqlString, diagnosesClause);
          Process.LOG.debug(Joiner.on("\n").join(populationSqlString, totalCasesSqlString, observedCasesSqlString));
          value = select(populationSqlString, totalCasesSqlString, observedCasesSqlString);
          stepResults.put(key, value);
        }
        Process.LOG.info(key + ":" + value);
        process.incrementProgress();
      } else {
        List<ForkJoinTask<Void>> tasks = new ArrayList<>();
        int halfIdx = settlements.size() / 2;
        tasks.add(new SettlementCasesCounter(process, settlements.subList(0, halfIdx), arguments, stepResults));
        tasks.add(new SettlementCasesCounter(process, settlements.subList(halfIdx, settlements.size()), arguments, stepResults));
        invokeAll(tasks);
      }
    }

    private Value select(String populationSqlString, String totalCasesSqlString, String observedCasesSqlString) {
      try(Connection connection = Process.DATA_DB.getConnection(); Statement statement = connection.createStatement()) {
        ResultSet resultSet = statement.executeQuery(populationSqlString);
        resultSet.next();
        int population = resultSet.getInt(1);
        resultSet.close();

        resultSet = statement.executeQuery(totalCasesSqlString);
        resultSet.next();
        int totalCases = resultSet.getInt(1);
        resultSet.close();

        resultSet = statement.executeQuery(observedCasesSqlString);
        resultSet.next();
        int observedCases = resultSet.getInt(1);
        resultSet.close();

        return new Value.Builder().setPopulation(population).setTotalCases(totalCases).setObservedCases(observedCases).build();

      } catch(SQLException exc) {
        throw new RuntimeException(exc);
      }
    }
  }
}
