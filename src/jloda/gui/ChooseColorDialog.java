/**
 * ChooseColorDialog.java 
 * Copyright (C) 2015 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jloda.gui;

import jloda.util.Single;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * choose a color
 * Daniel Huson, 4.2011
 */
public class ChooseColorDialog {
    private final static JColorChooser chooserPane = new JColorChooser();

    /**
     * show a choose color dialog
     *
     * @param parent
     * @param title
     * @param defaultColor
     * @return color chosen or null
     */
    public static Color showChooseColorDialog(JFrame parent, String title, Color defaultColor) {
        if (defaultColor != null)
            chooserPane.setColor(defaultColor);

        final Single<Color> result = new Single<>();

        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                result.set(chooserPane.getColor());
            }
        };

        ActionListener cancelListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                result.set(null);
            }
        };

        JDialog chooser = JColorChooser.createDialog(parent, title, true, chooserPane, okListener, cancelListener);
        chooser.setVisible(true);

        return result.get();
    }
}
