package elv.common.params;

/**
 * Gender.
 */
public enum Gender {
  ALL("0"), MALES("1"), FEMALES("2");
  
  public final String code;
  
  private Gender(String code) {
    this.code = code;
  }
}
