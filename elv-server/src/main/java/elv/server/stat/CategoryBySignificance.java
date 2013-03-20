package elv.server.stat;

/**
 * Category by significance.
 */
public class CategoryBySignificance implements Category {
  public CategoryBySignificance() {
  }

  @Override
  public int categorize(double value, CategorizingArgument argument) {
    if(argument == null) {
      throw new NullPointerException("Null argument!");
    }
    int category;
    if(argument.significance == 1) { // Significant.
      category = (value > 1 ? 1 : 5); // High or low.
    } else { // Non significant.
      category = (value > 1.1 ? 2 : (value < 0.9 ? 4 : 3)); // High, low or medium.
    }
    return category;
  }
}
