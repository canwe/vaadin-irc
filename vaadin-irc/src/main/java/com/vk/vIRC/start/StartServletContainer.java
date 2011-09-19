package com.vk.vIRC.start;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.util.Locale;
import java.util.ResourceBundle;

public class StartServletContainer {

    public static int PORT = 8085;

    public static void main(String[] args) {


    	System.out.println("Reading conf.properties...");
    	ResourceBundle rb = ResourceBundle.getBundle("conf", Locale.ENGLISH);
    	try {
   			PORT = Integer.parseInt(rb.getString("webappport"));
    	} catch (Exception e){
    		e.printStackTrace();
    	}

        try {
            final Server server = new Server();
            final Connector connector = new SelectChannelConnector();

            connector.setPort(PORT);
            server.setConnectors(new Connector[] { connector });

            final WebAppContext webappcontext = new WebAppContext("src/main/webapp", "vaadin-irc");
            webappcontext.setCopyWebDir(true);

            server.setHandler(webappcontext);

            server.start();
            System.out.println("**********************************************************/");
            System.out.println("vaadin-irc started, please go to http://localhost:" + PORT + "/");
            System.out.println("**********************************************************/");
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }
}