package elv.common.props;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree node.
 */
public class Node implements Serializable {
  protected transient Node parent;
  protected List<Node> children;
  
  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public List<Node> getChildren() {
    return children;
  }

  public void addChild(Node child) {
    if(children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
  }

  public boolean removeChild(Node child) {
    return children.remove(child);
  }

  public boolean isLeaf() {
    return children == null || children.isEmpty();
  }
}
