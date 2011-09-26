package com.vk.vIRC.view;

import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

import java.util.HashMap;

/**
 * @author victor.konopelko
 *         Date: 20.09.11
 */
public class MainLayout extends VerticalLayout {

	private AbstractView currentView;

	private final HashMap<Class, AbstractView> viewInstances = new HashMap<Class, AbstractView>();

	private ProgressIndicator pi = null;

	public MainLayout() {
		this.setHeight("100%");
	}

	public MainLayout(int heightInPixels) {
	    this.setHeight(heightInPixels + "px");
	}

	// We now have push, but to be sure, we use ProgressIndicator every 10th second anyway (for keepalive)
	public void attachPoller() {
		if (pi != null) return;
		pi = new ProgressIndicator();
		pi.setIndeterminate(false);
		//pi.setVisible(false);
		pi.setEnabled(true);
		pi.setPollingInterval(10000);
		addComponent(pi);
		pi.setWidth("100%");
	}

	public void removePoller() {
		if (pi == null) return;
		removeComponent(pi);
		pi = null;
	}

	@SuppressWarnings("unchecked")
	public AbstractView getAbstractView(Class viewClass) {
		AbstractView view = null;

		if (!viewInstances.containsKey(viewClass)) {
			try {
				// create new view
				view = (AbstractView) viewClass.newInstance();
				viewInstances.put(viewClass, view);
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			// user already created view instead
			view = (AbstractView) viewInstances.get(viewClass);
		}

		return view;
	}

	public AbstractView getCurrentView(){
		return currentView;
	}

	public AbstractView setCurrentView(Class clazz) {

        AbstractView view = getAbstractView(clazz);

        if (this.currentView != null)
        {
            replaceComponent(this.currentView, view);
        }
		else
        {
            addComponent(view);
        }
		setExpandRatio(view, 1);
		this.currentView = view;
		return view;
	}

	public void clearViewMap() {
		viewInstances.clear();

	}

	public AbstractView putView(Class clazz, AbstractView view) {
		return viewInstances.put(clazz, view);
	}

}