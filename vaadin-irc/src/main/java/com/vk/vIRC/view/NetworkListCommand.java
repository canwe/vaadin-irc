package com.vk.vIRC.view;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author victor.konopelko
 *         Date: 10.10.11
 */
public class NetworkListCommand implements MenuBar.Command {

    private Window mywindow;    // The window to be opened
    private BasicView mainview;

    public NetworkListCommand(BasicView main) {
        this.mainview = main;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        /* Create a new window. */
        mywindow = new Window("V-IRC: Network List");
        mywindow.setPositionX(200);
        mywindow.setPositionY(100);
        mywindow.setResizable(true);
        mywindow.setClosable(true);
        mywindow.setWidth("420px");
        mywindow.setHeight("495px");
        mywindow.setModal(true);
        mywindow.setDraggable(true);

        ComponentContainer componentContainer = mywindow.getContent();
        componentContainer.setWidth("390px");
        componentContainer.setHeight("440px");
        componentContainer.addComponent(new NetworkListView(mainview));

        mainview.getWindow().addWindow(mywindow);
    }
}
