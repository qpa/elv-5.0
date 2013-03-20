package elv.server;

import elv.common.io.Track;
import elv.common.io.Tracks;
import elv.server.io.Files;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * HTTP handler for tracks.
 */
public class TrackHandler implements HttpHandler {
  protected static final String TRACK = "/elv/track/";

  protected static enum TrackPattern {
    create, copy, move, delete;
  }
  protected static final Logger LOG = LoggerFactory.getLogger(TrackHandler.class);
  private final ExecutorService trackExecutor = Executors.newSingleThreadExecutor();

  @Override
  public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
    String command = URI.create(request.uri()).getPath().toLowerCase().substring(TRACK.length());
    if(TrackPattern.create == TrackPattern.valueOf(command)) {
      new Create().doIt(request, response);
    } else if(TrackPattern.copy == TrackPattern.valueOf(command)) {
      new Copy().doIt(request, response);
    } else if(TrackPattern.move == TrackPattern.valueOf(command)) {
      new Move().doIt(request, response);
    } else if(TrackPattern.delete == TrackPattern.valueOf(command)) {
      new Delete().doIt(request, response);
    } else {
      control.nextHandler();
    }
  }

  private abstract class Operation {
    void doIt(final HttpRequest request, final HttpResponse response) {
      final Track source = Tracks.create(request.queryParam("source"));
      final Track target = Tracks.create(request.queryParam("target"));
      trackExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            final String message = runIt(source, target);
            response.content(message).status(200).end();
          } catch(Exception ex) {
            final String message = ex.getClass().getCanonicalName() + " " + ex.getMessage();
            response.content(message).status(400).end();
            LOG.error("ELV:: Track operation error: ", ex);
          }
        }
      });
    }

    protected abstract String runIt(Track source, Track target) throws IOException;
  }

  private class Create extends Operation {
    @Override
    public String runIt(Track source, Track target) throws IOException {
      Files.create(target);
      return "Created " + target;
    }
  }

  private class Copy extends Operation {
    @Override
    public String runIt(Track source, Track target) throws IOException {
      Files.copy(source, target);
      return "Copied " + source + " to " + target;
    }
  }

  private class Move extends Operation {
    @Override
    public String runIt(Track source, Track target) throws IOException {
      Files.move(source, target);
      return "Moved " + source + " to " + target;
    }
  }

  private class Delete extends Operation {
    @Override
    public String runIt(Track source, Track target) throws IOException {
      Files.delete(target);
      return "Deleted " + target;
    }
  }
}
