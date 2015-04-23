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

package jloda.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * remembering combo box
 * bryant, huson
 * Date: Feb 1, 2006
 */
public class RememberingComboBox extends JComboBox<String> {
    private final BasicComboBoxEditor editor;

    /**
     * constructor
     */
    public RememberingComboBox() {
        super();
        setEnabled(true);
        setEditable(true);
        //clearHistory();

        editor = new BasicComboBoxEditor();
        setEditor(editor);

        editor.getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent keyEvent) {
                if (!RememberingComboBox.this.getBackground().equals(Color.WHITE))
                    RememberingComboBox.this.setBackground(Color.WHITE);
            }
        });
    }

    public Color getBackground() {
        if (editor != null)
            return editor.getEditorComponent().getBackground();
        else
            return super.getBackground();
    }

    public void setBackground(Color background) {
        if (editor != null)
            editor.getEditorComponent().setBackground(background);
        else
            super.setBackground(background);
    }

    public Color getForeground() {
        if (editor != null)
            return editor.getEditorComponent().getForeground();
        else
            return super.getForeground();
    }

    public void setForeground(Color foreground) {
        if (editor != null)
            editor.getEditorComponent().setForeground(foreground);
        else
            super.setForeground(foreground);
    }

    /**
     * Gets the current typed text. If save is true, then this is inserted into the list, after removing any
     * duplicate entries.
     *
     * @param save
     * @return current text
     */
    public String getCurrentText(boolean save) {
        String newEntry = null;
        if (getSelectedItem() != null)
            newEntry = getSelectedItem().toString();

        if (newEntry == null)
            newEntry = "";
        if (save && newEntry.length() > 0) {
            //Check to see if it already appears. If it does, remove, as well as any null entires. Then insert item at start of list.
            int index = 0;
            while (index < getItemCount()) {
                String thisEntry = getItemAt(index);
                if (thisEntry.length() == 0)
                    removeItemAt(index);
                else if (thisEntry.equals(newEntry))
                    removeItemAt(index);
                else
                    index++;
            }

            insertItemAt(newEntry, 0);
            setSelectedIndex(0);
        }
        return newEntry;
    }

    /**
     * Removes all entries and sets current text to ""
     */
    public void clearHistory() {
        removeAllItems();
        addItem("");
    }

    public void addItems(List<String> items) {
        for (String item : items) addItem(item);
    }

    /**
     * gets the list of items
     *
     * @param maxNumber
     * @return first maxNumber items
     */
    public List<String> getItems(int maxNumber) {
        List<String> list = new LinkedList<>();
        for (int i = 0; i < Math.min(maxNumber, this.getItemCount()); i++) {
            list.add(getItemAt(i));
        }
        return list;
    }

    /**
     * add a list of sep-separated items
     *
     * @param str
     * @param sep
     */
    public void addItemsFromString(String str, String sep) {
        if (str != null && str.length() > 0)
            for (StringTokenizer tok = new StringTokenizer(str, sep); tok.hasMoreElements(); ) {
                String string = tok.nextToken();
                if (string.length() > 0)
                    addItem(string);
            }
        //  if (getItemCount() > 0)
        //    setSelectedIndex(0);
    }

    /**
     * gets the list of items as a sep-separated string
     *
     * @param maxNumber
     * @param sep
     * @return first maxNumber items
     */
    public String getItemsAsString(int maxNumber, String sep) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < Math.min(maxNumber, this.getItemCount()); i++) {
            buf.append(getItemAt(i));
            buf.append(sep);
        }
        return buf.toString();
    }
}
