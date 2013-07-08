package com.sammarder.iheartdevs.traceprocessing;

import java.util.Timer;
import java.util.TimerTask;

import com.sammarder.iheartdevs.IRemindable;

// TODO: Migrate this over to Bukkit Task scheduler thingy

/**
 * Wrapper for a TimerTask that forces only one instance of a timer (per instance of this class). If a request to start
 * the timer is made while the timer is going, this class will prevent a new TimerTask from starting. When the current
 * TimerTask completes, instead of notifying the caller, a new one will start. When a TimerTask completes without
 * interruption, the caller will be notified via IRemindable.remind().
 */
// Note: This class implements IRemindable as an easy way for the TimerTask to perform a callback.
public class SingleCompletionTimer implements IRemindable {
	// The IRemindable object to remind() when an uninterrupted TimerTask finishes.
	private final IRemindable remindable;
	// isActive is true when the TimerTask is running. isValid is true if the TimerTask is uninterrupted.
	private boolean isActive, isValid;
	// Represents how long to wait (in ticks [20 ticks per second]) before run()ing the task.
	private long delay;

	private Timer timer;

	/**
	 * Constructor for creating a new SingleCompletionTimer.
	 * 
	 * @param timer
	 *            The timer to run TimerTasks off of.
	 * @param remindable
	 *            The object to remind when a TimerTask completes uninterrupted.
	 * @param delay
	 *            How long (in milliseconds) to wait before running the TimerTask.
	 */
	public SingleCompletionTimer(Timer timer, final IRemindable remindable, long delay) {
		this.remindable = remindable;
		this.timer = timer;
		this.delay = delay;
	}

	/**
	 * Either sets up a new task or invalidates the current one (will cause a new one to be created later).
	 */
	public void setReminder() {
		if (!isActive) {
			timer.schedule(new ReminderTask(this), delay);
			isActive = true;
			isValid = true;
		} else {
			// Attempted to start timer while active, so invalidate it.
			isValid = false;
		}
	}

	@Override
	public void remind() {
		// Timer was not invalidated during cycle, so remind() the caller.
		if (isValid) {
			remindable.remind();
			isActive = false;
		} else {
			// Timer was invalidated during cycle, so revalidate it and try again.
			isActive = false;
			isValid = true;
			setReminder();
		}
	}

	/**
	 * Internal TimerTask that is only accessible through the wrapper SingleCompletionTimer class.
	 */
	class ReminderTask extends TimerTask {
		// The object to notify when this TimerTask is run.
		private final IRemindable remindable;

		/**
		 * Constructor for creating a new ReminderTask.
		 * 
		 * @param remindable
		 *            The object to remind when the TimerTask goes off.
		 */
		public ReminderTask(final IRemindable remindable) {
			this.remindable = remindable;
		}

		@Override
		public void run() {
			// When called, alerts the IRemindable that it has gone off.
			remindable.remind();
		}
	}

}
