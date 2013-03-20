package elv.common.props;

/**
 * Class for properties.
 */
public enum Prop {
  locale,
  genders, resolution, benchmarkYear, yearWindows, yearIntervals, ageIntervals,
  baseRanges, benchmarkRanges, diseaseDiagnoses, mortalityDiagnoses,
  standardizationMode, smoothingMode, smoothingCategory;
//  public Resolution resolution;
//  public int benchmarkYear;
//  public int yearWindows;
//  public List<Interval> yearIntervals;
//  public List<Interval> ageIntervals;
//  public List<Territory> baseRanges;
//  public List<Territory> benchmarkRanges;
//  public List<Diagnosis> diseaseDiagnoses;
//  public Standardization standardizationMode;
//  public Smoothing smoothingMode;
//  public Category smoothingCategory;

  public void setDefaultValues() {
//    standardizationMode = Standardization.INDIRECT;
//    smoothingMode = Smoothing.IRP;
//    smoothingIterationCount = 50;
//    smoothingPartitionCount = 200;
//    isSMRWeighed = true;
//    distanceWeighingValue = 2.0;
  }
}
