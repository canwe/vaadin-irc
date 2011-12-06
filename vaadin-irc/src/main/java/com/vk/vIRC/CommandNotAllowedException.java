package com.vk.vIRC;

public class CommandNotAllowedException extends Exception {

	public CommandNotAllowedException(String command){
		super(command + " is not allowed in the strict irc client version!");
	}
	
}
