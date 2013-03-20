package elv.common.io;

/**
 * An immutable file system track (path).
 */
public interface Track {
  String get();
  
  @Override
  String toString();
}
