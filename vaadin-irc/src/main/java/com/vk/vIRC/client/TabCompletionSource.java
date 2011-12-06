package com.vk.vIRC.client;

public interface TabCompletionSource {

	/**
	 * This method will return an completed string of the argument fed. If there
	 * is no such string, the original argument will be returned. If there are several
	 * choices, the strings will be returned in a circular order (as long as the argument is
	 * the same). If the argument is changed, the cycle will start over. 
	 * 
	 */
	public String getNextTabbable(String incompleteText);
}
