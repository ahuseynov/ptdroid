/* View container that implements the PortableContainer interface.

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
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

///////////////////////////////////////////////////////////////////
//// PortableView

/** The screen container in which Ptolemy actors, parameters, and other widgets
 *  are expected to draw themselves.  As this class extends FrameLayout, it is
 *  assumed that only one view will exist within this container unless the user
 *  wishes to overlaying the output of one actor over another.
 *
 *  @author Justin Killian
 *  @version $Id: PortableView.java 152 2011-09-12 17:59:15Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class PortableView extends FrameLayout implements PortableContainer {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Initialize the container using its superclass constructor.
     *  @param context Operating context of the view.
     */
    public PortableView(Context context) {
        super(context);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Add the component to the ViewGroup.
     *  @param component The component to be added to the ViewGroup.
     *  @see ptolemy.actor.gui.PortableContainer#add(java.lang.Object)
     */
    public void add(Object component) {
        addView((View) component);
    }

    /** Return the platform dependent container that this instance wraps, in
     *  this case itself as a FrameLayout.
     *  
     *  @return The instance as an Android ViewGroup container.
     *  @see ptolemy.actor.gui.PortableContainer#getPlatformContainer()
     */
    public Object getPlatformContainer() {
        return this;
    }
}
