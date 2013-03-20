package elv.server.step;

import elv.common.props.Diagnosis;
import elv.common.props.Gender;
import elv.common.props.Interval;
import elv.common.props.Node;
import elv.common.props.Prop;
import elv.common.props.Resolution;
import elv.common.props.Territory;
import elv.common.props.TerritoryNode;
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

    List<Gender> genders = (List<Gender>)process.getProps().get(Prop.genders);

    Resolution resolution = (Resolution)process.getProps().get(Prop.resolution);

    List<Diagnosis> diseaseDiagnoses = (List<Diagnosis>)process.getProps().get(Prop.diseaseDiagnoses);
    List<Diagnosis> mortalityDiagnoses = (List<Diagnosis>)process.getProps().get(Prop.mortalityDiagnoses);
    String diagnosesClause = Sqls.createClause(diseaseDiagnoses, mortalityDiagnoses);

    List<Interval> yearIntervals = (List<Interval>)process.getProps().get(Prop.yearIntervals);
    List<Interval> ageIntervals = (List<Interval>)process.getProps().get(Prop.ageIntervals);
    List<TerritoryNode> rangeNodes = (List<TerritoryNode>)process.getProps().get(Prop.baseRanges);

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
