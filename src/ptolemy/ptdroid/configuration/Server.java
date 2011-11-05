/* Structure of the servers, including all the information required to
 connect to a Ptolemy server.
 
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

import java.io.Serializable;

///////////////////////////////////////////////////////////////////
//// Server

/** Structure of the servers, including all the information required to
 *  connect to a Ptolemy server.
 *   
 *  @author Peter Foldes
 *  @version $Id: Server.java 92 2011-06-24 04:42:19Z pdf $
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class Server implements Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                constructors                               ////

    /** Initialize the server record with the provided parameters.
     * 
     *  @param address IP/hostname of the server.
     *  @param port Port number on which the server operates.
     *  @param username The optional username used in basic authentication.
     *  @param password The optional password used in basic authentication.
     */
    public Server(String address, int port, String username, String password) {
        _address = address;
        _port = port;
        _username = username;
        _password = password;
    }

    /** Initialize the server record with only the address and the port.
     * 
     *  @param address IP/hostname of the server.
     *  @param port Port number on which the server operates.
     */
    public Server(String address, int port) {
        this(address, port, null, null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Get the unique record identifier of this server.
     *  @return The primary key of the server record.
     */
    public long getID() {
        return _id;
    }

    /** Get the unique record identifier of this server.
     *  @param id The primary key of the server record.
     */
    public void setID(long id) {
        _id = id;
    }

    /** Get the IP/hostname of the server  The "http://" portion of the address 
     *  is not necessary.
     *  @return The IP/hostname of the server.
     */
    public String getAddress() {
        return _address;
    }

    /** Set the IP/hostname of the server.  The "http://" portion of the address 
     *  is not necessary.
     *  @param address The IP/hostname of the server.
     */
    public void setAddress(String address) {
        _address = address;
    }

    /** Get the port number of the server.
     *  @return The port on which the Ptolemy servlet operates.
     */
    public int getPort() {
        return _port;
    }

    /** Set the port number of the server.
     *  @param port The port on which the Ptolemy servlet operates.
     */
    public void setPort(int port) {
        _port = port;
    }

    /** Get the username to be used in basic authentication with the server.
     *  @return The username authorized on the server.
     */
    public String getUsername() {
        return _username;
    }

    /** Set the username to be used in basic authentication with the server.
     *  @param username The username authorized on the server.
     */
    public void setUsername(String username) {
        _username = username;
    }

    /** Get the password to be used in basic authentication with the server.
     *  @return The user's password on the server.
     */
    public String getPassword() {
        return _password;
    }

    /** Set the password to be used in basic authentication with the server.
     *  @param password The user's password on the server.
     */
    public void setPassword(String password) {
        _password = password;
    }

    /** Generate the qualified server address using the IP/hostname and port number.
     *  @return The address of the Ptolemy server.
     */
    @Override
    public String toString() {
        return "http://" + _address + ":" + _port + "/PtolemyServer";
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The server's identifier on the persistent storage.
     */
    private long _id;

    /** The address of the server. Can be both IP address or hostname.
     */
    private String _address;

    /** The port the server uses.
     */
    private int _port;

    /** The username for the server.
     */
    private String _username;

    /** The password for the server.
     */
    private String _password;
}
