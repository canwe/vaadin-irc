package com.vk.vIRC.client;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vk.vIRC.CommandNotAllowedException;
import com.vk.vIRC.MainApplication;
import com.vk.vIRC.util.StringParser;
import com.vk.vIRC.view.dialogs.OKDialog;

/**
 * The user writes text in this component and it will be sent to the IRC server
 * @author johan
 *
 */

public class IRCInputTextFieldComponent extends CustomComponent implements Handler {

	private TextField inputTextField;

//	private Button sendButton;

	private Panel panel;
	
	private IRCEventManager.FromUserManager fromUser = null;
	
	private IRCWindow ircWindow;

	private TextHistory textHistory;

	private TabCompletionSource toTabComplete = null;

	private Button sendButton;
	
	public IRCInputTextFieldComponent(IRCWindow ircWindow){
		VerticalLayout ol = new VerticalLayout();
		this.ircWindow = ircWindow;
		panel = new Panel(ol);
		panel.setStyleName(Panel.STYLE_LIGHT);
		setCompositionRoot(panel);
		setHeight("120px");
		//panel.setO
		ol.setMargin(true);
		ol.setSpacing(true);
		sendButton = new Button("Send");
		
		HorizontalLayout ol2 = new HorizontalLayout();
		ol2.setSpacing(true);
		ol2.addComponent(inputTextField = new TextField());
		ol2.addComponent(sendButton);
		sendButton.setStyleName(Button.STYLE_LINK);
		sendButton.setIcon(new ThemeResource("../tkirc/images/say_2.png"));
		panel.addComponent(ol2);
		ol2.setWidth("100%");
		ol2.setExpandRatio(inputTextField, 1.0f);
		inputTextField.setWidth("100%");
		ol2.requestRepaint();
		panel.addActionHandler(this);
		textHistory = new TextHistory(200);

		sendButton.addListener(new ClickListener(){

			
			public void buttonClick(ClickEvent event) {
				sendText();
				focus();
			}
			
		});

	}

	
	public Action[] getActions(Object target, Object sender) {
		Action[] actions = new Action[7];

		// Set the action for the requested component 
		if (sender == panel) {
			actions[0] = new ShortcutAction("Send",
					ShortcutAction.KeyCode.ENTER, null);
			actions[1] = new ShortcutAction("Tab Completion",
					ShortcutAction.KeyCode.TAB, null);
			actions[2] = new ShortcutAction("Last history",
					ShortcutAction.KeyCode.ARROW_UP, null);
			actions[3] = new ShortcutAction("Next history",
					ShortcutAction.KeyCode.ARROW_DOWN, null);
			
			actions[4] = new ShortcutAction("CTRL+C",
					ShortcutAction.KeyCode.C, new int[]{
					ShortcutAction.ModifierKey.CTRL});
			actions[5] = new ShortcutAction("CTRL+B",
					ShortcutAction.KeyCode.B, new int[]{
					ShortcutAction.ModifierKey.CTRL});
			actions[6] = new ShortcutAction("CTRL+U",
					ShortcutAction.KeyCode.U, new int[]{
					ShortcutAction.ModifierKey.CTRL});
		} else
			return null;
		return actions;
	}

/*
 *          [15:29:53] Artur Signell:

            textField.addShortcutListener(new ShortcutListener(null,
                    KeyCode.ENTER, null) {

                @Override
                public void handleAction(Object sender, Object target) {
                    // Enter was pushed when TextField was focused

                }
            });
 */

	public void handleAction(Action action, Object sender, Object target) {
		if (sender == panel) {
			if (action.getCaption().equalsIgnoreCase("Send")) {
				sendText();
			}
			else if (action.getCaption().equalsIgnoreCase("Tab Completion")){
				String sendText = (String)inputTextField.getValue();
				if (fromUser != null && toTabComplete != null){
					String lastWord = StringParser.getLastWordFromSentence(sendText);
					String changeTo = toTabComplete.getNextTabbable(lastWord);
					inputTextField.setValue(StringParser.replaceLastWordInSentence(sendText, changeTo));
				} 
			}
			else if (action.getCaption().equalsIgnoreCase("Last history")){
				inputTextField.setValue(textHistory.getPreviousText());
			}
			else if (action.getCaption().equalsIgnoreCase("Next history")){
				inputTextField.setValue(textHistory.getNextText());
			}
		}
	}

	private void sendText() {
		try {
			String sendText = (String)inputTextField.getValue();
			if (sendText.equals("")) return;
			textHistory.addText(sendText);
			if (fromUser != null){
				if (sendText.trim().startsWith("/")) {
					String targName = null;
					try {
						targName = this.ircWindow.getTargetName();
					} catch (Exception e){}

					fromUser.executeLocalAction(sendText, targName);
					inputTextField.setValue("");
				}
				else {
					fromUser.doPrivmsg(ircWindow.getTargetName(), sendText);
					inputTextField.setValue("");
				}
			}
		} catch (CommandNotAllowedException e){
			String text = "<b>" + e.getMessage() + "</b><br />" + "You can download the full-featured IRC Client.";
			OKDialog okDialog = new OKDialog("Restricted Client Notification", text);
			okDialog.setModal(true);
			okDialog.setWidth("590px");
			MainApplication.getCurrent().getMainWindow().addWindow(okDialog);
		}

	}

	public void addFromUserManager(IRCEventManager.FromUserManager fromUser) {
		this.fromUser = fromUser;
	}

	public void focus(){
		inputTextField.focus();
	}
	
	//  TabCompletionsources... if no source added, then tab completion will just simply not work
	// this means, that the user is not forced to implement tab completion sources
	
	public void addUserTabCompletionSource(TabCompletionSource toTabComplete){
		this.toTabComplete = toTabComplete;
	}
	
	public void appendText(String s){
		inputTextField.setValue(inputTextField.getValue().toString() + s);
	}
	
	public String getCurrentText(){
		if (inputTextField.getValue() == null) return null;
		return inputTextField.getValue().toString();
	}
	
/*	
	public void addActionTabCompletionSource(TabCompletionSource toTabComplete){
		if (tab pressed, then) tabComplete.getCurrentUsers()...
	}
	
*/

	
}
