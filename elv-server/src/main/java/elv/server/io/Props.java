package elv.server.io;

import elv.server.stat.StandardizationProbability;
import elv.server.stat.TrendSignificance;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Property loaders
 */
public class Props {

  private Props() {
  }

  public static List<TrendSignificance> loadAllTrendSignificances() throws UnsupportedEncodingException, IOException {
    List<TrendSignificance> trendSignificances = new ArrayList<>();
    String line;
    try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(Props.class.getResource("/trend-significance.prop").openStream(), StandardCharsets.UTF_8))) {
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
    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(StandardizationProbability.class.getResource("/probability.prop").openStream(), StandardCharsets.UTF_8))) {
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
