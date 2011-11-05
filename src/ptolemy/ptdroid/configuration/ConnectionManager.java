/* Manages the persistent server list and hides the actual underlying
 persistent layer implementation.

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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

///////////////////////////////////////////////////////////////////
//// ConnectionManager

/** Manages the persistent server list and hides the actual underlying
 *  persistent layer implementation.
 *   
 *  @author Peter Foldes
 *  @version $Id: ConnectionManager.java 149 2011-07-30 01:41:38Z ahuseyno $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public final class ConnectionManager {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Initialize the persistent storage helper class.
     *  @param context The Android application context to use.
     */
    private ConnectionManager(Context context) {
        _helper = new ConnectionHelper(context);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Add a server to the persistent storage.
     *  @param server The new server to added to the persistent storage.
     *  @return The ID of the server assigned by the persistent storage.
     */
    public synchronized long addServer(Server server) {
        ContentValues values = new ContentValues();
        values.put(_helper.ADDRESS, server.getAddress());
        values.put(_helper.PORT, server.getPort());
        values.put(_helper.USERNAME, server.getUsername());
        values.put(_helper.PASSWORD, server.getPassword());

        SQLiteDatabase database = _helper.getWritableDatabase();
        long uniqueID = 0;

        try {
            uniqueID = database.insert(_helper.TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e("ConnectionManager", e.getMessage());
        } finally {
            if ((database != null) && (database.isOpen())) {
                database.close();
            }
        }

        return uniqueID;
    }

    /** Delete the server identified by the identifier.
     *  @param id The server identifier.
     *  @return True if the server was successfully deleted, false otherwise.
     */
    public synchronized boolean deleteServer(long id) {
        SQLiteDatabase database = _helper.getReadableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = database.delete(_helper.TABLE_NAME, BaseColumns._ID
                    + " = (?)", new String[] { String.valueOf(id) });
        } catch (Exception e) {
            Log.e("ConnectionManager", e.getMessage());
            return false;
        } finally {
            if ((database != null) && (database.isOpen())) {
                database.close();
            }
        }

        return (rowsAffected > 0);
    }

    /** Get the singleton instance.
     *  @param context The Android context used by the application.
     *  @return The single instance of the ConnectionManager class.
     */
    public static ConnectionManager getInstance(Context context) {
        if (_instance == null) {
            synchronized (ConnectionManager.class) {
                if (_instance == null) {
                    _instance = new ConnectionManager(context);
                }
            }
        }

        return _instance;
    }

    /** Get a specific server based on an id.
     *  @param id The identifier used to identify the server on the persistent storage.
     *  @return The server stored under the identifier.
     */
    public Server getServer(long id) {
        SQLiteDatabase database = _helper.getReadableDatabase();
        Server server = null;
        Cursor result = null;

        try {
            result = database.query(_helper.TABLE_NAME, null, BaseColumns._ID
                    + " = (?)", new String[] { String.valueOf(id) }, null,
                    null, null, "0, 1");

            if (result.moveToFirst()) {
                // Result comes back with address, port, username, and password
                // in that order.
                String address = result.getString(1);
                String portField = result.getString(2);
                String username = result.getString(3);
                String password = result.getString(4);

                // Validate result.
                if (address.trim().length() == 0
                        || portField.trim().length() == 0
                        || !portField.matches("[0-9]+")) {
                    throw new Exception("Validation problem.");
                }

                int port = Integer.parseInt(result.getString(2));

                server = new Server(address, port, username, password);
                server.setID(id);
            }
        } catch (Exception e) {
            Log.e("PtolemyConnection", e.getMessage());
        } finally {
            if (result != null) {
                result.close();
            }
            if ((database != null) && (database.isOpen())) {
                database.close();
            }
        }

        return server;
    }

    /** Get the list of server stored in the persistent storage.
     *  @return The list of servers stored.
     */
    public List<Server> getServers() {
        SQLiteDatabase database = _helper.getReadableDatabase();
        List<Server> servers = new ArrayList<Server>();
        Cursor results = null;

        try {
            results = database.query(_helper.TABLE_NAME, null, null, null,
                    null, null, null);
            if (results.moveToFirst()) {
                for (; !results.isAfterLast(); results.moveToNext()) {

                    // Result comes back with address, port, username, and password
                    // in that order.
                    String address = results.getString(1);
                    String port = results.getString(2);
                    String username = results.getString(3);
                    String password = results.getString(4);

                    // Validate the result.
                    if ((address.trim().length() == 0)
                            || (port.trim().length() == 0)
                            || (!port.matches("[0-9]+"))) {
                        throw new Exception("Validation problem.");
                    }

                    Server server = new Server(address, Integer.parseInt(port),
                            username, password);
                    server.setID(Integer.valueOf(results.getString(0)));
                    servers.add(server);
                }
            }
        } catch (Exception e) {
            Log.e("PtolemyConnection", e.getMessage());
        } finally {
            if (results != null) {
                results.close();
            }
            if ((database != null) && (database.isOpen())) {
                database.close();
            }
        }

        return servers;
    }

    /** Update the server details with the values of the server
     *  being passed in.
     *  @param server The server and values to be updated.
     *  @return True if the update was successful, false otherwise.
     */
    public synchronized boolean updateServer(Server server) {

        ContentValues values = new ContentValues();
        values.put(_helper.ADDRESS, server.getAddress());
        values.put(_helper.PORT, server.getPort());
        values.put(_helper.USERNAME, server.getUsername());
        values.put(_helper.PASSWORD, server.getPassword());

        SQLiteDatabase database = _helper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = database.update(_helper.TABLE_NAME, values,
                    BaseColumns._ID + " = (?)",
                    new String[] { String.valueOf(server.getID()) });

        } catch (Exception e) {
            Log.e("ConnectionManager", e.getMessage());
        } finally {
            if ((database != null) && (database.isOpen())) {
                database.close();
            }
        }

        return (rowsAffected > 0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** Static reference to the persistent storage helper class.
     *  The helper class is responsible to access the persistent storage.
     */
    private static ConnectionHelper _helper;

    /** The single instance of this ConnectionManager class.
     */
    private static volatile ConnectionManager _instance;
}
