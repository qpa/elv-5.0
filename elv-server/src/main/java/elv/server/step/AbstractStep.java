package elv.server.step;

import elv.server.proc.Process;
import elv.server.result.Key;
import elv.server.result.Value;
import java.util.Map;

/**
 * General Step. All Steps must extend this class, instead of implementing Step.
 */
public abstract class AbstractStep implements Step {
  public static final String POPULATION_SQL = "SELECT SUM(population) FROM population WHERE ";
  public static final String MORTALITY_SQL = "SELECT COUNT(*) FROM mortality WHERE ";
  protected final String name = this.getClass().getSimpleName();
  protected Map<Key, Value> results;

  private void initResults(Process process) {
    results = process.getResultDb().getHashMap(name);
  }

  @Override
  public void compute(Process process) {
    // Initialize result map.
    initResults(process);
    // Initialize parameters.
    int maxProgressValue = initParams(process);
    // Set progress.
    process.createProgress(name, maxProgressValue);
    // Do real computation.
    doCompute(process);
  }

  /**
   * Initializes the parameters for this step and returns the max progress number.
   * @param process
   * @return the max progress number.
   */
  public abstract int initParams(Process process);

  public abstract void doCompute(Process process);
}
