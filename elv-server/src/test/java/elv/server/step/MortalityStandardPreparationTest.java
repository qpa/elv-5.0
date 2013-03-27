package elv.server.step;

import elv.common.Analysis;
import elv.common.Attribute;
import elv.common.io.Tracks;
import elv.common.params.Diagnosis;
import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Param;
import elv.common.params.Territory;
import elv.common.params.TerritoryNode;
import elv.server.proc.Process;
import elv.server.result.Key;
import elv.server.result.Value;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

/**
 *
 */
public class MortalityStandardPreparationTest {
  public MortalityStandardPreparationTest() {
  }

  @Test
  public void testCompute() throws IOException {
    Analysis analysis = new Analysis.MortalityStandardization(Tracks.create(System.getProperty("user.home") + "/.elv/5.0/lofi"));
    Attribute analysisAttribute = new Attribute(null);
    Process process = new Process(analysis, analysisAttribute);
    process.getResults();

    List<Gender> genders = new ArrayList<>();
    genders.add(Gender.MALES);
    genders.add(Gender.FEMALES);
    process.getParams().put(Param.genders, genders);

    final List<Diagnosis> diagnoses = new ArrayList<>();
    diagnoses.add(new Diagnosis("C33", "A légcső rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3400", "Főhörgő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3410", "Felső lebeny, hörgő vagy tüdő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3420", "Középső lebeny, hörgő vagy tüdő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3430", "Alsó lebeny, hörgő vagy tüdő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3480", "A hörgő és tüdő átfedő elváltozása, rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3490", "Hörgő vagy tüdő rosszindulatú daganata, k.m.n."));
    process.getParams().put(Param.diseaseDiagnoses, diagnoses);

    List<Interval> yearIntervals = new ArrayList<>();
    yearIntervals.add(new Interval(2005, 2006));
    yearIntervals.add(new Interval(2007, 2008));
    process.getParams().put(Param.yearIntervals, yearIntervals);

    List<Interval> ageIntervals = new ArrayList<>();
    ageIntervals.add(new Interval(30, 40));
    ageIntervals.add(new Interval(50, 80));
    process.getParams().put(Param.ageIntervals, ageIntervals);

    List<TerritoryNode> rangeNodes = new ArrayList<>();
    TerritoryNode rangeNode = new TerritoryNode(new Territory("1", "Első terület", 0, 0, 0));
    rangeNode.addChild(new TerritoryNode(new Territory("2999", "Győr", 0, 0, 0)));
    rangeNode.addChild(new TerritoryNode(new Territory("956", "Budapest I. kerület", 0, 0, 0)));
    rangeNodes.add(rangeNode);
    rangeNode = new TerritoryNode(new Territory("2", "Második terület", 0, 0, 0));
    rangeNode.addChild(new TerritoryNode(new Territory("1806", "Budapest III. kerület", 0, 0, 0)));
    rangeNodes.add(rangeNode);
    process.getParams().put(Param.baseRanges, rangeNodes);

    String stepName = MortalityStandardPreparation.class.getSimpleName();
    Map<Key, Value> stepResult = process.getResultDb().getHashMap(stepName);
    process.getResults().put(stepName, stepResult);

    new MortalityStandardPreparation().compute(process);
  }
}
