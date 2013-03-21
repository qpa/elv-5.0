package elv.common;

import elv.common.io.Track;
import elv.common.io.Tracks;
import elv.common.params.Param;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Analysis.
 */
public abstract class Analysis implements Name {
  private static final long serialVersionUID = 1L;

  public enum Attribute {
    TYPE, NAME, DESCRIPTION, STATE, SCHEDULED_DATE;
  }

  public enum State {
    DEFINED, SCHEDULED, STARTED, STOPPED, FINISHED, FAILED;
  }
  public static String PARAM_FILE = "param" + Tracks.YML;
  public static String RESULT_FILE = "results-db";
  private Track track;
  private final List<Param> params;
  private final List<String> steps;

  private Analysis(Track track, List<Param> params, List<String> steps) {
    this.track = track;
    this.params = params;
    this.steps = steps;
  }

  @Override
  public final String getName() {
    return Tracks.base(track);
  }

  public final Track getTrack() {
    return track;
  }

  public final Track getAttributeTrack() {
    return Tracks.create(track, Tracks.file(Analysis.class));
  }

  public final Track getParamTrack() {
    return Tracks.create(track, PARAM_FILE);
  }

  public final Track getProcTrack() {
    return Tracks.create(track, RESULT_FILE);
  }

  public final List<Param> getParams() {
    return params;
  }

  public final List<String> getSteps() {
    return steps;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public final void setName(String name) {
    track = Tracks.rename(track, name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.track);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }
    final Analysis other = (Analysis)obj;
    if(!Objects.equals(this.track, other.track)) {
      return false;
    }
    return true;
  }

  public static final class MortalitySelection extends Analysis {
    public MortalitySelection(Track track) {
      super(track,
        Collections.unmodifiableList(Arrays.asList(Param.yearIntervals, Param.ageIntervals, Param.baseRanges)),
        Collections.unmodifiableList(Arrays.asList("MortalitySelection")));
    }
  }

  public static final class PopulationPreparation extends Analysis {
    public PopulationPreparation(Track track) {
      super(track,
        Collections.unmodifiableList(Arrays.asList(Param.yearIntervals, Param.ageIntervals, Param.baseRanges)),
        Collections.unmodifiableList(Arrays.asList("PopulationPreparation")));
    }
  }

  public static final class MortalityPreparation extends Analysis {
    public MortalityPreparation(Track track) {
      super(track,
        Collections.unmodifiableList(Arrays.asList(Param.genders, Param.resolution, Param.yearIntervals, Param.ageIntervals, Param.baseRanges,
          Param.diseaseDiagnoses, Param.mortalityDiagnoses)),
        Collections.unmodifiableList(Arrays.asList("MortalityPreparation")));
    }
  }

  public static final class MortalityStandardization extends Analysis {
    public MortalityStandardization(Track track) {
      super(track,
        Collections.unmodifiableList(Arrays.asList(Param.genders, Param.resolution, Param.yearIntervals, Param.benchmarkYear, Param.ageIntervals, Param.baseRanges,
          Param.benchmarkRanges, Param.diseaseDiagnoses, Param.mortalityDiagnoses, Param.standardizationMode)),
        Collections.unmodifiableList(Arrays.asList("MortalityStandardPreparation", "Standardization")));
    }
  }

  public static final class MortalitySmoothing extends Analysis {
    public MortalitySmoothing(Track track) {
      super(track,
        Collections.unmodifiableList(Arrays.asList(Param.genders, Param.resolution, Param.yearIntervals, Param.benchmarkYear, Param.ageIntervals, Param.baseRanges,
          Param.benchmarkRanges, Param.diseaseDiagnoses, Param.mortalityDiagnoses, Param.smoothingMode, Param.smoothingCategory)),
        Collections.unmodifiableList(Arrays.asList("MortalityStandardPreparation", "Standardization", "Smoothing")));
    }
  }
}
