package com.vk.vIRC.view;

import com.vaadin.ui.MenuBar;
import com.vk.Strings;

/**
 * @author victor.konopelko
 *         Date: 30.11.11
 */
public class NewChannelTabCommand implements MenuBar.Command {

    private BasicView mainview;
    private String channel;

    public NewChannelTabCommand(BasicView main) {
        this.mainview = main;
    }

    public NewChannelTabCommand(BasicView main, String channel) {
        this.mainview = main;
        this.channel = channel;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        mainview.getNavTree().addChannelTab(Strings.isEmpty(channel) ? "<none>" : channel);
    }
}