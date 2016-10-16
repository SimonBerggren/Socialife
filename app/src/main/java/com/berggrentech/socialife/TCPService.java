package com.berggrentech.socialife;

import android.app.ListActivity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

public class TCPService extends Service {

    public static class Connection implements ServiceConnection {

        TCPService service;

        public TCPService getService() {
            return service;
        }

        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            TCPService.LocalService ls = (TCPService.LocalService) binder;
            service = ls.getService();
            service.connect();
        }

        public void onServiceDisconnected(ComponentName arg0) {
        }
    }

    public static ArrayList<String> grpList;

    String register = "register";
    String unregister = "unregister";
    String members = "members";
    String groups = "groups";
    String location = "location";
    String locations = "locations";

    public static final String IP="195.178.227.53";
    public static final int PORT=7117; //

    private Socket socket;
    private TaskManager oThread;
    private TaskManager iThread;
    private DataInputStream iStream;
    private DataOutputStream oStream;
    private volatile boolean running = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        socket = new Socket();
        oThread = new TaskManager();
        iThread = new TaskManager();
        return Service.START_STICKY;
    }

    /**
     * Connects to the server via TCP.
     * Starts two threads - one for input and one for output.
     */
    public void connect() {

        // don't start a new connection if we already have one
        if(running)
            return;

        running = true;

        // start thread handling output, sending messages to server
        oThread.start();
        oThread.addTask(new Runnable() {
            @Override
            public void run() {
                try {

                    socket.connect(new InetSocketAddress(InetAddress.getByName(IP), PORT), 10000);
                    iStream = new DataInputStream(socket.getInputStream());
                    oStream = new DataOutputStream(socket.getOutputStream());
                    oStream.flush();

                    // start thread handling input, reading messages from server
                    // needs to happen after iStream has been initialized
                    iThread.start();
                    iThread.addTask(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                while (running) {

                                    String result = iStream.readUTF();

                                    JSONObject obj = new JSONObject(result);
                                    String type = obj.getString("type");

                                    Log.e("RECEIVED FROM SERVER", obj.toString());
                                    Log.e("TYPE FROM SERVER", type);

                                    if(type.equals(register)) {

                                        String group = obj.getString("group");
                                        String id = obj.getString("id");

                                        Log.e("RECEIVED FROM SERVER", "Added user to group " + group + " giving you an id of " + id);

                                    } else if(type.equals(unregister)) {
                                        // ignore
                                    } else if(type.equals(members)) {

                                    } else if(type.equals(groups)) {

                                        JSONArray groups = obj.getJSONArray("groups");

                                        if(groups.length() == 0) {
                                            Log.e("RECEIVED FROM SERVER", "THERE ARE NO GROUPS");
                                       } else {
                                            Log.e("RECEIVED FROM SERVER", String.valueOf(groups.length()) + " GROUPS FOUND");
                                        }

                                        grpList = new ArrayList<String>();

                                        for (int i = 0; i < groups.length(); ++i) {
                                            Log.e("JNIPOUSDFJHOIJHSD", "fdsöjofrndsahfhdsaökl");

                                            JSONObject group = groups.getJSONObject(i);
                                            String name = group.getString("group");
                                            grpList.add(name);
                                            Log.e("JSON Group Name", name);
                                        }
                                    } else if(type.equals(location)) {
                                        // ignore
                                    } else if(type.equals(locations)) {

                                    }

                                }
                            } catch(Exception e){ // IOException, ClassNotFoundException

                            }
                        }
                    });
                } catch (Exception e) { // SocketException, UnknownHostException

                }
            }
        });
    }
    /**
     * Disconnects from the server.
     * Closes all streams and sockets.
     * Stops all threads.
     */
    public void disconnect() {  oThread.addTask(new Runnable() {

            @Override
            public void run() {
                try {
                    if (iStream != null)
                        iStream.close();
                    if (oStream != null)
                        oStream.close();
                    if (socket != null)
                        socket.close();

                    running = false;

                    oThread.stop();
                    iThread.stop();

                } catch(IOException e) {
                }
            }
        });
    }

    /**
     * Sends a message to the server.
     */
    public void send(final String message) { oThread.addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    oStream.writeUTF(message);
                    oStream.flush();
                    Log.e("SERVER MESSAGE", "SENT: " + message);
                } catch (IOException e) {
                }
            }
        });
    }

    /**
     * Needed because internet said so.
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return new LocalService();
    }

    public class LocalService extends Binder {
        public TCPService getService() {
            return TCPService.this;
        }
    }
}