package elv.common;

import elv.common.io.Track;
import elv.common.io.Tracks;

/**
 * Catalog.
 */
public abstract class Catalog implements Name {
  private static final long serialVersionUID = 1L;
  public static String ARCHIVE_EXTENSION = ".zip";

  public enum Attribute {
    NAME, DESCRIPTION;
  }
  private Track track;

  private Catalog(Track track) {
    this.track = track;
  }

  @Override
  public final String getName() {
    return Tracks.base(track);
  }

  public final Track getTrack() {
    return track;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public final void setName(String name) {
    track = Tracks.rename(track, name);
  }

  public static final class Folder extends Catalog {
    public Folder(Track track) {
      super(track);
    }
  }

  public static final class Archive extends Catalog {
    public Archive(Track track) {
      super(track);
    }
  }
}
