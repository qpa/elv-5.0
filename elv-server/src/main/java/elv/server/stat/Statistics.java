package elv.server.stat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Standardization methods.
 */
public final class Statistics {
  /**
   * Computes Poisson SMR significance.
   * @param observedCases the analyzed cases.
   * @param expectedCases the expected cases.
   * @return the significance of standardization.
   */
  public static double signifyPoisson(int observedCases, double expectedCases) {
    return (observedCases == 0 || expectedCases == 0 ? 0 : ((double)observedCases - expectedCases) * ((double)observedCases - expectedCases) / expectedCases);
  }

  /**
   * Computes Pearson SMR significance.
   * @param observedCases the analyzed cases.
   * @param totalCases the total cases.
   * @param population the analyzed population.
   * @param totalPopulation the total population.
   * @return the significance of standardization.
   */
  public static double signifyPearson(int observedCases, int totalCases, int population, int totalPopulation) {
    double e1 = ((double)observedCases + population) * totalCases / (totalCases + totalPopulation);
    double e2 = ((double)observedCases + population) * totalPopulation / (totalCases + totalPopulation);
    double e3 = ((double)totalCases - observedCases + totalPopulation - population) * totalCases / (totalCases + totalPopulation);
    double e4 = ((double)totalCases - observedCases + totalPopulation - population) * totalPopulation / (totalCases + totalPopulation);

    return ((double)observedCases - e1) * ((double)observedCases - e1) / e1
            + ((double)population - e2) * ((double)population - e2) / e2
            + ((double)totalCases - observedCases - e3) * ((double)totalCases - observedCases - e3) / e3
            + ((double)totalPopulation - population - e4) * ((double)totalPopulation - population - e4) / e4;
  }

  /**
   * Computes Likelyhood SMR significance.
   * @param observedCases the analyzed cases.
   * @param totalCases the total cases.
   * @param population the analyzed population.
   * @param totalPopulation the total population.
   * @return the significance of standardization.
   */
  public static double signifyLikelyhood(int observedCases, int totalCases, int population, int totalPopulation) {
    double e1 = ((double)observedCases + population) * totalCases / (totalCases + totalPopulation);
    double e2 = ((double)observedCases + population) * totalPopulation / (totalCases + totalPopulation);
    double e3 = ((double)totalCases - observedCases + totalPopulation - population) * totalCases / (totalCases + totalPopulation);
    double e4 = ((double)totalCases - observedCases + totalPopulation - population) * totalPopulation / (totalCases + totalPopulation);

    return 2 * (observedCases * Math.log(observedCases / e1)
            + population * Math.log(population / e2)
            + (totalCases - observedCases) * Math.log((totalCases - observedCases) / e3)
            + (totalPopulation - population) * Math.log((totalPopulation - population) / e4));
  }

  /**
   * Computes SMR significance.
   * @param observedCases the analyzed cases.
   * @param expectedCases the expected cases.
   * @return the significance of standardization.
   */
  public static int signify(int observedCases, double expectedCases) {
    double hi2 = signifyPoisson(observedCases, expectedCases);
    return (hi2 > 3.84 ? 1 : -1);
  }

  /**
   * Computes probability.
   * @param hi2 the significance of standardization.
   * @param sP a list of standardization probabilities.
   * @return the probability of standardization.
   */
  public static double computeProbability(double hi2, List<StandardizationProbability> sP) {
    double probability;
    if(hi2 < sP.get(0).hi2) { // The firt element is the minimum in hi2s.
      probability = 1;
    } else if(hi2 > sP.get(sP.size() - 1).hi2) { // The last element is the maximum in hi2s.
      probability = 0;
    } else {
      int i = -1;
      for(StandardizationProbability iteratorSP : sP) {
        i++;
        if(hi2 < iteratorSP.hi2) {
          break;
        }
      }
      if(i == sP.size() - 1 && hi2 == sP.get(i).hi2) {
        probability = 1 - sP.get(i).probability;
      } else if(hi2 == sP.get(i - 1).hi2) {
        probability = 1 - sP.get(i - 1).probability;
      } else {
        probability = 1 - (sP.get(i - 1).probability + (sP.get(i).probability - sP.get(i - 1).probability)
                / (sP.get(i).hi2 - sP.get(i - 1).hi2) * (hi2 - sP.get(i - 1).hi2));
      }
    }
    return probability;
  }

  /**
   * Computes probability as string.
   * @param hi2 the significance of standardization.
   * @param sP a list of standardization probabilities.
   * @return the probability of standardization as a string.
   */
  public static String computeProbabilityAsString(double hi2, List<StandardizationProbability> sP) {
    double probability = computeProbability(hi2, sP);
    String probabilityString;
    if(probability == 1) {
      probabilityString = "= 1";
    } else if(probability == 0) {
      probabilityString = "= 0";
    } else if(probability <= 0.01) {
      probabilityString = "<= 0.01";
    } else if(probability <= 0.05) {
      probabilityString = "<= 0.05";
    } else if(probability <= 0.1) {
      probabilityString = "> 0.05 <= 0.1";
    } else {
      probabilityString = "> 0.1";
    }
    return probabilityString;
  }

  /**
   * Computes trend significance.
   * @param sQx the year correlation.
   * @param sQy the incidence correlation.
   * @param sP the mixed correlation.
   * @param years a list of years.
   * @param trendSignificances a list of trend significances.
   * @return the significance of standardization.
   */
  public static String signify(double sQx, double sQy, double sP, List<Integer> years, List<TrendSignificance> trendSignificances) {
    String significance = "= 0";
    if(years.size() > 2) {  // Otherwise trend significance has no meaning.
      int index = -1;
      for(TrendSignificance tS : trendSignificances) {
        index++;
        if(years.size() - 2 >= tS.yearCount) {
          break;
        }
      }
      double a = sQy - sP * sP / sQx;
      double b = a / (years.size() - 2);
      double trend = sP / sQx;
      double T = Math.abs(trend / Math.sqrt(Math.abs(b / sQx)));

      if(trend == 0) {
        significance = "= 0";
      } else if(T < trendSignificances.get(index).trend0_1) {
        significance = "> 0.1";
      } else if(T >= trendSignificances.get(index).trend0_1 && T < trendSignificances.get(index).trend0_05) {
        significance = "> 0.05 <= 0.1";
      } else if(T >= trendSignificances.get(index).trend0_05 && T < trendSignificances.get(index).trend0_01) {
        significance = "<= 0.05";
      } else if(T >= trendSignificances.get(index).trend0_01 && T < trendSignificances.get(index).trend0_001) {
        significance = "<= 0.01";
      } else {
        significance = "<= 0.001";
      }
    }
    return significance;
  }

  public static List<TrendSignificance> loadAllTrendSignificances() throws UnsupportedEncodingException, IOException {
    List<TrendSignificance> trendSignificances = new ArrayList<>();
    String line;
    try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(Statistics.class.getResource("/trend-significance.param").openStream(), StandardCharsets.UTF_8))) {
      while((line = fileReader.readLine()) != null) {
        String[] splittedLine = line.split("=");
        int yearCount = Integer.parseInt(splittedLine[0]);
        String[] trends = splittedLine[1].split(Pattern.quote(","));
        double trend0_1 = Double.parseDouble(trends[0]);
        double trend0_05 = Double.parseDouble(trends[1]);
        double trend0_01 = Double.parseDouble(trends[2]);
        double trend0_001 = Double.parseDouble(trends[3]);
        trendSignificances.add(new TrendSignificance(yearCount, trend0_1, trend0_05, trend0_01, trend0_001));
      }
    }
    return trendSignificances;
  }
  
  public static List<StandardizationProbability> loadAllStandardizationProbabilities() throws UnsupportedEncodingException, IOException {
    List<StandardizationProbability> standardizationProbabilities = new ArrayList<>();
    String line;
    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(Statistics.class.getResource("/probability.param").openStream(), StandardCharsets.UTF_8))) {
      while((line = fileReader.readLine()) != null) {
        final String[] splitedLine = line.split(Pattern.quote("="));
        final double probability = Double.parseDouble(splitedLine[0]);
        final double hi2 = Double.parseDouble(splitedLine[1]);
        standardizationProbabilities.add(new StandardizationProbability(probability, hi2));
      }
    }
    return standardizationProbabilities;
  }
}
