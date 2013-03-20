package elv.common.props;

import java.io.Serializable;
import java.util.Objects;

/**
 * Diagnosis.
 */
public class Diagnosis implements Serializable {
  public static final Diagnosis ROOT = new Diagnosis("0", "*");
  public final String code;
  public final String text;

  public Diagnosis(String code, String text) {
    this.code = code;
    this.text = text;
  }

  @Override
  public String toString() {
    return code + " " + text;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.code);
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
    final Diagnosis other = (Diagnosis)obj;
    if(!Objects.equals(this.code, other.code)) {
      return false;
    }
    return true;
  }

  public boolean isGroupingDisease() {
    return code.length() < 5 || code.contains("-");
  }
}
