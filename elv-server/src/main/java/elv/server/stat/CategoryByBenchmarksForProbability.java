package elv.server.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * Category by fixed benchmarks for probabilities.
 * @author Elv
 */
public final class CategoryByBenchmarksForProbability extends CategoryByBenchmarks {
  private static final List<Double> BENCHMARKS = new ArrayList<>();
  
  static {
    BENCHMARKS.add(0.01);
    BENCHMARKS.add(0.05);
    BENCHMARKS.add(0.1);
    BENCHMARKS.add(0.2);
  }
  
  public CategoryByBenchmarksForProbability() {
    super(BENCHMARKS);
  }
}
