package com.berggrentech.socialife;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

public class TCPService extends Service {
    public static final String IP="195.178.227.53";
    public static final int PORT=7117; //

    private ThreadPool thread;
    private Send send;
    private Receive receive;
    private Socket socket;
    private Buffer<String> receiveBuffer;
    private Buffer<String> sendBuffer;
    private ObjectInputStream iStream;
    private ObjectOutputStream oStream;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new ThreadPool();
        receiveBuffer = new Buffer<>();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return new LocalService();
    }

    public void connect() {
        thread.start();
        thread.execute(new Connect());
    }

    public void disconnect() {
        thread.execute(new Disconnect());
    }

    public void send(String message) {
        thread.execute(new Send(message));
    }

    public String receive() throws InterruptedException {
        return receiveBuffer.pop();
    }


    public class LocalService extends Binder {
        public TCPService getService() {
            return TCPService.this;
        }
    }

    private class Receive extends ThreadPool {
        public void run() {
            String result;
            try {
                while (receive != null) {
                    result = (String) iStream.readObject();
                    receiveBuffer.insert(result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
            }
        }
    }

    private class Connect implements Runnable {
        public void run() {
            try {
                socket = new Socket(IP, PORT);
                iStream = new ObjectInputStream(socket.getInputStream());
                oStream = new ObjectOutputStream(socket.getOutputStream());
                oStream.flush();
                receiveBuffer.insert("CONNECTED");
                receive = new Receive();
                receive.start();
            } catch (Exception e) { // SocketException, UnknownHostException
                receiveBuffer.insert("EXCEPTION");
            }
        }
    }

    private class Disconnect implements Runnable {
        public void run() {
            try {
                if (iStream != null)
                    iStream.close();
                if (oStream != null)
                    oStream.close();
                if (socket != null)
                    socket.close();
                thread.stop();
                receiveBuffer.insert("CLOSED");
            } catch(IOException e) {
                receiveBuffer.insert("EXCEPTION");
            }
        }
    }

    private class Send implements Runnable {
        private String exp;

        public Send(String exp) {
            this.exp = exp;
        }

        public void run() {
            try {
                oStream.writeObject(exp);
                oStream.flush();
            } catch (IOException e) {
                receiveBuffer.insert("EXCEPTION");
            }
        }
    }
}