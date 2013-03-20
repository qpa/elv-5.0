package elv.server.stat;

/**
 * Categorizing argument.
 */
public final class CategorizingArgument {
  public final int significance;
  public final double minValue;
  public final double maxValue;
  
  public CategorizingArgument(int significance, double minValue, double maxValue) {
    this.significance = significance;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }
}
