/* Main activity for the Ptolemy Android application. Controls the
 splash screen at application start and starts the server selection
 process.
 
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
package ptolemy.ptdroid.configuration;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.ptdroid.R;
import ptolemy.ptdroid.actor.PtolemyModuleAndroidInitializer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

///////////////////////////////////////////////////////////////////
//// PtolemyActivity

/** Main activity for the Ptolemy Android application. Controls the
 *  splash screen at application start and starts the server selection
 *  process.
 * 
 *  @author Peter Foldes
 *  @version $Id: PtolemyActivity.java 166 2011-11-13 22:02:55Z ishwinde $ 
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class PtolemyActivity extends Activity {

    /** 
     * Initialize the injection framework.
     */
    static {
        ActorModuleInitializer
                .setInitializer(new PtolemyModuleAndroidInitializer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Set the layout of the first screen and, after a period of
     *  time, forward the user into the server configuration.
     *  @param savedInstanceState State of the saved instance in the 
     *  back stack (if available).
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Set up the workspace.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        _launched = false;

        // Initialize the timed transition into the configuration.
        _runnable = new Runnable() {
            public void run() {
                synchronized (PtolemyActivity.this) {
                    if (!_launched) {
                        startActivity(new Intent(getBaseContext(),
                                ConfigurationActivity.class));
                        _launched = true;
                        finish();
                    }
                }
            }
        };

        // Start the timed transition.
        _handler = new Handler();
        _handler.postDelayed(_runnable, DELAY);
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        _launched = false;

    }

    /** In case the Ptolemy application gets back to this first screen,
     *  a touch to anywhere on the screen will move the application forward
     *  to the server selection process. 
     *  @param event Motion event captured by the OS.
     *  @return Whether or not the event was consumed.  
     *  @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {

        // Start the next activity.
        boolean isConsumed = false;
        try {
            if (!_launched) {
                startActivity(new Intent(this, ConfigurationActivity.class));
                _launched = true;
                finish();
            }

            isConsumed = true;
        } catch (Exception e) {
            isConsumed = false;
        }

        return isConsumed;
    }

    /** Back out of the application and cancel any pending callbacks.
     *  @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        // Cancel the runnable callback.
        _handler.removeCallbacks(_runnable);

        // Leave the application.
        super.onBackPressed();
        finish();
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** Handler to the user interface.
     */
    private Handler _handler;

    /** Runnable that is used forward the user into the application after a period of time.
     */
    private Runnable _runnable;

    /** The delay in milliseconds before switching to the next activity.
     */
    private final int DELAY = 1000;

    /** Whether or not the activity was launched or retrieved from backstack.
     */
    private boolean _launched = false;
}
