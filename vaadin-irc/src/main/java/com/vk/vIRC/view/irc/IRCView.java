package com.vk.vIRC.view.irc;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vk.vIRC.client.IRCEventManager;
import com.vk.vIRC.client.IRCNotificationWindow;
import com.vk.vIRC.client.IRCWindow;
import com.vk.vIRC.view.AbstractView;

import java.util.*;

/**
 * @author victor.konopelko
 *         Date: 05.12.11
 */
public class IRCView extends AbstractView {

	/**
	 *
	 */
	private static final long serialVersionUID = -3461786785778645951L;
	private IRCNotificationWindow notificationWindow = null;
	private IRCEventManager.FromUserManager fromUserMan = null;
	private boolean showSmileys = true;
	private List<String> favChans;

	private VerticalLayout layout = null;
	private HorizontalLayout loadingIndicator = null;

	private ThemeResource showIcon = null;
	private ThemeResource hideIcon = null;

	private Map<IRCWindow.HideableComponent, Object> hiddenComponents = new HashMap<IRCWindow.HideableComponent, Object>();

	public Map<IRCWindow.HideableComponent, Object> getHiddenComponents() {
		return hiddenComponents;
	}

	public IRCView(){
		layout = new VerticalLayout();
		HorizontalLayout hl = new HorizontalLayout();
		layout.setWidth("100%");
		setCompositionRoot(layout);

		layout.setHeight("100%");

		//hl.setHeight("100%");
		hl.setWidth("100%");
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setWidth("100%");
		hl.addComponent(hl2);
		hl.setExpandRatio(hl2, 1);

		loadingIndicator = new HorizontalLayout();
		loadingIndicator.addComponent(new Embedded("Connecting to Irc Server...", new ThemeResource("../tkirc/images/ajax-loader-big.gif")));
		layout.addComponent(loadingIndicator);
		layout.addComponent(hl);

		// TODO: Move!
		showIcon = new ThemeResource("../tkirc/images/show4.png");
		hideIcon = new ThemeResource("../tkirc/images/hide4.png");

		favChans = new ArrayList<String>();

		setHeight("100%");
		this.notificationWindow = new IRCNotificationWindow();
		layout.addComponent(this.notificationWindow);
		layout.setExpandRatio(this.notificationWindow, 1.0f);

	}

	private void swichIconButtonDataValue(Button b){
		if (b.getData() == null || !(b.getData() instanceof Boolean)) return;
		b.setData(new Boolean(!((Boolean)b.getData())));
		if ((Boolean)b.getData()){
			b.setIcon(hideIcon);
		} else {
			b.setIcon(showIcon);
		}
	}


	public VerticalLayout getLayout(){
		return layout;
	}

	public IRCNotificationWindow getIRCNotificationWindow(){
		return notificationWindow;
	}

    @Override
    public void setParameters(Properties p) {
        IRCEventManager.FromUserManager fromUserMan = (IRCEventManager.FromUserManager) p.get("fromusermanager");
        if (fromUserMan != null) {
            this.fromUserMan = fromUserMan;
            notificationWindow.addFromUserManager(this.fromUserMan);
            //System.out.println("");
        }

        if (p.get("smileys_on") != null) {
            if (p.get("smileys_on").toString().equalsIgnoreCase("true")) showSmileys = true;
            else showSmileys = false;
        }

        if (p.get("favchans") != null) {
            String[] chans = ((String) p.get("favchans")).split(",");
            for (int i = 0; i < chans.length; i++) {
                if (chans[i].trim().length() >= 1) {
                    favChans.add(chans[i].trim());
                }
            }
        }
        // FIXME colors... but not now

    }


	public List<String> getFavChannels(){
		return favChans;
	}

}