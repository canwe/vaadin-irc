package com.vk.vIRC.client;

import com.vk.vIRC.CommandNotAllowedException;

public interface UserEventListener {

	/**
	 * The user will invoke sendText method every time something has been written
	 */
// ------------------------------

	/**
	 * Removes away message.
	 */
	public void doAway();

// ------------------------------

	/**
	 * Sets away message.
	 * @param msg The away message.
	 */
	public void doAway(String msg);

// ------------------------------

	/**
	 * Invites a user to a channel. Note that the target channel must be known
	 * @param nick The nickname of the user who should be invited.
	 */
	public void doInvite(String nick);

// ------------------------------

	/**
	 * Invites a user to a channel.
	 * @param nick The nickname of the user who should be invited.
	 * @param chan The channel the user should be invited to.
	 */
	public void doInvite(String nick, String chan);

// ------------------------------

	/**
	 * Checks if one or more nicks are used on the server.
	 * @param nick The nickname of the user we search for.
	 */
	public void doIson(String nick);

// ------------------------------

	/**
	 * Joins a channel without a key.
	 * @param chan The channel which is to join.
	 */
	public void doJoin(String chan) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Joins a channel with a key.
	 * @param chan The channel which is to join.
	 * @param key The key of the channel.
	 */
	public void doJoin(String chan, String key)  throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Kicks a user from a channel. Note that target name must be found out somehow
	 * @param nick The nickname of the user who should be kicked.
	 */
	public void doKick(String nick) throws CommandNotAllowedException;


// ------------------------------

	/**
	 * Kicks a user from a channel. Note that target name must be found out somehow
	 * @param nick The nickname of the user who should be kicked.
	 * @param msg The optional kickmessage.
	 */
	public void doKick(String nick, String msg) throws CommandNotAllowedException;
// ------------------------------

	/**
	 * Kicks a user from a channel with a comment.
	 * @param chan The channel somebody should be kicked from.
	 * @param nick The nickname of the user who should be kicked.
	 * @param msg The optional kickmessage.
	 */
	public void doKick(String chan, String nick, String msg) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Lists all channels with their topic and status.
	 */
	public void doList() throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Lists channel(s) with their topic and status.
	 * @param chan The channel the <code>LIST</code> refers to.
	 */
	public void doList(String chan) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Lists all visible users in the current channel (it will not show all users at irc, that is overridden).
	 */
	public void doNames();

// ------------------------------

	/**
	 * Lists all visible users of (a) channel(s).
	 * @param chan The channel the <code>NAMES</code> command is refering to.
	 */
	public void doNames(String chan);

// ------------------------------

	/**
	 * Sends a message to a person or a channel.
	 * @param target The nickname or channel the message should be sent to.
	 * @param msg The message which should be transmitted.
	 */
	public void doPrivmsg(String target, String msg);

// ------------------------------

	/**
	 * See doMode(String target, String mode); THis is basically the same method, only that current channel is used
	 */
	public void doMode(String mode) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Requests a Reply 324 for the modes of the current channel.
	 * TODO possible param chan The channel the <code>MODE</code> request is refering to.
	 */
	public void doMode() throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Sends a mode to the server. <br />
	 * The first argument is a nickname (user-mode) or a channel (channel-mode).
	 * <code>String mode</code> must contain the operators (+/-), the modes
	 * (o/v/i/k/l/p/s/w) and the possibly values (nicks/banmask/limit/key).
	 * @param target The nickname or channel of the user whose modes will be
	 *               changed.
	 * @param mode The new modes.
	 */
	public void doMode(String target, String mode) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Changes the nickname.
	 * @param nick The new nickname.
	 */
	public void doNick(String nick);

// ------------------------------

	/**
	 * Notices a message to a person or a channel.
	 * @param target The nickname or channel (group) the message should be
	 *               sent to.
	 * @param msg The message which should be transmitted.
	 */
	public void doNotice(String target, String msg);

// ------------------------------

	/**
	 * Parts from current channel with quit msg
	 * @param msg Quit message.
	 */
	public void doPart(String msg) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Parts from the current channel.
	 */
	public void doPart()  throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Parts from a given channel with a given parg-msg.
	 * @param chan The channel you want to part from.
	 * @param msg The optional partmessage.
	 */
	public void doPart(String chan, String msg) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Quits from the IRC server with a quit-msg.
	 * @param ping The ping which was received in <code>onPing</code>. It's a
	 *             <code>String</code>, because sometimes on some networks
	 *             the server-hostname (for example splatterworld.quakenet.org) is
	 *             given as parameter which would throw an Exception if we
	 *             gave the ping as long.
	 */
	public void doPong(String ping);

// ------------------------------

	/**
	 * Quits from the IRC server.
	 * Calls the <code>disconnect</code>-method which does the work actually.
	 * @see #isConnected()
	 * @see #connect()
	 * @see #doQuit(String)
	 * @see #close()
	 */
	public void doQuit();

// ------------------------------

	/**
	 * Quits from the IRC server with a quit-msg.
	 * Calls the <code>disconnect</code>-method which does the work actually.
	 * @param msg The optional quitmessage.
	 * @see #isConnected()
	 * @see #connect()
	 * @see #doQuit()
	 * @see #close()
	 */
	public void doQuit(String msg) ;

// ------------------------------

	/**
	 * Requests the topic of the current channel
	 */
	public void doTopic();

// ------------------------------

	/**
	 * Sets the topic of the current chosen channel
	 * @param topic the topic for the channel
	 */
	public void doTopic(String topic) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Changes the topic of a chan.
	 * @param chan The channel which topic is changed.
	 * @param topic The new topic.
	 */
	public void doTopic(String chan, String topic) throws CommandNotAllowedException;

// ------------------------------

	/**
	 * Requests information about users matching the given criteric,
	 * for example a channel they are on.
	 * @param criteric The criterics of the <code>WHO</code> query.
	 */
	public void doWho(String criteric) ;

// ------------------------------

	/**
	 * Requires information about an existing user.
	 * @param nick The nickname of the user the query is refering to.
	 */
	public void doWhois(String nick) ;

// ------------------------------

	/**
	 * Requires host-information about a user, who is not connected anymore.
	 * @param nick The nickname of the user the query is refering to.
	 */
	public void doWhowas(String nick) ;

// ------------------------------

	/**
	 * Requires host-information about up to 5 users which must be listed and
	 * divided by spaces.
	 * @param nick The nickname of the user the query is refering to.
	 */
	public void doUserhost(String nick);

	public void doUncheckedSend(String string);
}
