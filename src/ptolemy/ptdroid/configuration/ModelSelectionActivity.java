/* Activity that lets the user select models and their corresponding
 layouts.

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

package ptolemy.ptdroid.configuration;

import java.io.File;
import java.net.MalformedURLException;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.ptdroid.R;
import ptolemy.ptdroid.actor.PtolemyModuleAndroidInitializer;
import ptolemy.ptdroid.simulation.SimulationActivity;
import ptolemy.ptdroid.util.DialogFactory;
import ptserver.control.IServerManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// ModelSelectionActivity

/** Activity that lets the user select models and their corresponding
 *  layouts.
 *   
 *  @author Peter Foldes
 *  @version $Id: ModelSelectionActivity.java 152 2011-09-12 17:59:15Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ModelSelectionActivity extends ListActivity {

    /** 
     * Initialize the injection framework.
     */
    static {
        ActorModuleInitializer.setInitializer(new PtolemyModuleAndroidInitializer());
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Allow the user to select a model to run.
     *  @param savedInstanceState State of the saved instance in the 
     *  back stack (if available).
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //        if (true) {
        //            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        //                    .detectDiskReads().detectDiskWrites().detectNetwork() // or .detectAll() for all detectable problems
        //                    .penaltyLog().build());
        //            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        //                    .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
        //                    .penaltyLog().penaltyDeath().build());
        //        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.model_list);

        try {
            _setupProxy(getIntent().getStringExtra("address"), getIntent()
                    .getStringExtra("username"),
                    getIntent().getStringExtra("password"));
            _state = ModelSelectionState.SelectingModel;
            _setupList();
        } catch (Exception e) {
            Log.e(getClass().getName(),
                    "A problem has occurred: " + e.getMessage());
            DialogFactory.getError(this, "Configuration Error", e.getMessage())
                    .show();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                protected methods                          ////

    /* Return to selecting a model if the user is viewing the layout list.
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        if (_state == ModelSelectionState.SelectingModel) {
            super.onBackPressed();
        } else if (_state == ModelSelectionState.SelectingLayout) {
            setContentView(R.layout.model_list);

            _modelUrl = "";
            _state = ModelSelectionState.SelectingModel;
            _setupList();
        }
    }

    /** Handle the user's selection of either a model or layout file.
     *  @param listView The ListView where the click happened.
     *  @param view The view that was clicked within the ListView.
     *  @param position The position of the view in the list.
     *  @param id The row id of the item that was clicked.
     *  @see android.app.ListActivity#onListItemClick(ListView, View, int, long)
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position,
            long id) {
        String selection = (String) listView.getItemAtPosition(position);
        if (_state == ModelSelectionState.SelectingModel) {
            setContentView(R.layout.layout_list);

            _modelUrl = selection;
            _state = ModelSelectionState.SelectingLayout;
            _setupList();
        } else if (_state == ModelSelectionState.SelectingLayout) {
            // Pass parameters into the intent.
            Intent intent = new Intent(getApplicationContext(),
                    SimulationActivity.class);
            intent.putExtra("address", getIntent().getStringExtra("address"));
            intent.putExtra("username", getIntent().getStringExtra("username"));
            intent.putExtra("password", getIntent().getStringExtra("password"));
            intent.putExtra("modelUrl", _modelUrl);
            intent.putExtra("layoutUrl", selection);

            startActivity(intent);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    /** Initialize the proxy reference to the server.
     *  @param address Address of the Ptolemy server.
     *  @param username Username used for basic authentication.
     *  @param password Password used for basic authentication.
     *  @exception IllegalStateException If there was a problem setting up the servlet proxy. 
     */
    private void _setupProxy(String address, String username, String password)
            throws IllegalStateException {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setHessian2Reply(false);
        factory.setUser(username);
        factory.setPassword(password);

        try {
            _proxy = factory.create(IServerManager.class, address,
                    getClassLoader());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to connect to server.", e);
        }
    }

    /** Set up the screen controls.
     */
    private void _setupList() {
        try {
            if (_state == ModelSelectionState.SelectingModel) {
                setListAdapter(new UrlAdapter(this, R.layout.model_list_item,
                        R.id.model_item, _proxy.getModelListing()));
            } else {
                // Determine if the selected host is still reachable.
                //                String serverAddress = getIntent().getStringExtra("address");
                //                if (!InetAddress.getByName(serverAddress).isReachable(2500)) {
                //                    DialogFactory.getError(
                //                            this,
                //                            "Connection Error",
                //                            "The server could not be reached at: "
                //                                    + serverAddress).show();
                //
                //                    // Go to the previous activity.
                //                    super.onBackPressed();
                //                    return;
                //                }

                setListAdapter(new UrlAdapter(this, R.layout.layout_list_item,
                        R.id.layout_item, _proxy.getLayoutListing(_modelUrl)));
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
            Toast.makeText(getBaseContext(), "Unable to load the "
                    + (_state == ModelSelectionState.SelectingModel ? "model"
                            : "layout") + " list.", Toast.LENGTH_SHORT);
            onBackPressed();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The proxy through which the client administers commands to the Ptolemy server.
     */
    private IServerManager _proxy;

    /** Path to the selected model file.
     */
    private String _modelUrl;

    /** The state in the model/layout selection process.
     */
    private ModelSelectionState _state;

    /** Enumeration representing the various states of the activity.
     */
    private enum ModelSelectionState {
        /** The state that the user interface is for selecting the model.
         */
        SelectingModel,

        /** The state that the user interface is for selecting a layout.
         *  Should get to this after the model has already been selected.
         */
        SelectingLayout;
    }

    /** Customized adapter to show only the filenames when url's are used.
     */
    private class UrlAdapter extends ArrayAdapter<String> {

        ///////////////////////////////////////////////////////////////////
        ////                constructor                                ////

        /** Initialize the server list adapter.
         *  @param context The current context.
         *  @param resource The resource ID for a layout file containing a layout 
         *  to use when instantiating views.
         *  @param textViewResourceId The id of the TextView within the layout resource to be populated.
         *  @param strings The id of the TextView within the layout resource to be populated.
         */
        public UrlAdapter(Context context, int resource,
                int textViewResourceId, String[] strings) {
            super(context, resource, textViewResourceId, strings);
            _inflater = LayoutInflater.from(context);
            _resource = resource;
            _textViewResourceId = textViewResourceId;
        }

        ///////////////////////////////////////////////////////////////////
        ////                public methods                             ////

        /** Create the view for a line item.
         *  @param position The position of the data for which the line is created.
         *  @param convertView The view containing the list item.
         *  @param parent The parent of the view.
         *  @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
         *  @return The created view for a line item.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String url = getItem(position);

            if (convertView == null) {
                convertView = _inflater.inflate(_resource, null);
            }

            TextView textView = (TextView) convertView
                    .findViewById(_textViewResourceId);
            textView.setText(url.substring(url.lastIndexOf(File.separator) + 1,
                    url.length()));

            return convertView;
        }

        ///////////////////////////////////////////////////////////////////
        ////                private variables                          ////

        /** The id of the TextView within the layout resource to be populated.
         */
        private int _textViewResourceId;

        /** The resource ID for a layout file containing a layout to use when 
         *  instantiating views.
         */
        private int _resource;

        /** System service used to instantiate a view if referenced with an
         *  application resource ID rather than a view in the activity's 
         *  current layout.
         */
        private LayoutInflater _inflater;
    }
}
