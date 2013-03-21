package elv.server.io;

import elv.common.params.Diagnosis;
import elv.common.params.DiagnosisNode;
import elv.common.params.Interval;
import elv.common.io.Params;
import elv.common.io.Track;
import elv.common.io.Tracks;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FilesTest {

  private Path testDir;

  public FilesTest() {
  }

  @BeforeClass
  public void setUpClass() throws Exception {
    try {
      testDir = Paths.get(System.getProperty("user.home"), ".elv-test");
      java.nio.file.Files.createDirectory(testDir);
    } catch(FileAlreadyExistsException ex) {
      LoggerFactory.getLogger(FilesTest.class).info("ELV-Suppression:: " + ex.getClass().getName() + ": " + ex.getMessage());
    } catch(IOException ex) {
      LoggerFactory.getLogger(FilesTest.class).info("ELV-Failure:: " + ex);
    }
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    Files.delete(Tracks.create(testDir));
  }

  @Test
  public void testCreate() throws Exception {
    // GIVEN
    final Track targetTrack = Tracks.create(testDir, "create");
    // WHEN
    final Track resultTrack = Files.create(targetTrack);
    // THEN
    assertEquals(resultTrack, targetTrack);
  }

  @Test
  public void testCreateSecondTimeShouldCreateNumbered() throws Exception {
    // GIVEN
    final Track targetTrack = Tracks.create(testDir, "create");
    // WHEN
    final Track resultTrack = Files.create(targetTrack);
    // THEN
    assertEquals(resultTrack.get(), targetTrack.get() + Tracks.NUM + "1");
  }

  @Test
  public void testCopy() throws Exception {
    // GIVEN
    final Track sourceTrack = Tracks.create("d:/Users/Sandor_Bencze/Apps/akka-2.1.0.zip/akka-2.1.0/bin");
    final Track targetTrack = Tracks.create(testDir, "copy");
    // WHEN
    final Track resultTrack = Files.copy(sourceTrack, targetTrack);
    // THEN
    assertEquals(resultTrack, targetTrack);
  }

  @Test
  public void testCopySecondTimeShouldCopyNumbered() throws Exception {
    // GIVEN
    final Track sourceTrack = Tracks.create("d:/Users/Sandor_Bencze/Apps/akka-2.1.0.zip/akka-2.1.0/bin");
    final Track targetTrack = Tracks.create(testDir, "copy");
    // WHEN
    final Track resultTrack = Files.copy(sourceTrack, targetTrack);
    // THEN
    assertEquals(resultTrack.get(), targetTrack.get() + Tracks.NUM + "1");
  }

  @Test
  public void testMove() throws Exception {
    // GIVEN
    final Track sourceTrack = Tracks.create(testDir, "copy");
    final Track targetTrack = Tracks.create(testDir, "create");
    // WHEN
    final Track resultTrack = Files.move(sourceTrack, targetTrack);
    // THEN
    assertEquals(resultTrack.get(), targetTrack.get() + Tracks.NUM + "2");
  }

  @Test
  public void testDelete() throws Exception {
    // GIVEN
    final Track targetTrack = Tracks.create(testDir, "copy" + Tracks.NUM + "1");
    // WHEN
    Files.delete(targetTrack);
    // THEN
    assertTrue(!java.nio.file.Files.exists(Paths.get(targetTrack.get())));
  }

  @Test(expectedExceptions = {IOException.class})
  public void testStoreWithDirTrackShouldThrowException() throws IOException {
    // GIVEN
    final Interval interval = new Interval(10, 26);
    final Track dirTrack = Tracks.create(testDir);
    // WHEN
    Files.store(interval, dirTrack);
    // THEN
    // Exception thrown
  }

  @Test
  public void testStoreWithFileTrackShouldCreateFile() throws IOException {
    // GIVEN
    final Object object = new Interval(10, 26);
    final Track sourceTrack = Tracks.create(testDir, "store" + Tracks.YML);
    // WHEN
    Files.store(object, sourceTrack);
    // THEN
    assertTrue(java.nio.file.Files.exists(Paths.get(sourceTrack.get())));
  }

  @Test(expectedExceptions = {IOException.class})
  public void testLoadWithDirPathShouldThrowException() throws IOException {
    // GIVEN
    final Track dirTrack = Tracks.create(testDir);
    // WHEN
    Files.load(Object.class, dirTrack);
    // THEN
    // Exception thrown
  }

  @Test
  public void testLoadWithFilePathShouldLoadObject() throws IOException {
    // GIVEN
    final Interval interval = new Interval(1011, 2666);
    final Track sourceTrack = Tracks.create(testDir, "load" + Tracks.YML);
    Files.store(interval, sourceTrack);
    // WHEN
    final Interval loadedInterval = Files.load(Interval.class, sourceTrack);
    // THEN
    assertEquals(loadedInterval, interval);
  }

  @Test
  public void testStoreDiagnoses() throws UnsupportedEncodingException, IOException {
    // GIVEN
    final DiagnosisNode diseaseRoot = Params.loadAllDiseaseDiagnoses();
    final List<Diagnosis> diagnoses = new LinkedList<>();
    diagnoses.add(((DiagnosisNode)diseaseRoot.getChildren().get(0).getChildren().get(0)).getDiagnosis());
    diagnoses.add(((DiagnosisNode)diseaseRoot.getChildren().get(0).getChildren().get(1)).getDiagnosis());
    diagnoses.add(((DiagnosisNode)diseaseRoot.getChildren().get(1)).getDiagnosis());
    diagnoses.add(((DiagnosisNode)diseaseRoot.getChildren().get(0).getChildren().get(9).getChildren().get(0).getChildren().get(2).getChildren().get(1)).getDiagnosis());
    final Track sourceTrack = Tracks.create(testDir, "diseases" + Tracks.YML);
    // WHEN
    Files.store(diagnoses, sourceTrack);
    // THEN
    final List<Diagnosis> loadedDiagnoses = Files.load(sourceTrack);
  }
}