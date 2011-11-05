/* Delegate value changes of a settable object to its Android
 representation.
 
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

package ptolemy.ptdroid.simulation;

import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.ptdroid.actor.gui.widgets.PtolemyAndroidWidget;

///////////////////////////////////////////////////////////////////
//// AndroidValueListener

/** Delegate value changes of a settable object to its Android representation.
 *
 *  @author Peter Foldes
 *  @version $Id: AndroidValueListener.java 152 2011-09-12 17:59:15Z ahuseyno $
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class AndroidValueListener implements ValueListener {

    /** Initialize the instance with the given token publisher
     *  and enable its listener.
     *  
     *  @param widget The Android representation of the settable. 
     */
    public AndroidValueListener(PtolemyAndroidWidget widget) {
        setEnabled(true);
        _androidWidget = widget;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Capture value changes of the settable and send them via the publisher to the remote model.
     * 
     *  @param settable The settable whose value changed.
     *  @see ptolemy.kernel.util.ValueListener#valueChanged(ptolemy.kernel.util.Settable)
     */
    public synchronized void valueChanged(Settable settable) {
        if (isEnabled()) {
            _androidWidget.setValue(settable.getExpression());
        }
    }

    /** Set enabled flag of the listener. If it's true, the listener would send the attribute
     *  value change token.
     *  
     *  @param enabled The enabled flag.
     *  @see #isEnabled()
     */
    public synchronized void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /** Return the enabled flag of the listener. If it's true, the listener will send the
     *  attribute value change token.
     *  
     *  @return The enabled flag.
     *  @see #setEnabled(boolean)
     */
    public synchronized boolean isEnabled() {
        return _enabled;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Android widget representing the settable object.
     */
    private PtolemyAndroidWidget _androidWidget;

    /** The enabled flag. The value change will only get delegated if the
     *  flag is enabled.
     */
    private boolean _enabled;
}
