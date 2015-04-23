/*
 * $Id: PDFOutline.java,v 1.3 2007/09/22 12:58:40 gil1 Exp $
 *
 * $Date: 2007/09/22 12:58:40 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version. 
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 * Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
package gnu.jpdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class manages the documents outlines (also known as bookmarks).
 *
 * @author Peter T Mount http://www.retep.org.uk/pdf/
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.3 $, $Date: 2007/09/22 12:58:40 $
 */
public class PDFOutline extends PDFObject implements Serializable {

    /*
    * NOTE: The original class is the work of Peter T. Mount, who released it
    * in the uk.org.retep.pdf package.  It was modified by Eric Z. Beard as
    * follows:
    * The package name was changed to gnu.pdf.
    * The formatting was changed a little bit.
    * It is still licensed under the LGPL.
    */

    /**
     * This holds any outlines below us
     */
    private final Vector<PDFOutline> outlines;

    /**
     * For subentries, this points to it's parent outline
     */
    protected PDFOutline parent;

    /**
     * This is this outlines Title
     */
    private String title;

    /**
     * The destination page
     */
    PDFPage dest;

    /**
     * The region on the destination page
     */
    int l, b, r, t;

    /**
     * How the destination is handled
     */
    boolean destMode;

    /**
     * When jumping to the destination, display the whole page
     */
    static final boolean FITPAGE = false;

    /**
     * When jumping to the destination, display the specified region
     */
    static final boolean FITRECT = true;

    /**
     * Constructs a PDF Outline object. This method is used internally only.
     */
    protected PDFOutline() {
        super("/Outlines");
        outlines = new Vector<PDFOutline>();
        title = null;
        dest = null;
        destMode = FITPAGE;
    }

    /**
     * Constructs a PDF Outline object. When selected, the whole page is
     * displayed.
     *
     * @param title Title of the outline
     * @param dest  The destination page
     */
    public PDFOutline(String title, PDFPage dest) {
        this();
        this.title = title;
        this.dest = dest;
    }

    /**
     * Constructs a PDF Outline object. When selected, the specified region
     * is displayed.
     *
     * @param title Title of the outline
     * @param dest  The destination page
     * @param l     left coordinate
     * @param b     bottom coordinate
     * @param r     right coordinate
     * @param t     top coordinate
     */
    public PDFOutline(String title, PDFPage dest, int l, int b, int r, int t) {
        this(title, dest);
        this.destMode = FITRECT;
        this.l = l;
        this.b = b;
        this.r = r;
        this.t = t;
    }

    /**
     * This method creates an outline, and attaches it to this one.
     * When the outline is selected, the entire page is displayed.
     * <p/>
     * <p>This allows you to have an outline for say a Chapter,
     * then under the chapter, one for each section. You are not really
     * limited on how deep you go, but it's best not to go below say 6 levels,
     * for the reader's sake.
     *
     * @param title Title of the outline
     * @param dest  The destination page
     * @return PDFOutline object created, for creating sub-outlines
     */
    public PDFOutline add(String title, PDFPage dest) {
        PDFOutline outline = new PDFOutline(title, dest);
        pdfDocument.add(outline); // add to the pdf first!
        add(outline);
        return outline;
    }

    /**
     * This method creates an outline, and attaches it to this one.
     * When the outline is selected, the supplied region is displayed.
     * <p/>
     * <p>Note: the coordiates are in Java space. They are converted to User
     * space.
     * <p/>
     * <p>This allows you to have an outline for say a Chapter,
     * then under the chapter, one for each section. You are not really
     * limited on how deep you go, but it's best not to go below say 6 levels,
     * for the reader's sake.
     *
     * @param title Title of the outline
     * @param dest  The destination page
     * @param x     coordinate of region in Java space
     * @param y     coordinate of region in Java space
     * @param w     width of region in Java space
     * @param h     height of region in Java space
     * @return PDFOutline object created, for creating sub-outlines
     */
    public PDFOutline add(String title, PDFPage dest,
                          int x, int y, int w, int h) {
        int xy1[] = dest.cxy(x, y + h);
        int xy2[] = dest.cxy(x + w, y);
        PDFOutline outline = new PDFOutline(title, dest,
                xy1[0], xy1[1],
                xy2[0], xy2[1]);
        pdfDocument.add(outline); // add to the pdf first!
        add(outline);
        return outline;
    }

    /**
     * This adds an already existing outline to this one.
     * <p/>
     * <p>Note: the outline must have been added to the PDF document before
     * calling this method. Normally the other add methods are used.
     *
     * @param outline PDFOutline to add
     */
    public void add(PDFOutline outline) {
        outlines.addElement(outline);

        // Tell the outline of ourselves
        outline.parent = this;
    }

    /**
     * @param os OutputStream to send the object to
     * @throws IOException on error
     */
    public void write(OutputStream os) throws IOException {
        // Write the object header
        writeStart(os);

        // now the objects body

        // These are for kids only
        if (parent != null) {
            os.write("/Title ".getBytes());
            os.write(PDFStringHelper.makePDFString(title).getBytes());
            os.write("\n/Dest [".getBytes());
            os.write(dest.toString().getBytes());

            if (destMode == FITPAGE) {
                //os.write(" null null null]\n/Parent ".getBytes());
                os.write(" /Fit]\n/Parent ".getBytes());
            } else {
                os.write(" /FitR ".getBytes());
                os.write(Integer.toString(l).getBytes());
                os.write(" ".getBytes());
                os.write(Integer.toString(b).getBytes());
                os.write(" ".getBytes());
                os.write(Integer.toString(r).getBytes());
                os.write(" ".getBytes());
                os.write(Integer.toString(t).getBytes());
                os.write("]\n/Parent ".getBytes());
            }
            os.write(parent.toString().getBytes());
            os.write("\n".getBytes());
        }

        // the number of outlines in this document
        if (parent == null) {
            // were the top level node, so all are open by default
            os.write("/Count ".getBytes());
            os.write(Integer.toString(outlines.size()).getBytes());
            os.write("\n".getBytes());
        } else {
            // were a decendent, so by default we are closed. Find out how many
            // entries are below us
            int c = descendants();
            if (c > 0) {
                os.write("/Count ".getBytes());
                os.write(Integer.toString(-c).getBytes());
                os.write("\n".getBytes());
            }
        }

        // These only valid if we have children
        if (outlines.size() > 0) {
            // the number of the first outline in list
            os.write("/First ".getBytes());
            os.write(outlines.elementAt(0).toString().getBytes());
            os.write("\n".getBytes());

            // the number of the last outline in list
            os.write("/Last ".getBytes());
            os.write(outlines.elementAt(outlines.size() - 1).toString().getBytes());
            os.write("\n".getBytes());
        }

        if (parent != null) {
            int index = parent.getIndex(this);
            if (index > 0) {
                // Now if were not the first, then we have a /Prev node
                os.write("/Prev ".getBytes());
                os.write(parent.getNode(index - 1).toString().getBytes());
                os.write("\n".getBytes());
            }
            if (index < parent.getLast()) {
                // We have a /Next node
                os.write("/Next ".getBytes());
                os.write(parent.getNode(index + 1).toString().getBytes());
                os.write("\n".getBytes());
            }
        }

        // finish off with its footer
        writeEnd(os);
    }

    /**
     * This is called by children to find their position in this outlines
     * tree.
     *
     * @param outline PDFOutline to search for
     * @return index within Vector
     */
    protected int getIndex(PDFOutline outline) {
        return outlines.indexOf(outline);
    }

    /**
     * Returns the last index in this outline
     *
     * @return last index in outline
     */
    protected int getLast() {
        return outlines.size() - 1;
    }

    /**
     * Returns the outline at a specified position.
     *
     * @param i index
     * @return the node at index i
     */
    protected PDFOutline getNode(int i) {
        return outlines.elementAt(i);
    }

    /**
     * Returns all outlines directly below this one.
     *
     * @return Enumeration of child elements
     */
    public Enumeration<PDFOutline> elements() {
        return outlines.elements();
    }

    /**
     * Returns the total number of descendants below this one.
     *
     * @return the number of descendants below this one
     */
    protected int descendants() {
        int c = outlines.size(); // initially the number of kids

        // now call each one for their descendants
        for (PDFOutline o : outlines) {
            c += o.descendants();
        }

        return c;
    }
} // end class PDFOutline
