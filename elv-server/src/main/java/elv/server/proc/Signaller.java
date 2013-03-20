package elv.server.proc;

import elv.common.Analysis;
import elv.common.App;
import elv.common.Attribute;
import elv.common.io.Tracks;
import elv.server.WebSocketHandler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signaler.
 */
public class Signaller {
  protected static final Logger LOG = LoggerFactory.getLogger(Signaller.class);
  private final Processes processes;
  private final WebSocketHandler wsHandler;
  private final Map<WatchKey, Path> watchKeys = new HashMap<>();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final WatchService watchService;

  public Signaller(App app, Processes processes, WebSocketHandler wsHandler) throws IOException {
    this.processes = processes;
    this.wsHandler = wsHandler;
    watchService = FileSystems.getDefault().newWatchService();
    final String procDir = app.props.get(App.Prop.PROC_DIR.key);
    register(Paths.get(procDir));
    executor.execute(new Watcher());
  }

  private void register(Path path) throws IOException {
    // Register directory and sub-directories at start-up.
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        watchKeys.put(key, path);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // Start processing for already scheduled analysises at start-up.
        scheduleAnalysis(file);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void signal(String eventName, Path path) {
    // Signal event
    final String json = "{event: " + eventName + ",track: " + path + "}";
    wsHandler.broadcast(json);
    // Start processing for scheduled analysis.
    scheduleAnalysis(path);
  }

  private void scheduleAnalysis(Path path) {
    try {
      if(Files.isRegularFile(path, NOFOLLOW_LINKS) && Tracks.file(Analysis.class).equals(path.getFileName().toString())) {
        final Attribute analysisAttribute = elv.server.io.Files.load(Attribute.class, Tracks.create(path));
        if(analysisAttribute.get(Analysis.Attribute.SCHEDULED_DATE.name()) != null) {
          final String analysisClassName = (String)analysisAttribute.get(Analysis.Attribute.TYPE.name());
          final Analysis analysis = (Analysis)Class.forName(analysisClassName).getConstructor(String.class).newInstance(Tracks.create(path.getParent()));
          processes.push(analysis, analysisAttribute);
        }
      }
    } catch(IOException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      LOG.error("ELV:: ", ex);
    }
  }

  private class Watcher implements Runnable {
    @Override
    public void run() {
      try {
        for(;;) {
          final WatchKey key = watchService.take();
          final Path dir = watchKeys.get(key);
          if(dir == null) {
            LOG.info("ELV:: WatchKey not recognized!");
            continue;
          }
          for(WatchEvent<?> event : key.pollEvents()) {
            final WatchEvent.Kind kind = event.kind();
            if(kind == OVERFLOW) {
              continue;
            }
            Path noticedName = ((WatchEvent<Path>)event).context();
            // Full path
            Path noticedPath = dir.resolve(noticedName);
            // Signal noticed event
              signal(kind.name(), noticedPath);
            // Register creted directory and its sub-directories
            if(kind == ENTRY_CREATE) {
              try {
                if(Files.isDirectory(noticedPath, NOFOLLOW_LINKS) && !noticedName.toString().contains(Analysis.Dir.proc.name())) {
                  register(noticedPath);
                }
              } catch(IOException ex) {
                LOG.error("ELV:: ", ex);
              }
            }
          }

          // Reset key and remove from set if directory no longer accessible
          boolean valid = key.reset();
          if(!valid) {
            watchKeys.remove(key);
            // All directories are inaccessible
            if(watchKeys.isEmpty()) {
              break;
            }
          }
          if(Thread.interrupted()) {
            throw new InterruptedException("Fatal! Watcher interrupted!");
          }
        }
      } catch(InterruptedException ex) {
        LOG.error("ELV:: ", ex);
      }
    }
  }
}
