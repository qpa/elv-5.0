package elv.server.step;

import elv.common.params.Interval;
import elv.common.params.Territory;
import elv.common.params.TerritoryNode;
import elv.server.proc.Params;
import static elv.server.proc.Params.getYearWindows;
import elv.server.proc.Process;
import elv.server.result.Key;
import elv.server.result.Value;
import java.util.List;
import java.util.Map;

/**
 * Standardization.
 */
public class Standardization extends AbstractStep {
  private elv.common.params.Standardization standardizationMode;
  private List<TerritoryNode> allRangeNodes;
  private List<Interval> yearIntervals;
  private List<Integer> yearWindows;
  private List<List<Interval>> yearWindowIntervals;
  private List<Integer> years;
  private Integer benchmarkYear;
  private List<Interval> ageIntervals;
  private Map<Key, Value> preparationResults;

  @Override
  public int initParams(Process process) {
    allRangeNodes = Params.getAllRangeNodes(process);
    standardizationMode = Params.getStandardizationMode(process);
    yearIntervals = Params.getYearIntervals(process);
    yearWindows = getYearWindows(process);
    yearWindowIntervals = Params.getYearWindowIntervals(process);
    years = Params.getYears(process);
    benchmarkYear = Params.getBenchmarkYear(process);
    ageIntervals = Params.getAgeIntervals(process);
    preparationResults = process.getResultDb().getHashMap(MortalityStandardPreparation.class.getSimpleName());
    return;
  }

  @Override
  public void doCompute(Process process) {
    int fromYearPeriod = years.get(0);
    int toYearPeriod = years.get(years.size() - 1);
    int periodMeanYear = benchmarkYear == null ? (fromYearPeriod + toYearPeriod) / 2 : benchmarkYear;
    int periodMeanYearCount = benchmarkYear == null && periodMeanYear - fromYearPeriod < toYearPeriod - periodMeanYear ? 2 : 1;

    execution.getProgresses().push(new elv.util.Progress(elv.util.parameters.District.TITLE, 0, execution.getDistricts().size()));
    for(TerritoryNode iRangeNode : allRangeNodes) {
      Territory iRange = iRangeNode.territory;
      double populationPeriod = 0;
      int observedCasesPeriod = 0;
      double expectedCasesPeriod = 0;
      double benchPopulationPeriod = 0;
      int benchCasesPeriod = 0;
      int sumX = 0;
      double sumY = 0;
      int sumX2 = 0;
      double sumY2 = 0;
      double sumXY = 0;

      execution.getProgresses().push(new elv.util.Progress(YEAR_WINDOWS_NAME, 0, yearWindowIntervals.size()));
      for(int yearWindowCount = 0; yearWindowCount < yearWindowIntervals.size(); yearWindowCount++) {
        int iteratorYearWindow = yearWindows.get(yearWindowCount);

        execution.getProgresses().push(new elv.util.Progress(elv.util.parameters.YearInterval.TITLE, 0, yearWindowIntervals.get(yearWindowCount).size()));
        for(Interval iYearInterval : yearWindowIntervals.get(yearWindowCount)) {
          int meanYearCount = 1;
          int meanYear = (iYearInterval.from + iYearInterval.to) / 2;
          if(meanYear - iYearInterval.from < iYearInterval.to - meanYear) {
            meanYearCount = 2;
          }

            double populationYearInterval = 0;
            int observedCasesYearInterval = 0;
            double expectedCasesYearInterval = 0;
            double benchPopulationYearInterval = 0;
            int benchCasesYearInterval = 0;

            execution.getProgresses().push(new elv.util.Progress(elv.util.parameters.YearInterval.YEARS_TITLE, 0, iYearInterval.getToValue() - iYearInterval.getFromValue() + 1));
            for(int iYear = iYearInterval.from; iYear <= iYearInterval.to; iYear++) {
              // Assigning the benchmark year to the standardization benchmark, if there is.
              int benchYear;
              if(benchmarkYear != null) {
                benchYear = benchmarkYear;
                meanYear = benchYear;
                meanYearCount = 1;
                periodMeanYear = benchYear;
                periodMeanYearCount = 1;
              } else {
                benchYear = iYear;
              }

              Key yearKey = new Key.Builder().setTerritory(iRange).setYear(iYear).build();
              Value yearValue = results.get(yearKey);
              if(yearValue == null) {

              populationPeriod = 0;
              observedCasesPeriod = 0;
              expectedCasesPeriod = 0;
              benchPopulationPeriod = 0;
              benchCasesPeriod = 0;

              populationYearInterval = 0;
              observedCasesYearInterval = 0;
              expectedCasesYearInterval = 0;
              benchPopulationYearInterval = 0;
              benchCasesYearInterval = 0;

              int populationYear = 0;
              int observedCasesYear = 0;
              double expectedCasesYear = 0;
              int benchPopulationYear = 0;
              int benchCasesYear = 0;

              for(Interval iAgeInterval : ageIntervals) {
                double benchPopulationPeriodAgeInterval = 0;
                int benchCasesPeriodAgeInterval = 0;
                double populationPeriodAgeInterval = 0;
                int observedCasesPeriodAgeInterval = 0;

                double benchPopulationYearIntervalAgeInterval = 0;
                int benchCasesYearIntervalAgeInterval = 0;
                double populationYearIntervalAgeInterval = 0;
                int observedCasesYearIntervalAgeInterval = 0;

                int benchPopulationYearAgeInterval = 0;
                int benchCasesYearAgeInterval = 0;
                int populationYearAgeInterval = 0;
                int observedCasesYearAgeInterval = 0;

                for(elv.util.parameters.Settlement iteratorSettlement : execution.getAllSettlements()) {
                  for(SettlementY8earResult iteratorYearResult : iteratorSettlement.getYearResults()) {
                    if(iteratorYearResult.year == benchYear) {
                      if(iteratorYearResult.ageInterval.equals(iAgeInterval)) {
                        // Count district population and cases for this year and age-interval.
                        if(iteratorSettlement.getDistrictCode() == iteratorDistrict.getCode()
                          && iteratorSettlement.getAggregation().equals(iteratorDistrict.getAggregation())) {
                          populationYearAgeInterval += iteratorYearResult.population;
                          observedCasesYearAgeInterval += iteratorYearResult.analyzedCases;
                        }
                        // Count benchmark population and cases for this year and age-interval.
                        if(iteratorSettlement instanceof elv.util.parameters.BenchmarkSettlement) {
                          benchPopulationYearAgeInterval += iteratorYearResult.population;
                          benchCasesYearAgeInterval += iteratorYearResult.analyzedCases;
                        }
                      }
                    }
                    if(iYearInterval.getFromValue() <= iteratorYearResult.year && iteratorYearResult.year <= iYearInterval.getToValue()) {
                      if(iteratorYearResult.ageInterval.equals(iAgeInterval)) {
                        // Count district cases for this year-interval and age-interval.
                        if(iteratorSettlement.getDistrictCode() == iteratorDistrict.getCode()
                          && iteratorSettlement.getAggregation().equals(iteratorDistrict.getAggregation())) {
                          observedCasesYearIntervalAgeInterval += iteratorYearResult.analyzedCases;
                        }
                        // Count benchmark cases for this year-interval and age-interval.
                        if(iteratorSettlement instanceof elv.util.parameters.BenchmarkSettlement) {
                          benchCasesYearIntervalAgeInterval += iteratorYearResult.analyzedCases;
                        }
                      }
                    }
                    if(iteratorYearResult.year == meanYear || iteratorYearResult.year == (meanYear + meanYearCount - 1)) {
                      if(iteratorYearResult.ageInterval.equals(iAgeInterval)) {
                        // Count district population for this year-interval and age-interval.
                        if(iteratorSettlement.getDistrictCode() == iteratorDistrict.getCode()
                          && iteratorSettlement.getAggregation().equals(iteratorDistrict.getAggregation())) {
                          populationYearIntervalAgeInterval += (double)iteratorYearResult.population / meanYearCount;
                        }
                        // Count benchmark population for this year-interval and age-interval.
                        if(iteratorSettlement instanceof elv.util.parameters.BenchmarkSettlement) {
                          benchPopulationYearIntervalAgeInterval += (double)iteratorYearResult.population / meanYearCount;
                        }
                      }
                    }
                    if(iteratorYearResult.ageInterval.equals(iAgeInterval)) {
                      // Count district population and cases for this period and age-interval.
                      if(iteratorSettlement.getDistrictCode() == iteratorDistrict.getCode()
                        && iteratorSettlement.getAggregation().equals(iteratorDistrict.getAggregation())) {
                        observedCasesPeriodAgeInterval += iteratorYearResult.analyzedCases;
                      }
                      // Count benchmark population and cases for this period and age-interval.
                      if(iteratorSettlement instanceof elv.util.parameters.BenchmarkSettlement) {
                        benchCasesPeriodAgeInterval += iteratorYearResult.analyzedCases;
                      }
                    }
                    if(iteratorYearResult.year == periodMeanYear || iteratorYearResult.year == (periodMeanYear + periodMeanYearCount - 1)) {
                      if(iteratorYearResult.ageInterval.equals(iAgeInterval)) {
                        // Count district population for this district and age-interval.
                        if(iteratorSettlement.getDistrictCode() == iteratorDistrict.getCode()
                          && iteratorSettlement.getAggregation().equals(iteratorDistrict.getAggregation())) {
                          populationPeriodAgeInterval += (double)iteratorYearResult.population / periodMeanYearCount;
                        }
                        // Count benchmark population for this perion and age-interval.
                        if(iteratorSettlement instanceof elv.util.parameters.BenchmarkSettlement) {
                          benchPopulationPeriodAgeInterval += (double)iteratorYearResult.population / periodMeanYearCount;
                        }
                      }
                    }
                  }
                }

                benchPopulationYear += benchPopulationYearAgeInterval;
                benchCasesYear += benchCasesYearAgeInterval;
                benchPopulationYearInterval += benchPopulationYearIntervalAgeInterval;
                benchCasesYearInterval += benchCasesYearIntervalAgeInterval;
                benchPopulationPeriod += benchPopulationPeriodAgeInterval;
                benchCasesPeriod += benchCasesPeriodAgeInterval;

                // Observed and expected.
                populationYear += populationYearAgeInterval;
                observedCasesYear += observedCasesYearAgeInterval;
                populationYearInterval += populationYearIntervalAgeInterval;
                observedCasesYearInterval += observedCasesYearIntervalAgeInterval;
                populationPeriod += populationPeriodAgeInterval;
                observedCasesPeriod += observedCasesPeriodAgeInterval;
                if(standardizationMethod.equals(STANDARDIZATIONS[DIRECT])) {
                  expectedCasesYear += (populationYearAgeInterval == 0 ? 0 : (double)observedCasesYearAgeInterval / populationYearAgeInterval * benchPopulationYearAgeInterval);
                  expectedCasesYearInterval += (populationYearIntervalAgeInterval == 0 ? 0 : (double)observedCasesYearIntervalAgeInterval / populationYearIntervalAgeInterval * benchPopulationYearIntervalAgeInterval);
                  expectedCasesPeriod += (populationPeriodAgeInterval == 0 ? 0 : (double)observedCasesPeriodAgeInterval / populationPeriodAgeInterval * benchPopulationPeriodAgeInterval);
                } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
                  expectedCasesYear += (benchPopulationYearAgeInterval == 0 ? 0 : (double)benchCasesYearAgeInterval / benchPopulationYearAgeInterval * populationYearAgeInterval);
                  expectedCasesYearInterval += (benchPopulationYearIntervalAgeInterval == 0 ? 0 : (double)benchCasesYearIntervalAgeInterval / benchPopulationYearIntervalAgeInterval * populationYearIntervalAgeInterval);
                  expectedCasesPeriod += (benchPopulationPeriodAgeInterval == 0 ? 0 : (double)benchCasesPeriodAgeInterval / benchPopulationPeriodAgeInterval * populationPeriodAgeInterval);
                }
              }
              // Determine smr and incidence for year.
              double smrYear = 0;
              if(standardizationMethod.equals(STANDARDIZATIONS[DIRECT])) {
                smrYear = (benchCasesYear == 0 ? 0 : expectedCasesYear / benchCasesYear);
                expectedCasesYear = (smrYear == 0 ? 0 : observedCasesYear / smrYear);
              } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
                smrYear = (expectedCasesYear == 0 ? 0 : observedCasesYear / expectedCasesYear);
              }
              double incidenceYear = (benchPopulationYear == 0 ? 0 : smrYear * benchCasesYear / benchPopulationYear * 1000);

              // Determine the significance, category and probability for year.
              int smrSignificanceYear = signify(observedCasesYear, expectedCasesYear);
              int smrCategoryYear = categorize(smrYear, smrSignificanceYear);
              double probabilityYear = computeProbability(signifyPoisson(observedCasesYear, expectedCasesYear), standardizationProbabilities);
              int probabilityCategoryYear = categorizeProbability(probabilityYear);

              DistrictYearResult districtYearResult = new DistrictYearResult(iYear, populationYear, observedCasesYear, expectedCasesYear, incidenceYear, smrYear, smrSignificanceYear, smrCategoryYear, probabilityYear, probabilityCategoryYear);
              if(!yearIsParsed) {
                // Summarize.
                sumX += iYear;
                sumY += incidenceYear;
                sumX2 += iYear * iYear;
                sumY2 += incidenceYear * incidenceYear;
                sumXY += iYear * incidenceYear;

                // Prepare line for year.
                line = iYear + VS + iteratorDistrict.getCode() + VS
                  + populationYear + VS + observedCasesYear + VS + elv.util.Util.toString(expectedCasesYear) + VS
                  + elv.util.Util.toString(incidenceYear) + VS + elv.util.Util.toString(smrYear) + VS
                  + elv.util.Util.toString(smrSignificanceYear) + VS + smrCategoryYear + VS
                  + elv.util.Util.toString(probabilityYear) + VS + probabilityCategoryYear;
                // Store line for year.
                java.io.PrintWriter yearsFileWriter = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(yearsPathName, true), elv.util.Util.FILE_ENCODING));
                yearsFileWriter.println(line);
                yearsFileWriter.close();
                iteratorDistrict.getYearResults().add(districtYearResult);
              }
              execution.getProgresses().peek().setValue(iYear - iYearInterval.getFromValue() + 1);
            }
            execution.getProgresses().pop();
            // Determine smr and incidence for year-interval.
            double smrYearInterval = 0;
            if(standardizationMethod.equals(STANDARDIZATIONS[DIRECT])) {
              smrYearInterval = (benchCasesYearInterval == 0 ? 0 : expectedCasesYearInterval / benchCasesYearInterval);
              expectedCasesYearInterval = (smrYearInterval == 0 ? 0 : observedCasesYearInterval / smrYearInterval);
            } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
              smrYearInterval = (expectedCasesYearInterval == 0 ? 0 : observedCasesYearInterval / expectedCasesYearInterval);
            }
            double incidenceYearInterval = (benchPopulationYearInterval == 0 ? 0 : smrYearInterval * benchCasesYearInterval / benchPopulationYearInterval * 1000);
            // Determine the significance, category and probability for year.
            int smrSignificanceYearInterval = signify(observedCasesYearInterval, expectedCasesYearInterval);
            int smrCategoryYearInterval = categorize(smrYearInterval, smrSignificanceYearInterval);
            double probabilityYearInterval = computeProbability(signifyPoisson(observedCasesYearInterval, expectedCasesYearInterval), standardizationProbabilities);
            int probabilityCategoryYearInterval = categorizeProbability(probabilityYearInterval);

            iteratorDistrict.getWindowIntervalResults().add(new DistrictWindowIntervalResult(iteratorYearWindow, iYearInterval, (int)populationYearInterval, observedCasesYearInterval, expectedCasesYearInterval, incidenceYearInterval, smrYearInterval, smrSignificanceYearInterval, smrCategoryYearInterval, probabilityYearInterval, probabilityCategoryYearInterval));
            // Prepare line for year-interval.
            line = iteratorYearWindow + VS + iYearInterval + VS + iteratorDistrict.getCode() + VS
              + (int)populationYearInterval + VS + observedCasesYearInterval + VS
              + elv.util.Util.toString(expectedCasesYearInterval) + VS + elv.util.Util.toString(incidenceYearInterval) + VS
              + elv.util.Util.toString(smrYearInterval) + VS + elv.util.Util.toString(smrSignificanceYearInterval) + VS
              + smrCategoryYearInterval + VS + elv.util.Util.toString(probabilityYearInterval) + VS + probabilityCategoryYearInterval;
            // Store line for year-interval.
            java.io.PrintWriter windowsIntervalsFileWriter = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(windowsIntervalsPathName, true), elv.util.Util.FILE_ENCODING));
            windowsIntervalsFileWriter.println(line);
            windowsIntervalsFileWriter.close();
          }
        }
        execution.getProgresses().peek().setValue(yearWindowCount + 1);
      }
      execution.getProgresses().pop();

      double smrPeriod = 0;
      if(standardizationMethod.equals(STANDARDIZATIONS[DIRECT])) {
        smrPeriod = (benchCasesPeriod == 0 ? 0 : expectedCasesPeriod / benchCasesPeriod);
        expectedCasesPeriod = (smrPeriod == 0 ? 0 : observedCasesPeriod / smrPeriod);
      } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
        smrPeriod = (expectedCasesPeriod == 0 ? 0 : observedCasesPeriod / expectedCasesPeriod);
      }
      double incidencePeriod = (benchPopulationPeriod == 0 ? 0 : smrPeriod * benchCasesPeriod / benchPopulationPeriod * 1000);
      // Determine the significance, category and probability for year.
      int smrSignificancePeriod = signify(observedCasesPeriod, expectedCasesPeriod);
      int smrCategoryPeriod = categorize(smrPeriod, smrSignificancePeriod);
      double probabilityPeriod = computeProbability(signifyPoisson(observedCasesPeriod, expectedCasesPeriod), standardizationProbabilities);
      int probabilityCategoryPeriod = categorizeProbability(probabilityPeriod);
      double sQx = (double)sumX2 - (double)sumX * sumX / execution.getYears().size();
      double sQy = sumY2 - sumY * sumY / execution.getYears().size();
      double sP = sumXY - (double)sumX * sumY / execution.getYears().size();
      double trendPeriod = (sQx == 0 ? 0 : sP / sQx);
      java.lang.String trendSignificancePeriod = signify(sQx, sQy, sP, execution.getYears(), trendSignificances);
      double trendCorrelationPeriod = (sQx == 0 || sQy == 0 ? 0 : java.lang.Math.abs(sP / java.lang.Math.sqrt(java.lang.Math.abs(sQx * sQy))));
      int trendCategoryPeriod = categorizeTrend(trendPeriod);
      iteratorDistrict.setResult(new DistrictResult((int)populationPeriod, observedCasesPeriod, expectedCasesPeriod, incidencePeriod,
        smrPeriod, smrSignificancePeriod, smrCategoryPeriod, probabilityPeriod, probabilityCategoryPeriod, trendPeriod,
        trendSignificancePeriod, trendCorrelationPeriod, trendCategoryPeriod));
      // Prepare line for district.
      line = iteratorDistrict.getCode() + VS + (int)populationPeriod + VS
        + observedCasesPeriod + VS + elv.util.Util.toString(expectedCasesPeriod) + VS
        + elv.util.Util.toString(incidencePeriod) + VS + elv.util.Util.toString(smrPeriod) + VS
        + elv.util.Util.toString(smrSignificancePeriod) + VS + smrCategoryPeriod + VS
        + elv.util.Util.toString(probabilityPeriod) + VS + probabilityCategoryPeriod + VS
        + elv.util.Util.toString(trendPeriod) + VS + trendSignificancePeriod + VS
        + elv.util.Util.toString(trendCorrelationPeriod) + VS + trendCategoryPeriod;
      // Store line for year-interval.
      java.io.PrintWriter fileWriter = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(pathName, true), elv.util.Util.FILE_ENCODING));
      fileWriter.println(line);
      fileWriter.close();

    }
  }
}
