package com.vk.vIRC.view;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vk.vIRC.MainApplication;

import java.util.Properties;

/**
 * @author victor.konopelko
 *         Date: 20.09.11
 */
public class BasicView extends AbstractView {

    public static final String brownFox = "The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. ";

    @AutoGenerated
	private AbsoluteLayout mainLayout;
	@AutoGenerated
	private AbsoluteLayout absoluteLayout;
	@AutoGenerated
	private MenuBar menuBar_1;

    private com.vk.vIRC.Properties properties;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public BasicView() {
        properties = MainApplication.getCurrent().getProperties();

		buildMainLayout();
		setCompositionRoot(mainLayout);
		// TODO add user code here
	}

	@AutoGenerated
	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// verticalLayout_1
		absoluteLayout = buildAbsoluteLayout_1();
		mainLayout.addComponent(absoluteLayout, "top:0.0px;left:0.0px;");

		return mainLayout;
	}

	@AutoGenerated
	private AbsoluteLayout buildAbsoluteLayout_1() {
		// common part: create layout
		absoluteLayout = new AbsoluteLayout();
		absoluteLayout.setWidth("100.0%");
		absoluteLayout.setHeight("100.0%");
		absoluteLayout.setImmediate(false);
		absoluteLayout.setMargin(false);

		// menuBar_1
		menuBar_1 = new MenuBar();
		menuBar_1.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		menuBar_1.setHeight("25px");
		menuBar_1.setImmediate(false);
		absoluteLayout.addComponent(menuBar_1, "top:0.0px;left:0.0px;bottom:25.0px;");

        // Save reference to individual items so we can add sub-menu items to them
        final MenuBar.MenuItem V_IRC = menuBar_1.addItem("V-IRC", null);
          final MenuBar.MenuItem V_IRC_NETWORK_LIST = V_IRC.addItem("Network List", new NetworkListCommand(this));
          V_IRC.addSeparator();
          final MenuBar.MenuItem V_IRC_NEW = V_IRC.addItem("New", null);
            final MenuBar.MenuItem V_IRC_NEW_SERVER_TAB = V_IRC_NEW.addItem("Server Tab", null);
            final MenuBar.MenuItem V_IRC_NEW_CHANNEL_TAB = V_IRC_NEW.addItem("Channel Tab", null);
          V_IRC.addSeparator();
          final MenuBar.MenuItem V_IRC_CLOSE = V_IRC.addItem("Close", null);

        final MenuBar.MenuItem VIEW = menuBar_1.addItem("View", null);
          final MenuBar.MenuItem VIEW_TOPIC_BAR = VIEW.addItem("Topic Bar", null);
          final MenuBar.MenuItem VIEW_USER_LIST = VIEW.addItem("User List", null);
          final MenuBar.MenuItem VIEW_USER_LIST_BUTTONS = VIEW.addItem("Userlist Buttons", null);
          final MenuBar.MenuItem VIEW_MODE_BUTTONS = VIEW.addItem("Mode Buttons", null);
          VIEW_TOPIC_BAR.setCheckable(true); VIEW_TOPIC_BAR.setChecked(properties.isChecked("VIEW_TOPIC_BAR"));
          VIEW_USER_LIST.setCheckable(true); VIEW_USER_LIST.setChecked(properties.isChecked("VIEW_USER_LIST"));
          VIEW_USER_LIST_BUTTONS.setCheckable(true); VIEW_USER_LIST_BUTTONS.setChecked(properties.isChecked("VIEW_USER_LIST_BUTTONS"));
          VIEW_MODE_BUTTONS.setCheckable(true); VIEW_MODE_BUTTONS.setChecked(properties.isChecked("VIEW_MODE_BUTTONS"));

        final MenuBar.MenuItem SERVER = menuBar_1.addItem("Server", null);
          final MenuBar.MenuItem SERVER_DISCONNECT = SERVER.addItem("Disconnect", null);
          final MenuBar.MenuItem SERVER_RECONNECT = SERVER.addItem("Reconnect", null);
          final MenuBar.MenuItem SERVER_JOIN_A_CHANNEL = SERVER.addItem("Join a Channel", null);
          final MenuBar.MenuItem SERVER_LIST_OF_CHANNELS = SERVER.addItem("List of Channels", null);
          SERVER.addSeparator();
          final MenuBar.MenuItem SERVER_MARK_AS_AWAY = SERVER.addItem("Mark as \"Away\"", null);

        final MenuBar.MenuItem SETTINGS = menuBar_1.addItem("Settings", null);
        SETTINGS.setEnabled(false);

        final MenuBar.MenuItem WINDOW = menuBar_1.addItem("Window", null);
          final MenuBar.MenuItem WINDOW_BAN_LIST = WINDOW.addItem("Ban  List", null);
          final MenuBar.MenuItem WINDOW_FRIENDS_LIST = WINDOW.addItem("Friends  List", null);
          final MenuBar.MenuItem WINDOW_IGNORE_LIST = WINDOW.addItem("Ignore  List", null);
          WINDOW.addSeparator();
          final MenuBar.MenuItem WINDOW_CLEAR = WINDOW.addItem("Clear", null);
          final MenuBar.MenuItem WINDOW_SAVE_TEXT = WINDOW.addItem("Save Text", null);

        final MenuBar.MenuItem HELP = menuBar_1.addItem("Help", null);


        // Add a horizontal SplitPanel to the lower area
        final HorizontalSplitPanel horiz = new HorizontalSplitPanel();
        horiz.setSplitPosition(15); // percent
        horiz.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        horiz.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        absoluteLayout.addComponent(horiz, "top:25.0px;left:0.0px;");

        VerticalLayout left = new VerticalLayout();
        VerticalLayout right = new VerticalLayout();
        // left component:
        horiz.setFirstComponent(left);
        left.addComponent(new Label(brownFox));

        // right component:
        horiz.setSecondComponent(right);
        right.addComponent(new Label(brownFox));

		return absoluteLayout;
	}

    @Override
    public void setParameters(Properties p) {
        //TODO
    }
}
