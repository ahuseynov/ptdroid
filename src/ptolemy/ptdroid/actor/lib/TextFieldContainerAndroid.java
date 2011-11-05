/* Android implementation of the TextFieldContainerInterface 
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

package ptolemy.ptdroid.actor.lib;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.lib.TextFieldContainerInterface;
import ptolemy.data.Token;
import android.view.ViewGroup;
import android.widget.EditText;

///////////////////////////////////////////////////////////////////
////TextFieldContainerInterface

/** Android implementation of the TextFieldContainerInterface.
 *
 *  @author Ishwinder Singh
 *  @version $Id: TextFieldContainerAndroid.java 158 2011-10-09 15:38:46Z jkillian $
 *  @since @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ishwinde)
 *  @Pt.AcceptedRating Red (ishwinde)
 */
public class TextFieldContainerAndroid implements TextFieldContainerInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Place the visual representation of the actor into the specified container.
     *  @param container The container in which to place the object
     */
    public void place(PortableContainer container) {
        ViewGroup parent = (ViewGroup) container.getPlatformContainer();
        _editText = new EditText(parent.getContext());

        container.add(_editText);
        _editText.setClickable(false);
    }

    /** Set the text to the value of the parameter.
     *  @param value The Parameter containing the value
     */
    public synchronized void setValue(Token value) {
        _lastToken = value;
        if (!_messagePosted) {
            if (_editText != null) {
                _editText.post(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (TextFieldContainerAndroid.this) {
                            _messagePosted = false;
                            _editText.setText(_lastToken.toString());
                        }
                    }
                });

                _messagePosted = true;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Text field for displaying the value
     */
    private EditText _editText;

    /** Last token received.
     */
    private Token _lastToken;

    /** Whether or not the message has been posted yet.
     */
    private boolean _messagePosted = false;
}
