package elv.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

/**
 * Web socket handler.
 */
public class WebSocketHandler extends BaseWebSocketHandler {
  protected static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);
  private List<WebSocketConnection> connections = new ArrayList<>();

  @Override
  public void onOpen(WebSocketConnection connection) throws Exception {
    connections.add(connection);
    LOG.info("ELV:: WS connection opened: " + connection);
  }

  @Override
  public void onClose(WebSocketConnection connection) throws Exception {
    connections.remove(connection);
    LOG.info("ELV:: WS connection closed: " + connection);
  }

  @Override
  public void onMessage(WebSocketConnection connection, String message) throws Throwable {
    LOG.info("ELV:: WS message intercepted: " + message);
  }

  public List<WebSocketConnection> getConnections() {
    return Collections.unmodifiableList(connections);
  }

  public void broadcast(final String json) {
    for(WebSocketConnection connection : connections) {
      connection.send(json);
      LOG.info("ELV:: WS event broadcasted: " + json);
    }
  }
}
