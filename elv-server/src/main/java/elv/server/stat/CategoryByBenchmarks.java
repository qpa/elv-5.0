package elv.server.stat;

import java.util.List;

/**
 * Category by benchmarks.
 */
public class CategoryByBenchmarks implements Category {
  public final List<Double> benchmarks;

  public CategoryByBenchmarks(List<Double> benchmarks) {
    this.benchmarks = benchmarks;
  }

  @Override
  public int categorize(double value, CategorizingArgument argument) {
    int category;
    for(category = 0; category < benchmarks.size(); category++) {
      if(benchmarks.get(category) > value) {
        break;
      }
    }
    return category + 1;
  }
}
