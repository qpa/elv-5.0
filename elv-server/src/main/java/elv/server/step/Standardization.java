package elv.server.step;

import elv.common.params.Interval;
import elv.common.params.TerritoryNode;
import elv.server.proc.Params;
import elv.server.proc.Process;
import java.util.List;

/**
 * Standardization.
 */
public class Standardization extends AbstractStep {
  private List<Interval> yearIntervals;
  private List<List<Interval>> yearWindowIntervals;
  private List<TerritoryNode> baseRangeNodes;

  @Override
  public int initParams(Process process) {
    yearIntervals = Params.getYearIntervals(process);
    yearWindowIntervals = Params.getYearWindowIntervals(process);
    baseRangeNodes = Params.getBaseRangeNodes(process);
    return;
  }

  @Override
  public void doCompute(Process process) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
