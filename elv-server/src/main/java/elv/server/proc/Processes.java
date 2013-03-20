package elv.server.proc;

import elv.common.Analysis;
import elv.common.App;
import elv.common.Attribute;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Processes.
 */
public class Processes {
  private final ScheduledThreadPoolExecutor scheduler;
  public final Map<Analysis, ScheduledFuture> processes = new HashMap<>();
  
  public Processes(App app) {
    final int poolCount = Integer.parseInt(app.props.get(App.Prop.PROC_MAXCOUNT.key));
    scheduler = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(poolCount);
    scheduler.setRemoveOnCancelPolicy(true);
  }
  
  public void push(Analysis analysis, Attribute analysisAttribute) {
    Date scheduled = (Date)analysisAttribute.get(Analysis.Attribute.SCHEDULED_DATE.name());
    if(scheduled != null && processes.get(analysis) == null) {
      final long delay = scheduled.getTime() - new Date().getTime();
      final ScheduledFuture proc = scheduler.schedule(new Process(analysis, analysisAttribute), delay, TimeUnit.MILLISECONDS);
      processes.put(analysis, proc);
    }
  }

  public void pull(Analysis analysis) {
    final ScheduledFuture proc = processes.remove(analysis);
    if(proc != null) {
      proc.cancel(true);
    }
  }
}
