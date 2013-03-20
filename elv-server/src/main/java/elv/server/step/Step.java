package elv.server.step;

import elv.server.proc.Process;

/**
 * Step interface.
 */
public interface Step {
  void compute(Process process);
}
