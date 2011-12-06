package com.vk.vIRC.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import com.vk.vIRC.CommandNotAllowedException;
import com.vk.vIRC.MainApplication;
import com.vk.vIRC.util.StringParser;
import com.vk.vIRC.view.dialogs.GeneralDialog;
import com.vk.vIRC.view.irc.IRCView;
import org.schwering.irc.lib.IRCConstants;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IRCEventViewManager is an middle-tier between the client view and the IRC client.
 * Stuff like XML/HTML-filtering and colors will be set in this phase.
 *
 * @author johan
 *
 */
public class IRCEventManager {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(IRCEventManager.class);

	private IRCTextOutputter notificationWindow = null;

	private FromUserManager fromUser;
	private ToUserManager toUser;
	private IRCClient client;

	private IRCUser me;
	private MainApplication mainReference;

	private HashMap<String, String> currentInvitations = new HashMap<String, String>();
	/**
	 * Used to define which action has to be taken, so there is no need to create a new
	 * method in the IRCCHannel interface for each method in the IRCEventManager
	 * @author johan
	 *
	 */
	public enum Actions {
		ONJOIN { public String getActionText(String optional){
			return "has joined " + StringParser.escapeXMLAndColorize(optional); }},
		ONKICK { public String getActionText(String optional){
			return "was kicked by " + optional; }},
		ONMODE { public String getActionText(String optional){
			return "sets mode " + optional; }},
		ONPART { public String getActionText(String optional){
			return "has parted " + StringParser.escapeXMLAndColorize(optional); }},
		ONQUIT { public String getActionText(String optional){
			return "has quit";}},
		ONNICK { public String getActionText(String optional){
			return "changed nick to " + optional;}},
		ONACTION { public String getActionText(String optional){
			return " " + optional;}},
		ONTOPIC { public String getActionText(String optional){
			return "changed topic to: " + StringParser.escapeXMLAndColorize(optional); }};

		public abstract String getActionText(String optional);
	}


	public IRCEventManager(IRCClient client, IRCTextOutputter notificationWindow){
		this.client = client;
		fromUser = new FromUserManager();
		toUser = new ToUserManager();
		this.notificationWindow = notificationWindow;
		Properties p = client.getUserData();
		this.mainReference = MainApplication.getCurrent();//.getMainLayout().getAbstractView(IRCView.class);
		setMe(new IRCUser((String)p.get("nick"), (String)p.get("user"), null));
	}

	public List<String> getFavChans(){
		return getIrcView().getFavChannels();
	}

	private IRCView getIrcView(){
		return client.getIrcView();
	}

	public FromUserManager fromUser(){
		return fromUser;
	}

	public ToUserManager toUser(){
		return toUser;
	}

	/**
	 * This method is called when user disconnects from an irc server.
     * (Also if user disconnects from Settings view)
	 */
	public void close() {
		// TODO!
		// FIMXE!d

		try { // to be sure, we try to close the irc connection
			MainApplication.getCurrent().getIrcClientRef().disconnect("disconnected!");
		} catch (Exception e){

		}

		try {
			MainApplication.getCurrent().setIrcClientRef(null);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void setMe(IRCUser user) {
		this.me = user;

	}

	public IRCUser getMe(){
		return this.me ;
	}

    public static String generateNick(String oldNick){
		String newNick = oldNick;
		if (oldNick.length() > 8) newNick = oldNick.substring(0, 7);
		Random r = new Random(System.currentTimeMillis());
		newNick += "" + Math.abs(r.nextInt(8999) + 1000);
		return newNick;
	}

	public class ToUserManager implements IRCEventListener{




		public void onDisconnected() {
			try {

				String msg = "You have been disconnected.";

				mainReference.getMainWindow().addWindow(new GeneralDialog(
						"You have been disconnected " , new String[]{"Ok"},
						msg,
						new GeneralDialog.Callback(){


							public void onDialogResult(int result, String optional) {
								close();
							}

						}, 2 , 1, null, 0));
			} catch (Exception e) {
				e.printStackTrace();
			}
			mainReference.pushData();
		}


		public void onError(String msg) {
			log.debug("ERROR:" + msg);
			notificationWindow.addText(StringParser.errorText(msg));
			mainReference.pushData();
		}


		public void onError(int num, String msg) {
			log.debug("ERROR:" + msg);
			notificationWindow.addText(StringParser.errorText(msg));
			notificationWindow.addText("Vaadin TKIRC Generates a new nick...");
			if (num == IRCConstants.ERR_NICKNAMEINUSE){
				fromUser().doNick(generateNick(getMe().getNick()));
				getMe().getNick();
			}
			mainReference.pushData();
		}


		public void onInvite(String chan, IRCUser user, String passiveNick) {

			// This is kind of a ugly hack, since the view is touched from here ;(
			// only show your dialogs
			if (!passiveNick.equalsIgnoreCase(me.getNick())) return;
			// do not show dialog if joined
			//if (getChannel(chan) != null) return;
			// prevent many of same invitations
			if (currentInvitations.get(chan) != null) return;
			else currentInvitations.put(chan, chan);
			try {
				String yes = "Yes";
				// if (MainApplication.isStrict) yes = "Yes (not possible in strict client)";
                GeneralDialog gd;
                mainReference.getMainWindow().addWindow(gd = new GeneralDialog(
                        "Invitation to " + chan, new String[]{yes, "No"},
                        "<b>" + user.getNick() + " (</b>" + user.getHost() + "<b>)</b> has invited you to <b>" + chan + "</b>.<br>Do you want to Join?",
                        new GeneralDialog.Callback() {


                            public void onDialogResult(int result, String channel) {
                                try {
                                    if (result == 0) fromUser.doJoin(channel);
                                } catch (CommandNotAllowedException e) {

                                }
                                currentInvitations.remove(channel);
                            }

                        }, 2, 1, chan, 1));
                gd.setWidth("400px");
			} catch (Exception e) {
				e.printStackTrace();
			}
			mainReference.pushData();
		}


		public void onJoin(String chan, IRCUser user) {
			//MainApplication ma = MainApplication.getCurrent();

			// This will fix a lot of problems
			if (me.getHost() == null) {
				setMe(user);
			}

			if (me.getNick().equalsIgnoreCase(user.getNick())){
				//addChannel(chan);
				IRCEventManager.this.fromUser.doWho(chan); // THis is a hack to get the host of all users
			} else {
			    // if someone else joins
				//IRCChannel ichan = getChannel(chan);
				//ichan.addUser(new ExtendedIRCUser(user), Actions.ONJOIN, null, chan);
			}


			log.debug("JOIN: CHAN: " + chan + " USER: " + user);
			mainReference.pushData();
		}


		public void onKick(String chan, IRCUser user, String passiveNick, String msg) {
			msg = StringParser.escapeXMLAndColorize(msg);
			log.debug("KICK: CHAN: " + chan + " USER: " + user + " PASSIVENICK: " + passiveNick + " MSG: " + msg);
			if (me.getNick().equalsIgnoreCase(passiveNick)){
				//removeChannel(chan);
				notificationWindow.addText(StringParser.notificationText("You Were kicked by " + user.getNick() + " from " + chan + ", Reason: [" + msg + "]"));

				try {

					String windowMessage = "You have been Kicked from " + chan + ". Reason: \"" + msg + "\"";

					mainReference.getMainWindow().addWindow(new GeneralDialog(
							"You have been Kicked " , new String[]{"Ok"},
							windowMessage,
							new GeneralDialog.Callback(){


								public void onDialogResult(int result, String optional) {

								}

							}, 2 , 1, null, 0));
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else { // if someone else joins
				//IRCChannel ichan = getChannel(chan);
				//ExtendedIRCUser kicker = new ExtendedIRCUser(user);
				// Making it extended user, so it is easy to strip off possible prefixes
				//ExtendedIRCUser kicked = new ExtendedIRCUser(new IRCUser(passiveNick, "", ""));
				//ichan.removeUser(kicked, Actions.ONKICK, msg, kicker.getNick());
			}


			log.debug("KICK: USER:" + user + " MSG: " + msg + " CHAN: " + chan);
			mainReference.pushData();

		}


		public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
			//IRCChannel ch = getChannel(chan);
			//ch.doUserAction(new ExtendedIRCUser(user), Actions.ONMODE, null, modeParser.getLine());
			// searching for voice/op:
			for (int i = 0; i < modeParser.getCount(); i++){
				boolean remove = false;
				if (modeParser.getModeAt(i + 1) == 'o' || modeParser.getModeAt(i + 1) == 'v'){
					if (modeParser.getOperatorAt(i + 1) == '-') remove = true;
					String modedNick = modeParser.getArgAt(i + 1);

//					IRCUsersTable iut = ch.getIRCUsersTable();
//					ExtendedIRCUser eiu = iut.getUser(modedNick);
//
//					if (modeParser.getModeAt(i + 1) == 'o')
//						eiu.setOp(modeParser.getOperatorAt(i + 1) == '+');
//					else eiu.setVoice(modeParser.getOperatorAt(i + 1) == '+');
//
//					iut.replaceUser(modedNick, eiu);
//					iut.sort();

				}
			}

			log.debug("MODE: USER: " + user + " CHANNEL: " + chan + " MODE: " + modeParser.getLine());
			mainReference.pushData();
		}


		public void onMode(IRCUser user, String passiveNick, String mode) {
			// TODO Auto-generated method stub
			log.debug("MODE: USER: " + user + " PASSIVENICK: " + passiveNick + " MODE: " + mode);
			mainReference.pushData();

		}


		public void onNick(IRCUser user, String newNick) {
			// TODO: if user changes nick, then also change tab caption (and change in privateTabs)
			log.debug("MODE: USER: " + user + " NEWNICK:" + newNick);
			if (me.getNick().equalsIgnoreCase(user.getNick())){
				log.debug("STILL TODO AND FIX, for instance all places where is me used"); //FIXME
				IRCUser tmpMe = new IRCUser(newNick, me.getUsername(), me.getHost());
				setMe(tmpMe);
			}

			// Iterate through all channels, because if a user changes nick, it will apply everywhere (if found)

//			for (String chan : channels.keySet()){
//				IRCChannel ch = getChannel(chan);
//				IRCUsersTable iut = ch.getIRCUsersTable();
//				ExtendedIRCUser eiu = iut.getUser(user.getNick());
//				if (eiu == null) continue;
//				eiu.setUser(new IRCUser(newNick, user.getUsername(), user.getHost()), false); // set false, otherwise possible + and @ are lost
//				iut.replaceUser(user.getNick(), eiu);
//				iut.sort();
//				ch.doUserAction(new ExtendedIRCUser(user), Actions.ONNICK, null, newNick);
//			}
//
//			IRCPrivateTab ipt = getPrivateTab(user.getNick());
//			if (ipt != null){
//				ipt.setCaption(newNick);
//				privateTabs.remove(user.getNick().trim().toLowerCase());
//				privateTabs.put(newNick.trim().toLowerCase(), ipt);
//			}
			mainReference.pushData();

		}


		public void onNotice(String target, IRCUser user, String msg) {
			msg = StringParser.escapeXMLAndColorize(msg);
			log.debug("NOTICE: USER:" + user + " MSG: " + msg);
			notificationWindow.addText(msg);

			mainReference.pushData();

		}


		public void onPart(String chan, IRCUser user, String msg) {
			msg = StringParser.escapeXMLAndColorize(msg);
//			if (me.getNick().equalsIgnoreCase(user.getNick())){
//				removeChannel(chan);
//			} else { // if someone else joins
//				IRCChannel ichan = getChannel(chan);
//				ichan.removeUser(new ExtendedIRCUser(user), Actions.ONPART, msg, chan);
//			}


			mainReference.pushData();
			log.debug("PART: USER:" + user + " MSG: " + msg + " CHAN: " + chan);

		}


		public void onPing(String ping) {
			// TODO Auto-generated method stub
			mainReference.pushData();

		}


		/**
		 * Since IRCLibs didn't provide any support for dcc, a dcc request will be routed through onPrivmsg -> onAction -> onDccReceive.
		 * onDccSend is called when ANOTHER user sends a file to THIS client (not vice versa)
		 */
		private void onDccSend(IRCUser user, String msg){

			long ipAsLongValue = -1;
			final String fileName;
			final int port;
			final int fileSize;
			final String ipAddress;
			if (!msg.trim().startsWith("SEND")) return;
			msg = msg.trim().substring(4).trim();

			try {
				String[] args = msg.split("\\s");
				fileName = args[0];
				ipAsLongValue = Long.parseLong(args[1]);
				port = Integer.parseInt(args[2]);
				fileSize = Integer.parseInt(args[3]);

				int[] ip = new int[4];
				String tmp = "";
				for (int i = 3; i >= 0; i--) {
					tmp = "." + ((int) (ipAsLongValue % 256)) + tmp;
					ipAsLongValue = ipAsLongValue / 256;
				}
				ipAddress = tmp.substring(1, tmp.length()); // remove first dot
			} catch (Exception e){
				log.error("Problems when parsing the dcc send request!");
				e.printStackTrace();
				return; // do not continue
			}



			boolean accept = false;
			try {
				mainReference.getMainWindow().addWindow(new GeneralDialog(
						"DCC - File sent from " + user.getNick(), new String[]{"Yes", "No"},
						"<b>" + user.getNick() + " (</b>" + user.getHost() + "<b>)</b> is trying to send you a file: " + fileName + " (" + (fileSize / 1024) + " KiB) <br>Do you accept?<br> By accepting the file, a connection to the distributed Vaadin TKIRC client will be established. <br> When the transfer is finished, you should be able to download the file.",
						new GeneralDialog.Callback(){


							public void onDialogResult(int result, String optionalData) {
								if (result == 0) {
//									DCCReceiveComponent dccRec = new DCCReceiveComponent(fileName, ipAddress, port, fileSize);
//									mainReference.getMainWindow().addWindow(dccRec);
//									dccRec.receive();
//									mainReference.pushData();
								}

							}

						}, 2 , 1, null, 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			mainReference.pushData();

		}



		private void onAction(String target, IRCUser user, String msg){

			try {
				msg = msg.replaceAll(Character.toString(IRCConstants.ACTION_INDICATOR), "");
				if (msg.trim().startsWith("ACTION")) msg = msg.substring(6);
				else if (msg.trim().startsWith("DCC")) {

					onDccSend(user, msg.substring(3));
					return;
				}


			} catch (Exception e){e.printStackTrace(); }

//			if (getChannel(target) != null){
//				IRCChannelTab chan = (IRCChannelTab) getChannel(target);
//				chan.doUserAction(new ExtendedIRCUser(user), Actions.ONACTION, null, msg);
//			}
			mainReference.pushData();

		}




		public void onPrivmsg(String target, IRCUser user, String msg) {
			msg = StringParser.escapeXMLAndColorize(msg);
			msg = StringParser.parseHttpAsLink(msg);
			log.debug("PRIVMSG: TARGET:" + target + " USER:" + user + " MSG: " + msg);

			if (msg.charAt(0) == IRCConstants.ACTION_INDICATOR) {
				onAction(target, user, msg.substring(1));
				return;
			}

//			if (getChannel(target) != null){
//				IRCChannelTab chan = (IRCChannelTab) getChannel(target);
//				// FIXME: an xmlTextWrapper
//				if (msg.startsWith(me.getNick())) chan.addText("<span class=\"fgcol4\"<b>&lt;" + user.getNick() + "&gt;</b></span> " + msg);
//				else chan.addText("&lt;" + user.getNick() + "&gt; " + msg);
//			} 		/*Else is a Private message */
//			else {
//				IRCPrivateTab privTab = (IRCPrivateTab) getPrivateTab(user.getNick());
//				if (privTab == null) privTab = addPrivateTab(user.getNick());
//				if (msg.startsWith(me.getNick())) privTab.addText("<span class=\"fgcol4\"<b>&lt;" + user.getNick() + "&gt;</b></span> " + msg);
//				else privTab.addText("&lt;" + user.getNick() + "&gt; " + msg);
//			}
			mainReference.pushData();

		}


		public void onQuit(IRCUser user, String msg) {
			msg = StringParser.escapeXMLAndColorize(msg);
			log.debug("QUIT: USER:" + user + " MSG: " + msg);
			if (me.getNick().equalsIgnoreCase(user.getNick())){
				log.debug("remove this text");
			} else {

				// Iterate through all channels, because if a user changes nick, it will apply everywhere (if found)

//				for (String chan : channels.keySet()){
//					IRCChannel ch = getChannel(chan);
//					IRCUsersTable iut = ch.getIRCUsersTable();
//					ExtendedIRCUser eiu = iut.getUser(user.getNick());
//					if (eiu == null) continue;
//					ch.removeUser(eiu, Actions.ONQUIT, msg, null);
//					iut.sort();
//				}
			}
			mainReference.pushData();
		}


		public void onRegistered() {
			// TODO Auto-generated method stub
			mainReference.pushData();

		}


		public void onReply(int num, String value, String msg) {
			msg = StringParser.escapeXMLAndColorize(msg);
			log.debug("[" + value + "] " + msg + " [num: " + num + "]");
			String nick = client.getUserData().getProperty("nick");
			// Remove the nickname from reply
			notificationWindow.addText(value.replace(nick, "") + " " + msg);

			mainReference.pushData();

		}


		public void onTopic(String chan, IRCUser user, String topic) {
			topic = StringParser.escapeXMLAndColorize(topic);
			log.debug("TOPIC: CHANNEL:" + chan + " USER: " + user + " TOPIC: " + topic);
			//IRCChannel ch = getChannel(chan);
			//ch.doUserAction(new ExtendedIRCUser(user), Actions.ONTOPIC, null, topic);
			//ch.setTopic(topic);
			mainReference.pushData();
		}


		public void unknown(String prefix, String command, String middle,
				String trailing) {
			log.debug("UNKNOWN:");

			mainReference.pushData();

		}

		public void popupHelp() {
			//mainReference.getMainWindow().addWindow(new HelpWindow());
			mainReference.pushData();
		}

	}

	/**
	 * Important note! Do not name own (local) methods to start with do*, since reflect will try to find these and invoke them
	 * @author johan
	 *
	 */

	public class FromUserManager implements UserEventListener {

		/**
		 * This is a small hack (try not to use too much :)). Since input commands preceded by a / will use java reflection,
		 * we cannot add own parameters to the methods. Often we want the currentlu chosen Irc tab, so here it is provided
		 * (but without warranties).
		 */
		private String currentTargetName = null;

		// This method was added here to ease the use
		public IRCUser getMe(){
			return IRCEventManager.this.me;
		}

		/**
		 * Private channels may be closed without notifying the irc server (as opposed to irc channels)
		 */
		public void closePrivateChannelDiscretely(String name){
			//IRCEventManager.this.removePrivateTab(name);
		}

		/**
		 * Private channels may be opened without notifying the irc server (as opposed to irc channels)
		 */
		public void openPrivateChannelDiscretely(String name){
			//IRCTab itab = IRCEventManager.this.addPrivateTab(name);
			//IRCView ircView = (IRCView) MainApplication.getCurrent().getMainLayout().getAbstractView(IRCView.class);
			//ircView.setSelectedTab(itab);
		}


		public void doUncheckedSend(String text) {
			//client.getIRCConnection().doUncheckedSend(text);
		}


		/**
		 * If the purpose doesn't fit any of the categories below, one can
		 * call this method and handle it internally here.
		 * For instance writing /join in a text box could call this method and then from here
		 * delegate the request to doJoin(). This will allow for more flexible use of the text boxes.
		 * A requirement is, that the text is preceded by a slash "/"
		 * <br><br>
		 * NOTE to the developers: Do not add surrogate methods, because it will require harder coupling between
		 * IRCEventManager and the IRCTabs. This means that no shortcuts will be allowed  (for instance /kick #channel nick -> /kick nick)
		 * @param text The message to send
		 * @param targetName This is optional, but can be used for different purposes
		 */
		public void executeLocalAction(String text, String targetName) throws CommandNotAllowedException{
			//fixa nе fak check
				currentTargetName = targetName;


			if (notificationWindow == null) return;
			text = text.trim();
			if (!text.startsWith("/")) {
				notificationWindow.addText(StringParser.warningText(text + " should be preceded by a slash!"));
				return;
			}


			String actionString = text.replaceFirst("/", "");
			String actionX = actionString.replaceFirst("\\s+.*", "");
			String parameterString = actionString.replaceFirst(actionX, "");
			parameterString = parameterString.trim();

			String parameters[] = (parameterString.trim()).split("\\s");
			if (parameters.length == 1){
				if (parameters[0].equals("")) parameters = new String[0];
			}
			log.debug("PARAMTER SIZE:" + parameters.length);
			String action = StringParser.getUniqueCommand(actionX);

			if (action == null) {
				notificationWindow.addText(StringParser.warningText("Not a valid command!"));
				return;
			}
			if (action.equalsIgnoreCase("")) {
				notificationWindow.addText(StringParser.warningText("Ambiguous command (more commands using the prefix was found)!"));
				return;
			}

			// ugly hack branch (the stupid behaviour of methods with messages/modes (and one parameter needed))
			if ((action.equalsIgnoreCase("mode") || action.equalsIgnoreCase("topic") || action.equalsIgnoreCase("part")
					|| action.equalsIgnoreCase("me"))&& parameters.length >= 2){
				String tmp = "";
				tmp = parameters[0];
				for (int ii = 1; ii < parameters.length; ii++)
					tmp += (" " + parameters[ii]);

				parameters = new String[]{tmp};
			}

			// TODO: Before this can be accomplished with for instance /j, we have to complete the actions
			ArrayList<Method> methodCandidates = new ArrayList<Method>();

			Method[] methods = FromUserManager.this.getClass().getDeclaredMethods();
			for (int i=0; i < methods.length; i++){
				if (("do" + action).equalsIgnoreCase(methods[i].getName())){
					methodCandidates.add(methods[i]);
				}
			}

			if (methodCandidates.size() <= 0){
				notificationWindow.addText(StringParser.warningText(action + " - Command not found, or not implemented yet!"));
				return;
			}

			// Sort the methods according to parameter size
			// The ones with shorter ones will be ordered first in the list
			//Collections.sort(methodCandidates, new MethodParameterComparator());


			// Searching for the exact match, starting from LEAST NUMBER of parameter types...
			// ... if not found, and this object's method's parameters grow bigger than
			// the amount of provided ones, then use last one that fit.

			Method methodWithLeastParametersAndOK = null;
			boolean anyMethodParametersOk = false;

			for (Method m: methodCandidates){
				Class<?>[] classes = m.getParameterTypes();
                log.debug("Matches: " + m.getName() + " parameters: " + classes.length);
				boolean parameterTypesOk = true;
				// First check that all parameters are of type String
				for (int ii = 0; ii<classes.length; ii++){

					if (!(classes[ii].getCanonicalName().equalsIgnoreCase(String.class.getCanonicalName()))) {
						parameterTypesOk = false;
						break;
					} else {

					}
				}

				// this won't pass if something else than strings found
				if (!parameterTypesOk) continue;
				// at least one method that can be invoked was found
				anyMethodParametersOk = true;

				if (classes.length == parameters.length){
					methodWithLeastParametersAndOK = m;
					break;
				} else if (classes.length > parameters.length) {
					if (methodWithLeastParametersAndOK == null){ // if we have no method to call, we have to return
						notificationWindow.addText(StringParser.warningText(action + ": Not enough parameters given!"));
						return;
					} else break; // This is an OK state, since methodWithLeastParametersAndOK is not null
				}

				methodWithLeastParametersAndOK = m;
			}

			if (!anyMethodParametersOk) {
				notificationWindow.addText(StringParser.warningText(action + " No such action can be taken!"));
				return;
			}

			try {


				Class[] classes = methodWithLeastParametersAndOK.getParameterTypes();
				String[] args = new String[classes.length];



				if (classes.length > 0) {
					int i = 0;
					for (; i<classes.length - 1; i++) args[i] = parameters[i];
					for (; i<parameters.length; i++){
						if (args[args.length - 1] == null) args[args.length - 1] = "";
						args[args.length - 1] += parameters[i] + " ";  // the last parameter is often a message, so add up as a message
					}
					args[args.length - 1] = args[args.length - 1].trim();
				}


				methodWithLeastParametersAndOK.invoke(FromUserManager.this, args);
				return;
			} catch (IllegalAccessException iae){
				iae.printStackTrace();
				notificationWindow.addText(StringParser.errorText(iae.getMessage()));
				return;
			} catch (InvocationTargetException ite){
				if (ite.getTargetException() instanceof CommandNotAllowedException){
					throw (CommandNotAllowedException)ite.getTargetException();
				}
				ite.printStackTrace();
				notificationWindow.addText(StringParser.errorText(ite.getMessage()));
				return;
			}

		}


		public void doAway() {
			client.getIRCConnection().doAway();

		}


		public void doAway(String msg) {
			client.getIRCConnection().doAway(msg);

		}


		public void doInvite(String nick, String chan) {
			client.getIRCConnection().doInvite(nick, chan);
		}


		public void doInvite(String nick) {
			client.getIRCConnection().doInvite(nick, currentTargetName);
		}


		public void doIson(String nick) {
			client.getIRCConnection().doIson(nick);

		}


		public void doJoin(String chan) throws CommandNotAllowedException {
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Join");
			chan = "#" + chan.replaceAll("#", "");
			client.getIRCConnection().doJoin(chan);

		}


		public void doJoin(String chan, String key) throws CommandNotAllowedException {
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Join");
			chan = "#" + chan.replaceAll("#", "");
			client.getIRCConnection().doJoin(chan, key);

		}


		public void doKick(String nick) throws CommandNotAllowedException {
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Kick");
			client.getIRCConnection().doKick(currentTargetName, nick);
		}


		public void doKick(String nick, String msg) throws CommandNotAllowedException {
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Kick");
			client.getIRCConnection().doKick(currentTargetName, nick, msg);
		}


		public void doKick(String target, String nick, String msg) throws CommandNotAllowedException {
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Kick");
			client.getIRCConnection().doKick(target, nick, msg);
		}


		public void doList()  throws CommandNotAllowedException {
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("List");
			// TODO Auto-generated method stub

		}


		public void doList(String chan)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("List");
			// TODO Auto-generated method stub

		}


		public void doMode() {
			client.getIRCConnection().doMode(currentTargetName);
		}


		public void doMode(String mode)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Mode");
			client.getIRCConnection().doMode(currentTargetName, mode);
		}


		public void doMode(String target, String mode)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Mode");
			client.getIRCConnection().doMode(target, mode);

		}


		public void doNames() {
			client.getIRCConnection().doNames(currentTargetName);

		}


		public void doNames(String chan) {
			client.getIRCConnection().doNames(chan);

		}


		public void doNick(String nick) {
			client.getIRCConnection().doNick(nick);

		}


		public void doNotice(String target, String msg) {
			client.getIRCConnection().doNotice(target, msg);

		}


		public void doPart()  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Part");
			client.getIRCConnection().doPart(currentTargetName);

		}


		public void doPart(String msg)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Part");
			client.getIRCConnection().doPart(currentTargetName, msg);

		}


		public void doPart(String chan, String msg)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Part");
			client.getIRCConnection().doPart(chan, msg);

		}


		public void doPong(String ping) {
			client.getIRCConnection().doPong(ping);

		}


		public void doPrivmsg(String target, String msg) {

			try {
				log.info(MainApplication.getCurrent().getRemoteIP(), msg);
			} catch (NullPointerException npe) {
				// DO nothing...
			}

//			if (target == null) target = "";
//			if (getChannel(target) != null){
//				if ( msg.charAt(0) != IRCConstants.ACTION_INDICATOR){
//					IRCChannelTab chan = (IRCChannelTab) getChannel(target);
//
//					chan.addText(StringParser.hilight("&lt;" + me.getNick() + "&gt; ") + (getIrcView().isShowSmileys() ? StringParser.parseSmileys(StringParser.escapeXMLAndColorize(msg)):StringParser.escapeXMLAndColorize(msg)));
//				} else {
//					toUser.onAction(target, getMe(), msg.substring(1));
//				}
//			} else if (getPrivateTab(target) != null){
//
//				if ( msg.charAt(0) != IRCConstants.ACTION_INDICATOR){
//					IRCPrivateTab privtab = (IRCPrivateTab) getPrivateTab(target);
//
//					privtab.addText(StringParser.hilight("&lt;" + me.getNick() + "&gt; ") + (getIrcView().isShowSmileys() ? StringParser.parseSmileys(StringParser.escapeXMLAndColorize(msg)):StringParser.escapeXMLAndColorize(msg)));
//				} else {
//					toUser.onAction(target, getMe(), msg.substring(1));
//				}
//			}
//			client.getIRCConnection().doPrivmsg(target, msg);

		}


		public void doQuit() {
			client.getIRCConnection().doQuit();

		}


		public void doQuit(String msg) {
			client.getIRCConnection().doQuit(msg);

		}


		public void doTopic() {
			client.getIRCConnection().doTopic(currentTargetName);
		}


		public void doTopic(String topic)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Setting topic");
			client.getIRCConnection().doTopic(currentTargetName, topic);

		}


		public void doTopic(String chan, String topic)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Setting topic");
			client.getIRCConnection().doTopic(chan, topic);

		}


		public void doUserhost(String nick) {
			client.getIRCConnection().doUserhost(nick);

		}


		public void doWho(String criteric) {
			client.getIRCConnection().doWho(criteric);

		}


		public void doWhois(String nick) {
			client.getIRCConnection().doWhois(nick);

		}


		public void doWhowas(String nick) {
			client.getIRCConnection().doWhowas(nick);

		}

		// Own added methods

		public void doVoice(String channel, String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command voice");
			doMode(channel, "+v " + nick);
		}

		public void doVoice(String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command voice");
			doMode(currentTargetName, "+v " + nick);
		}

		public void doOp(String channel, String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command op");
			doMode(channel, "+o " + nick);
		}

		public void doOp(String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("mode");
			doMode(currentTargetName, "+o " + nick);
		}

		public void doDeVoice(String channel, String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command devoice");
			doMode(channel, "-v " + nick);
		}

		public void doDeVoice(String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command devoice");
			doMode(currentTargetName, "-v " + nick);
		}

		public void doDeOp(String channel, String nick)  throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command deop");
			doMode(channel, "-o " + nick);
		}
		public void doDeOp(String nick) throws CommandNotAllowedException{
			// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command deop");
			doMode(currentTargetName, "-o " + nick);
		}

		public void doMe(String msg) throws CommandNotAllowedException{
			doMe(currentTargetName, msg);
		}


		public void doMe(String target, String msg)  throws CommandNotAllowedException{
			//// if (MainApplication.isStrict) throw new CommandNotAllowedException("Command me");

			msg = (new Character(IRCConstants.ACTION_INDICATOR)).toString() + "ACTION "
			+ msg + (new Character(IRCConstants.ACTION_INDICATOR));
			doPrivmsg(target, msg);
		}

		/** We will not provide this method for reflection */

		//public void dccSend(ArrayList<ExtendedIRCUser> users){




			//DCCSendComponent dccSend = new DCCSendComponent(users, getMe(), FromUserManager.this);
			//mainReference.getMainWindow().addWindow(dccSend);

		//}

		// THis method cannot be called upon using reflect (not a do-method)
		public void makeFavChanJoin(String chan) {
			chan = "#" + chan.replaceAll("#", "");
			client.getIRCConnection().doJoin(chan);

		}

	}




}

