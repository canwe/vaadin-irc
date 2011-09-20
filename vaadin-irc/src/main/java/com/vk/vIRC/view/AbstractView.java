package com.vk.vIRC.view;

/**
 * @author victor.konopelko
 *         Date: 20.09.11
 */
import java.util.Properties;

import com.vaadin.ui.CustomComponent;

public abstract class AbstractView extends CustomComponent{

	public AbstractView() {

	}

	/**
	 *  This method can be used to transport parameters between the views, which should reduce the coupling
	 * @param p properties to set
	 */
	public abstract void setParameters(Properties p);

	//abstract public void initialize();

}