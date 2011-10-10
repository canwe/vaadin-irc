package com.vk.vIRC.view;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;

/**
 * @author victor.konopelko
 *         Date: 10.10.11
 */
public class NetworkListCommand implements MenuBar.Command {

    private Window mywindow;    // The window to be opened
    private AbstractView mainview;

    public NetworkListCommand(AbstractView main) {
        mainview = main;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        /* Create a new window. */
        mywindow = new Window("V-IRC: Network List");
        mywindow.setPositionX(200);
        mywindow.setPositionY(100);
        mywindow.setResizable(true);
        mywindow.setClosable(true);
        mywindow.setWidth("350px");
        mywindow.setHeight("480px");
        mywindow.setModal(false);

        mywindow.addComponent(new NetworkListView());

        mainview.getWindow().addWindow(mywindow);
    }
}
