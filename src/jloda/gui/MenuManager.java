/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.gui;

import jloda.util.MenuMnemonics;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import jloda.util.lang.Translator;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * class for creating and managing menus
 * Daniel Huson, 8.2006
 */
public class MenuManager {
    public final static String MENUBAR_TAG = "MenuBar";
    // action values:
    public final static String CHECKBOXMENUITEM = "CheckBox";
    public final static String ALT_NAME = "ALT_NAME"; // alternative name recognized
    public final static String ALT_TEXT = "ALT_TEXT"; // alternative menu-item text

    private final SortedMap<String, Action> actions = new TreeMap<String, Action>();

    /**
     * constructor
     */
    public MenuManager() {

    }

    /**
     * add a collection of actions to the list of available actions
     *
     * @param actions
     */
    public void addAll(Collection<Action> actions) {
        for (Action action : actions) {
            String label = (String) action.getValue(ALT_NAME);
            if (label == null)
                label = (String) action.getValue(Action.NAME);
            if (label != null)
                this.actions.put(label, action);
        }
    }

    /**
     * add an action
     *
     * @param label
     * @param action
     */
    public void add(String label, Action action) {
        actions.put(label, action);
    }

    /**
     * remove the action corresponding to the given label
     *
     * @param label
     */
    public void remove(String label) {
        actions.keySet().remove(label);
    }

    /**
     * remove a collection of actions
     *
     * @param actions
     */
    public void removeAll(Collection<Action> actions) {
        for (Action action : actions) {
            String label = (String) action.getValue(ALT_NAME);
            if (label == null)
                label = (String) action.getValue(Action.NAME);
            if (label != null)
                remove(label);
        }
    }

    /**
     * builds a menu bar from a set of description lines.
     * Description must contain one menu bar line in the format:
     * MenuBar.menuBarLabel=item;item;item...;item, where menuBarLabel must match the
     * given name and each item is of the form Menu.menuBarLabel or simply menuBarLabel,
     * Used in Dendroscope
     *
     * @param menuBarLabel
     * @param descriptions
     * @param menuBar
     * @throws Exception
     */
    public void buildMenuBar(String menuBarLabel, Hashtable descriptions, JMenuBar menuBar) throws Exception {
        /*
        System.err.println("Known actions:");
        for (Iterator it = actions.keySet().iterator(); it.hasNext();) {
            System.err.println(it.next());
        }
         */

        menuBarLabel = MENUBAR_TAG + "." + menuBarLabel;
        if (!descriptions.keySet().contains(menuBarLabel))
            throw new Exception("item not found: " + menuBarLabel);

        List<String> menuLabels = getTokens((String) descriptions.get(menuBarLabel));

        for (String menuLabel : menuLabels) {
            if (!menuLabel.startsWith("Menu."))
                menuLabel = "Menu." + menuLabel;

            if (descriptions.keySet().contains(menuLabel)) {
                JMenu menu = buildMenu(menuLabel, descriptions, false);
                addSubMenus(0, menu, descriptions);
                MenuMnemonics.setMnemonics(menu);
                menuBar.add(menu);
            }
        }
    }

    /**
     * builds a menu from a description.
     * Format:
     * Menu.menuLabel=name;item;item;...;item;  where  name is menu name
     * and item is either the menuLabel of an action, | to indicate a separator
     * or @menuLabel to indicate menuLabel name of a submenu
     *
     * @param menuLabel
     * @param descriptions
     * @param addEmptyIcon
     * @return menu
     * @throws Exception
     */
    private JMenu buildMenu(String menuLabel, Hashtable descriptions, boolean addEmptyIcon) throws Exception {
        if (!menuLabel.startsWith("Menu."))
            menuLabel = "Menu." + menuLabel;
        String description = (String) descriptions.get(menuLabel);
        if (description == null)
            return null;
        List menuDescription = getTokens(description);
        if (menuDescription.size() == 0)
            return null;
        boolean skipNextSeparator = false;  // avoid double separators
        Iterator it = menuDescription.iterator();
        String menuName = (String) it.next();
        JMenu menu = new JMenu(Translator.get(menuName));
        if (addEmptyIcon)
            menu.setIcon(ResourceManager.getIcon("Empty16.gif"));
        String[] labels = (String[]) menuDescription.toArray(new String[menuDescription.size()]);
        for (int i = 1; i < labels.length; i++) {
            String label = labels[i];
            if (i == labels.length - 2 && ProgramProperties.isMacOS() && label.equals("|") && labels[i + 1].equals("Quit"))
                skipNextSeparator = true; // avoid separator at bottom of File menu in mac version

            if (skipNextSeparator && label.equals("|")) {
                skipNextSeparator = false;
                continue;
            }
            skipNextSeparator = false;

            if (label.startsWith("@")) {
                JMenu subMenu = new JMenu(Translator.get(label));
                menu.add(subMenu);
            } else if (label.equals("|")) {
                menu.addSeparator();
                skipNextSeparator = true;
            } else {
                final Action action = actions.get(label);
                if (action != null) {
                    boolean done = false;
                    if (ProgramProperties.isMacOS()) {
                        if (label.equals("Quit")) {
                            AppleStuff.getInstance().setQuitAction(action);
                            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) == null) {
                                skipNextSeparator = true;
                            }
                            done = true;
                        } else if (label.equals("About") || label.equals("About...")) {
                            AppleStuff.getInstance().setAboutAction(action);
                            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) == null) {
                                skipNextSeparator = true;
                            }
                            done = true;
                        } else if (label.equals("Preferences") || label.equals("Preferences...")) {
                            AppleStuff.getInstance().setPreferencesAction(action);
                            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) == null) {
                                skipNextSeparator = true;
                            }
                            done = true;
                        }
                    }


                    if (!done) {
                        if (action.getValue(CHECKBOXMENUITEM) != null) {
                            JCheckBoxMenuItem cbox = (JCheckBoxMenuItem) action.getValue(CHECKBOXMENUITEM);
                            cbox.setAction(action);
                            cbox.setName(Translator.get((String) action.getValue(Action.NAME)));
                            cbox.setToolTipText(Translator.get((String) action.getValue(Action.SHORT_DESCRIPTION)));
                            menu.add(cbox);
                        } else {
                            menu.add(action);
                        }
                        JMenuItem item = menu.getItem(menu.getItemCount() - 1);
                        // the following makes sure the alt-name is used, if present
                        if (action.getValue(ALT_NAME) != null)
                            item.setText(Translator.get((String) action.getValue(ALT_NAME)));
                        else
                            item.setText(Translator.get((String) action.getValue(Action.NAME)));
                        item.setToolTipText(Translator.get((String) action.getValue(Action.SHORT_DESCRIPTION)));
                    }
                    // always add empty icon, if non is given
                    if (action.getValue(AbstractAction.SMALL_ICON) == null)
                        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Empty16.gif"));
                } else {
                    menu.add(label + " #");
                    menu.getItem(menu.getItemCount() - 1).setEnabled(false);
                }
            }
        }
        return menu;
    }

    /**
     * adds submenus to a menu
     *
     * @param depth
     * @param menu
     * @param descriptions
     * @throws Exception
     */
    private void addSubMenus(int depth, JMenu menu, Hashtable descriptions) throws Exception {
        if (depth > 5)
            throw new Exception("Submenus: too deep: " + depth);
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null && item.getText() != null && item.getText().startsWith("@")) {
                String name = item.getText().substring(1);
                item.setText(name);
                JMenu subMenu = buildMenu(name, descriptions, true);
                if (subMenu != null) {
                    addSubMenus(depth + 1, subMenu, descriptions);
                    menu.remove(i);
                    menu.add(subMenu, i);
                }
            }
        }
    }

    /**
     * find named menu
     *
     * @param name
     * @param menuBar
     * @param mayBeSubmenu also search for sub menu
     * @return menu or null
     */
    public static JMenu findMenu(String name, JMenuBar menuBar, boolean mayBeSubmenu) {
        name = Translator.get(name, false);
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu result = findMenu(name, menuBar.getMenu(i), mayBeSubmenu);
            if (result != null)
                return result;
        }
        return null;
    }

    /**
     * searches for menu by name
     *
     * @param name
     * @param menu
     * @param mayBeSubmenu
     * @return menu or null
     */
    public static JMenu findMenu(String name, JMenu menu, boolean mayBeSubmenu) {
        name = Translator.get(name, false);
        if (menu.getText().equals(name))
            return menu;
        if (mayBeSubmenu) {
            for (int j = 0; j < menu.getItemCount(); j++) {
                JMenuItem item = menu.getItem(j);
                if (item != null) {
                    Component comp = item.getComponent();
                    if (comp instanceof JMenu) {
                        JMenu result = findMenu(name, (JMenu) comp, mayBeSubmenu);
                        if (result != null)
                            return result;
                    }
                }
            }
        }
        return null;
    }


    /**
     * get the list of tokens in a description
     *
     * @param str
     * @return list of tokens
     * @throws Exception
     */
    static public List<String> getTokens(String str) throws Exception {
        try {
            int pos = str.indexOf("=");
            str = str.substring(pos + 1).trim();
            StringTokenizer tokenizer = new StringTokenizer(str, ";");
            List<String> result = new LinkedList<String>();
            while (tokenizer.hasMoreTokens())
                result.add(tokenizer.nextToken());
            return result;
        } catch (Exception ex) {
            throw new Exception("failed to parse description-line: <" + str + ">: " + ex);
        }
    }

    /**
     * use reflection to make all actions by calling all methods that
     * return an action
     *
     * @param actions
     */
    public static void makeAllActions(Object actions) {
        Method[] methods = actions.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get")
                    && (method.getReturnType().isAssignableFrom(Action.class)
                    || method.getReturnType().isAssignableFrom(AbstractAction.class))) {
                try {
                    //System.err.println("invoking: " + method.getName());
                    method.invoke(actions, new Object());
                } catch (Exception ex) {
                    // try again with a check box
                    try {
                        JCheckBoxMenuItem cbox = new JCheckBoxMenuItem();
                        method.invoke(actions, cbox);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}

