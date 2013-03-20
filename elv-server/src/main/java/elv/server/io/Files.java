package elv.server.io;

import elv.common.io.Track;
import elv.common.io.Tracks;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import net.java.truevfs.access.TPath;
import net.java.truevfs.access.TVFS;

/**
 * Server-side file methods.
 */
public class Files {
  private static final ReentrantLock lock = new ReentrantLock();

  /**
   * Creates the given target. The creation is made with numbering, if the target already exists.
   * @param targetTrack the target track.
   * @return the created target track (numbered, if needed).
   * @throws IOException
   */
  public static Track create(final Track targetTrack) throws IOException {
    lock.lock();
    try {
      final TPath target = numberPath(targetTrack);
      java.nio.file.Files.createDirectories(target);
      return Tracks.create(target);
    } finally {
      lock.unlock();
      TVFS.umount();
    }
  }

  /**
   * Copies the source to the target. The copy is made with numbering, if the target already exists.
   * @param sourceTrack the source track.
   * @param targetTrack the target track.
   * @return the target track (numbered, if needed).
   * @throws IOException
   */
  public static Track copy(final Track sourceTrack, final Track targetTrack) throws IOException {
    lock.lock();
    try {
      final TPath source = new TPath(sourceTrack.get());
      final TPath target = numberPath(targetTrack);
      java.nio.file.Files.walkFileTree(source, new TreeCopier(source, target));
      return Tracks.create(target);
    } finally {
      lock.unlock();
      TVFS.umount();
    }
  }

  /**
   * Deletes the given target.
   * @param targetTrack the target track.
   * @throws IOException
   */
  public static void delete(final Track targetTrack) throws IOException {
    lock.lock();
    try {
      final TPath target = new TPath(targetTrack.get());
      java.nio.file.Files.walkFileTree(target, new TreeDeleter());
    } finally {
      lock.unlock();
      TVFS.umount();
    }
  }

  /**
   * Moves the source to the target. The move is made with numbering, if the target already exists.
   * @param sourceTrack the source track.
   * @param targetTrack the target track.
   * @return the target track (numbered, if needed).
   * @throws IOException
   */
  public static Track move(final Track sourceTrack, final Track targetTrack) throws IOException {
    lock.lock();
    try {
      final Track movedTrack = copy(sourceTrack, targetTrack);
      delete(sourceTrack);
      return Tracks.create(movedTrack);
    } finally {
      lock.unlock();
      TVFS.umount();
    }
  }

  /**
   * Stores the object in the given target.
   * @param object the storable object.
   * @param targetTrack the target track.
   * @throws IOException
   */
  public static void store(final Object object, final Track targetTrack) throws IOException {
    lock.lock();
    try {
      final TPath target = new TPath(targetTrack.get());
      if(java.nio.file.Files.isDirectory(target)) {
        throw new IOException("<" + target + "> is DIRECTORY instead of FILE!");
      }
      try(Writer writer = java.nio.file.Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
        final Yaml yaml = new Yaml();
        yaml.dump(object, writer);
      }
      TVFS.umount();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Loads the object from the given source.
   * @param O the type of the object.
   * @param sourceTrack the source track.
   * @return the loaded object.
   * @throws IOException
   */
  public static <O> O load(final Class<O> type, final Track sourceTrack) throws IOException {
    lock.lock();
    try {
      final TPath source = new TPath(sourceTrack.get());
      if(java.nio.file.Files.isDirectory(source)) {
        throw new IOException("<" + source + "> is DIRECTORY instead of FILE!");
      }
      O object;
      try(Reader reader = java.nio.file.Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
        final Yaml yaml = new Yaml();
        object = yaml.loadAs(reader, type);
      }
      TVFS.umount();
      return object;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Loads the object from the given source.
   * @param O the type of the object.
   * @param sourceTrack the source track.
   * @return the loaded object.
   * @throws IOException
   */
  public static <O> O load(final Track sourceTrack) throws IOException {
    lock.lock();
    try {
      final TPath source = new TPath(sourceTrack.get());
      if(java.nio.file.Files.isDirectory(source)) {
        throw new IOException("<" + source + "> is DIRECTORY instead of FILE!");
      }
      O object;
      try(Reader reader = java.nio.file.Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
        final Yaml yaml = new Yaml();
        object = (O)yaml.load(reader);
      }
      TVFS.umount();
      return object;
    } finally {
      lock.unlock();
    }
  }
//
//  /**
//   * Loads a list of objects from given file located under the sub-directories of the given directory.
//   * @param O the type of the objects.
//   * @param dirPath the directory path.
//   * @param fileName the file name.
//   * @return the loaded objects.
//   * @throws IOException
//   */
//  public <O> List<O> load(final String dirPath, String fileName, Class<O> type) throws IOException {
//    lock.lock();
//    try {
//      TFile dir = new TFile(dirPath);
//      if(dir.isFile()) {
//        throw new IOException("<" + dir + "> is file instead of directory!");
//      }
//      List<O> objects = new ArrayList<>();
//      for(TFile subDir : dir.listFiles()) {
//        if(subDir.isDirectory()) {
//          try(Reader reader = new TFileReader(new TFile(subFolder.getPath() + "/" + fileName))) {
//            Yaml yaml = new Yaml();
//            objects.add(yaml.loadAs(reader, type));
//          }
//        }
//      }
//      TVFS.umount();
//      return objects;
//    } finally {
//      lock.unlock();
//    }
//  }

  public static TPath numberPath(Track track) throws IOException {
    TPath target = new TPath(track.get());
    final TPath parent = target.getParent();
    if(parent != null) {
      try(DirectoryStream<Path> ds = java.nio.file.Files.newDirectoryStream(parent)) {
        track = Tracks.numberEntry(track, new TrackIterator(ds.iterator()), "");
      }
    }
    return new TPath(track.get());
  }

  public static class TreeCopier extends SimpleFileVisitor<Path> {
    private final Path source;
    private final Path target;

    TreeCopier(final Path source, final Path target) {
      this.source = source;
      this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      java.nio.file.Files.createDirectory(target.resolve(source.relativize(dir)));
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      java.nio.file.Files.copy(file, target.resolve(source.relativize(file)));
      return FileVisitResult.CONTINUE;
    }
  }

  public static class TreeDeleter extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      java.nio.file.Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      if(exc == null) {
        java.nio.file.Files.delete(dir);
        return FileVisitResult.CONTINUE;
      } else {
        throw exc;
      }
    }
  }
  
  public static class TrackIterator implements Iterator<Track> {
    private final Iterator<Path> iterator;

    public TrackIterator(Iterator<Path> iterator) {
      this.iterator = iterator;
    }
    
    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Track next() {
      return Tracks.create(iterator.next().toString());
    }

    @Override
    public void remove() {
      iterator.remove();
    }
  }
}
