package com.fonzp.service;

import java.util.Map;

public abstract class Task extends Thread
{
	protected Map<String, String[]> parameterMap;
	
	public final void setParameterMap(final Map<String, String[]> parameterMap)
	{
		this.parameterMap = parameterMap;
	}
	
	@Override
	public final void run()
	{
		onStart();
		doInBackground();
		onFinish();
	}
	
	/**
	 * This method is called before doing the work in background,
	 * but it's still called asynchronously. Here you can do initialization
	 * routines like cold starts, resource lookups, connection openings, etc...
	 */
	protected abstract void onStart();
	
	/**
	 * This method must be the implementation for the behaviour
	 * that must be execute asynchronously.
	 */
	protected abstract void doInBackground();
	
	/**
	 * This method is called when the task is completed. Implementation
	 * must be defined in the subclass which inherit this class. Here you
	 * can do resource disposal, like connection destroy, shutdown, etc...
	 */
	protected abstract void onFinish();
	
	/**
	 * Get the result of this task after its completion. Implementation may
	 * differ between subclasses.
	 *
	 * @return any implementation can differ, so a generic Object is returned.
	 */
	public abstract Object getResult();
}