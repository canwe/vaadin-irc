package com.vk.vIRC.client;

import com.vk.vIRC.MainApplication;
import com.vk.vIRC.view.dialogs.GeneralDialog;
import com.vk.vIRC.view.irc.IRCView;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.locks.Lock;


//TODO ircEventManager
public class IRCClient {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(IRCClient.class);
	
	/** The IRC connection. */
	private IRCConnection conn;
	
	/** The current default target of PRIVMSGs (a channel or nickname). */
	private String target;
	
	private boolean isRunning = false;
	
	private IRCEventManager ircMan = null;
	
	private Properties userData = null;

	private IRCView ircView;

    /**
	 * Returns a value of a key in the arguments.
     * @param args arguments
     * @param key key
     * @return Object
     */
	private static Object getParam(String[] args, Object key) {
		return getParam(args, key, null);
	}
	
	/**
	 * Returns a value of a key in the arguments. If a key without a value is
	 * found, a Boolean object with true is returned. If no key is found, the
	 * default value is returned.
     * @param args arguments
     * @param key key
     * @param def default value
     * @return Object
     */
	private static Object getParam(String[] args, Object key, Object def) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-"+ key)) {
				if (i + 1 < args.length) {
					String value = args[i + 1];
					if (value.charAt(0) == '-')
						return true;
					else 
						return value;
				} else {
					return true;
				}
			} 
		}
		if (def != null)
			return def;
		else 
			throw new IllegalArgumentException("No value for "+ key +" found.");
	}
	
	

	private static void print(Object o) {
		log.debug(o.toString());
	}
	

	private static boolean startsWith(String s1, String s2) {
		return (s1.length() >= s2.length()) ? 
				s1.substring(0, s2.length()).equalsIgnoreCase(s2) : false;
	}
	
	public Properties getUserData(){
		return userData;
	}
	
	public IRCClient(String host,
                     int port,
                     String pass,
                     String nick,
                     String user,
                     String name,
                     boolean ssl,
                     IRCView ircView,
                     Lock synchObject,
                     String encoding) throws IOException {
		
		userData = new Properties();
		userData.put("host", host.trim());
		userData.put("port", ""+port);
		userData.put("pass", pass.trim());
		userData.put("nick", nick.trim());
		userData.put("user", user.trim());
		userData.put("name", name.trim());

		//in = new BufferedReader(new InputStreamReader(System.in));
		if (!ssl) {
			conn = new IRCConnection(host, new int[] {port}, pass, nick, user,name);
		} else {
			conn = new SSLIRCConnection(host, new int[] { port }, pass, nick, user, name);
			((SSLIRCConnection)conn).addTrustManager(new TrustManager());
		}
		log.debug("Testing output!");
		ircMan = new IRCEventManager(this, ircView.getIRCNotificationWindow()); //TODO
		conn.addIRCEventListener(ircMan.toUser()); //TODO
		conn.setEncoding(encoding); // TODO
		conn.setPong(true);
		conn.setDaemon(false);
		conn.setColors(true); // TODO!
		isRunning = false;
		try {
			conn.connect();
			isRunning = true;
		} catch (IOException e){
			log.error(e.getMessage());
			try {
				String msg = "You have been disconnected.";

				MainApplication.getCurrent().getMainWindow().addWindow(new GeneralDialog(
						"You have been disconnected " , new String[]{"Ok"},
						msg,
						new GeneralDialog.Callback(){

							public void onDialogResult(int result, String optional) {
								ircMan.close(); //TODO
							}

						}, 2 , 1, null, 0));

			} catch (Exception e2){}
		}

		if (isRunning){
			Properties p = new Properties(); //TODO
			p.put("fromusermanager", ircMan.fromUser()); //TODO
			ircView.setParameters(p); //TODO
			this.ircView = ircView;
		}

	}
	
	
	/**
	 * Parses the input and sends it to the IRC server.
     * @throws Exception ex
     */
	public void shipInput() throws Exception {
		String input = "";//in.readLine();
		if (input == null || input.length() == 0)
			return;
		
		if (input.charAt(0) == '/') {
			if (startsWith(input, "/TARGET")) {
				target = input.substring(8);
				return;
			} else if (startsWith(input, "/JOIN")) {
				target = input.substring(6);
			}
			input = input.substring(1);
			print("Exec: "+ input);
			conn.send(input);
		} else {
			conn.doPrivmsg(target, input);
			print(target +"> "+ input);
		}
	}
	
	// TODO: FIX!
	public class TrustManager implements SSLTrustManager {
		private X509Certificate[] chain;
		public X509Certificate[] getAcceptedIssuers() {
			return chain != null ? chain : new X509Certificate[0];
		}
		public boolean isTrusted(X509Certificate[] chain) {
			log.debug("Do you want to trust? [yes/no]");
			String s;
			try {
				s = ""; // TODO!
			} catch (Exception exc) {
				exc.printStackTrace();
				return false;
			}
				this.chain = chain;
				if (s.equalsIgnoreCase("yes")) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	
	public boolean isConnected(){
		if (conn == null) return false;
		else return (conn.isAlive() && conn.isConnected());
	}
	
	public void disconnect(String msg){
		try{
			conn.doQuit(msg);
		} catch (Exception e){}
		try{
			conn.interrupt();
		} catch (Exception e){}
		
		ircMan = null; //TODO
		this.isRunning = false;
	}

	public IRCConnection getIRCConnection() {
		return conn;
	}

	public IRCView getIrcView() {
		return ircView;
	}


	
}
