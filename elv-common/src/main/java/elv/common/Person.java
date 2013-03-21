package elv.common;

import elv.common.io.Track;
import elv.common.io.Tracks;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Person.
 */
public abstract class Person implements Name, Serializable {
  private static final long serialVersionUID = 1L;

  public enum Attribute {
    NAME, DESCRIPTION, PASSWORD;
  }
  private Track track;

  public Person(Track track) {
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

  /**
   * Gets the relatives.
   * @param persons the list of existing persons.
   * @return a list of relatives.
   */
  public List<Person> getRelatives(List<Person> persons) {
    List<Person> relatives = new ArrayList<>();
    if(this instanceof Administrator) {
      relatives = persons;
    } else if(this instanceof Simple) {
      relatives.add(this);
    } else {
      for(Person person : persons) {
        if(((Group)this).groupName.equals(((Group)person).groupName)) {
          relatives.add(person);
        }
      }
    }
    return relatives;
  }

  /**
   * Finds the named person.
   */
  public static Person findPerson(List<Person> persons, String name) {
    for(Person iteratorPerson : persons) {
      if(iteratorPerson.getName().equals(name)) {
        return iteratorPerson;
      }
    }
    return null;
  }

  public static final class Administrator extends Person {
    public Administrator(Track track) {
      super(track);
    }
  }

  public static final class Simple extends Person {
    public Simple(Track track) {
      super(track);
    }
  }

  public static final class Group extends Person {
    public final String groupName;

    public Group(Track track, String groupName) {
      super(track);
      this.groupName = groupName;
    }
  }
}
