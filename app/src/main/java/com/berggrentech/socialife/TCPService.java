package com.berggrentech.socialife;

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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

public class TCPService extends Service {

    public static final int CONNECTION_TIMEOUT = 5000;
    public static final String IP = "195.178.227.53";
    public static final int PORT = 7117;

    private Socket socket;
    private TaskManager oThread;
    private TaskManager iThread;
    private DataInputStream iStream;
    private DataOutputStream oStream;
    private volatile boolean running = false;

    boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Connects to the server via TCP.
     * Starts two threads - one for input and one for output.
     */
    void connect() {

        // don't start a new connection if we already have one
        if(running) {
            return;
        }

        running = true;

        socket = new Socket();
        oThread = new TaskManager();
        iThread = new TaskManager();

        // start thread handling output, sending messages to server
        oThread.start();
        oThread.addTask(new Runnable() {
            @Override
            public void run() {
                try {

                    socket.connect(new InetSocketAddress(InetAddress.getByName(IP), PORT), CONNECTION_TIMEOUT);

                    Controller.getInstance().onConnected();

                    iStream = new DataInputStream(socket.getInputStream());
                    oStream = new DataOutputStream(socket.getOutputStream());
                    oStream.flush();

                    // start thread handling input, reading messages from server
                    // needs to happen after iStream has been initialized
                    iThread.start();
                    iThread.addTask(new Runnable() {

                        @Override
                        public void run() {
                            while (running) {
                                try {

                                    String result = iStream.readUTF();
                                    JSONObject obj = new JSONObject(result);

                                    Log.e("RECEIVED FROM SERVER", obj.toString());

                                    String type = obj.getString("type");
                                    switch (type) {
                                        case "register":

                                            String id = obj.getString("id");

                                            Controller.getInstance().onRegister(id);

                                            break;
                                        case "groups":

                                            JSONArray groups = obj.getJSONArray("groups");

                                            ArrayList<String> grpList = new ArrayList<>();

                                            for (int i = 0; i < groups.length(); ++i) {
                                                JSONObject group = groups.getJSONObject(i);
                                                String name = group.getString("group");
                                                grpList.add(name);
                                            }

                                            Controller.getInstance().onGroupsReceived(grpList);

                                            break;
                                        case "locations":

                                            String groupname = obj.getString("group");
                                            JSONArray members = obj.getJSONArray("location");
                                            ArrayList<Member> locations = new ArrayList<Member>();

                                            for (int i = 0; i < members.length(); ++i) {
                                                JSONObject member = members.getJSONObject(i);
                                                String name = member.getString("member");
                                                String lng = member.getString("longitude");
                                                String lat = member.getString("latitude");
                                                locations.add(new Member(name, lng, lat));
                                            }

                                            Controller.getInstance().onMembersUpdated(groupname, locations);
                                            break;
                                    }
                                } catch (IOException e) {
                                    Controller.getInstance().onErrorReceived(getString(R.string.error_cannot_read));
                                } catch (Exception e) { // IOException, ClassNotFoundException
                                    Log.e("THREAD ERROR", e.getClass().toString());
                                    Log.e("THREAD ERROR", e.getMessage());
                                }
                            }
                        }
                    });
                } catch (ConnectException e) {
                    Controller.getInstance().onErrorReceived(getString(R.string.error_cannot_connect));
                    disconnect();
                } catch (Exception e) { // SocketException, UnknownHostException
                    Log.e("CONNECTION ERROR", e.getClass().toString());
                    Log.e("CONNECTION ERROR", e.getMessage());
                    disconnect();
                }
            }
        });
    }
    /**
     * Disconnects from the server.
     * Closes all streams and sockets.
     * Stops all threads.
     */
    void disconnect() {

        if(!running) {
            Controller.getInstance().onErrorReceived(getString(R.string.error_not_connected));
            return;
        }

        Log.e("SIMONS SAYS", "DISCONNECTING");
        oThread.addTask(new Runnable() {

            @Override
            public void run() {
                try {
                    if (iStream != null) {
                        iStream.close();
                        iStream = null;
                    }
                    if (oStream != null) {
                        oStream.close();
                        oStream = null;
                    }
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }

                    running = false;

                    oThread.stop();
                    iThread.stop();

                } catch(IOException ignored) { }
            }
        });
    }

    /**
     * Sends a message to the server.
     */
    void send(final String message) {

        if(!running) {
            Controller.getInstance().onErrorReceived(getString(R.string.error_not_connected));
            return;
        }

        oThread.addTask(new Runnable() {
        @Override
        public void run() {
            try {
                oStream.writeUTF(message);
                oStream.flush();
            } catch (IOException e) {
            }
        }
    });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
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

    public static class Connection implements ServiceConnection {

        private TCPService service;

        TCPService getService() {
            return service;
        }

        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            TCPService.LocalService ls = (TCPService.LocalService) binder;
            service = ls.getService();
            service.connect();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("SERVICE STATUS", "DISCONNECTED");
        }
    }
}