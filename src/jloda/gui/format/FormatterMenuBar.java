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

package jloda.gui.format;

/**
 * Formatter menu bar
 * Daniel Huson, 7.2010
 *
 */

import jloda.gui.director.IDirector;
import jloda.util.MenuMnemonics;
import jloda.util.ProgramProperties;

import javax.swing.*;


/**
 * menubar for node/edge configurator
 */
public class FormatterMenuBar extends JMenuBar {
    private final Formatter conf;
    private IDirector dir;

    /**
     * construtor
     *
     * @param conf
     * @param dir
     */
    public FormatterMenuBar(Formatter conf, IDirector dir) {
        super();

        this.conf = conf;
        this.dir = dir;

        addFileMenu();
        addEditMenu();
        addOptionsMenu();
    }

    public void setViewer(IDirector dir) {
        this.dir = dir;
    }

    /**
     * returns the tool bar for this simple viewer
     */
    private void addFileMenu() {
        JMenu menu = new JMenu("File");

        // viewer version opens new browser, dir version doesn't
        // menu.add(viewer.getActions().getOpenFile());

        //menu.addSeparator();

        menu.add(conf.getActions().getClose());
        if (!ProgramProperties.isMacOS()) {
            menu.addSeparator();
            menu.add(dir.getMainViewer().getQuit());
        }
        MenuMnemonics.setMnemonics(menu);
        add(menu);
    }

    private void addEditMenu() {
        JMenu menu = new JMenu("Edit");

        //menu.addSeparator();
        JMenuItem menuItem = new JMenuItem(conf.getActions().getCut());
        menuItem.setText("Cut");
        menu.add(menuItem);
        menuItem = new JMenuItem(conf.getActions().getCopy());
        menuItem.setText("Copy");
        menu.add(menuItem);
        menuItem = new JMenuItem(conf.getActions().getPaste());
        menuItem.setText("Paste");
        menu.add(menuItem);
        menu.addSeparator();
        //menuItem = new JMenuItem(viewer.getActions().getSelectAll());
        //menuItem.setText("Select All");
        menu.add(menuItem);
        MenuMnemonics.setMnemonics(menu);
        add(menu);
    }

    private void addOptionsMenu() {
        JMenu menu = new JMenu("Options");
        menu.add(conf.getActions().getSaveDefaultFont());
        MenuMnemonics.setMnemonics(menu);
        add(menu);
    }
}



