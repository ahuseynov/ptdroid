/* Control the persistent database operations for the Android application.

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

///////////////////////////////////////////////////////////////////
//// ConnectionHelper

/** Control the persistent database operations for the Android application.
 *   
 *  @author Peter Foldes
 *  @version $Id: ConnectionHelper.java 167 2011-11-20 19:24:03Z jkillian $
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class ConnectionHelper extends SQLiteOpenHelper {

    ///////////////////////////////////////////////////////////////////
    ////                public variables                           ////

    /** The name of the database table to store server information.
     */
    public static final String TABLE_NAME = "servers";

    /** The name of the server address field in the database. 
     */
    public static final String ADDRESS = "address";

    /** The name of the server port field in the database. 
     */
    public static final String PORT = "port";

    /** The name of the field to store user names in the database. 
     */
    public static final String USERNAME = "username";

    /** The name of the field to store user passwords in the database. 
     */
    public static final String PASSWORD = "password";

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Initialize the connection helper.
     *  @param context The context of the application.
     */
    public ConnectionHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /* Log the event of a new server being added.
     * @param db The database being modified.
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("EventsData", "onCreate: " + TABLE_CREATE);
        db.execSQL(TABLE_CREATE);
    }

    /* Upgrade the database to a newer version
     * @param db The database being upgraded.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
        }

        String sql = null;
        if (oldVersion == 1) {
            sql = "ALTER TABLE " + TABLE_CREATE + " ADD NOTE TEXT;";
        } else if (oldVersion == 2) {
            sql = "";
        }

        Log.d("EventsData", "onUpgrade  : " + sql);
        if (sql != null) {
            db.execSQL(sql);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The original database version.
     */
    private static final int DATABASE_VERSION = 1;

    /** The name of the database used to store the server information.
     */
    private static final String DATABASE_NAME = "ptolemy.db";

    /** The SQL script to create the database used to store server information.
     */
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ADDRESS + " TEXT NOT NULL, " + PORT + " INTEGER NOT NULL, "
            + USERNAME + " TEXT, " + PASSWORD + " TEXT);";
}
