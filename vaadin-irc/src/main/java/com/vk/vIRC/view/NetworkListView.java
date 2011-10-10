package com.vk.vIRC.view;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.VerticalLayout;

import java.util.Properties;

/**
 * @author victor.konopelko
 *         Date: 10.10.11
 */
public class NetworkListView extends AbstractView {


    private AbsoluteLayout mainLayout;
	private VerticalLayout verticalLayout;

    public NetworkListView() {

        buildMainLayout();
		setCompositionRoot(mainLayout);

    }

	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// verticalLayout_1
		verticalLayout = buildVerticalLayout();
		mainLayout.addComponent(verticalLayout, "top:0.0px;left:0.0px;");

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

    @Override
    public void setParameters(Properties p) {
        //TODO
    }
}
