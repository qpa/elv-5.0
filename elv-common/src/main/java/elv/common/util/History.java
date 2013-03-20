package elv.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;

/**
 * History.
 */
public class History {
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

  private final Deque<String> queue = new ArrayDeque<>();

  public History() {
  }
  
  public void push(String msg) {
    queue.push("ELV:: " + DATE_FORMAT.format(new Date()) + " " + msg);
  }
  
  public Iterator<String> iterator() {
    return Collections.unmodifiableCollection(queue).iterator();
  }
}
