/* Define interface for common portable widgets that will be visualized
   on the Android UI.

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

///////////////////////////////////////////////////////////////////
//// PtolemyAndroidWidget

/** Define calls for common portable widgets.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 *  
 *  @param <ValueType> The type of the value the widget uses for representation.
 *  @param <ListenerType> The type of the listener the widget uses to delegate its
 *  value changed events. 
 */
public interface PtolemyAndroidWidget<ValueType, ListenerType> {

    /** Set the value for the widget.
     * 
     * @param value The value to use.
     */
    void setValue(ValueType value);

    /** Add a listener to the widget. The listener should fire whenever the
     *  represented value changed.
     *  
     *  @param listener The listener object to fire when the widget's value changes.
     */
    void addListener(ListenerType listener);

    /** Remove a listener from the widget.
     *  
     *  @param listener The listener object to remove from the widget.
     */
    void removeListener(ListenerType listener);
}
