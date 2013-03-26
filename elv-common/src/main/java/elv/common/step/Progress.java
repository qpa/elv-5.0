package elv.common.step;

import java.io.Serializable;

/**
 * Execution progress.
 */
public class Progress implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public final String message;
  public final int min;
  private int value = 0;
  public final int max;
 
  public Progress(String message, int min, int max) {
    this.message = message;
    this.min = min;
    this.value = min;
    this.max = max;
  }

  public final int getValue() {
    return value;
  }

  public final void setValue(int value) {
    this.value = value;
  }

  public final void increment() {
    value++;
  }

  /**
   * Getter of the visibility.
   * @return true, if there are more than 1 objects to progress.
   */
  public boolean isVisible() {
    return (max > 1);
  }
}
