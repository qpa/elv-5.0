package elv.server.io;

import elv.common.params.Diagnosis;
import elv.common.params.Interval;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

/**
 * Custom Yaml.
 */
public class Yaml extends org.yaml.snakeyaml.Yaml {
  private static final Tag INTERVAL_TAG = new Tag(Interval.class);
  private static final Tag DIAGNOSIS_TAG = new Tag(Diagnosis.class);

  public Yaml() {
    super(new Constructor(), new Representer(), new DumperOptions());
    addImplicitResolver(INTERVAL_TAG, Pattern.compile("^\\d+-\\d+$"), "123456789");
    addImplicitResolver(DIAGNOSIS_TAG, Pattern.compile("^[\\w\\-/]+\\|.*$"), null);
  }
  
  private static class DumperOptions extends org.yaml.snakeyaml.DumperOptions {

    public DumperOptions() {
      setDefaultFlowStyle(FlowStyle.BLOCK);
      setWidth(200);
    }
  }
  
  private static class Representer extends org.yaml.snakeyaml.representer.Representer {

    public Representer() {
      representers.put(Interval.class, new RepresentInterval());
      representers.put(Diagnosis.class, new RepresentDiagnosis());
    }

    private class RepresentInterval implements Represent {

      @Override
      public Node representData(Object data) {
        final Interval interval = (Interval) data;
        final String value = interval.from + "-" + interval.to;
        return representScalar(INTERVAL_TAG, value);
      }
    }

    private class RepresentDiagnosis implements Represent {

      @Override
      public Node representData(Object data) {
        final Diagnosis diagnosis = (Diagnosis) data;
        final String value = diagnosis.code + "|" + diagnosis.text;
        return representScalar(DIAGNOSIS_TAG, value);
      }
    }
//    
//    private class DiagnosisPropertyUtils extends PropertyUtils {
//      
//      @Override
//      protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) throws IntrospectionException {
//        Set<Property> properties = new TreeSet<>(new DiagnosisFieldOrder());
//        Collection<Property> props = getPropertiesMap(type, BeanAccess.FIELD).values();
//        for(Property property : props) {
//          if(!"parent".equals(property.getName())) {
//            properties.add(property);
//          }
//        }
//        return properties;
//      }
//    }
//    
//    private class DiagnosisFieldOrder implements Comparator<Property> {
//      private final Map<String, Integer > orderMap = new HashMap<>();
//
//      public DiagnosisFieldOrder() {
//        orderMap.put("diagnosis", 0);
//        orderMap.put("children", 1);
//      }
//
//      @Override
//      public int compare(Property o1, Property o2) {
//        return orderMap.get(o1.getName()) - orderMap.get(o2.getName());
//      }
//      
//    }
  }
  
  private static class Constructor extends org.yaml.snakeyaml.constructor.Constructor {

    public Constructor() {
      this.yamlConstructors.put(INTERVAL_TAG, new ConstructInterval());
      this.yamlConstructors.put(DIAGNOSIS_TAG, new ConstructDiagnosis());
    }
    
    private class ConstructInterval extends  AbstractConstruct {

      @Override
      public Object construct(Node node) {
        final String value = (String)constructScalar((ScalarNode)node);
        final int index = value.indexOf("-");
        final int from = Integer.parseInt(value.substring(0, index).trim());
        final int to = Integer.parseInt(value.substring(index + 1).trim());
        return new Interval(from, to);
      }
    }

    private class ConstructDiagnosis extends AbstractConstruct {

      @Override
      public Object construct(Node node) {
        final String value = (String) constructScalar((ScalarNode) node);
        final String[] parts = value.split("\\|");
        return new Diagnosis(parts[0].trim(), parts[1].trim());
      }
    }
  }
}
