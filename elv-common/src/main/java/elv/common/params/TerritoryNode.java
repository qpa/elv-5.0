package elv.common.params;

/**
 * Territory node.
 */
public class TerritoryNode extends Node {

  public final Territory territory;

  public TerritoryNode(Territory territory) {
    this.territory = territory;
  }

  public Territory getTerritory() {
    return territory;
  }

  /**
   * Creator of a new custom range code.
   * @return the newly created code.
   */
  public String createNewCustomCode() {
    int newCode = 0;
    while(newCode <= children.size()) {
      newCode++;
      boolean isCreated = false;
      for (final Node child : children) {
        Territory iteratorRange = ((TerritoryNode) child).territory;
        int iteratorCode = Integer.valueOf(iteratorRange.code);
        if (newCode == iteratorCode) {
          isCreated = true;
          break;
        }
      }
      if (!isCreated) {
        return String.valueOf(newCode);
      }
    }
    return "";
  }
}
