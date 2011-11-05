/* Android user interface representation of the server list. This
 includes handling deletion of a server from a list.
  
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

import java.util.List;

import ptolemy.ptdroid.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

///////////////////////////////////////////////////////////////////
//// ServerAdapter

/** Android user interface representation of the server list. This
 *  includes handling deletion of a server from a list.
 *   
 *  @author Peter Foldes
 *  @version $Id: ServerAdapter.java 149 2011-07-30 01:41:38Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ServerAdapter extends ArrayAdapter<Server> {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Initialize the server list adapter.
     *  @param context The current context.
     *  @param resource The resource ID for a layout file containing a layout 
     *  to use when instantiating views.
     *  @param textViewId The id of the TextView within the layout resource to be populated.
     *  @param objects The id of the TextView within the layout resource to be populated.
     */
    public ServerAdapter(Context context, int resource, int textViewId,
            List<Server> objects) {
        super(context, resource, textViewId, objects);
        _inflater = LayoutInflater.from(context);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Create the view for a line item.
     * 
     *  @param position The position of the data for which the line is created.
     *  @param convertView The view containing the list item.
     *  @param parent The parent of the view.
     *  @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     *  @return The created view for a line item.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        final Server rowData = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.server_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextView textLine = holder.getText();
        textLine.setText(rowData.getAddress() + ":" + rowData.getPort());

        return convertView;
    }

    /** Add the server to the database and refresh the list.
     *  @param server New server information that should be persisted.
     *  @exception Exception If the server could not be added to the persistent
     *  storage.
     */
    public void addServer(Server server) throws Exception {
        // Call the default add() method.
        super.add(server);

        // Save the new server to the database.
        server.setID(ConnectionManager.getInstance(getContext()).addServer(
                server));

        if (server.getID() <= 0) {
            throw new Exception("Failed to save server configuration.");
        }

        // Refresh the list display.
        notifyDataSetChanged();
    }

    /** Delete the server from the application database.
     *  @param rowData Server item that should be removed.
     */
    public void deleteServer(Server rowData) {
        final Server selectedItem = rowData;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.server_delete_confirm_header)
                .setMessage(
                        "Are you sure you want to delete '"
                                + selectedItem.getAddress() + "'?")
                .setCancelable(false)
                .setIcon(R.drawable.icon)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (ConnectionManager.getInstance(getContext())
                                        .deleteServer(selectedItem.getID())) {
                                    remove(selectedItem);
                                    notifyDataSetChanged();
                                }

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /** Update the server record in the database and refresh the list.
     *  @param server Updated server information that should be persisted.
     *  @exception Exception If the server could not be updated in the persistent
     *  storage.
     */
    public void updateServer(Server oldServer, Server newServer)
            throws Exception {
        if (!ConnectionManager.getInstance(getContext())
                .updateServer(newServer)) {
            throw new Exception("Failed to update server configuration.");
        }

        // For some reason, notifyDataSetChanged() doesn't rebind the adapter and there
        // isn't an update method on the superclass so we have to manually remove the old 
        // item from the adapter and replace it with new values. 
        remove(oldServer);
        add(newServer);

        // Refresh the list display.
        notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** System service used to instantiate a view if referenced with an
     *  application resource ID rather than a view in the activity's 
     *  current layout.
     */
    private LayoutInflater _inflater;

    ///////////////////////////////////////////////////////////////////
    ////                inner class                                ////

    /** Define the screen elements that will make up an individual item in the
     *  list view.
     */
    static class ViewHolder {

        ///////////////////////////////////////////////////////////////////
        ////                constructor                                ////

        /** Initialize the ViewHolder with the inflated view.
         *  @param row The view container of the list item.
         */
        public ViewHolder(View row) {
            _row = row;
        }

        ///////////////////////////////////////////////////////////////////
        ////                public methods                             ////

        /** Get the TextView element that contains the server name.
         *  @return The TextView element of the item.
         */
        public TextView getText() {
            if (!(_textLine instanceof TextView)) {
                _textLine = (TextView) _row.findViewById(R.id.server_item);
            }

            return _textLine;
        }

        ///////////////////////////////////////////////////////////////////
        ////                private variables                          ////

        /** The view container of the list item represented.
         */
        private View _row;

        /** The text view widget part of the line. Used to show the name of the item.
         */
        private TextView _textLine = null;
    }
}
