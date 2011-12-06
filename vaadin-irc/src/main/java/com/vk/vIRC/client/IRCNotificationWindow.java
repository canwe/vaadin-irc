package com.vk.vIRC.client;


import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.VerticalLayout;
import com.vk.vIRC.util.StringParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IRCNotificationWindow extends IRCWindow {

    /** log. */
	private static final Logger log = LoggerFactory.getLogger(IRCNotificationWindow.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 8511728148579332415L;

	private IRCOutputWindow outWindow;
	
	private IRCInputTextFieldComponent inTFC;

	private boolean focused = true;

	public IRCNotificationWindow(){
		VerticalLayout ol = new VerticalLayout();
		setCompositionRoot(ol);
		ol.setHeight("100%");
		setHeight("100%");
		ol.setMargin(true);
		ol.setSpacing(true);
		ol.addComponent(outWindow = new IRCOutputWindow());
		outWindow.addTextLine("<b>Remember to behave!<b>");
		ol.addComponent(inTFC = new IRCInputTextFieldComponent(this));
		inTFC.setWidth("70%");
		ol.setExpandRatio(outWindow, 1.0f);

	}

	
	public void addText(String txt) {
		if (outWindow != null) {
            outWindow.addTextLine(txt);
        }
		else {
            log.warn("Output window not ready!");
        }
		if (!focused && StringParser.isErrorOrWarningMsg(txt)) {
            this.setTabIcon(new ThemeResource ("../tkirc/images/errorwarning.gif"));
        }
		if (!focused && this.getTabIcon() == null) {
            this.setTabIcon(new ThemeResource ("../tkirc/images/action.png"));
        }
		log.debug("" + this.isVisible());
	}

	@Override
	public void addFromUserManager(IRCEventManager.FromUserManager fromUser) {
		inTFC.addFromUserManager(fromUser);
	}

	@Override
	public String getTargetName() {
		return "$Notifications";
	}

	@Override
	public void focusOnTextInput() {
		inTFC.focus();
		focused = true;
	}

	@Override
	public void removeFocusFlag() {
		focused = false;
		
	}

	@Override
	public void closeTab() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scrollOutputWindowDown() {
		outWindow.scrollDown();
		
	}

	@Override
	public String getTextBoxText() {
		return inTFC.getCurrentText();
	}

	@Override
	public void hideComponent(HideableComponent comp, Boolean hide) {

	}
	@Override
	public void setAutoScrolling(boolean scroll) {
		outWindow.setAllowAutoScrolling(scroll);
		
	}
}


