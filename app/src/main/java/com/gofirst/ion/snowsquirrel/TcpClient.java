package com.gofirst.ion.snowsquirrel;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClient{

    private String server_ip;
    private int server_port;

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private Socket socket;

    private long lastUpdate;
    private long lastTime;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            if (MainActivity.LOGS)
                Log.i("Time", "Current: "+(System.currentTimeMillis() - lastTime));
            lastTime = System.currentTimeMillis();

            mBufferOut.write(message+'\n');
            mBufferOut.flush();

            lastTime = System.currentTimeMillis();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;

        try {
            if (MainActivity.LOGS)
                Log.e("socket closed", "SOCKET FORCEFULLY CLOSED ++++++++++++++++++++++++++");
            socket.close();
        }
        catch (IOException e) {
            if (MainActivity.LOGS)
                Log.e("error", e.toString());
        }
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            tryToConnect();

            try {
                //sends the message to the server
                //mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage != null && mMessageListener != null) {
                        lastUpdate = System.currentTimeMillis();
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                        if (MainActivity.LOGS)
                            Log.i("received", mServerMessage);
                    }
                    else {
                        break;
                    }

                }
                if (MainActivity.LOGS)
                    Log.i("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                if (MainActivity.LOGS)
                    Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                if (mRun)
                    run();
            }

        } catch (Exception e) {
            if (MainActivity.LOGS)
                Log.e("TCP", "C: Error", e);

        }

    }

    private void tryToConnect()
    {
        while ((socket == null || !socket.isConnected() || socket.isClosed()) && mRun) {
            try {
                InetAddress serverAddr = InetAddress.getByName(server_ip);
                if (MainActivity.LOGS)
                    Log.i("SERVER ADDR", server_ip);
                socket = new Socket();
                InetSocketAddress sa = new InetSocketAddress(serverAddr, server_port);
                socket.connect(sa, 500);
                socket.setTcpNoDelay(true);
            } catch (IOException e) { }
        }
    }


    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public long getLastUpdateTime()
    {
        return lastUpdate;
    }

    public void setIPandPORT(String ip, int port)
    {
        server_ip = ip;
        server_port = port;
    }

    public boolean isConnected() {
        if (socket != null)
            return socket.isConnected();
        else
            return false;
    }

}