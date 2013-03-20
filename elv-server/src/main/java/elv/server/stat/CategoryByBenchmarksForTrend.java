package elv.server.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * Category by fixed benchmarks for trends.
 */
public final class CategoryByBenchmarksForTrend extends CategoryByBenchmarks {
  private static final List<Double> BENCHMARKS = new ArrayList<>();
  
  static {
    BENCHMARKS.add(0.25);
    BENCHMARKS.add(0.06);
    BENCHMARKS.add(-0.06);
    BENCHMARKS.add(-0.25);
  }
  
  public CategoryByBenchmarksForTrend() {
    super(BENCHMARKS);
  }
}
