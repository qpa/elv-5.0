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
  private List<TerritoryNode> baseRangeNodes;
  private Territory benchmarkRange;
  private List<Integer> yearWindows;
  private List<List<Interval>> yearWindowIntervals;
  private List<Integer> years;
  private Integer benchmarkYear;
  private List<Interval> ageIntervals;
  private Map<Key, Value> preparationResults;

  @Override
  public int initParams(Process process) {
    baseRangeNodes = Params.getBaseRangeNodes(process);
    benchmarkRange = Params.getBenchmarkRangeNode(process).territory;
    standardizationMode = Params.getStandardizationMode(process);
    yearWindows = getYearWindows(process);
    yearWindowIntervals = Params.getYearWindowIntervals(process);
    years = Params.getYears(process);
    benchmarkYear = Params.getBenchmarkYear(process);
    ageIntervals = Params.getAgeIntervals(process);
    preparationResults = process.getResultDb().getHashMap(MortalityStandardPreparation.class.getSimpleName());
    return baseRangeNodes.size() * yearWindows.size() *;
  }

  @Override
  public void doCompute(Process process) {
    int fromYearPeriod = years.get(0);
    int toYearPeriod = years.get(years.size() - 1);
    int periodMeanYear = benchmarkYear == null ? (fromYearPeriod + toYearPeriod) / 2 : benchmarkYear;
    int periodMeanYearCount = benchmarkYear == null && periodMeanYear - fromYearPeriod < toYearPeriod - periodMeanYear ? 2 : 1;

    int meanYearCount = 1;
    int meanYear = benchmarkYear == null ? 0 : benchmarkYear;
    int benchYear = benchmarkYear == null ? 0 : benchmarkYear;

    for(TerritoryNode iRangeNode : baseRangeNodes) {
      Territory iRange = iRangeNode.territory;
      double periodPopulation = 0;
      int periodObservedCases = 0;
      double periodExpectedCases = 0;
      double periodBenchPopulation = 0;
      int periodBenchCases = 0;
      int sumX = 0;
      double sumY = 0;
      int sumX2 = 0;
      double sumY2 = 0;
      double sumXY = 0;

      for(int yearWindowCount = 0; yearWindowCount < yearWindowIntervals.size(); yearWindowCount++) {
        int iteratorYearWindow = yearWindows.get(yearWindowCount);

        execution.getProgresses().push(new elv.util.Progress(elv.util.parameters.YearInterval.TITLE, 0, yearWindowIntervals.get(yearWindowCount).size()));
        for(Interval iYearInterval : yearWindowIntervals.get(yearWindowCount)) {
          if(benchmarkYear == null) {
            meanYearCount = 1;
            meanYear = (iYearInterval.from + iYearInterval.to) / 2;
            if(meanYear - iYearInterval.from < iYearInterval.to - meanYear) {
              meanYearCount = 2;
            }
          }

          double yearIntervalPopulation = 0;
          int yearIntervalObservedCases = 0;
          double yearIntervalExpectedCases = 0;
          double yearIntervalBenchPopulation = 0;
          int yearIntervalBenchCases = 0;

          execution.getProgresses().push(new elv.util.Progress(elv.util.parameters.YearInterval.YEARS_TITLE, 0, iYearInterval.getToValue() - iYearInterval.getFromValue() + 1));
          for(int iYear = iYearInterval.from; iYear <= iYearInterval.to; iYear++) {
            // Assigning the benchmark year to the standardization benchmark, if there is.
            if(benchmarkYear == null) {
              benchYear = iYear;
            }

            Key yearKey = new Key.Builder().setTerritory(iRange).setYear(iYear).build();
            Value yearValue = results.get(yearKey);
            if(yearValue == null) {

              int yearPopulation = 0;
              int yearObservedCases = 0;
              double yearExpectedCases = 0;
              int yearBenchPopulation = 0;
              int yearBenchCases = 0;

              for(Interval iAgeInterval : ageIntervals) {
                Key yearAgeIKey = new Key.Builder().setTerritory(iRange).setYear(iYear).setAgeInterval(iAgeInterval).build();
                Key benchYearAgeIKey = new Key.Builder().setTerritory(benchmarkRange).setYear(iYear).setAgeInterval(iAgeInterval).build();
                
                int populationYearAgeI = preparationResults.get(yearAgeIKey).population;
                int observedCasesYearAgeI = preparationResults.get(yearAgeIKey).observedCases;
                int benchPopulationYearAgeI = preparationResults.get(benchYearAgeIKey).population;
                int benchCasesYearAgeInterval = preparationResults.get(benchYearAgeIKey).observedCases;
                
                
                double benchPopulationPeriodAgeInterval = 0;
                int benchCasesPeriodAgeInterval = 0;
                double populationPeriodAgeInterval = 0;
                int observedCasesPeriodAgeInterval = 0;

                double benchPopulationYearIntervalAgeInterval = 0;
                int benchCasesYearIntervalAgeInterval = 0;
                double populationYearIntervalAgeInterval = 0;
                int observedCasesYearIntervalAgeInterval = 0;

                for(elv.util.parameters.Settlement iteratorSettlement : execution.getAllSettlements()) {
                  for(SettlementYearResult iteratorYearResult : iteratorSettlement.getYearResults()) {
                    if(iteratorYearResult.year == benchYear) {
                      if(iteratorYearResult.ageInterval.equals(iAgeInterval)) {
                        // Count district population and cases for this year and age-interval.
                        if(iteratorSettlement.getDistrictCode() == iteratorDistrict.getCode()
                          && iteratorSettlement.getAggregation().equals(iteratorDistrict.getAggregation())) {
                          populationYearAgeI += iteratorYearResult.population;
                          observedCasesYearAgeI += iteratorYearResult.analyzedCases;
                        }
                        // Count benchmark population and cases for this year and age-interval.
                        if(iteratorSettlement instanceof elv.util.parameters.BenchmarkSettlement) {
                          benchPopulationYearAgeI += iteratorYearResult.population;
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

                yearBenchPopulation += benchPopulationYearAgeI;
                yearBenchCases += benchCasesYearAgeInterval;
                yearIntervalBenchPopulation += benchPopulationYearIntervalAgeInterval;
                yearIntervalBenchCases += benchCasesYearIntervalAgeInterval;
                periodBenchPopulation += benchPopulationPeriodAgeInterval;
                periodBenchCases += benchCasesPeriodAgeInterval;

                // Observed and expected.
                yearPopulation += populationYearAgeI;
                yearObservedCases += observedCasesYearAgeI;
                yearIntervalPopulation += populationYearIntervalAgeInterval;
                yearIntervalObservedCases += observedCasesYearIntervalAgeInterval;
                periodPopulation += populationPeriodAgeInterval;
                periodObservedCases += observedCasesPeriodAgeInterval;
                if(standardizationMethod.equals(STANDARDIZATIONS[DIRECT])) {
                  yearExpectedCases += (populationYearAgeI == 0 ? 0 : (double)observedCasesYearAgeI / populationYearAgeI * benchPopulationYearAgeI);
                  yearIntervalExpectedCases += (populationYearIntervalAgeInterval == 0 ? 0 : (double)observedCasesYearIntervalAgeInterval / populationYearIntervalAgeInterval * benchPopulationYearIntervalAgeInterval);
                  periodExpectedCases += (populationPeriodAgeInterval == 0 ? 0 : (double)observedCasesPeriodAgeInterval / populationPeriodAgeInterval * benchPopulationPeriodAgeInterval);
                } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
                  yearExpectedCases += (benchPopulationYearAgeI == 0 ? 0 : (double)benchCasesYearAgeInterval / benchPopulationYearAgeI * populationYearAgeI);
                  yearIntervalExpectedCases += (benchPopulationYearIntervalAgeInterval == 0 ? 0 : (double)benchCasesYearIntervalAgeInterval / benchPopulationYearIntervalAgeInterval * populationYearIntervalAgeInterval);
                  periodExpectedCases += (benchPopulationPeriodAgeInterval == 0 ? 0 : (double)benchCasesPeriodAgeInterval / benchPopulationPeriodAgeInterval * populationPeriodAgeInterval);
                }
              }
              // Determine smr and incidence for year.
              double smrYear = 0;
              if(standardizationMethod.equals(STANDARDIZATIONS[DIRECT])) {
                smrYear = (yearBenchCases == 0 ? 0 : yearExpectedCases / yearBenchCases);
                yearExpectedCases = (smrYear == 0 ? 0 : yearObservedCases / smrYear);
              } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
                smrYear = (yearExpectedCases == 0 ? 0 : yearObservedCases / yearExpectedCases);
              }
              double incidenceYear = (yearBenchPopulation == 0 ? 0 : smrYear * yearBenchCases / yearBenchPopulation * 1000);

              // Determine the significance, category and probability for year.
              int smrSignificanceYear = signify(yearObservedCases, yearExpectedCases);
              int smrCategoryYear = categorize(smrYear, smrSignificanceYear);
              double probabilityYear = computeProbability(signifyPoisson(yearObservedCases, yearExpectedCases), standardizationProbabilities);
              int probabilityCategoryYear = categorizeProbability(probabilityYear);

              DistrictYearResult districtYearResult = new DistrictYearResult(iYear, yearPopulation, yearObservedCases, yearExpectedCases, incidenceYear, smrYear, smrSignificanceYear, smrCategoryYear, probabilityYear, probabilityCategoryYear);
              if(!yearIsParsed) {
                // Summarize.
                sumX += iYear;
                sumY += incidenceYear;
                sumX2 += iYear * iYear;
                sumY2 += incidenceYear * incidenceYear;
                sumXY += iYear * incidenceYear;

                // Prepare line for year.
                line = iYear + VS + iteratorDistrict.getCode() + VS
                  + yearPopulation + VS + yearObservedCases + VS + elv.util.Util.toString(yearExpectedCases) + VS
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
              smrYearInterval = (yearIntervalBenchCases == 0 ? 0 : yearIntervalExpectedCases / yearIntervalBenchCases);
              yearIntervalExpectedCases = (smrYearInterval == 0 ? 0 : yearIntervalObservedCases / smrYearInterval);
            } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
              smrYearInterval = (yearIntervalExpectedCases == 0 ? 0 : yearIntervalObservedCases / yearIntervalExpectedCases);
            }
            double incidenceYearInterval = (yearIntervalBenchPopulation == 0 ? 0 : smrYearInterval * yearIntervalBenchCases / yearIntervalBenchPopulation * 1000);
            // Determine the significance, category and probability for year.
            int smrSignificanceYearInterval = signify(yearIntervalObservedCases, yearIntervalExpectedCases);
            int smrCategoryYearInterval = categorize(smrYearInterval, smrSignificanceYearInterval);
            double probabilityYearInterval = computeProbability(signifyPoisson(yearIntervalObservedCases, yearIntervalExpectedCases), standardizationProbabilities);
            int probabilityCategoryYearInterval = categorizeProbability(probabilityYearInterval);

            iteratorDistrict.getWindowIntervalResults().add(new DistrictWindowIntervalResult(iteratorYearWindow, iYearInterval, (int)yearIntervalPopulation, yearIntervalObservedCases, yearIntervalExpectedCases, incidenceYearInterval, smrYearInterval, smrSignificanceYearInterval, smrCategoryYearInterval, probabilityYearInterval, probabilityCategoryYearInterval));
            // Prepare line for year-interval.
            line = iteratorYearWindow + VS + iYearInterval + VS + iteratorDistrict.getCode() + VS
              + (int)yearIntervalPopulation + VS + yearIntervalObservedCases + VS
              + elv.util.Util.toString(yearIntervalExpectedCases) + VS + elv.util.Util.toString(incidenceYearInterval) + VS
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
        smrPeriod = (periodBenchCases == 0 ? 0 : periodExpectedCases / periodBenchCases);
        periodExpectedCases = (smrPeriod == 0 ? 0 : periodObservedCases / smrPeriod);
      } else if(standardizationMethod.equals(STANDARDIZATIONS[INDIRECT])) {
        smrPeriod = (periodExpectedCases == 0 ? 0 : periodObservedCases / periodExpectedCases);
      }
      double incidencePeriod = (periodBenchPopulation == 0 ? 0 : smrPeriod * periodBenchCases / periodBenchPopulation * 1000);
      // Determine the significance, category and probability for year.
      int smrSignificancePeriod = signify(periodObservedCases, periodExpectedCases);
      int smrCategoryPeriod = categorize(smrPeriod, smrSignificancePeriod);
      double probabilityPeriod = computeProbability(signifyPoisson(periodObservedCases, periodExpectedCases), standardizationProbabilities);
      int probabilityCategoryPeriod = categorizeProbability(probabilityPeriod);
      double sQx = (double)sumX2 - (double)sumX * sumX / execution.getYears().size();
      double sQy = sumY2 - sumY * sumY / execution.getYears().size();
      double sP = sumXY - (double)sumX * sumY / execution.getYears().size();
      double trendPeriod = (sQx == 0 ? 0 : sP / sQx);
      java.lang.String trendSignificancePeriod = signify(sQx, sQy, sP, execution.getYears(), trendSignificances);
      double trendCorrelationPeriod = (sQx == 0 || sQy == 0 ? 0 : java.lang.Math.abs(sP / java.lang.Math.sqrt(java.lang.Math.abs(sQx * sQy))));
      int trendCategoryPeriod = categorizeTrend(trendPeriod);
      iteratorDistrict.setResult(new DistrictResult((int)periodPopulation, periodObservedCases, periodExpectedCases, incidencePeriod,
        smrPeriod, smrSignificancePeriod, smrCategoryPeriod, probabilityPeriod, probabilityCategoryPeriod, trendPeriod,
        trendSignificancePeriod, trendCorrelationPeriod, trendCategoryPeriod));
      // Prepare line for district.
      line = iteratorDistrict.getCode() + VS + (int)periodPopulation + VS
        + periodObservedCases + VS + elv.util.Util.toString(periodExpectedCases) + VS
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
