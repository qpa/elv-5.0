package elv.common.params;

import java.io.Serializable;
import java.util.Objects;

/**
 * A territory.
 */
public class Territory implements Serializable {
  public static final Territory ROOT = new Territory("0", "*", 0, 0, 0);
  public final String code;
  public final String name;
  public final double area;
  public final double x;
  public final double y;

  public Territory(String code, String name, double area, double x, double y) {
    this.area = area;
    this.code = code;
    this.x = x;
    this.y = y;
    this.name = name;
  }

  public double getKm2Area() {
    return area / 1000000;
  }

  @Override
  public String toString() {
    return code + " " + name;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.code);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Territory other = (Territory) obj;
    if (!Objects.equals(this.code, other.code)) {
      return false;
    }
    return true;
  }
}
