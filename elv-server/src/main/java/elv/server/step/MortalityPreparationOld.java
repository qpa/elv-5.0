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
import java.util.List;
import java.util.Map;

/**
 * Mortality preparation.
 */
public class MortalityPreparationOld implements Step {
  public static final String SQL = "SELECT COUNT(*) FROM mortality WHERE ";

  @Override
  public void compute(Process process) {
    Progresses progresses = process.getProgresses();

    Map<Key, Value> stepResult = process.getResults().get(MortalityPreparation.class.getSimpleName());

    List<Gender> genders = (List<Gender>)process.getParams().get(Param.genders);

    Resolution resolution = (Resolution)process.getParams().get(Param.resolution);

    List<Diagnosis> diseaseDiagnoses = (List<Diagnosis>)process.getParams().get(Param.diseaseDiagnoses);
    List<Diagnosis> mortalityDiagnoses = (List<Diagnosis>)process.getParams().get(Param.mortalityDiagnoses);
    String diagnosesClause = Sqls.createClause(diseaseDiagnoses, mortalityDiagnoses);

    List<Interval> yearIntervals = (List<Interval>)process.getParams().get(Param.yearIntervals);
    List<Interval> ageIntervals = (List<Interval>)process.getParams().get(Param.ageIntervals);
    List<TerritoryNode> rangeNodes = (List<TerritoryNode>)process.getParams().get(Param.baseRanges);

    progresses.push(new Progress("YearIntervals", 0, yearIntervals.size()));
    for(int yearIntervalCount = 0; yearIntervalCount < yearIntervals.size(); yearIntervalCount++) {
      Interval iYearInterval = yearIntervals.get(yearIntervalCount);

      int years = resolution == Resolution.YEAR_INTERVALY ? 1 : iYearInterval.to - iYearInterval.from + 1;
      progresses.push(new Progress("Years", 0, years));
      for(int yearCount = 0; yearCount < years; yearCount++) {
        int iYear = iYearInterval.from + yearCount;

        int months = resolution == Resolution.MONTHLY ? 12 : 1;
        progresses.push(new Progress("Months", 0, months));
        for(int monthCount = 0; monthCount < months; monthCount++) {
          int iMonth = resolution == Resolution.MONTHLY ? monthCount + 1 : monthCount;

          progresses.push(new Progress("Genders", 0, genders.size()));
          for(int genderCount = 0; genderCount < genders.size(); genderCount++) {
            Gender iGender = genders.get(genderCount);

            progresses.push(new Progress("AgeIntervals", 0, ageIntervals.size()));
            for(int ageIntervalCount = 0; ageIntervalCount < ageIntervals.size(); ageIntervalCount++) {
              Interval iAgeInterval = ageIntervals.get(ageIntervalCount);

              progresses.push(new Progress("Ranges", 0, rangeNodes.size()));
              for(int rangeCount = 0; rangeCount < rangeNodes.size(); rangeCount++) {
                TerritoryNode iRangeNode = rangeNodes.get(rangeCount);
                int observedCases = 0;

                Key key = new Key.Builder().setYearInterval(iYearInterval).setYear(iYear)
                  .setMonth(iMonth).setGender(iGender).setAgeInterval(iAgeInterval)
                  .setRange(iRangeNode.territory).build();
                Value value = stepResult.get(key);
                if(value == null) {
                  for(Node iSettlementNode : iRangeNode.getChildren()) {
                    Territory iSettlement = ((TerritoryNode)iSettlementNode).territory;
                    String sqlString = Sqls.AND.join(SQL + Sqls.createClause(resolution, iYearInterval, iYear, iMonth),
                      Sqls.createClause(iGender), Sqls.createClause(iAgeInterval), Sqls.createClause(iSettlement), diagnosesClause);
                    try(Connection connection = Process.DATA_DB.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sqlString)) {
                      resultSet.next();
                      observedCases += resultSet.getInt(1);
                    } catch(SQLException exc) {
                      throw new RuntimeException(exc);
                    }
                  }
                  value = new Value.Builder().setObservedCases(observedCases).build();
                  stepResult.put(key, value);
                  Process.LOG.info(key + " " + value);
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
  }
}
