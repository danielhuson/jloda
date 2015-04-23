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

package jloda.gui.director;

import javax.swing.*;

/**
 * main viewer interface
 * Daniel Huson, 3.2007
 */
public interface IMainViewer extends IDirectableViewer {
    /**
     * gets the window menu
     *
     * @return window menu
     */
    public JMenu getWindowMenu();

    /**
     * get the quit action
     *
     * @return quit action
     */
    public AbstractAction getQuit();
}
