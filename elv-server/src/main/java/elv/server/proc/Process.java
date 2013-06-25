package elv.server.proc;

import elv.server.step.Step;
import com.google.common.collect.ImmutableList;
import elv.common.Analysis;
import elv.common.Attributes;
import elv.common.io.Tracks;
import elv.common.params.Param;
import elv.common.step.Progress;
import elv.server.Config;
import elv.server.io.Files;
import elv.server.result.Key;
import elv.server.result.Value;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import javax.sql.DataSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process that runs an analysis.
 */
public final class Process implements Runnable {
  public static final Logger LOG = LoggerFactory.getLogger(Process.class);
  public static final DataSource DATA_DB = Config.dataBase(null);//Config.app());
  public static final ForkJoinPool EXECUTOR = new ForkJoinPool();
  private Analysis analysis;
  private Attributes analysisAttribute;
  private DB resultDb;
  private Map<Param, Object> params;
  private List<Step> steps;
  private Progress progress;

  public Process(Analysis analysis, Attributes analysisAttribute) {
    this.analysis = analysis;
    this.analysisAttribute = analysisAttribute;
  }

  public Analysis getAnalysis() {
    return analysis;
  }

  public Attributes getAnalysisAttribute() {
    return analysisAttribute;
  }

  public Map<Param, Object> getParams() {
    if(params == null) {
      params = getResultDb().getHashMap("params");
    }
    return params;
  }

  public List<Step> getSteps() {
    return steps;
  }

  public DB getResultDb() {
    if(resultDb == null) {
      resultDb = DBMaker.newFileDB(new File(analysis.getProcTrack().get())).closeOnJvmShutdown().compressionEnable().make();
    }
    return resultDb;
  }

  public void createProgress(String name, int maxProgressValue) {
    progress = new Progress(name, 0, maxProgressValue);
  }

  public void incrementProgress() {
    progress.increment();
  }

  @Override
  public void run() {
    try {
      LOG.info("ELV:: Processing started for " + analysis);
      getResultDb();
      analysisAttribute.put(Analysis.Attribute.STATE.name(), Analysis.State.STARTED);
      Files.store(analysisAttribute, analysis.getAttributeTrack());

      getParams();

      steps = loadSteps();
      for(Step step : steps) {
        step.compute(this);
        if(Thread.interrupted()) {
          throw new InterruptedException("Stopped " + analysis);
        }
      }
    } catch(InterruptedException ex) {
      getResultDb().commit();
      analysisAttribute.put(Analysis.Attribute.STATE.name(), Analysis.State.STOPPED);
      LOG.error("ELV:: Processing stopped for " + analysis, ex);
    } catch(Exception ex) {
      analysisAttribute.put(Analysis.Attribute.STATE.name(), Analysis.State.FAILED, ex.getClass().getName() + " " + ex.getMessage());
      LOG.error("ELV:: Processing failed for " + analysis, ex);
    } finally {
      if(analysisAttribute.get(Analysis.Attribute.STATE.name()) == Analysis.State.STARTED) {
        analysisAttribute.put(Analysis.Attribute.STATE.name(), Analysis.State.FINISHED);
        LOG.info("ELV:: Processing finished for " + analysis);
      }
      try {
        Files.store(analysis, analysis.getAttributeTrack());
        getResultDb().close();
      } catch(IOException ex) {
        LOG.error("ELV:: Store failed for " + analysis, ex);
      }
    }
  }

  private List<Step> loadSteps() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    Step[] stepArray = new Step[analysis.getSteps().size()];
    for(int idx = 0; idx < stepArray.length; idx++) {
      Class<Step> stepClass = (Class<Step>)Class.forName(analysis.getSteps().get(idx));
      stepArray[idx] = Files.load(stepClass, Tracks.create(analysis.getParamTrack(), analysis.getSteps().get(idx)));
    }
    return ImmutableList.copyOf(stepArray);
  }
}
