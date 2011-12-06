package com.vk.vIRC.client;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet.Tab;

public abstract class IRCWindow extends CustomComponent implements IRCTextOutputter{

	public enum TabType {UNKNOWN, NOTIFICATION, CHANNEL, PRIVATE}

	public enum HideableComponent {USERTABLE, USERACTIONS, IRCTABACTIONS}

	private Tab tab;
	
	public abstract void addFromUserManager(IRCEventManager.FromUserManager fromUser);
	
	/**
	 * For IRC Channels, targets may be channel names
	 * For private messages, targets can be nicks.
	 * @return
	 */
	public abstract String getTargetName();
	
	/**
	 * This method should be implemented to focus on the text input area when, for instance, tabs change
	 */
	public abstract void focusOnTextInput();
	
	public abstract void removeFocusFlag();
	
	/**
	 * Call this method to release any resources bound to it. The actual tab will be closed from
	 * the view.
	 */
	public abstract void closeTab();
	
	public abstract void scrollOutputWindowDown();
	
	/**
	 * 
	 * @param scroll defaults to true;
	 */
	public abstract void setAutoScrolling(boolean scroll);
	
	/**
	 * Return the text currently in the input textfield.
	 */
	public abstract String getTextBoxText();

	// TODO document
	public void setTab(Tab tab) {
		this.tab = tab;
	}
	
	// TODO document
	public void setTabIcon(ThemeResource icon) {
		//setIcon(icon);
		if (tab != null) {
			tab.setIcon(icon);
		}
	}
	
	public Resource getTabIcon(){
		return tab.getIcon();
	}
	/**
	 *  Use this method to hide or show The Users Table Component,
	 *  User Actions component and IRCTabActions component. This will allow for more room.
	 *	enums: USERTABLE, USERACTIONS, IRCTABACTIONS.
	 *  Please override this method where necessary
	 */
	public void hideComponent(HideableComponent hComp, Boolean hide){
		// Do nothing here
	}
	
	/**
	 *  This method returns whether a hideable component is hidden or not.
	 *	enums: USERTABLE, USERACTIONS, IRCTABACTIONS.
	 */
	public boolean isComponentHidden(HideableComponent hComp){
		return false;
	}
	
	
}
