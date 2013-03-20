package elv.server.stat;

import java.io.Serializable;

/**
 * Trend significance.
 */
public class TrendSignificance implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public final int yearCount;
  public final double trend0_1;
  public final double trend0_05;
  public final double trend0_01;
  public final double trend0_001;

  public TrendSignificance(int yearCount, double trend0_1, double trend0_05, double trend0_01, double trend0_001) {
    this.yearCount = yearCount;
    this.trend0_1 = trend0_1;
    this.trend0_05 = trend0_05;
    this.trend0_01 = trend0_01;
    this.trend0_001 = trend0_001;
  }
}
