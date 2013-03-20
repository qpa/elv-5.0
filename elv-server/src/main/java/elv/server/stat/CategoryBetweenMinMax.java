package elv.server.stat;

/**
 * Category between the minimum and maximum values.
 */
public final class CategoryBetweenMinMax implements Category {
  public static final int DEFAULT_COUNT = 5;
  public final int count;

  public CategoryBetweenMinMax() {
    this(DEFAULT_COUNT);
  }

  public CategoryBetweenMinMax(int count) {
    this.count = count;
  }

  @Override
  public int categorize(double value, CategorizingArgument argument) {
    if(argument == null) {
      throw new NullPointerException("Null argument!");
    }
    double interval = (argument.maxValue - argument.minValue) / count;
    double level = argument.minValue + interval;
    int category;
    for(category = 1; category <= count; category++) {
      if(level >= value) {
        break;
      } else {
        level = level + interval;
      }
    }
    return category;
  }
}
