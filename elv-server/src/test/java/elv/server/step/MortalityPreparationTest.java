package elv.server.step;

import elv.common.Analysis;
import elv.common.Attribute;
import elv.common.io.Tracks;
import elv.common.props.Diagnosis;
import elv.common.props.Gender;
import elv.common.props.Interval;
import elv.common.props.Prop;
import elv.common.props.Resolution;
import elv.common.props.Territory;
import elv.common.props.TerritoryNode;
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
public class MortalityPreparationTest {
  public MortalityPreparationTest() {
  }

  @Test
  public void testCompute() throws IOException {
    Analysis analysis = new Analysis.MortalityPreparation(Tracks.create(System.getProperty("user.home") + "/.elv/5.0/lofi"));
    Attribute analysisAttribute = new Attribute(null);
    Process process = new Process(analysis, analysisAttribute);
    process.getProgresses();
    process.getResults();

    List<Gender> genders = new ArrayList<>();
    genders.add(Gender.MALES);
    genders.add(Gender.FEMALES);
    process.getProps().put(Prop.genders, genders);

    process.getProps().put(Prop.resolution, Resolution.MONTHLY);

    final List<Diagnosis> diagnoses = new ArrayList<>();
    diagnoses.add(new Diagnosis("C33", "A légcső rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3400", "Főhörgő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3410", "Felső lebeny, hörgő vagy tüdő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3420", "Középső lebeny, hörgő vagy tüdő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3430", "Alsó lebeny, hörgő vagy tüdő rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3480", "A hörgő és tüdő átfedő elváltozása, rosszindulatú daganata"));
    diagnoses.add(new Diagnosis("C3490", "Hörgő vagy tüdő rosszindulatú daganata, k.m.n."));
    process.getProps().put(Prop.diseaseDiagnoses, diagnoses);

    List<Interval> yearIntervals = new ArrayList<>();
    yearIntervals.add(new Interval(2005, 2006));
    yearIntervals.add(new Interval(2007, 2008));
    process.getProps().put(Prop.yearIntervals, yearIntervals);

    List<Interval> ageIntervals = new ArrayList<>();
    ageIntervals.add(new Interval(30, 40));
    ageIntervals.add(new Interval(50, 80));
    process.getProps().put(Prop.ageIntervals, ageIntervals);

    List<TerritoryNode> rangeNodes = new ArrayList<>();
    TerritoryNode rangeNode = new TerritoryNode(new Territory("1", "Első terület", 0, 0, 0));
    rangeNode.addChild(new TerritoryNode(new Territory("2999", "Győr", 0, 0, 0)));
    rangeNode.addChild(new TerritoryNode(new Territory("956", "Budapest I. kerület", 0, 0, 0)));
    rangeNodes.add(rangeNode);
    rangeNode = new TerritoryNode(new Territory("2", "Második terület", 0, 0, 0));
    rangeNode.addChild(new TerritoryNode(new Territory("1806", "Budapest III. kerület", 0, 0, 0)));
    rangeNodes.add(rangeNode);
    process.getProps().put(Prop.baseRanges, rangeNodes);

    Map<Key, Value> stepResult = process.getResultDb().getHashMap(MortalityPreparation.class.getSimpleName());
    process.getResults().put(MortalityPreparation.class.getSimpleName(), stepResult);

    new MortalityPreparation().compute(process);
  }
}
