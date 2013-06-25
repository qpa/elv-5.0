package elv.common;

import elv.common.util.History;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Attributes with history.
 */
public class Attributes {
  protected final History history = new History();
  private final Map<String, Object> attributes = new ConcurrentHashMap<>();

  public Attributes(Map<String, Object> startAttributes) {
    if(startAttributes != null) {
      attributes.putAll(startAttributes);
    }
    for(Map.Entry<String, Object> attribute : attributes.entrySet()) {
      history.push(attribute.getKey() + " : " + attribute.getValue());
    }
  }

  public final Object get(String key) {
    return attributes.get(key);
  }

  public Iterator<String> getHistory() {
    return history.iterator();
  }

  public final Object put(String key, Object value) {
    return put(key, value, null);
  }

  public final Object put(String key, Object value, String message) {
    final Object oldValue = attributes.put(key, value);
    if(message == null || message.isEmpty()) {
      message = "";
    } else {
      message = " " + message;
    }
    if(value instanceof Date) {
      history.push(key + " : " + (oldValue == null ? "" : History.DATE_FORMAT.format(oldValue) + " -> ") + History.DATE_FORMAT.format(value) + message);
    } else {
      history.push(key + " : " + (oldValue == null ? "" : oldValue + " -> ") + value + message);
    }
    return oldValue;
  }
}
