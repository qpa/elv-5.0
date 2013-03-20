package elv.common.props;

import java.io.Serializable;

/**
 * Interval of integers.
 */
public class Interval implements Serializable {
  public static final Interval AGE = new Interval(0, 100);
  
  public final int from ;
  public final int to;
  
  public Interval(int from, int to) {
    if(from > to) {
      throw new IllegalArgumentException("FROM value bigger than TO value: " + from + " >" + to);
    }
    this.from = from;
    this.to = to;
  }
    
  /**
   * Tests the inclusion.
   * @param value the test value.
   * @return true, if this interval contains the given value.
   */
  public boolean contains(int value) {
    return (from <= value && value <= to);
  }

  @Override
  public String toString() {
    return from + "-" + to;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + this.from;
    hash = 23 * hash + this.to;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }
    final Interval other = (Interval) obj;
    if(this.from != other.from) {
      return false;
    }
    if(this.to != other.to) {
      return false;
    }
    return true;
  }
  
  /**
   * Morbidity year-interval.
   */
  public static class Morbidity extends Interval {
    public Morbidity(int from, int to) {
      super(from, to);
    }
  }
  
  /**
   * Mortality year-interval.
   */
  public static class Mortality extends Interval {
    public Mortality(int from, int to) {
      super(from, to);
    }
  }
  
  /**
   * Population year-interval.
   */
  public static class Population extends Interval {
    public Population(int from, int to) {
      super(from, to);
    }
  }
}
