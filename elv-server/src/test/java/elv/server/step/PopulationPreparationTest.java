package elv.server.step;

import elv.common.Analysis;
import elv.common.Attributes;
import elv.common.io.Tracks;
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
public class PopulationPreparationTest {
  public PopulationPreparationTest() {
  }

  @Test
  public void testCompute() throws IOException {
    Analysis analysis = new Analysis.PopulationPreparation(Tracks.create(System.getProperty("user.home") + "/.elv/5.0/lofi"));
    Attributes analysisAttribute = new Attributes(null);
    Process process = new Process(analysis, analysisAttribute);

    List<Gender> genders = new ArrayList<>();
    genders.add(Gender.MALES);
    genders.add(Gender.FEMALES);
    process.getParams().put(Param.genders, genders);

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

    String stepName = PopulationPreparation.class.getSimpleName();
    Map<Key, Value> stepResult = process.getResultDb().getHashMap(stepName);

    new PopulationPreparation().compute(process);
  }
}
