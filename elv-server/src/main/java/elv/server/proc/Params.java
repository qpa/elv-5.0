package elv.server.proc;

import elv.common.params.Diagnosis;
import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Node;
import elv.common.params.Param;
import elv.common.params.Resolution;
import elv.common.params.Territory;
import elv.common.params.TerritoryNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.joda.time.DateTime;

/**
 * Parameter helper methods.
 */
public class Params {
  private Params() {
  }

  public static Gender getGender(Process process) {
    Gender gender = (Gender)process.getParams().get(Param.gender);
    if(gender == null) {
      List<Gender> genders = (List<Gender>)process.getParams().get(Param.genders);
      if(genders == null || genders.isEmpty() || genders.size() == 2) {
        gender = Gender.ALL;
      } else {
        gender = genders.get(0);
      }
      process.getParams().put(Param.gender, gender);
    }
    return gender;
  }

  public static List<Gender> getGenders(Process process) {
    List<Gender> genders = (List<Gender>)process.getParams().get(Param.genders);
    if(genders == null || genders.isEmpty()) {
      genders = new ArrayList<>();
      genders.add(Gender.ALL);
      process.getParams().put(Param.genders, genders);
    }
    return genders;
  }

  public static Resolution getResolution(Process process) {
    Resolution resolution = (Resolution)process.getParams().get(Param.resolution);
    if(resolution == null) {
      resolution = Resolution.YEARLY;
      process.getParams().put(Param.resolution, resolution);
    }
    return resolution;
  }

  public static List<Interval> getYearIntervals(Process process) {
    List<Interval> yearIntervals = (List<Interval>)process.getParams().get(Param.yearIntervals);
    if(yearIntervals == null) {
      int year = new DateTime().getYear();
      yearIntervals = new ArrayList<>();
      yearIntervals.add(new Interval(year, year));
      process.getParams().put(Param.yearIntervals, yearIntervals);
    }
    return yearIntervals;
  }

  public static List<Integer> getYears(Process process) {
    List<Integer> years = (List<Integer>)process.getParams().get(Param.years);
    if(years == null) {
      final List<Interval> yearIntervals = getYearIntervals(process);
      final Set<Integer> yearSet = new TreeSet<>();
      final Integer benchmarkYear = (Integer)process.getParams().get(Param.benchmarkYear);
      if(benchmarkYear != null) {
        yearSet.add(benchmarkYear);
      }
      for(Interval iYearInterval : yearIntervals) {
        for(int year = iYearInterval.from; year <= iYearInterval.to; year++) {
          yearSet.add(year);
        }
      }
      years = new ArrayList<>(yearSet);
      process.getParams().put(Param.years, years);
    }
    return years;
  }

  public static List<Interval> getAgeIntervals(Process process) {
    List<Interval> ageIntervals = (List<Interval>)process.getParams().get(Param.ageIntervals);
    if(ageIntervals == null) {
      ageIntervals = new ArrayList<>();
      ageIntervals.add(Interval.AGE);
      process.getParams().put(Param.ageIntervals, ageIntervals);
    }
    return ageIntervals;
  }

  public static List<Diagnosis> getDiseaseDiagnoses(Process process) {
    List<Diagnosis> diseaseDiagnoses = (List<Diagnosis>)process.getParams().get(Param.diseaseDiagnoses);
    if(diseaseDiagnoses == null) {
      diseaseDiagnoses = new ArrayList<>();
      diseaseDiagnoses.add(Diagnosis.ROOT);
      process.getParams().put(Param.diseaseDiagnoses, diseaseDiagnoses);
    }
    return diseaseDiagnoses;
  }

  public static List<Diagnosis> getMortalityDiagnoses(Process process) {
    List<Diagnosis> mortalityDiagnoses = (List<Diagnosis>)process.getParams().get(Param.mortalityDiagnoses);
    if(mortalityDiagnoses == null) {
      mortalityDiagnoses = new ArrayList<>();
      mortalityDiagnoses.add(Diagnosis.ROOT);
      process.getParams().put(Param.mortalityDiagnoses, mortalityDiagnoses);
    }
    return mortalityDiagnoses;
  }

  public static List<TerritoryNode> getBaseRangeNodes(Process process) {
    List<TerritoryNode> rangeNodes = (List<TerritoryNode>)process.getParams().get(Param.baseRanges);
    if(rangeNodes == null) {
      rangeNodes = new ArrayList<>();
      final TerritoryNode rangeNode = new TerritoryNode(Territory.ROOT);
      rangeNode.addChild(new TerritoryNode(Territory.ROOT));
      rangeNodes.add(rangeNode);
      process.getParams().put(Param.baseRanges, rangeNodes);
    }
    return rangeNodes;
  }

  public static List<TerritoryNode> getBenchmarkRangeNodes(Process process) {
    return (List<TerritoryNode>)process.getParams().get(Param.benchmarkRanges);
  }

  public static List<Territory> getSettlements(Process process) {
    List<Territory> settlements = (List<Territory>)process.getParams().get(Param.settlements);
    if(settlements == null) {
      final Set<Territory> settlementSet = new TreeSet<>();
      addSettlements(getBaseRangeNodes(process), settlementSet);
      List<TerritoryNode> benchmarkRanges = getBenchmarkRangeNodes(process);
      if(benchmarkRanges != null) {
        addSettlements(benchmarkRanges, settlementSet);
      }
      settlements = new ArrayList<>(settlementSet);
      process.getParams().put(Param.settlements, settlements);
    }
    return settlements;
  }

  private static void addSettlements(List<TerritoryNode> rangeNodes, final Set<Territory> settlementSet) {
    for(TerritoryNode iBaseRange : rangeNodes) {
      for(Node iSettlementNode : iBaseRange.getChildren()) {
        settlementSet.add(((TerritoryNode)iSettlementNode).territory);
      }
    }
  }
}
