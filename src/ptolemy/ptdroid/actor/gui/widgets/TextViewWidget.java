/* Represents a TextView widget to be placed on the Android user interface
  using the place method 

 Copyright (c) 2011 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */

package ptolemy.ptdroid.actor.gui.widgets;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.TextView;

///////////////////////////////////////////////////////////////////
//// TextViewWidget

/** Represents a TextView widget to be placed on the Android user interface.
*
*  @author Ishwinder Singh
*  @version $Id: TextViewWidget.java 152 2011-09-12 17:59:15Z ahuseyno $
*  @since Ptolemy II 8.1
*  @Pt.ProposedRating Red (ishwinde)
*  @Pt.AcceptedRating Red (ishwinde)
*/

public class TextViewWidget implements PortablePlaceable,
        PtolemyAndroidWidget<String, TextWatcher> {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Return the TextView object.
     *  @return TextView object.
     */
    public TextView getTextView() {
        return _textView;
    }

    /** Place the TextView into the specified container.
     *  @param container The container in which to place the TextView.
     */
    public void place(PortableContainer container) {
        ViewGroup parent = (ViewGroup) container.getPlatformContainer();
        _textView = new TextView(parent.getContext());
        container.add(_textView);
        _textView.setWidth(parent.getWidth());
        _textView.setWidth(parent.getHeight());

    }

    /** Set the font size for the text in the TextView.
     * 
     *  @param size The new font size for the text.
     */
    public void setFontSize(float size) {
        _textView.setTextSize(size);
    }

    /** Set the text value in the TextView.
     * 
     *  @param value The text to be set in the TextView.
     */
    public void setValue(String value) {
        _textView.setText(value);
    }

    /** Add a listener to the widget. The listener should fire whenever the
     *  represented value changed.
     *  
     *  @param listener The listener object to fire when the widget's value changes.
     */
    public void addListener(TextWatcher listener) {
        _textView.addTextChangedListener(listener);
    }

    /** Remove a listener from the widget.
     *  
     *  @param listener The listener object to remove from the widget.
     */
    public void removeListener(TextWatcher listener) {
        _textView.addTextChangedListener(listener);
    }

    ////////////////////////////////////////////////////////////////    
    ////////            Private variables                 ////////////

    /** The TextView object to be placed in the container.
     */
    private TextView _textView;
}
