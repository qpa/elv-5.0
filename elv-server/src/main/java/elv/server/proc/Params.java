package elv.server.proc;

import elv.common.params.Diagnosis;
import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Param;
import elv.common.params.Resolution;
import elv.common.params.Territory;
import elv.common.params.TerritoryNode;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/**
 * Parameter helper methods.
 */
public class Params {
  private Params() {
  }

  public static Gender getGender(Process process) {
    List<Gender> genders = (List<Gender>)process.getParams().get(Param.genders);
    Gender gender;
    if(genders == null || genders.isEmpty() || genders.size() == Gender.values().length) {
      gender = Gender.ALL;
    } else {
      gender = genders.get(0);
    }
    return gender;
  }

  public static List<Gender> getGenders(Process process) {
    List<Gender> genders = (List<Gender>)process.getParams().get(Param.genders);
    if(genders == null || genders.isEmpty()) {
      genders = new ArrayList<>();
      genders.add(Gender.ALL);
    }
    return genders;
  }

  public static Resolution getResolution(Process process) {
    Resolution resolution = (Resolution)process.getParams().get(Param.resolution);
    if(resolution == null) {
      resolution = Resolution.YEARLY;
    }
    return resolution;
  }

  public static List<Interval> getYearIntervals(Process process) {
    List<Interval> yearIntervals = (List<Interval>)process.getParams().get(Param.yearIntervals);
    if(yearIntervals == null) {
      int year = new DateTime().getYear();
      yearIntervals = new ArrayList<>();
      yearIntervals.add(new Interval(year, year));
    }
    return yearIntervals;
  }

  public static List<Interval> getAgeIntervals(Process process) {
    List<Interval> ageIntervals = (List<Interval>)process.getParams().get(Param.ageIntervals);
    if(ageIntervals == null) {
      ageIntervals = new ArrayList<>();
      ageIntervals.add(Interval.AGE);
    }
    return ageIntervals;
  }

  public static List<Diagnosis> getDiseaseDiagnoses(Process process) {
    List<Diagnosis> diseaseDiagnoses = (List<Diagnosis>)process.getParams().get(Param.diseaseDiagnoses);
    if(diseaseDiagnoses == null) {
      diseaseDiagnoses = new ArrayList<>();
      diseaseDiagnoses.add(Diagnosis.ROOT);
    }
    return diseaseDiagnoses;
  }

  public static List<Diagnosis> getMortalityDiagnoses(Process process) {
    List<Diagnosis> mortalityDiagnoses = (List<Diagnosis>)process.getParams().get(Param.mortalityDiagnoses);
    if(mortalityDiagnoses == null) {
      mortalityDiagnoses = new ArrayList<>();
      mortalityDiagnoses.add(Diagnosis.ROOT);
    }
    return mortalityDiagnoses;
  }

  public static List<TerritoryNode> getBaseRageNodes(Process process) {
    List<TerritoryNode> rangeNodes = (List<TerritoryNode>)process.getParams().get(Param.baseRanges);
    if(rangeNodes == null) {
      rangeNodes = new ArrayList<>();
      rangeNodes.add(new TerritoryNode(Territory.ROOT));
    }
    return rangeNodes;
  }
}
