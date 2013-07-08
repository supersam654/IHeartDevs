package com.sammarder.iheartdevs;

/**
 * Interface for "ticking" an object at a given time. This was designed to be passed into a TimerTask object. When the
 * task is run(), it should call IRemindable.remind(). It is highly effective at "ticking" objects across threads
 * (albeit with no information).
 */
public interface IRemindable {
	/**
	 * Alerts the implementer that it should perform a task now. Note that this is designed for simple cross-thread
	 * communication.
	 */
	public void remind();
}
