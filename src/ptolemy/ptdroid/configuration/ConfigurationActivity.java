/* Control Ptolemy server selection, including adding, selecting,
 and removing servers persistently.
 
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.ptdroid.R;
import ptolemy.ptdroid.actor.PtolemyModuleAndroidInitializer;
import ptolemy.ptdroid.util.DialogFactory;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

///////////////////////////////////////////////////////////////////
//// ConfigurationActivity

/** Control Ptolemy server selection, including adding, selecting,
 *  and removing servers persistently.
 *   
 *  @author Peter Foldes
 *  @version $Id: ConfigurationActivity.java 158 2011-10-09 15:38:46Z jkillian $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ConfigurationActivity extends ListActivity {

    /** 
     * Initialize the injection framework.
     */
    static {
        ActorModuleInitializer
                .setInitializer(new PtolemyModuleAndroidInitializer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Show the server selection menu.
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
        setContentView(R.layout.server_list);
        setListAdapter(new ServerAdapter(this, R.layout.server_list_item,
                R.id.server_item, ConnectionManager.getInstance(this)
                        .getServers()));
        registerForContextMenu(getListView());

        Button addServer = (Button) findViewById(R.id.server_add_button);
        addServer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                _showServerDialog(null);
            }
        });
    }

    /** Create the context menu for the selected item.
     *  @param menu The context menu that is being built
     *  @param v The view for which the context menu is being built
     *  @param menuInfo Extra information about the item for which the 
     *  context menu should be shown. This information will vary depending on the view.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Actions");
        getMenuInflater().inflate(R.layout.server_list_context, menu);
    }

    /** Edit or delete the current list view item depending on
     *  which option is selected from the context menu.
     *  @param item The context menu item that was selected. 
     *  @return Whether or not the event was responded to and consumed by the handler.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        boolean consumed = false;

        if (item.getItemId() == R.id.edit_server) {
            Server server = (Server) getListView().getItemAtPosition(
                    info.position);
            if (server != null) {
                _showServerDialog(server);
                consumed = true;
            }
        } else if (item.getItemId() == R.id.delete_server) {
            Server server = (Server) getListView().getItemAtPosition(
                    info.position);
            if (server != null) {
                ((ServerAdapter) getListAdapter()).deleteServer(server);
                consumed = true;
            }
        }

        return consumed;
    }

    ///////////////////////////////////////////////////////////////////
    ////                protected methods                          ////    

    /** Select the server and transition the user to selecting a model and layout file.
     *  @param listView The ListView where the click happened.
     *  @param view The view that was clicked within the ListView.
     *  @param position The position of the view in the list.
     *  @param id The row id of the item that was clicked.
     *  @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position,
            long id) {
        try {
            // Get the current selection.
            Object item = listView.getItemAtPosition(position);
            if (item instanceof Server) {

                // Determine if the selected host is reachable.
                if (InetAddress.getByName(((Server) item).getAddress())
                        .isReachable(2000) == false) {
                    DialogFactory.getError(
                            this,
                            "Connection Error",
                            "The server could not be reached at: "
                                    + ((Server) item).getAddress()).show();
                    return;
                }

                // Proceed to the next intent with the selected server.
                Intent intent = new Intent(getApplicationContext(),
                        ModelSelectionActivity.class);
                intent.putExtra("address", ((Server) item).toString());
                intent.putExtra("username", ((Server) item).getUsername());
                intent.putExtra("password", ((Server) item).getPassword());

                startActivity(intent);
            }
        } catch (UnknownHostException e) {
            Log.e(this.getClass().getName(), e.getMessage());
            DialogFactory.getError(this, "Connection Error",
                    "Host is unknown: " + e.getMessage()).show();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.getMessage());
            DialogFactory.getError(this, "Connection Error", e.getMessage())
                    .show();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

    /** Show the dialog for adding new servers to the application database.
     *  @param oldServer The server to be edited, null if adding a new one.
     */
    private void _showServerDialog(final Server oldServer) {
        // Set up dialog box.
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_server_dialog);
        dialog.setTitle(R.string.add_server_header);

        final EditText address = (EditText) dialog
                .findViewById(R.id.server_address);
        final EditText port = (EditText) dialog.findViewById(R.id.server_port);
        final EditText username = (EditText) dialog
                .findViewById(R.id.server_username);
        final EditText password = (EditText) dialog
                .findViewById(R.id.server_password);

        // If server element was passed in, pre-populate the dialog fields.
        if (oldServer != null) {
            address.setText(oldServer.getAddress());
            port.setText(String.valueOf(oldServer.getPort()));
            username.setText(oldServer.getUsername());
            password.setText(oldServer.getPassword());
        }

        // Set up the save button.
        Button button = (Button) dialog.findViewById(R.id.server_save);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // Perform field validation.
                if (address.getText().toString().trim().length() == 0) {
                    address.setError("Address not specified.");
                } else if (port.getText().toString().trim().length() == 0) {
                    port.setError("Port not specified.");
                } else if (!port.getText().toString().matches("[0-9]+")) {
                    port.setError("Port must be numeric.");
                } else {
                    try {
                        // Create the server object & save to the database.
                        Server newServer = new Server(address.getText()
                                .toString(), Integer.parseInt(port.getText()
                                .toString()), username.getText().toString(),
                                password.getText().toString());
                        if (oldServer == null) {
                            // Server doesn't exist - create it.
                            ((ServerAdapter) getListAdapter())
                                    .addServer(newServer);
                        } else {
                            // Server exists - update it.
                            newServer.setID(oldServer.getID());

                            ((ServerAdapter) getListAdapter()).updateServer(
                                    oldServer, newServer);
                        }

                        dialog.dismiss();
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.getMessage());
                        DialogFactory.getError(ConfigurationActivity.this,
                                "Configuration Error", e.getMessage()).show();
                    }
                }
            }
        });

        // Show the server configuration dialog.    
        dialog.show();
    }
}
