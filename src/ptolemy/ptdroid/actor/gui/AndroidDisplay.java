/* An Android implementation of the the DisplayInterface 
 that displays input data in a text area on the screen.

 @Copyright (c) 1998-2010 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */

package ptolemy.ptdroid.actor.gui;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.lib.gui.DisplayInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

///////////////////////////////////////////////////////////////////
//// AnroidDisplay

/**
<p>
AnroidDisplay is the implementation of the DisplayInterface that uses Android java.  
Values of the tokens arriving on the input channels in a
text area on the screen.  Each input token is written on a
separate line.  The input type can be of any type.
Thus, string-valued tokens can be used to
generate arbitrary textual output, at one token per line.
</p>

@author Ishwinder Singh
@version $Id: AndroidDisplay.java 152 2011-09-12 17:59:15Z ahuseyno $
@since Ptolemy II 8.1
@Pt.ProposedRating Red (ishwinde)
@Pt.AcceptedRating Red (ishwinde)
*/

public class AndroidDisplay implements DisplayInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the string value of the token to the text area
     *  on the screen.  Each value is terminated with a newline 
     *  character.
     */
    public void display(String value) {
        Message message = _handler.obtainMessage(0, value);
        _handler.sendMessage(message);
    }

    /** Free up memory when closing. */
    public void cleanUp() {
        _textArea.setText("");
    }

    /** Get the TextArea object 
     */
    public Object getTextArea() {
        return _textArea;
    }

    /** Set the reference to the actor Display actor object.
    *
    *  @param displayActor The Display actor 
    */

    public void init(Display displayActor) throws IllegalActionException,
            NameDuplicationException {
        _display = displayActor;
    }

    /** Open the display window if it has not been opened.
     *  @exception IllegalActionException If there is a problem creating
     *  the effigy and tableau.
     */
    public void openWindow() throws IllegalActionException {
        String title = _display.title.stringValue();
        if (title.trim().equals("")) {
            title = _display.getFullName();
        }
        setTitle(title);
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of TextView and EditText will be added to that container
     *  in a vertical linear layout.
     *
     *  @param portableContainer The container into which to place the text area, or
     *   null to specify that there is no current container.
     */

    public void place(PortableContainer portableContainer) {
        ViewGroup container = (ViewGroup) (portableContainer != null ? portableContainer
                .getPlatformContainer() : null);

        LinearLayout linearLayout = new LinearLayout(container.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        _title = new TextView(container.getContext());
        _title.setBackgroundColor(Color.GRAY);
        _title.setTextColor(Color.BLACK);
        _title.setText("Display Actor");

        _textArea = new EditText(container.getContext());
        _textArea.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        _textArea.setClickable(false);

        int lHeight = LinearLayout.LayoutParams.FILL_PARENT;
        int lWidth = LinearLayout.LayoutParams.FILL_PARENT;
        linearLayout.addView(_title, new LinearLayout.LayoutParams(lWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(_textArea, new LinearLayout.LayoutParams(lWidth,
                LinearLayout.LayoutParams.FILL_PARENT));

        container.addView(linearLayout, lWidth, lHeight);

    }

    /** Remove the display from the current container, if there is one.
     */
    public void remove() {

    }

    /** Set the desired number of rows of the textArea, if there is one.
     *  
     *  @param numRows The new value of the attribute.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */

    public void setRows(int numRows) throws IllegalActionException {
    }

    /** Set the desired number of columns of the textArea, if there is one.
     *  
     *  @param numColumns The new value of the attribute.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */

    public void setColumns(int numColumns) throws IllegalActionException {
    }

    /** Set the title of the window.
     */
    public void setTitle(String stringValue) throws IllegalActionException {
        if (_title != null) {
            if (_display.title.stringValue().trim().equals("")) {
                _title.setText(stringValue);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /** Reference to the Display actor */
    private Display _display;
    /** The text area in which the data will be displayed. */
    private EditText _textArea;
    /**
     * The text view displaying display title
     */
    private TextView _title;
    /** The handler that runs on the UI thread and updates the text area
     */

    /** Count for number of lines printed.
     */
    private int _count = 100;

    private Handler _handler = new Handler() {

        public void handleMessage(Message msg) {
            if (_textArea == null) {
                return;
            }
            String text = msg.obj.toString();
            _textArea.append(text);

            // Append a newline character.
            if (text.length() > 0 || !_display.isSuppressBlankLines) {
                _textArea.append("\n");
            }

            --_count;
            if (_count < 0) {
                _textArea.setText("");
                _count = 100;
            }

        }
    };
};
