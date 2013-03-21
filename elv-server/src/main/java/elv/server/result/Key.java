package elv.server.result;

import elv.common.params.Interval;
import elv.common.params.Territory;
import elv.common.params.Gender;
import java.io.Serializable;
import java.util.Objects;

/**
 * Result keys.
 */
public class Key implements Serializable {
  public final Integer yearWindow;
  public final Interval yearInterval;
  public final Integer year;
  public final Integer month;
  public final Interval ageInterval;
  public final Gender gender;
  public final Territory territory;
  public final String string;
  private int hash;

  private Key(Integer yearWindow, Interval yearInterval, Integer year, Integer month, Interval ageInterval, Gender gender, Territory territory) {
    this.yearWindow = yearWindow;
    this.yearInterval = yearInterval;
    this.year = year;
    this.month = month;
    this.ageInterval = ageInterval;
    this.gender = gender;
    this.territory = territory;
    this.string = Result.CSV.join(yearWindow, yearInterval, year, ageInterval, gender, territory);
  }

  public String toHeader() {
    return string;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public int hashCode() {
    if(hash == 0 && string != null && string.length() > 0) {
      hash = string.hashCode();
    }
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
    final Key other = (Key)obj;
    if(!Objects.equals(this.string, other.string)) {
      return false;
    }
    return true;
  }

  public static class Builder {
    private Integer yearWindow = null;
    private Interval yearInterval = null;
    private Integer year = null;
    private Integer month = null;
    private Interval ageInterval = null;
    private Gender gender = null;
    private Territory territory = null;

    public Builder setYearWindow(Integer yearWindow) {
      this.yearWindow = yearWindow;
      return this;
    }

    public Builder setYearInterval(Interval yearInterval) {
      this.yearInterval = yearInterval;
      return this;
    }

    public Builder setYear(Integer year) {
      this.year = year;
      return this;
    }

    public Builder setMonth(Integer month) {
      this.month = month;
      return this;
    }

    public Builder setAgeInterval(Interval ageInterval) {
      this.ageInterval = ageInterval;
      return this;
    }

    public Builder setGender(Gender gender) {
      this.gender = gender;
      return this;
    }

    public Builder setRange(Territory territory) {
      this.territory = territory;
      return this;
    }

    public Key build() {
      return new Key(yearWindow, yearInterval, year, month, ageInterval, gender, territory);
    }
  }
}
