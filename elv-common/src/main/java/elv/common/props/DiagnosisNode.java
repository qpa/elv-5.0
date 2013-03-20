package elv.common.props;

/**
 * Disease node.
 */
public class DiagnosisNode extends Node {

  public final Diagnosis diagnosis;

  public DiagnosisNode(Diagnosis diagnosis) {
    this.diagnosis = diagnosis;
  }

  public Diagnosis getDiagnosis() {
    return diagnosis;
  }

  public DiagnosisNode getDescendant(Diagnosis diagnosis) {
    DiagnosisNode descendant = null;
    if(!isLeaf()) {
      for(Node node : children) {
        DiagnosisNode dNode = (DiagnosisNode)node;
        if(diagnosis.equals(dNode.diagnosis)) {
          descendant = dNode;
        } else {
          descendant = dNode.getDescendant(diagnosis);
        }
      }
    }
    return descendant;
  }
}
