package com.vk.vIRC.view;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.CloseListener;

/**
 * This dialog is ripped (and slightly modified) from http://forum.itmill.com/posts/list/532.page
 */

public class GeneralDialog extends Window implements ClickListener, CloseListener {


    Callback callback;
    Button buttons[];
	private String optional;
	private int onCloseOption;

	/**
	 *
	 * @param caption Caption of the window
	 * @param options Array of strings, the captions of the buttons
	 * @param question The question (or statement) the dialog will show
	 * @param callback a callback interface (GeneralDialog.Callback)
	 * @param sizex The amount of buttons in a row
	 * @param sizey Not so important, but specifies the amount of rows :)
	 * @param optional This is an optional string that can be used as a store for some data and
	 * be returned through the callback interface
	 * @param onCloseOption the default option. If your cancelbutton is first, and you want
	 * cancel to be the default behaviour, set it to 0.
	 */

    public GeneralDialog(String caption,
                         String[] options,
                         String question,
                         Callback callback,
                         int sizex,
                         int sizey,
                         String optional,
                         int onCloseOption) {
        super(caption);
        this.optional = optional;
        //setModal(true);

        this.callback = callback;
        this.onCloseOption = onCloseOption;
        this.addListener((CloseListener)this);

        if (question != null) {
        	Label l = new Label(question);
        	l.setContentMode(Label.CONTENT_RAW);
            addComponent(l);
        }

        buttons = new Button[options.length];
        GridLayout gl = new GridLayout(sizex, sizey);
        gl.setSpacing(true);
        gl.setMargin(true);
        for (int i=0; i<options.length; i++){
            buttons[i] = new Button(options[i]);
            gl.addComponent(buttons[i]);
            buttons[i].addListener(this);
        }

        addComponent(gl);
    }

    public void buttonClick(ClickEvent event) {

        if (getParent() != null) {
            ((Window) getParent()).removeWindow(this);
        }
        for (int i=0; i<buttons.length; i++){
            if (event.getSource() == buttons[i]) callback.onDialogResult(i, optional);

        }
    }

    public interface Callback {
        public void onDialogResult(int result, String optional);
    }


	public void windowClose(CloseEvent e) {
		this.removeAllComponents();
		callback.onDialogResult(onCloseOption, optional);

	}

}