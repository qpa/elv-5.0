package elv.common.params;

/**
 * Class for parameters.
 */
public enum Param {
  // Parameter types at input
  locale,
  genders, gender, resolution, benchmarkYear, yearWindows, yearIntervals, years, ageIntervals,
  baseRanges, benchmarkRanges, diseaseDiagnoses, mortalityDiagnoses,
  standardizationMode, smoothingMode, smoothingCategory,
  // Parameter types at process
  yearInterval, year, month, yearWindowIntervals, ageInterval, settlements, diagnosesClause;

  public void setDefaultValues() {
//    standardizationMode = Standardization.INDIRECT;
//    smoothingMode = Smoothing.IRP;
//    smoothingIterationCount = 50;
//    smoothingPartitionCount = 200;
//    isSMRWeighed = true;
//    distanceWeighingValue = 2.0;
  }
}
