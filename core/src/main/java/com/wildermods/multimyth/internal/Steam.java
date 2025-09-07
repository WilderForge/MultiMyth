package com.wildermods.multimyth.internal;

public interface Steam {
	
	/**
	 * @return the currently logged in user's username, "Anonymous" if the user is logged in anonymously, or null if the user is not logged in.
	 */
	public String username();
	
}
