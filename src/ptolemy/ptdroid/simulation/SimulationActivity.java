/* The activity that enables the user to start, pause, resume, and stop
   a simulation as well as view the visual output of graphical sink actors.
   
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

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.style.NotEditableLineStyle;
import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.homer.kernel.AttributeElement;
import ptolemy.homer.kernel.HomerConstants;
import ptolemy.homer.kernel.LayoutParser;
import ptolemy.homer.kernel.LayoutParser.ScreenOrientation;
import ptolemy.homer.kernel.MultiContent;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.homer.kernel.TabDefinition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.ptdroid.R;
import ptolemy.ptdroid.actor.PtolemyModuleAndroidInitializer;
import ptolemy.ptdroid.util.DialogFactory;
import ptserver.communication.ProxyModelAdapter;
import ptserver.communication.ProxyModelInfrastructure;
import ptserver.communication.ProxyModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;
import ptserver.control.Ticket;
import ptserver.data.RemoteEventToken;
import ptserver.data.TokenParser;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.ServerUtility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// SimulationActivity

/** The activity that enables the user to start, pause, resume, and stop
 *  a simulation as well as view the visual output of graphical sink actors.
 *
 *  @author Justin Killian
 *  @version $Id: SimulationActivity.java 171 2011-11-20 19:45:16Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class SimulationActivity extends Activity implements
        TabHost.TabContentFactory {

    /** 
     * Initialize the injection framework.
     */
    static {
        ActorModuleInitializer
                .setInitializer(new PtolemyModuleAndroidInitializer());
        Logger.getLogger("PtolemyServer").setLevel(Level.SEVERE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Create and initialize the activity.
     *  @param savedInstanceState State of the saved instance in the 
     *  back stack (if available).
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simulation);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                _handleFatalError(e);
            }
        });

        try {
            _setupProxy(getIntent().getStringExtra("address"), getIntent()
                    .getStringExtra("username"),
                    getIntent().getStringExtra("password"));
            TokenParser.getInstance().setTokenHandlers(
                    _proxy.getTokenHandlerMap());
            _setupModel(getIntent().getStringExtra("modelUrl"), getIntent()
                    .getStringExtra("layoutUrl"));
            _setupDisplay();
            _setupControls();
        } catch (Throwable e) {
            _handleFatalError(e);
        }
    }

    /** Handle switching content when changing tabs.
     *  @param tag The tag of the tab activated. 
     *  @return Contents for the new tab.
     */
    public View createTabContent(String tag) {
        View view = (View) _contents.getContent(tag);
        if (view == null) {
            Log.e(getClass().getName(), "Could not switch to contents of "
                    + tag + ".");
            DialogFactory.getError(this, "Error switching tabs",
                    "Content is not set").show();
        }

        return view;
    }

    /* Close the open ticket and clean up the simulation.
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        _cleanUp();
        // Go back as expected.
        super.onBackPressed();
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    /**
     * Clean up before finishing the activity.
     * 
     */
    private void _cleanUp() {
        try {
            if (_response != null) {
                Ticket ticket = _response.getTicket();
                // Kill the simulation remotely.
                _proxy.close(ticket);
            }
        } catch (Throwable e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        } finally {
            if (_localModel != null) {
                // Stop the local instance.
                _localModel.getManager().finish();
                _localModel.close();
            }
        }
    }

    /** Initialize the proxy reference to the server.
     *  @param address Address of the Ptolemy server.
     *  @param username Username used for basic authentication.
     *  @param password Password used for basic authentication.
     *  @exception IllegalStateException If there was a problem setting up the servlet proxy. 
     * @throws MalformedURLException 
     */
    private void _setupProxy(String address, String username, String password)
            throws IllegalStateException, MalformedURLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setHessian2Reply(false);
        factory.setUser(username);
        factory.setPassword(password);
        _proxy = factory
                .create(IServerManager.class, address, getClassLoader());
    }

    /** Initialize the local model and open the corresponding file on the server.
     * 
     *  @param modelUrl URL address of the model file.
     *  @param layoutUrl URL address of the layout file.
     * @throws Exception 
     */
    private void _setupModel(String modelUrl, String layoutUrl)
            throws Exception {
        // Opens a ticket on the server and stores the response.
        _response = _proxy.open(modelUrl, layoutUrl);

        // Create and initialize the local model based on the response. 
        CompositeActor topLevelActor = (CompositeActor) ServerUtility
                .createMoMLParser().parse(_response.getModelXML());
        _localModel = new ProxyModelInfrastructure(ProxyModelType.CLIENT,
                topLevelActor, _response.getModelTypes());
        topLevelActor.getDirector().setContainer(null);
        topLevelActor.setDirector(new PNDirector(
                _localModel.getTopLevelActor(), "PNDirector"));
        _localModel.setUpInfrastructure(_response.getTicket(),
                _response.getBrokerUrl());
        _localModel.addProxyModelListener(new ProxyModelAdapter() {

            private boolean hasFired = false;

            @Override
            public void modelConnectionExpired(
                    ProxyModelInfrastructure proxyModelInfrastructure) {
                _handleFatalError(new IllegalStateException(
                        "Model connection has expired"));
            }

            @Override
            public void modelException(
                    ProxyModelInfrastructure proxyModelInfrastructure,
                    String message, Throwable exception) {
                if (!hasFired) {
                    hasFired = true;
                    _handleFatalError(exception);
                }
            }

            @Override
            public void onRemoteEvent(
                    ProxyModelInfrastructure proxyModelInfrastructure,
                    RemoteEventToken event) {
                _handleFatalError(new IllegalStateException(event.getMessage()));
            }

        });
    }

    /** Initialize the tabbed area of the screen so that each model actor
     *  with visual output can be given a canvas on which to draw itself.
     *  @exception IllegalActionException If the tabbed area could not be created or
     *  there was a problem parsing the layout file.
     *  @exception NameDuplicationException If there's a problem with setting up a
     *  tab due to label style naming duplications.
     */
    private void _setupDisplay() throws IllegalActionException,
            NameDuplicationException {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        if (tabHost == null) {
            throw new IllegalActionException("Tabhost is not defined.");
        }

        tabHost.setup();
        tabHost.clearAllTabs();

        // Get a new parser for creating a layout.
        LayoutParser parser = new LayoutParser(_localModel.getTopLevelActor());

        // Set the orientation.
        ScreenOrientation orientation = parser.getOrientation();
        switch (orientation) {
        case UNSPECIFIED:
            setRequestedOrientation(DEFAULT_SCREEN_ORIENTATION);
            break;
        case PORTRAIT:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            break;
        case LANDSCAPE:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            break;
        }

        _contents = new MultiContent(new AndroidContent(this),
                _localModel.getTopLevelActor());

        // No tabs defined, add a default tab
        ArrayList<TabDefinition> tabs = _contents.getAllTabs();
        if (tabs.size() == 0) {
            tabHost.addTab(tabHost.newTabSpec(HomerConstants.TAG)
                    .setIndicator(HomerConstants.TAG)
                    .setContent(new TabContentFactory() {
                        public View createTabContent(String tag) {
                            return (View) new AndroidContent(
                                    SimulationActivity.this).getContent();
                        }
                    }));
        } else {
            for (TabDefinition tab : tabs) {
                tabHost.addTab(tabHost.newTabSpec(tab.getTag())
                        .setIndicator(tab.getName()).setContent(this));
            }
        }
    }

    /** Initialize the control buttons on the user interface that will be used
     *  to start, pause, and stop the simulation.
     *  @exception IllegalStateException If the simulation fails to start, pause, or stop.
     */
    private void _setupControls() throws IllegalStateException {
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button pauseButton = (Button) findViewById(R.id.pause_button);
        final Button stopButton = (Button) findViewById(R.id.stop_button);
        final TextView latencyText = (TextView) findViewById(R.id.latency_text);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (_localModel.getManager().getState() == Manager.IDLE) {
                        if (_verifyRequiredFields()) {
                            _proxy.start(_response.getTicket());
                            _localModel.getManager().startRun();

                            playButton.setEnabled(false);
                            pauseButton.setEnabled(true);
                            stopButton.setEnabled(true);

                            Toast.makeText(SimulationActivity.this,
                                    "Simulation started.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else if (_localModel.getManager().getState() == Manager.PAUSED) {
                        _proxy.resume(_response.getTicket());
                        _localModel.getManager().resume();

                        playButton.setEnabled(false);
                        pauseButton.setEnabled(true);
                        stopButton.setEnabled(true);

                        Toast.makeText(SimulationActivity.this,
                                "Simulation resumed.", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (Throwable e) {
                    _handleFatalError(e);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (_localModel.getManager().getState() != Manager.PAUSED) {
                        _proxy.pause(_response.getTicket());
                        _localModel.getManager().pause();

                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                        stopButton.setEnabled(true);

                        Toast.makeText(SimulationActivity.this,
                                "Simulation paused.", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (Throwable e) {
                    _handleFatalError(e);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (_localModel.getManager().getState() != Manager.EXITING) {
                        _proxy.stop(_response.getTicket());
                        _localModel.getManager().finish();

                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                        stopButton.setEnabled(false);

                        Toast.makeText(SimulationActivity.this,
                                "Simulation stopped.", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (Throwable e) {
                    _handleFatalError(e);
                }
            }
        });

        latencyText.postDelayed(new Runnable() {

            public void run() {
                latencyText.setText(_localModel.getPingPongLatency() + " ms");
                latencyText.postDelayed(this, 100);
            }
        }, 100);
    }

    private synchronized void _handleFatalError(final Throwable throwable) {
        Log.e(getClass().getName(), throwable.getMessage(), throwable);
        if (!_handlingFatalError) {
            _handlingFatalError = true;
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            SimulationActivity.this);
                    builder.setTitle("Unhandled Exception")
                            .setMessage(
                                    "The execution would now terminate due to unhandled exception \n"
                                            + throwable.getMessage()
                                            + "\n"
                                            + KernelException
                                                    .stackTraceToString(throwable))
                            .setIcon(R.drawable.icon)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            _cleanUp();
                                            finish();
                                        }
                                    }).show();
                }
            });
        }
    }

    /** Verify that attributes denoted as being required before a 
     *  simulation can be started have an expression set.
     * @throws IllegalActionException if there is a problem reading attribute's expression.
     */
    private boolean _verifyRequiredFields() throws IllegalActionException {

        for (TabDefinition tabdefinition : (ArrayList<TabDefinition>) _contents
                .getAllTabs()) {
            for (PositionableElement element : tabdefinition.getElements()) {
                // If its an attribute and settable, move on.
                if ((!(element instanceof AttributeElement))
                        || (!(element.getElement() instanceof Settable))
                        || (((Settable) element.getElement()).getVisibility() != Settable.FULL)) {
                    continue;
                }

                // If its required, check token for true value.
                Parameter required = (Parameter) element.getElement()
                        .getAttribute(HomerConstants.REQUIRED_NODE);
                if (required == null
                        || (!(required.getToken() instanceof BooleanToken))
                        || (((BooleanToken) required.getToken()).booleanValue() == false)) {
                    continue;
                }

                // If its an editable style, keep going.
                ParameterEditorStyle style = AttributeAndroidRepresentation
                        .getAcceptedStyle((Attribute) element.getElement());
                if (style instanceof NotEditableLineStyle) {
                    continue;
                }

                // Get the expression value and ensure that its populated.
                String expression = ((Settable) element.getElement())
                        .getExpression();
                if ((expression == null) || (expression.length() == 0)) {
                    DialogFactory.getError(
                            this,
                            "Required Fields",
                            "Attribute " + element.getElement().getFullName()
                                    + " is required.").show();
                    return false;
                }
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /**
     * This flag is used to prevent other fatal errors from showing up if there is one currently being handled.
     */
    private boolean _handlingFatalError = false;
    /** The proxy through which the client administers commands to the Ptolemy server.
     */
    private IServerManager _proxy;

    /** The response received from the server opening the current model.
     */
    private ProxyModelResponse _response;

    /** Locally running model consisting of sources and sinks.
     */
    private ProxyModelInfrastructure _localModel;

    /** Multiple content wrapper. Provide access to different content areas
     *  depending on a tag.
     */
    private MultiContent _contents;

    /** Define Android specific default screen orientation.
     */
    private final int DEFAULT_SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
}
