/*
 Initializer of the PtolemyModule with Java SE specific interface to
 implementation mappings.
 
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

package ptolemy.ptdroid.actor;

import java.util.ResourceBundle;

import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.injection.PtolemyModule;
import ptolemy.actor.injection.ActorModuleInitializer.Initializer;

///////////////////////////////////////////////////////////////////
//// PtolemyModuleJavaSEInitializer
/**
 * Initializer of the PtolemyModule with Android specific interface to
 * implementation mappings.  The module uses ptolemy.actor.JavaSEActorModule.properties
 * file to initialize the PtolemyModule.
 * @author Anar Huseynov
 * @version $Id: PtolemyModuleAndroidInitializer.java 154 2011-09-30 19:48:35Z ahuseyno $ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PtolemyModuleAndroidInitializer implements Initializer {

    /** 
     * Initialize the injector with android modules.
     * @see ptolemy.actor.injection.ActorModuleInitializer.Initializer#initialize()
     */
    @Override
    public void initialize() {
        PtolemyInjector.createInjector(PTOLEMY_MODULES);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static final PtolemyModule[] PTOLEMY_MODULES = {
            new PtolemyModule(
                    ResourceBundle
                            .getBundle("ptolemy.ptdroid.actor.AndroidActorModule")),
            new PtolemyModule(
                    ResourceBundle
                            .getBundle("ptolemy.ptdroid.simulation.AndroidSimulationModule")) };

}
