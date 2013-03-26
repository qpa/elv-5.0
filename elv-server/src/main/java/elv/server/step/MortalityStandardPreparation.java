package elv.server.step;

import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Param;
import elv.common.params.Territory;
import elv.common.step.Progress;
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
public class MortalityStandardPreparation implements Step {
  public static final String POPULATION_SQL = "SELECT SUM(population) FROM population WHERE  ";
  public static final String MORTALITY_SQL = "SELECT COUNT(*) FROM mortality WHERE ";

  @Override
  public void compute(Process process) {
    String stepName = this.getClass().getSimpleName();
    Map<Key, Value> stepResults = process.getResults().get(stepName);

    // Get parameters
    Gender gender = Params.getGender(process);
    List<Integer> years = Params.getYears(process);
    List<Interval> ageIntervals = Params.getAgeIntervals(process);
    List<Territory> settlements = Params.getSettlements(process);
    String diagnosesClause = Sqls.createClause(Params.getDiseaseDiagnoses(process), Params.getMortalityDiagnoses(process));

    // Prepare multithreading arguments
    Map<Param, Object> arguments = new HashMap<>();
    arguments.put(Param.gender, gender);
    arguments.put(Param.diagnosesClause, diagnosesClause);

    // Set progress
    int maxProgressValue = years.size() * ageIntervals.size() * settlements.size();
    Progress progress = new Progress(stepName, 0, maxProgressValue);
    process.setProgress(progress);

    for(int iYear : years) {
      arguments.put(Param.year, iYear);
      for(Interval iAgeInterval : ageIntervals) {
        arguments.put(Param.ageInterval, iAgeInterval);
        Process.EXECUTOR.invoke(new SettlementCasesCounter(progress, settlements, arguments, stepResults));
      }
    }
    process.getResultDb().commit();
  }

  private static class SettlementCasesCounter extends RecursiveAction {
    private final Progress progress;
    private final List<Territory> settlements;
    private final Map<Param, Object> arguments;
    private final Map<Key, Value> stepResults;

    public SettlementCasesCounter(Progress progress, List<Territory> settlements, Map<Param, Object> arguments, Map<Key, Value> stepResults) {
      this.progress = progress;
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
          value = select(populationSqlString, totalCasesSqlString, observedCasesSqlString);
          stepResults.put(key, value);
Process.LOG.info(key + ":" + value);
        }
        progress.increment();
      } else {
        List<ForkJoinTask<Void>> tasks = new ArrayList<>();
        int halfIdx = settlements.size() / 2;
        tasks.add(new SettlementCasesCounter(progress, settlements.subList(0, halfIdx), arguments, stepResults));
        tasks.add(new SettlementCasesCounter(progress, settlements.subList(halfIdx, settlements.size()), arguments, stepResults));
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
