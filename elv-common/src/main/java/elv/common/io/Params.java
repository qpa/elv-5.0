package elv.common.io;

import elv.common.params.Node;
import elv.common.params.Diagnosis;
import elv.common.params.DiagnosisNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameter loaders
 */
public class Params {

  private Params() {
  }

  public static DiagnosisNode loadAllDiseaseDiagnoses() throws IOException {
    return loadAllDiagnoses("/disease.param");
  }
  
  public static DiagnosisNode loadAllAdmissionDiagnoses() throws IOException {
    return loadAllDiagnoses("/admission.param");
  }
  
  public static DiagnosisNode loadAllDismissalDiagnoses() throws IOException {
    return loadAllDiagnoses("/dismissal.param");
  }
  
  public static DiagnosisNode loadAllMorbidityDiagnoses() throws IOException {
    return loadAllDiagnoses("/morbidity.param");
  }
  
  public static DiagnosisNode loadAllMortalityDiagnoses() throws IOException {
    return loadAllDiagnoses("/mortality.param");
  }
  
  public static DiagnosisNode loadAllMorfologyDiagnoses() throws IOException {
    return loadAllDiagnoses("/morfology.param");
  }
  
  private static DiagnosisNode loadAllDiagnoses(String resourceName) throws IOException {
    DiagnosisNode rootNode = new DiagnosisNode(Diagnosis.ROOT);
    try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(Params.class.getResource(resourceName).openStream(), StandardCharsets.UTF_8))) {
      final Map<Node, Integer> depthMap = new HashMap<>();
      depthMap.put(rootNode, 0);
      DiagnosisNode prevNode = rootNode;
      String line;
      while((line = fileReader.readLine()) != null) {
        int eqIndex = line.indexOf("=");
        final String paragraph = line.substring(0, eqIndex);
        int depth = paragraph.split("\\.").length;
        final String cT = line.substring(eqIndex + 1);
        int spIndex = cT.indexOf(" ");
        final String code = cT.substring(0, spIndex).split(",")[0];
        final String text = cT.substring(spIndex + 1);
        Diagnosis lineDiagnosis = new Diagnosis(code, text);
        DiagnosisNode lineNode = new DiagnosisNode(lineDiagnosis);
        
        for(Node iteratorNode = prevNode; iteratorNode != null; iteratorNode = iteratorNode.getParent()) {
          if(depth > depthMap.get(iteratorNode)) {
            lineNode.setParent(iteratorNode);
            iteratorNode.addChild(lineNode);
            break;
          }
        }
        prevNode = lineNode;
        depthMap.put(prevNode, depth);
      }
    }
    return rootNode;
  }
}
