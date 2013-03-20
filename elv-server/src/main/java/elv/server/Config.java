package elv.server;

import elv.common.App;
import elv.common.props.DiagnosisNode;
import elv.common.io.Props;
import elv.server.proc.Processes;
import elv.server.proc.Signaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;

/**
 * Configurations for the application.
 */
public class Config {
  protected static final Logger LOG = LoggerFactory.getLogger(Config.class);
  public App app;

  public static App app() {
    Properties properties = new Properties();
    try(InputStream inputStream = Config.class.getResourceAsStream("app.properties")) {
      properties.load(inputStream);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    }
    final Map<String, String> props = new HashMap<>();
    for(App.Prop prop : App.Prop.values()) {
      props.put(prop.key, properties.getProperty(prop.key, prop.def));
    }
    return new App(props);
  }

  public static DataSource dataBase(App app) {
    final String url = "jdbc:h2:~/.elv/5.0/db"; //app.props.get(App.Prop.DB_URL.key);
    final String user = "elv"; //app.props.get(App.Prop.DB_USER.key);
    final String password = "elv"; //app.props.get(App.Prop.DB_PASSWORD.key);
    return JdbcConnectionPool.create(url, user, password);
  }

  public WebSocketHandler webSocketHandler() {
    return new WebSocketHandler();
  }

  public WebServer webServer() throws InterruptedException, ExecutionException {
    return WebServers.createWebServer(80).add("/elv/ws", webSocketHandler()).add(new TrackHandler()).add(new StaticFileHandler("/elv")).start().get();
  }

  public Processes processes() {
    return new Processes(app);
  }

  public Signaller signaller() throws IOException {
    return new Signaller(app, processes(), webSocketHandler());
  }

  public DiagnosisNode allDiseaseDiagnoses() throws IOException {
    return Props.loadAllDiseaseDiagnoses();
  }

  public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
    WebSocketHandler webSocketHandler = new WebSocketHandler();
    WebServers.createWebServer(80).add("/elv/ws", webSocketHandler).add(new TrackHandler()).add(new StaticFileHandler("/elv")).start().get();
    App app = app();
    new Signaller(app, new Processes(app), webSocketHandler);
  }
}
