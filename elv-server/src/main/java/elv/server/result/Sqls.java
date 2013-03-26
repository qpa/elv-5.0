package elv.server.result;

import com.google.common.base.Joiner;
import elv.common.params.Diagnosis;
import elv.common.params.Gender;
import elv.common.params.Interval;
import elv.common.params.Resolution;
import elv.common.params.Territory;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL utility methods.
 */
public class Sqls {
  public static final Joiner AND = Joiner.on(" AND ").skipNulls();
  public static final Joiner OR = Joiner.on(" OR ").skipNulls();
  private static int indexOfH = 3;

  private Sqls() {
  }

  public static String createClause(Gender gender) {
    if(gender == null || gender == Gender.ALL) {
      return null;
    }
    return "gender = " + gender.code;
  }

  public static String createClause(Interval ageInterval) {
    if(ageInterval == null || ageInterval == Interval.AGE) {
      return null;
    }
    return "age BETWEEN " + ageInterval.from + " AND " + ageInterval.to;
  }

  public static String createClause(Territory territory) {
    if(territory == null || territory == Territory.ROOT) {
      return null;
    }
    return "settlement = " + territory.code;
  }

  public static String createClause(Resolution resolution, Interval yearInterval, Integer year, Integer month) {
    if(resolution == Resolution.YEAR_INTERVALY) {
      return "year BETWEEN " + yearInterval.from + " AND " + yearInterval.to;
    } else {
      return AND.join("year = " + year, resolution == Resolution.MONTHLY ? "month = " + month : null);
    }
  }

  public static String createClause(Integer year) {
    assert year != null;
    return "year = " + year;
  }

  public static String createClause(List<Diagnosis> diseaseDiagnoses, List<Diagnosis> mortalityDiagnoses) {
    if(diseaseDiagnoses == null || diseaseDiagnoses.isEmpty() || (diseaseDiagnoses.contains(Diagnosis.ROOT))) {
      return null;
    }
    if(mortalityDiagnoses == null || mortalityDiagnoses.isEmpty() || (mortalityDiagnoses.contains(Diagnosis.ROOT))) {
      mortalityDiagnoses = new ArrayList<>();
      // Defaults to the first disease, because there are a lot of years with just this completed.
      mortalityDiagnoses.add(new Diagnosis("1", ""));
    }
    String clause = null;
    for(Diagnosis disease : diseaseDiagnoses) {
      if(disease.isGroupingDisease() || isHDisease(disease)) {
        String fromCode;
        String toCode;
        if(disease.isGroupingDisease()) {
          String[] codes = disease.code.split(java.util.regex.Pattern.quote("-"));
          fromCode = codes[0];
          toCode = (codes.length == 1 ? codes[0] : codes[1]);
        } else { // isHDisease()
          fromCode = disease.code.substring(0, indexOfH);
          toCode = fromCode;
        }
        for(Diagnosis mortality : mortalityDiagnoses) {
          clause = OR.join(clause, "SUBSTRING(diagnosis_" + mortality.code + " FROM 1 FOR " + fromCode.length() + ") BETWEEN '" + fromCode + "' AND '" + toCode + "'");
        }
      } else {
        for(Diagnosis mortality : mortalityDiagnoses) {
          clause = OR.join(clause, "'" + disease.code + "' LIKE diagnosis_" + mortality.code);
        }
      }
    }
    return "(" + clause + ")";
  }

  /** Codes like A09H0 are not completed properly. */
  private static boolean isHDisease(Diagnosis disease) {
    return disease.code.charAt(indexOfH) == 'H';
  }
}
