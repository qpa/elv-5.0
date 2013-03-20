package elv.common.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Track methods.
 */
public class Tracks {
  public static final String SLASH = "/";
  public static final String NUM = "#";
  public static final String YML = ".yml";

  /**
   * Creates a new Track, with the given components.
   * @param first
   * @param more
   * @return
   */
  public static Track create(String first, String... more) {
    return new BaseTrack(Paths.get(first, more).toString());
  }

  /**
   * Creates a new Track, with the given components.
   * @param first
   * @param more
   * @return
   */
  public static Track create(Track first, String... more) {
    return create(first.get(), more);
  }

  /**
   * Creates a new Track, with the given components.
   * @param first
   * @param more
   * @return
   */
  public static Track create(Path first, String... more) {
    return create(first.toString(), more);
  }

  /**
   * Renames the given track. The extension of the track is kept.
   * @param name the new base name to set.
   * @return a new track
   */
  public static Track rename(Track track, String name) {
    return create(parent(track), name + extension(track));
  }

  /**
   * Returns the base part of a track (the name without extension).
   * @param track the track.
   * @return the base name.
   */
  public static String base(Track track) {
    String baseName = "";
    if(track != null) {
      // Get the last track component.
      String trackString = Paths.get(track.get()).getFileName().toString();
      // Get base name.
      int i = trackString.lastIndexOf('.');
      if(i > 0 && i < trackString.length() - 1) {
        baseName = trackString.substring(0, i);
      } else {
        baseName = trackString;
      }
    }
    return baseName;
  }

  /**
   * Returns the extension part of a track with the DOT (.).
   * @param track the file track.
   * @return the extension.
   */
  public static String extension(Track track) {
    String extension = "";
    if(track != null) {
      // Get the last track component.
      String trackString = Paths.get(track.get()).getFileName().toString();
      int i = trackString.lastIndexOf('.');
      if(i > 0 && i < trackString.length() - 1) {
        extension = trackString.substring(i).toLowerCase();
      }
    }
    return extension;
  }

  /**
   * Returns the parent of a track.
   * @param track the track.
   * @return the parent track.
   */
  public static Track parent(Track track) {
    String parent = "";
    if(track != null) {
      // Get the last track component.
      parent = Paths.get(track.get()).getParent().toString();
      parent = parent == null ? "" : parent;
    }
    return create(parent);
  }

  /**
   * Returns the file name for a persisted class.
   * @param clazz the persisted class.
   * @return the file name.
   */
  public static String file(Class clazz) {
    return clazz.getSimpleName() + YML;
  }

  /**
   * Returns the numbered entry name for the given entry between the array of entries.
   * @param <T> the type of the entry.
   * @param entry the entry.
   * @param entries the already existing entries.
   * @param postfix the postfix string of the entry, the numbering has to be done before the postfix.
   * @return the numbered (if needed) entry name.
   */
  public static Track numberEntry(final Track entry, final Iterator<Track> entries, String postfix) {
    if(entry == null || entries == null || !entries.hasNext()) {
      return entry;
    }
    if(postfix == null) {
      postfix = "";
    }
    Track parent = parent(entry);
    String extension = extension(entry);
    String pureBase = base(entry).replace(postfix, "");

    Set<Integer> relatives = new TreeSet<>();

    boolean found = false;
    while(entries.hasNext()) {
      Track iEntry = entries.next();
      String pureIBase = base(iEntry).replace(postfix, "");
      int number = 0;
      String unnumberedPureIBase = pureIBase;
      int nIndex = pureIBase.lastIndexOf(NUM);
      if(nIndex > 0) {
        number = Integer.parseInt(unnumberedPureIBase.substring(nIndex + 1));
        unnumberedPureIBase = unnumberedPureIBase.substring(0, nIndex);
      }
      if(pureBase.equals(pureIBase)) {
        found = true;
      } else if(pureBase.equals(unnumberedPureIBase)) {
        relatives.add(number);
      }
    }

    String numberedBase = pureBase;
    if(found) {
      for(int i = 0;; i++) {
        found = false;
        for(int number : relatives) {
          if(i + 1 == number) {
            found = true;
            break;
          }
        }
        if(!found) {
          numberedBase = pureBase + NUM + Integer.toString(i + 1);
          break;
        }
      }
    }
    return create(parent, numberedBase + postfix + extension);
  }

  public static class BaseTrack implements Track {
    private String track;

    public BaseTrack(String track) {
      if(track == null) {
        throw new IllegalArgumentException("Null track!");
      }
      if(track.isEmpty()) {
        throw new IllegalArgumentException("Empty track!");
      }
      // Normalize track string for operating system interoparability.
      this.track = track.replaceAll("\\\\", SLASH);
    }

    @Override
    public String get() {
      return track;
    }

    @Override
    public String toString() {
      return track;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(track);
    }

    @Override
    public boolean equals(Object obj) {
      if(obj == null) {
        return false;
      }
      if(getClass() != obj.getClass()) {
        return false;
      }
      final BaseTrack other = (BaseTrack)obj;
      if(!Objects.equals(this.track, other.track)) {
        return false;
      }
      return true;
    }
  }
}
