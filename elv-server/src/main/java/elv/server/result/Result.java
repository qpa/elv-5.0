package elv.server.result;

import com.google.common.base.Joiner;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Result.
 */
public class Result implements Serializable {
  public static final Joiner CSV = Joiner.on(",").skipNulls();
  public static final TableJoiner TABLE = new TableJoiner();
  public final Key key;
  public final Value value;

  public Result(Key key, Value value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String toString() {
    return CSV.join(key, value);
  }

  public static class TableJoiner {
    private final Joiner joiner = Joiner.on("</td><td>").skipNulls();

    public final String join(Iterable<?> parts) {
      return join(parts.iterator());
    }

    public final String join(Iterator<?> parts) {
      return "</td>" + joiner.join(parts) + "</td>";
    }

    public final String join(Object[] parts) {
      return join(Arrays.asList(parts));
    }

    public final String join(Object first, Object second, Object... rest) {
      return "</td>" + joiner.join(first, second, rest) + "</td>";
    }
  }
}
