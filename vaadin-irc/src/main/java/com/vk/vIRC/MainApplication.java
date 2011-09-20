package com.vk.vIRC;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Window;
import com.vk.vIRC.view.AbstractView;
import com.vk.vIRC.view.BasicView;
import com.vk.vIRC.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainApplication
     extends Application
  implements TransactionListener,
             URIHandler,
             ParameterHandler {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

	private static ThreadLocal<MainApplication> current = new ThreadLocal<MainApplication>();

	// Application main window contains one layout, main layout
	private Window mainWindow = new Window("MainWindow");

	// Main layout contains all the components and views
	private MainLayout mainLayout;

	//private IRCClient ircClient = null;

	private int defaultDCCSendPort = 10027;

	public static final int SERVERSIDE_TIMEOUT = 60000;

	private Lock synchLock;

	private String remoteAddress = "unknown";

	//private TimeOutCheckerThread checkerThread;

	private ICEPush pusher = new ICEPush();

	@Override
	public void init() {
		mainWindow = new Window("Vaadin");
		synchLock = new ReentrantLock();
		setMainWindow(mainWindow);
		//mainWindow.addComponent(pusher);

		mainWindow.addParameterHandler(this);

		getContext().addTransactionListener(this);
	}

	public Lock getSynchObject() {
		return this.synchLock;
	}

	public void transactionStart(Application application, Object transactionData) {
		if (application == this) {

			synchLock.lock();
			try {
				current.set(this);

				// If mainLayout not instantiated, do it here. NOT in Application
				// constructor
				while (null == mainLayout) {
					mainLayout = new MainLayout();

					mainWindow.setContent(mainLayout);
					mainWindow.addComponent(pusher);
					this.getContext();
					if (transactionData instanceof HttpServletRequest) {
						this.remoteAddress = resolveRemoteHost((HttpServletRequest) transactionData);
					}
					mainLayout.setCurrentView(BasicView.class);
					mainLayout.attachPoller(); // will only be attached if no poller found from before
				}
			} catch (Exception e) {
				e.printStackTrace();
				synchLock.unlock();
			}
		}
	}

	private String resolveRemoteHost(HttpServletRequest request) {
		String remoteHost = request.getRemoteHost();

		// Liferay hacking needed. Somehow Liferay didn't get the remote IP:
		if (remoteHost == null) {
			remoteHost = request.getRemoteAddr();
			if (remoteHost == null) {
				return "unknown";
			}
		}
		return remoteHost;
	}

	public void transactionEnd(Application application, Object transactionData) {
		if (application == this) {
			current.set(null);
			try {
				synchLock.unlock();
			} catch (IllegalMonitorStateException e) {

			}
		}
	}

	/**
	 * It is very discouraged to use this method to grab someting. If any other
	 * thread tries to access the current application, it will return null.
	 *
	 * @return
	 */
	public static MainApplication getCurrent() {
		return current.get();
	}

	@Override
	public void close() {

        //disconnect from all irc networks
        //TODO
		//if (ircClient != null && ircClient.isConnected()) {
		//	ircClient.disconnect("Powered by IT Mill http://www.vaadin.com ");
		//}

		// let's just wait a few seconds...
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}

		// Just to be sure
        //TODO
		//try {
		//	ircClient.getIRCConnection().close();
		//} catch (NullPointerException npe) {
		//	log.debug("Looks like there were no connection in the first place...");
		//}
		setUser(null);
		if (current != null) {
			current.set(null);
		}
		mainWindow = null;
		mainLayout.removePoller();
		super.close();

		log.debug("Forcing a flush");
	}

	@SuppressWarnings("unchecked")
	public void handleParameters(Map parameters) {
		if (parameters.containsKey("testParameter")) {
			String[] values = (String[]) parameters.get("testParameter");
			String msg = "Received testParameter on URL with values:";
			for (String value : values) {
				msg += " " + value;
			}
			getMainWindow().showNotification(msg);
		}
	}

	@Override
	public DownloadStream handleURI(URL context, String relativeUri) {
		if (relativeUri.startsWith("testURI")) {
			String msg = "Received testURI on URI.\n"
				+ "POC says: date and time is " + new java.util.Date()
			+ "\n";
			ByteArrayInputStream is = new ByteArrayInputStream(msg.getBytes());
			DownloadStream result = new DownloadStream(is, "text/plain", "datetime.txt");
			return result;
		}

		// nothing to download, let Toolkit handle the request
		return super.handleURI(context, relativeUri);
	}

	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		// Print errors always to standard errors streams too
		try {
			synchLock.unlock();
		} catch (IllegalMonitorStateException e) {

		}
		System.err.println("Terminal error:");
		event.getThrowable().printStackTrace();
		// And let them fall to GUI too (default)
		super.terminalError(event);
	}

	public MainLayout getMainLayout() {
		return mainLayout;
	}

	@Override
	public void finalize() {
		//if (ircClient != null) {
		//	ircClient.disconnect("Disconnect by object finalization");
		//}
		//ircClient = null;
	}

//	public IRCClient getIrcClientRef() {
//		return ircClient;
//	}
//
//	public void setIrcClientRef(IRCClient ircClient)
//	throws DisconnectFirstException {
//		if (this.ircClient != null && this.ircClient.isConnected()) {
//			throw new DisconnectFirstException(
//					"You must disconnect before you can connect to a new server!");
//		}
//		this.ircClient = ircClient;
//	}

	public void clearMainLayoutViewMap() {
		mainLayout.clearViewMap();
	}

	public int getDefaultDCCSendPort() {
		return defaultDCCSendPort;
	}

	public void setDefaultDCCSendPort(int defaultDCCSendPort) {
		this.defaultDCCSendPort = defaultDCCSendPort;
	}

	public void putViewIntoMainLayoutViewMap(Class clazz, AbstractView view) {
		mainLayout.putView(clazz, view);

	}

	public String getRemoteIP() {
		return remoteAddress;
	}


	private class TimeOutCheckerThread implements Runnable{

		private boolean isRunning;

		private Thread t;

		private boolean visited = false;

		public void stop(){
			isRunning = false;
			t.interrupt();
		}

		public synchronized void setVisited() {
			visited = true;
		}

		private synchronized boolean isVisited() {
			return visited;
		}


		public TimeOutCheckerThread(){
			isRunning = true;
			t = new Thread(TimeOutCheckerThread.this);
			t.start();
		}

		@Override
		public void run() {

			while (isRunning){
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					log.debug("Interrupted from other thread. QUIT!");
					break; // do not try close
				}
				if (!isVisited()) {
					log.warn("Forcing the application and irc connection to end from server side");
					isRunning = false;
				}
				visited = false;

			}

			try {
				if (isRunning()) close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	public void pushData() {
		pusher.push();
	}
}
