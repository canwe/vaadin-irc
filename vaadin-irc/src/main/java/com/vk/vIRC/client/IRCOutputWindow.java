package com.vk.vIRC.client;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class IRCOutputWindow extends com.vaadin.ui.CustomComponent{

	private Queue<Label> buffer;
	
	private Panel windowPanel;
	
	public static final int MAX_BUFFER_SIZE = 100;
	
	int i = 0;

	private boolean allowAutoScrolling = true;
	
	public IRCOutputWindow(){
		
		VerticalLayout ol = new VerticalLayout();
//		ol.setMargin(true);
//		ol.setSpacing(true);
//		ol.setWidth(("100%"));
//		ol.setHeight("100%");
		setHeight("100%");
		windowPanel = new Panel(ol);
		//windowPanel.setWidth("100%");
		windowPanel.setImmediate(true);
		setCompositionRoot(windowPanel);
		//windowPanel.setWidth("100%");
		windowPanel.setHeight("100%"); 
		windowPanel.setScrollable(true);
//		windowPanel.setWidth("100%");
		buffer = new LinkedList<Label>();
//		Queue<Label> buffer = new LinkedList<Label>();
		
	}
	
	/**
	 * Add a line of text that has already been formatted and html tags filtered. Also colors and stuff
	 * are added already, so that the label is only to be displayed.
	 * @param parsedString
	 */
	public void addTextLine(String parsedString){
		try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            String time = sdf.format(new Date(System.currentTimeMillis()));
			Label l = new Label("[" + time + "]" + " " + parsedString, Label.CONTENT_RAW);
			windowPanel.addComponent(l);
			buffer.add(l);
			if (buffer.size() > MAX_BUFFER_SIZE) {
				Label l2 = buffer.remove();
				windowPanel.removeComponent(l2);			
			}

			scrollDown();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setHeader(String topic) {
		windowPanel.setCaption(topic);
	}
	
	public void scrollDown(){
		if (!allowAutoScrolling) return;
		windowPanel.setScrollTop(80000 + (i++ % 10)); // to be sure
		windowPanel.requestRepaint();
	}
	
	public void setAllowAutoScrolling(boolean allowAutoScrolling) {
		this.allowAutoScrolling = allowAutoScrolling;
		
	}
	
}
