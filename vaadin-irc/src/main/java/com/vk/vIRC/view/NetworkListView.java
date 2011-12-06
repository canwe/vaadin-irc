package com.vk.vIRC.view;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import com.vk.vIRC.DisconnectFirstException;
import com.vk.vIRC.MainApplication;
import com.vk.vIRC.client.IRCClient;
import com.vk.vIRC.view.irc.IRCView;

import java.io.IOException;
import java.util.*;

/**
 * @author victor.konopelko
 *         Date: 10.10.11
 */
public class NetworkListView extends AbstractView implements Property.ValueChangeListener {


    private VerticalLayout mainLayout;
    private VerticalLayout verticalLayout;
    private ListSelect networkListSelect;
    private BasicView mainview;

    public NetworkListView(BasicView main) {

        super();
        this.mainview = main;
        buildMainLayout();
        setCompositionRoot(mainLayout);

    }

//	private AbsoluteLayout buildMainLayout() {
//		// common part: create layout
//		mainLayout = new AbsoluteLayout();
//
//		// top-level component properties
//		setWidth("100.0%");
//		setHeight("100.0%");
//
//		// verticalLayout_1
//		//verticalLayout = buildVerticalLayout();
//		mainLayout.addComponent(buildUserInfoBox(), "top:0.0px;left:0.0px;");
//		mainLayout.addComponent(buildNetworkListBox(), "top:140.0px;left:0.0px;bottom:60px;");
//
//		return mainLayout;
//	}

    private VerticalLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(true);

        // top-level component properties
        setWidth("100.0%");
        setHeight("100.0%");

        Component c;
        mainLayout.addComponent(buildUserInfoBox());
        mainLayout.addComponent(c = buildNetworkListBox());
        mainLayout.setExpandRatio(c, 1.0f);
        //https://vaadin.com/forum/-/message_boards/view_message/94033
        mainLayout.addComponent(new Label("<hr />", Label.CONTENT_XHTML));
        mainLayout.addComponent(buildToolbox());

        return mainLayout;
    }

    private VerticalLayout buildVerticalLayout() {
        // common part: create layout
        verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("100.0%");
        verticalLayout.setHeight("100.0%");
        verticalLayout.setImmediate(false);
        verticalLayout.setMargin(false);

        return verticalLayout;
    }

    private Panel buildUserInfoBox() {
        FormLayout layout = new FormLayout();
        layout.setVisible(true);
        layout.setSpacing(true);
        layout.setWidth("100.0%");
        layout.setImmediate(true);
        layout.setHeight(120, Sizeable.UNITS_PIXELS);

        TextField nick1 = new TextField("Nick:");
        nick1.setRequired(true);
        nick1.setImmediate(true);
        layout.addComponent(nick1);
        TextField nick2 = new TextField("Second choice:");
        nick2.setRequired(true);
        nick2.setImmediate(true);
        layout.addComponent(nick2);

        Panel p = new Panel();
        p.setCaption("User info");
        p.setSizeFull();
        p.setContent(layout);
        p.setImmediate(true);
        p.setStyleName(Reindeer.PANEL_LIGHT);

        return p;
    }

    private Panel buildToolbox() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setVisible(true);
        layout.setSpacing(true);
        layout.setWidth("100.0%");
        layout.setImmediate(true);
        layout.setHeight(40, Sizeable.UNITS_PIXELS);

        Button close = new Button("Close");
        Button connect = new Button("Connect");
        connect.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object value = networkListSelect.getValue();
                if (null == value) return;
                NetworkItem selectedItem = (NetworkItem) value;

                try {
                    mainview.getRightLayout().removeAllComponents();
                    MainApplication.getCurrent().getMainLayout().putView(IRCView.class, new IRCView());
                    mainview.getRightLayout().addComponent(MainApplication.getCurrent().getMainLayout().getAbstractView(IRCView.class));

                    MainApplication.getCurrent().setIrcClientRef(
                            new IRCClient(
                                    "irc.chatjunkies.org",
                                    6667,
                                    "075561",
                                    "gnusmas",
                                    "gnusmas",
                                    "gnusmas",
                                    false,
                                    (IRCView) MainApplication.getCurrent().getMainLayout().getAbstractView(IRCView.class),
                                    MainApplication.getCurrent().getSynchObject(),
                                    "UTF-8")
                    );

                    IRCView ircView = (IRCView) MainApplication.getCurrent().getMainLayout().getAbstractView(IRCView.class);
                    Properties props = new Properties();
                    props.put("smileys_on", "false");
                    props.put("colors_on", "false");
                    props.put("favchans", "#xchat");
                    ircView.setParameters(props);
                } catch (DisconnectFirstException e) {
                    //this.getWindow().showNotification("Disconnect first!");
                    e.printStackTrace(); // TODO!
                } catch (IOException ioe) {
                    ioe.printStackTrace(); // TODO!
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        });

        layout.addComponent(close);
        layout.setComponentAlignment(close, Alignment.MIDDLE_LEFT);
        layout.addComponent(connect);
        layout.setComponentAlignment(connect, Alignment.MIDDLE_RIGHT);

        Panel p = new Panel();
        p.setSizeFull();
        p.setContent(layout);
        p.setImmediate(true);
        p.setStyleName(Reindeer.PANEL_LIGHT);

        return p;
    }

    private Panel buildNetworkListBox() {
        FormLayout layout1 = new FormLayout();
        layout1.setSizeFull();
        layout1.setSpacing(true);
        layout1.setImmediate(true);

        networkListSelect = new ListSelect("", new NetworkContainer());
        networkListSelect.setNullSelectionAllowed(false); // user can not 'unselect'
        networkListSelect.setMultiSelect(false);
        networkListSelect.setRows(10);
        networkListSelect.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        //networkListSelect.select(networks.get(0)); // select this by default
        networkListSelect.setImmediate(true); // send the change to the server at once
        networkListSelect.addListener(this); // react when the user selects something
        networkListSelect.setItemCaptionPropertyId("networkName");

        layout1.addComponent(networkListSelect);
        layout1.setExpandRatio(networkListSelect, 1.0f);

        Panel p1 = new Panel();
        p1.setSizeFull();
        p1.setContent(layout1);
        p1.setImmediate(true);
        p1.setStyleName(Reindeer.PANEL_LIGHT);

        VerticalLayout layout2 = new VerticalLayout();
        layout2.setSizeFull();
        layout2.setSpacing(true);
        layout2.setImmediate(true);
        layout2.setMargin(true, false, false, false);

        Button b = new Button("Add");
        b.setImmediate(true);
        b.setWidth(60, Sizeable.UNITS_PIXELS);
        layout2.addComponent(b);
        layout2.setComponentAlignment(b, Alignment.TOP_LEFT);

        b = new Button("Delete");
        b.setImmediate(true);
        b.setWidth(60, Sizeable.UNITS_PIXELS);
        layout2.addComponent(b);
        layout2.setComponentAlignment(b, Alignment.TOP_LEFT);

        b = new Button("Edit");
        b.setImmediate(true);
        b.setWidth(60, Sizeable.UNITS_PIXELS);
        layout2.addComponent(b);
        layout2.setComponentAlignment(b, Alignment.TOP_LEFT);

        b = new Button("Sort");
        b.setImmediate(true);
        b.setWidth(60, Sizeable.UNITS_PIXELS);
        b.addListener(new Button.ClickListener() {

            private int i = 0;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                NetworkContainer nc = ((NetworkContainer) networkListSelect.getContainerDataSource());
                nc.sort(i % 2 == 0);
                i++;
            }
        });
        layout2.addComponent(b);
        layout2.setComponentAlignment(b, Alignment.TOP_LEFT);

        Panel p2 = new Panel();
        p2.setWidth(70, Sizeable.UNITS_PIXELS);
        p2.setContent(layout2);
        p2.setImmediate(true);
        p2.setStyleName(Reindeer.PANEL_LIGHT);

        HorizontalLayout layout3 = new HorizontalLayout();
        layout3.setSizeFull();
        layout3.addComponent(p1);
        layout3.addComponent(p2);
        layout3.setImmediate(true);
        layout3.setExpandRatio(p1, 1.0f);
        Panel p3 = new Panel();
        p3.setCaption("Networks");
        p3.setContent(layout3);
        p3.setSizeFull();
        p3.setImmediate(true);
        p3.setStyleName(Reindeer.PANEL_LIGHT);

        return p3;
    }

    /*
     * Shows a notification when a selection is made. The listener will be
     * called whenever the value of the component changes, i.e when the user
     * makes a new selection.
     */
    public void valueChange(Property.ValueChangeEvent event) {
        getWindow().showNotification("Selected network: " + event.getProperty());

    }

    @Override
    public void setParameters(Properties p) {
        //TODO
    }

    public static class NetworkItem implements Comparable<NetworkItem> {

        private String networkName;
        private String[] servers;

        public NetworkItem(String networkName, String[] servers) {
            this.networkName = networkName;
            this.servers = servers;
        }

        public String getNetworkName() {
            return networkName;
        }

        public void setNetworkName(String networkName) {
            this.networkName = networkName;
        }

        public String[] getServers() {
            return servers;
        }

        public void setServers(String[] servers) {
            this.servers = servers;
        }

        @Override
        public int compareTo(NetworkItem o) {
            return this.networkName.compareTo(o.networkName);
        }
    }

    public static class NetworkContainer extends BeanItemContainer<NetworkItem> {
        public NetworkContainer() throws IllegalArgumentException {
            super(NetworkItem.class);

            Map<String, String[]> networksMap = com.vk.vIRC.Properties.Networks.networks();

            addNestedContainerProperty("networkName");

            for (Map.Entry<String, String[]> e : networksMap.entrySet()) {
                addItem(new NetworkItem(e.getKey(), e.getValue()));
            }
        }

        @Override
        protected Collection<?> getSortablePropertyIds() {
            LinkedList<Object> propertyIds = new LinkedList<Object>();
            propertyIds.add("networkName");
            return propertyIds;
        }

        public synchronized void sort(boolean asc) {
            Collection<?> c = getSortableContainerPropertyIds();
            Object[] sortablePropertyIds = new Object[c.size()];
            boolean[] ascending = new boolean[sortablePropertyIds.length];
            int i = 0;
            for (Object o : c) {
                sortablePropertyIds[i] = o;
                ascending[i] = asc;
                i++;
            }

            sort(sortablePropertyIds, ascending);
        }


    }
}
