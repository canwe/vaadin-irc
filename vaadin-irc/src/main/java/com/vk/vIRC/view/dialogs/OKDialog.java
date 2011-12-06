package com.vk.vIRC.view.dialogs;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.CloseListener;

public class OKDialog  extends Window implements ClickListener, CloseListener{
	
	private HorizontalLayout hl;
	
	public OKDialog(String caption, String displayText){
		super(caption);
		this.setContent(new VerticalLayout());
		Embedded e = new Embedded("", new ThemeResource("../tkirc/images/info.png"));
		hl = new HorizontalLayout();
		Label l = new Label(displayText, Label.CONTENT_XHTML);
		hl.addComponent(e);
		hl.addComponent(l);
		hl.setExpandRatio(l, 1);
		hl.setWidth("100%");
		addComponent(hl);
		Button b = new Button("Ok");
		
		addComponent(b);
		b.addListener(this);
		((VerticalLayout)getContent()).setComponentAlignment(b, Alignment.MIDDLE_CENTER);
		((VerticalLayout)getContent()).setMargin(true);
	}

	
	@Override
	public void buttonClick(ClickEvent event) {
		this.removeAllComponents();
        if (getParent() != null) {
            ((Window) getParent()).removeWindow(this);
        }
		
	}

	@Override
	public void windowClose(CloseEvent e) {
		this.removeAllComponents();
        if (getParent() != null) {
            ((Window) getParent()).removeWindow(this);
        }
	}

}
