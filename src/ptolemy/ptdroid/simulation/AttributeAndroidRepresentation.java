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

/**
 * 
 */
package ptolemy.ptdroid.simulation;

import java.util.HashMap;
import java.util.List;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.actor.gui.style.NotEditableLineStyle;
import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.homer.kernel.AttributeRepresentation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;
import ptolemy.ptdroid.actor.gui.widgets.CheckBoxWidget;
import ptolemy.ptdroid.actor.gui.widgets.EditTextWidget;
import ptolemy.ptdroid.actor.gui.widgets.SpinnerWidget;
import ptolemy.ptdroid.actor.gui.widgets.TextViewWidget;
import ptolemy.ptdroid.util.DialogFactory;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;

///////////////////////////////////////////////////////////////////
//// AttributeAndroidRepresentation

/**
 * @author Peter
 *
 */
public class AttributeAndroidRepresentation implements AttributeRepresentation {

    public void placeWidget(Attribute element, PortableContainer container)
            throws IllegalActionException {
        ParameterEditorStyle style = getAcceptedStyle(element);

        // No style is present, assume a simple editable text field
        if (style == null) {
            _addToEditTextWidget(element, container);
            return;
        }

        if (style instanceof NotEditableLineStyle) {
            _addToTextViewWidget(element, container);
            return;
        }

        if (style instanceof CheckBoxStyle) {
            _addToCheckBoxWidget(element, container);
            return;
        }

        if (style instanceof ChoiceStyle) {
            _addToSpinnerWidget(element, container);
            return;
        }

        // TODO Parse the different styles
    }

    /** Check if there's an acceptable style defined for this attribute.
     * 
     *  @return The style if it's available ad acceptable, null otherwise.
     */
    public static ParameterEditorStyle getAcceptedStyle(Attribute element) {
        ParameterEditorStyle style = null;
        List<ParameterEditorStyle> attributeList = element
                .attributeList(ParameterEditorStyle.class);
        if (!attributeList.isEmpty()) {
            style = attributeList.get(0);
        }
        if (style == null) {
            // Style not set.
            return null;
        }

        if (!style.acceptable((Settable) element)) {
            // Style is not accepted for the given element.
            return null;
        }

        return style;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    /** Add a SpinnerWdiget to the container with its properties set.
     * 
     *  @param context Context of the Android application.
     *  @param container The container in which the SpinnerWidget should
     *  be placed in.
     */
    private void _addToSpinnerWidget(final Attribute element,
            PortableContainer container) {
        final SpinnerWidget spinnerWidget;
        ParameterEditorStyle style = getAcceptedStyle(element);

        // Gather the choices and their values from the attribute
        HashMap<String, String> choicesToValues = new HashMap<String, String>();
        HashMap<String, String> valuesToChoices = new HashMap<String, String>();

        List<Settable> choices = style.attributeList(Settable.class);
        for (Settable choice : choices) {
            choicesToValues.put(choice.getName(), choice.getExpression());
            valuesToChoices.put(choice.getExpression(), choice.getName());
        }

        // Create the new widget based on the information gathered.
        spinnerWidget = new SpinnerWidget(choicesToValues, valuesToChoices);
        spinnerWidget.place(container);

        // Set the default choice on the spinner widget based on the attributes attribute.
        final String defaultChoice = ((Settable) element).getExpression();
        spinnerWidget.setValue(defaultChoice);

        // Listen if the value is changed delegate to the representation.
        _listener = new AndroidValueListener(spinnerWidget);
        ((Settable) element).addValueListener(_listener);

        // Listen if the value of the representation changed and delegate to the element.
        spinnerWidget.addListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {

                synchronized (_listener) {
                    try {
                        _listener.setEnabled(false);
                        ((Settable) element).setExpression(spinnerWidget
                                .getValue(position));
                        ((Settable) element).validate();
                    } catch (Exception e) {
                        // Expression is invalid.
                        DialogFactory.getError(parent.getContext(),
                                element.getName(), e.getMessage());
                    } finally {
                        _listener.setEnabled(true);
                    }
                }

            }

            public void onNothingSelected(AdapterView<?> parent) {
                synchronized (_listener) {
                    try {
                        element.workspace().getWriteAccess();
                        _listener.setEnabled(false);
                        ((Settable) element).setExpression(defaultChoice);
                        ((Settable) element).validate();
                    } catch (Exception e) {
                        // Expression is invalid.
                        DialogFactory.getError(parent.getContext(),
                                element.getName(), e.getMessage());
                    } finally {
                        _listener.setEnabled(true);
                        element.workspace().doneWriting();
                    }
                }
            }
        });
    }

    /** Add a CheckBoxWidget to the container with its properties set.
     * 
     *  @param context Context of the Android application.
     *  @param container The container in which the CheckBoxWidget should
     *  be placed in.
     *  @exception IllegalActionException If the attribute containing the
     *  CheckBoxStyle is not a parameter containing a boolean token. 
     */
    private void _addToCheckBoxWidget(final Attribute element,
            PortableContainer container) throws IllegalActionException {
        // Check if the style can actually be used on the element.
        // Expecting parameters with boolean value token.
        if (!(element instanceof Parameter)) {
            throw new IllegalActionException(element,
                    "CheckBoxStyle can only be "
                            + "contained by instances of Parameter.");
        }

        Parameter parameter = (Parameter) element;
        Token current = parameter.getToken();

        if (!(current instanceof BooleanToken)) {
            throw new IllegalActionException(element,
                    "CheckBoxStyle can only be "
                            + "used for boolean-valued parameters");
        }

        // Create new widget
        final CheckBoxWidget checkBoxWidget = new CheckBoxWidget();
        checkBoxWidget.place(container);
        checkBoxWidget.setValue(((BooleanToken) current).booleanValue());

        // Listen if the value is changed delegate to the representation
        _listener = new AndroidValueListener(checkBoxWidget);
        ((Settable) element).addValueListener(_listener);

        // Listen if the user changed the text, and delegate it to the model
        // TODO
    }

    /** Add a TextViewWidget to the container with its properties set.
     * 
     *  @param context Context of the Android application.
     *  @param container The container in which the TextViewWidget should
     *  be placed in.
     */
    private void _addToTextViewWidget(final Attribute element,
            PortableContainer container) {
        // Create new widget
        final TextViewWidget textViewWidget = new TextViewWidget();
        textViewWidget.place(container);

        // Set parameters
        textViewWidget.setValue(((Settable) element).getExpression());

        // Listen if the value is changed delegate to the representation.
        _listener = new AndroidValueListener(textViewWidget);
        ((Settable) element).addValueListener(_listener);
    }

    /** Add an EditTextWidget to the container with its properties set and
     *  listeners attached to delegate changes.
     * 
     *  @param context Context of the Android application.
     *  @param container The container in which the EditTextWidget should be
     *  placed in.
     */
    private void _addToEditTextWidget(final Attribute element,
            PortableContainer container) {
        // Create new widget
        final EditTextWidget editTextWidget = new EditTextWidget();
        editTextWidget.place(container);

        // Set parameters
        editTextWidget.setValue(((Settable) element).getExpression());

        // Listen if the value is changed delegate to the representation
        _listener = new AndroidValueListener(editTextWidget);
        ((Settable) element).addValueListener(_listener);

        // Listen if the user changed the text, and delegate it to the model
        editTextWidget.addListener(new OnFocusChangeListener() {

            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String text = ((EditText) view).getText().toString();
                    // Check if the text is different as the expression for the element.
                    if (!((Settable) element).getExpression().equals(text)) {
                        synchronized (_listener) {
                            try {
                                element.workspace().getWriteAccess();
                                _listener.setEnabled(false);
                                ((Settable) element).setExpression(text);
                                ((Settable) element).validate();
                            } catch (IllegalActionException e) {
                                // Expression is invalid.
                                DialogFactory.getError(view.getContext(),
                                        element.getName(), e.getMessage());
                            } finally {
                                _listener.setEnabled(true);
                                element.workspace().doneWriting();
                            }
                        }
                    }
                }

            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The listener attached to this attribute by the Android representation. 
     */
    private AndroidValueListener _listener;

}
