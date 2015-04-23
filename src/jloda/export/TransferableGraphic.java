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

package jloda.export;

import jloda.util.Alert;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Transferable for exporting graphics to the clipboard.
 * To add a new export type, implement the <code>jloda.export.ExportGraphicType</code>
 * interface and add it to the addCommonTypes or addCustomTypes() method.
 *
 * @author huson, schr�der
 * @version $Id: TransferableGraphic.java,v 1.9 2006-06-08 04:17:38 huson Exp $
 */
public class TransferableGraphic implements ClipboardOwner, Transferable {

    /**
     * map supported <code>DataFlavor</code>s to
     * <code>jloda.export.ExportGraphicType</code>s. *
     */
    private final Map types = new HashMap();

    /**
     * the JPanel doing the paint work
     */
    private final JPanel panel;

    public TransferableGraphic(JPanel panel) {
        this(panel, null);
    }

    public TransferableGraphic(JPanel panel, JScrollPane scrollPane) {
        if (scrollPane != null)
            this.panel = ExportManager.makePanelFromScrollPane(panel, scrollPane);
        else
            this.panel = panel;
        addCommonTypes();
        //addCustomTypes();
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }


    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = new DataFlavor[types.size()];
        types.keySet().toArray(flavors);
        return flavors;
    }


    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return types.containsKey(flavor);
    }

    /**
     * get the transfer data from supported exportTypes
     *
     * @param dataFlavor the requested dataFlavor
     * @return the data to be transferred to the clipboard
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {

        ExportGraphicType type = (ExportGraphicType) types.get(dataFlavor);
        if (type != null) {
            return type.getData(panel);
        } else {
            throw new UnsupportedFlavorException(dataFlavor);
        }
    }

    /**
     * add types which don't need to be added to the native-mime mapping.
     */
    private void addCommonTypes() {

        ExportGraphicType renderedType = new RenderedExportType();
        types.put(renderedType.getDataFlavor(), renderedType);
    }

    /**
     * add exportTypes which alter the mapping of native clipboard-types
     * to mime types.
     */
    private void addCustomTypes() {

        addType("Encapsulated PostScript", "image/x-eps",
                "EPS graphic",
                "jloda.export.EPSExportType");
    }

    /**
     * add exportType to native-mime mapping.
     *
     * @param atom        name of the type in native clipboard
     * @param mimeType    the mime type
     * @param description human-readable name
     * @param className   the corresponding java class
     */
    private void addType(String atom, String mimeType, String description, String className) {

        try {
            DataFlavor df = new DataFlavor(mimeType, description);
            SystemFlavorMap map = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
            map.addUnencodedNativeForFlavor(df, atom);

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class cls = loader == null ? Class.forName(className) : loader.loadClass(className);
            ExportGraphicType type = (ExportGraphicType) cls.newInstance();
            types.put(df, type);

        } catch (Throwable x) {
            new Alert("Unable to install flavor for mime type '" + mimeType + "'");
        }
    }
}
