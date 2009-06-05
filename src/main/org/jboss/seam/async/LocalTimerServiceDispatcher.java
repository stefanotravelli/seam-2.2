package org.jboss.seam.async;

import java.util.concurrent.Callable;

import javax.ejb.Local;
import javax.ejb.Timer;

/**
 * Local interface for TimerServiceDispatcher.
 * 
 * @author Gavin King
 *
 */
@Local
public interface LocalTimerServiceDispatcher extends Dispatcher<Timer, TimerSchedule>
{   
   public Object call(Callable task);
}
