/* 
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

package ptolemy.ptdroid.actor.gui;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.data.Token;
import ptserver.test.SysOutActorInterface;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.EditText;

///////////////////////////////////////////////////////////////////
//// AndroidSysOutActor

/** The activity that enables the user to start, pause, resume, and stop
 *  a simulation as well as view the visual output of graphical sink actors.
 *
 *  @author Anar Huseynov
 *  @version $Id: AndroidSysOutActor.java 152 2011-09-12 17:59:15Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class AndroidSysOutActor implements SysOutActorInterface {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Notify the UI that a new token has arrived and the screen must
     *  be redrawn.
     *  
     *  @param token The token that arrived.
     */
    public void printToken(Token token) {
        Message message = _handler.obtainMessage(0, token);
        _handler.sendMessage(message);
    }

    /** Set the _view into which this actor will draw itself.
     * 
     *  @param container The container containing the _view.
     */
    public void place(PortableContainer container) {
        _view = (ViewGroup) container.getPlatformContainer();
        _text = new EditText(_view.getContext());
        _view.addView(_text);
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** Initialize the UI _handler.
     */
    private Handler _handler = new Handler() {
        public void handleMessage(Message msg) {
            if (_text != null) {
                _text.append(msg.obj + "\n");
            } else {
                System.out.println(msg.obj);
            }
            --_count;
            if (_count < 0) {
                _text.setText("");
                _count = 100;
            }
        };
    };

    /** The view containing the Android representation for the actor.
     */
    private ViewGroup _view;

    /** The Android widget for the output.
     */
    private EditText _text;

    /** Counter to clear the widget used for output after each 100 iteration.
     */
    private int _count = 100;
}
