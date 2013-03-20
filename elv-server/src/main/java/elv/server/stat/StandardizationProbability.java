package elv.server.stat;

import java.io.Serializable;

/**
 * Standardization probability.
 */
public class StandardizationProbability implements Serializable {
  private static final long serialVersionUID = 1L;

  public final double probability;
  public final double hi2;

  public StandardizationProbability(double probability, double hi2) {
    this.probability = probability;
    this.hi2 = hi2;
  }
}
