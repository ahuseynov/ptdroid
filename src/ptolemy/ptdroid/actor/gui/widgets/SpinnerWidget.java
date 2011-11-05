/* Represents a spinner  widget to be placed on the Android user interface
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

import java.util.ArrayList;
import java.util.HashMap;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import android.R;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

///////////////////////////////////////////////////////////////////
//// SpinnerWidget

/** Represents a spinner widget to be placed on the Android user interface.
*
*  @author Ishwinder Singh
*  @version $Id: SpinnerWidget.java 152 2011-09-12 17:59:15Z ahuseyno $
*  @since Ptolemy II 8.1
*  @Pt.ProposedRating Red (ishwinde)
*  @Pt.AcceptedRating Red (ishwinde)
*/

public class SpinnerWidget implements PortablePlaceable,
        PtolemyAndroidWidget<String, OnItemSelectedListener> {

    /** Set up the underlying spinner widget and set its possible values (choices)
     *  and what those values represent (values). Both way mapping is required for
     *  performance reasons.
     * 
     *  @param choicesToValues A mapping of the available choices to the
     *  values they represent.
     *  @param valuesToChoices A mapping of the available values to the
     *  choices
     */
    public SpinnerWidget(HashMap<String, String> choicesToValues,
            HashMap<String, String> valuesToChoices) {
        _choicesToValues = choicesToValues;
        _valuesToChoices = valuesToChoices;
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Return the Spinner object.
     *  @return Spinner object.
     */
    public Spinner getSpinner() {
        return _spinner;
    }

    /** Place the Spinner into the specified container.
     *  @param container The container in which to place the Spinner.
     */
    public void place(PortableContainer container) {
        _spinner = new Spinner(
                ((ViewGroup) container.getPlatformContainer()).getContext());
        container.add(_spinner);

        _setAdapter();
    }

    /** Add listener to the widget. The listener should fire whenever the
     *  represented value changed.
     *  
     *  @param listener The listener object to fire when the widget's value changes.
     */
    public void addListener(OnItemSelectedListener listener) {
        _spinner.setOnItemSelectedListener(listener);
    }

    /** Remove the listener from the widget.
     *  
     *  @param listener Optional parameter, it's not currently used.
     */
    public void removeListener(OnItemSelectedListener listener) {
        _spinner.setOnItemSelectedListener(null);
    }

    /** Set the state of the spinner.
     *  
     *  @param value The value of the choice selected.
     */
    public void setValue(String value) {
        _spinner.setSelection(((ArrayAdapter<String>) _spinner.getAdapter())
                .getPosition(_valuesToChoices.get(value)));
    }

    /** Get the value represented by the spinner based on its position.
     * 
     *  @param position The position of the selected item.
     *  @return The value represented by the given position.
     */
    public String getValue(int position) {
        return _choicesToValues.get(_spinner.getItemAtPosition(position));
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    /** Set the adapter for the underlying spinner widget based on the
     *  available choices.
     *  
     *  This method should only be called after the spinner widget is initiated,
     *  and the map containing the available choices and values is set.
     */
    private void _setAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                _spinner.getContext(), R.layout.simple_spinner_item,
                new ArrayList<String>(_choicesToValues.keySet()));
        _spinner.setAdapter(adapter);
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The underlying spinner widget that represents the available choices. 
     */
    private Spinner _spinner;

    /** The mapping of choices to their values.
     */
    private HashMap<String, String> _choicesToValues;

    /** The mapping of values to their original choices.
     */
    private HashMap<String, String> _valuesToChoices;
}
