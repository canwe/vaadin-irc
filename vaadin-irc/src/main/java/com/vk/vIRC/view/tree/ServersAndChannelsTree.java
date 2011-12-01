package com.vk.vIRC.view.tree;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Tree;
import com.vk.vIRC.view.NetworkListView;

/**
 * @author victor.konopelko
 *         Date: 30.11.11
 */
public class ServersAndChannelsTree extends Tree {

    /**
	 * Field <code>serialVersionUID</code>.
	 */
	private final static long   serialVersionUID = 1L;

	private final static String CAPTION_PROPERTY = "CAPTION";

    public final  static  String ROOT_CAPTION = "Servers";

    public final Object ROOT_ID;

    /**
     * Constructor NavigationTree.
     */
    public ServersAndChannelsTree() {

    	setImmediate(true);
        setSizeFull();

        // we'll use a property for caption instead of the item id ("value"),
        // so that multiple items can have the same caption
        addContainerProperty(CAPTION_PROPERTY, String.class, "");

        setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        setItemCaptionPropertyId(CAPTION_PROPERTY);

        ROOT_ID = addCaptionedItem(this, ROOT_CAPTION, null, true);
        expandItem(ROOT_ID);

        /*
         * We want items to be selectable
         */
        setSelectable(true);

        /*
         * We want the user to be able to de-select an item.
         */
        setNullSelectionAllowed(true);

        /*
         * We want the user to be able to select more than one item.
         */
        setMultiSelect(false);
    }

    /**
     * Adds server tab for given tree with specified options
     *
     * @param server server
     * @return id of the item in tree
     */
    public synchronized Object addServerTab(NetworkListView.NetworkItem server) {
        return addCaptionedItem(this, server.getNetworkName(), ROOT_ID, true);
    }

    /**
     * Adds channel tab for given tree with specified options
     *
     * @param serverId - parent server id in tree
     * @param name - name of the channel
     * @return id of the item in tree
     */
    public synchronized Object addChannelTab(Object serverId, String name) {
        return addCaptionedItem(this, name, serverId, false);
    }

    /**
     * Adds channel tab for given tree with specified options
     *
     * @param name - the name of the channel
     * @return id of the item in tree
     */
    public synchronized Object addChannelTab(String name) {

        // find selected server tab
        Object itemId = this.getValue();

        // if tried to add channel to root of the tree
        if (null == itemId || ROOT_ID.equals(itemId)) return null;

        if (ROOT_ID.equals(this.getParent(itemId))) {
            return addChannelTab(itemId, name);
        } else {
            return addChannelTab(this.getParent(itemId), name);
        }
    }

    /**
     * Adds item for given tree with specified options
     *
     * @param tree destination tree
     * @param caption used caption
     * @param parent parent item
     * @param childrenAllowed whether children items is allowed or not
     * @return id of the item in tree
     */
    private Object addCaptionedItem(Tree     tree,
    		                        String   caption,
    		                        Object   parent,
    		                        boolean  childrenAllowed) {
        // add item, let tree decide id
        final Object id = tree.addItem();
        // get the created item
        final Item item = tree.getItem(id);
        // set our "caption" property
        final Property p = item.getItemProperty(CAPTION_PROPERTY);
        p.setValue(caption);

        tree.setChildrenAllowed(id, childrenAllowed);

        if (parent != null) {
            tree.setParent(id, parent);
        }
        return id;
    }
}
