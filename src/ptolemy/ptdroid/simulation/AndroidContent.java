/* Android implementation of a tab's content area.

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

import ptolemy.homer.kernel.ContentPrototype;
import ptolemy.homer.kernel.HomerLocation;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.ptdroid.actor.gui.PortableView;
import android.content.Context;
import android.widget.RelativeLayout;

///////////////////////////////////////////////////////////////////
//// AndroidContent

/** Android implementation of a tab's content area.
 *
 *  @author Peter Foldes
 *  @version $Id: AndroidContent.java 152 2011-09-12 17:59:15Z ahuseyno $
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class AndroidContent implements ContentPrototype {

    public AndroidContent(Context context) {
        _content = new RelativeLayout(context);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Add an element to the content area.
     * 
     *  @param element The element to be added to the content area.
     *  @exception IllegalActionException If the content area is not set.
     */
    public void add(PositionableElement element) throws IllegalActionException {
        // Create wrapper layout for the element
        PortableView portable = new PortableView(_content.getContext());
        element.addToContainer(portable);

        // Set location on the layout
        HomerLocation location = element.getLocation();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                location.getWidth(), location.getHeight());
        layoutParams.setMargins(location.getX(), location.getY(), 0, 0);
        portable.setLayoutParams(layoutParams);

        // Add representation to the tab contents
        if (_content == null) {
            throw new IllegalActionException(
                    "Content could not be added to content.");
        }

        _content.addView(portable);
    }

    /** Remove an element from the contents. This is not allowed on the client, will
     *  always throw an exception.
     *  
     *  @exception IllegalActionException Will always throw exception.
     */
    public void remove(PositionableElement element)
            throws IllegalActionException {
        throw new IllegalActionException(
                "Removing element is not allowed on the client.");
    }

    /** Get the content area.
     * 
     *  @return The content area.
     */
    public Object getContent() {
        return _content;
    }

    /** Get a new instance of the prototype.
     * 
     *  @return The new instance of the prototype.
     */
    public ContentPrototype getNewInstance() {
        return new AndroidContent(_content.getContext());
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** A placeholder area for Android specific element representations.
     */
    private RelativeLayout _content;
}
