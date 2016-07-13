package clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
* faster than System.currentTimeMillis()
*/
public class SystemClock
{
   private static final SystemClock systemClock = new SystemClock(1);
   
   private final long precision;
   
   private final AtomicLong now;
   
   public SystemClock(long precision)
   {
       this.precision = precision;
       now = new AtomicLong(System.currentTimeMillis());
       scheduleClockUpdating();
   }
   
   public static SystemClock getDefault()
   {
       return systemClock;
   }
   
   private void scheduleClockUpdating()
   {
       ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory()
       {
           @Override
           public Thread newThread(Runnable runnable)
           {
               Thread thread = new Thread(runnable, "System Clock");
               thread.setDaemon(true);
               return thread;
           }
       });
       scheduler.scheduleAtFixedRate(new Runnable()
       {
           @Override
           public void run()
           {
               now.set(System.currentTimeMillis());
           }
       }, precision, precision, TimeUnit.MILLISECONDS);
   }
   
   public long now()
   {
       return now.get();
   }
   
   public long precision()
   {
       return precision;
   }
}