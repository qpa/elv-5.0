package elv.common.step;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

/**
 * Stack of progresses.
 */
public class Progresses implements Serializable {
  private static final long serialVersionUID = 1L;

  private String message;
  private final Deque<Progress> progresses = new ArrayDeque<>();
  
  public Progresses(String message) {
    this.message = message;
  }

  /**
   * Getter of the message.
   * @return the message.
   */
  public final String getMessage() {
    return message;
  }

  /**
   * Setter of the message.
   * @param message the message to set.
   */
  public final void setMessage(String message) {
    this.message = message;
  }

  /**
   * Getter of the progresses.
   * @return the progresses.
   */
  public final Collection<Progress> getProgresses() {
    return Collections.unmodifiableCollection(progresses);
  }
  
  /**
   * Pusher of a new progress.
   * @param progress
   */
  public void push(Progress progress) {
    progresses.push(progress);
  }
  
  /**
   * Popper of the last progress.
   * @return the last progress from the stack.
   */
  public Progress pop() {
    return progresses.pop();
  }
  
  /**
   * Setter of the value of peek progress.
   * @param value the value to set.
   */
  public void setPeekValue(int value) {
    progresses.peek().setValue(value);
  }
  
  /**
   * Returns the aggregate progress.
   * @return a single progress resulting from the aggregation of the stacked progresses.
   */
  public Progress getAggregateProgress() {
    int value = 0;
    int max = 0;
    for(Progress iteratorProgress : progresses) {
      value += iteratorProgress.getValue() - iteratorProgress.min;
      max += iteratorProgress.max - iteratorProgress.min;
    }
    Progress aggregateProgress = new Progress("Aggregation", value, max);
    return aggregateProgress;
  }
}
