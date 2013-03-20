package elv.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Application properties.
 */
public final class App implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public static enum Prop {
    VERSION("app.version", "5.0"),
    PROC_DIR("app.proc.dir", System.getProperty("user.home") + "/.elv-" + VERSION.def),
    PROC_MAXCOUNT("app.proc.maxcount", "5"),
    DB_DRIVER("app.db.driver", "org.h2.Driver"), DB_URL("app.db.url", "jdbc:h2:" + PROC_DIR.def + "/db/elv"),
    DB_USER("app.db.user", "elv"), DB_PASSWORD("app.db.password", "elv");
    
    public final String key;
    public final String def;
    
    private Prop(String key, String def) {
      this.key = key;
      this.def = def;
    }
    
    @Override
    public String toString() {
      return key;
    }
  }
  
  public final Map<String, String> props;
  
  public App(final Map props) {
    this.props = Collections.unmodifiableMap(props);
  }
}
