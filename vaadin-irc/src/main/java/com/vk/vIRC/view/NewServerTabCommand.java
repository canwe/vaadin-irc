package com.vk.vIRC.view;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;

/**
 * @author victor.konopelko
 *         Date: 30.11.11
 */
public class NewServerTabCommand implements MenuBar.Command {

    private BasicView mainview;
    private NetworkListView.NetworkItem server;

    public NewServerTabCommand(BasicView main) {
        this.mainview = main;
    }

    public NewServerTabCommand(BasicView main, NetworkListView.NetworkItem server) {
        this.mainview = main;
        this.server = server;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        mainview.getNavTree().addServerTab(server == null ? new NetworkListView.NetworkItem("<none>", null) : server);
    }
}
