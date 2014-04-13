package com.magnux.iobahn;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.codehaus.jackson.type.TypeReference;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.util.Log;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

public class SocketIOConnection extends WebSocketConnection implements SocketIO {

    private static final boolean DEBUG = true;
    private static final String TAG = SocketIOConnection.class.getName();

    // / The message handler of the background writer.
    protected SocketIOWriter mWriterHandler;

    // / RNG for IDs.
    private final Random mRng = new Random();

    // / Set of chars to be used for IDs.
    private static final char[] mBase64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
            .toCharArray();

    
    private static int mHeartbeat = 10000;
    
    /**
     * Event subscription metadata.
     */
    public static class EventMeta<T> {

        // Event handler to be fired on.
        public final EventHandler mEventHandler;

        // Desired event type or null.
        public final Class<T> mEventClass;

        // Desired event type or null.
        public final TypeReference<T> mEventTypeRef;

        EventMeta(EventHandler<T> handler, Class<T> resultClass) {
            this.mEventHandler = handler;
            this.mEventClass = resultClass;
            this.mEventTypeRef = null;
        }

        EventMeta(EventHandler<T> handler, TypeReference<T> resultTypeReference) {
            this.mEventHandler = handler;
            this.mEventClass = null;
            this.mEventTypeRef = resultTypeReference;
        }

    }

    // / Metadata about active event subscriptions.
    private final ConcurrentHashMap<String, EventMeta<?>> mEvents = new ConcurrentHashMap<String, EventMeta<?>>();

    // / The session handler provided to connect().
    private SocketIO.ConnectionHandler mSessionHandler;

    /**
     * Create the connection transmitting leg writer.
     */
    protected void createWriter() {

        mWriterThread = new HandlerThread("SocketIOWriter");
        mWriterThread.start();
        mWriter = new SocketIOWriter(mWriterThread.getLooper(), mMasterHandler, mTransportChannel, mOptions);

        if (DEBUG)
            Log.d(TAG, "writer created and started");
    }

    /**
     * Create the connection receiving leg reader.
     */
    protected void createReader() {
        mReader = new SocketIOReader(mEvents, mMasterHandler, mTransportChannel, mOptions, "SocketIOReader");
        mReader.start();

        if (DEBUG)
            Log.d(TAG, "reader created and started");
    }
        
    @Override
    public void connect(String wsUri, SocketIO.ConnectionHandler sessionHandler) {
        SocketIOOptions options = new SocketIOOptions();
        options.setReceiveTextMessagesRaw(true);
        options.setMaxMessagePayloadSize(64*1024);
        options.setMaxFramePayloadSize(64*1024);
        options.setTcpNoDelay(true);

        connect(wsUri, sessionHandler, options);
    }
        
    /**
     * Connect to server.
     *
     * @param wsUri            WebSockets server URI.
     * @param sessionHandler   The session handler to fire callbacks on.
     */
    @Override
    public void connect(String wsUri, SocketIO.ConnectionHandler sessionHandler, SocketIOOptions options) {

       mSessionHandler = sessionHandler;
       mEvents.clear();
       
       new SocketIOConnector(wsUri, sessionHandler, options).execute();
    }
    
    
    /**
     * Asynch socket connector.
     */
    private class SocketIOConnector extends AsyncTask<Void, Void, String> {
        String wsUri;
        final SocketIO.ConnectionHandler sessionHandler;
        final SocketIOOptions options;

        public SocketIOConnector(final String wsUri, 
                final SocketIO.ConnectionHandler sessionHandler, 
                final SocketIOOptions options) {
            super();
            this.wsUri = wsUri;
            this.sessionHandler = sessionHandler;
            this.options = options;
        }
        
        @Override
        protected String doInBackground(Void... params) {

            Thread.currentThread().setName("SocketIOConnector");

            try {
                final HttpPost post = new HttpPost("http"
                    + wsUri.substring(2) + "/socket.io/1/");
                final String line = downloadUriAsString(post);

                // TODO there's no need to allocate an array
                //  just for these... but since connect will
                //  not happen often, let's not over-optimize yet
                final String[] parts = line.split(":");
                final String sessionId = parts[0];
                final String heartbeat = parts[1];
                if (!"".equals(heartbeat))
                    mHeartbeat = Integer.parseInt(heartbeat) / 2 * 1000;
                final String transportsLine = parts[3];
                if (transportsLine.indexOf("websocket") == -1)
                    throw new Exception("websocket not supported");

                wsUri = wsUri+"/socket.io/1/websocket/" + sessionId;
                
                return null;
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String reason) {
            if (reason != null) {
                mSessionHandler.onClose(WebSocketConnectionHandler.CLOSE_CANNOT_CONNECT, reason);
            } else {
                resumeConnect(wsUri, sessionHandler, options);
            }
        }

    }
    
    private static String downloadUriAsString(final HttpUriRequest req) throws IOException {
        AndroidHttpClient client = AndroidHttpClient.newInstance("IOBahn");
        try {
            HttpResponse res = client.execute(req);
            return readToEnd(res.getEntity().getContent());
        }
        finally {
            client.close();
        }
    }
    
    private static String readToEnd(InputStream input) throws IOException {
        return new String(readToEndAsArray(input));
    }
    
    private static byte[] readToEndAsArray(InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(input);
        byte[] stuff = new byte[1024];
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        int read = 0;
        while ((read = dis.read(stuff)) != -1) {
            buff.write(stuff, 0, read);
        }

        return buff.toByteArray();
    }
    
    private void resumeConnect(String wsUri, SocketIO.ConnectionHandler sessionHandler, SocketIOOptions options){
        
        try {
            connect(wsUri, new String[] {"socket.io"}, new WebSocketConnectionHandler() {

               @Override
               public void onOpen() {
                  if (mSessionHandler != null) {
                     mSessionHandler.onOpen();
                  } else {
                     if (DEBUG) Log.d(TAG, "could not call onOpen() .. handler already NULL");
                  }
               }

               @Override
               public void onClose(int code, String reason) {
                  if (mSessionHandler != null) {
                     mSessionHandler.onClose(code, reason);
                  } else {
                     if (DEBUG) Log.d(TAG, "could not call onClose() .. handler already NULL");
                  }
               }

            }, options);

         } catch (WebSocketException e) {

            if (mSessionHandler != null) {
               mSessionHandler.onClose(WebSocketConnectionHandler.CLOSE_CANNOT_CONNECT, "cannot connect (" + e.toString() + ")");
            } else {
               if (DEBUG) Log.d(TAG, "could not call onClose() .. handler already NULL");
            }
         }
    }
    
    
    /**
     * Process SocketIO messages coming from the background reader.
     */
    protected void processAppMessage(Object message) {

       if (message instanceof SocketIOMessage.Event) {

           SocketIOMessage.Event event = (SocketIOMessage.Event) message;

          if (mEvents.containsKey(event.mName)) {
             final EventMeta<?> meta = mEvents.get(event.mName);
             if (meta != null && meta.mEventHandler != null) {
                meta.mEventHandler.onEvent(event.mEvent);
                SocketIOMessage.ACK ack = new SocketIOMessage.ACK(event.mId,null);
                mWriter.forward(ack);
             }
          }
       } else if (message instanceof SocketIOMessage.Connect) {

           SocketIOMessage.Connect connect = (SocketIOMessage.Connect) message;
           startHeartbeat();
           
          if (DEBUG) 
              Log.d(TAG, "Endpoint: " + connect.mEndpoint + " Params: " + connect.mParams);

       } else {

          if (DEBUG) Log.d(TAG, "unknown SocketIO message in SocketIOConnection.processAppMessage");
       }
    }
    
    @Override
    public void disconnect() {
    	final SocketIOMessage.Disconnect dis = new SocketIOMessage.Disconnect(null);
        mWriter.forward(dis);
        super.disconnect();
    }
    
    @Override
    public void disconnect(final String endpoint) {
    	final SocketIOMessage.Disconnect dis = new SocketIOMessage.Disconnect(endpoint);
        mWriter.forward(dis);
    }
    
    private void on(final String name, final EventMeta<?> meta) {
        mEvents.put(name, meta);
    }
    
    
    @Override
    public <T> void on(final String name, final Class<T> eventType, 
            final EventHandler<T> eventHandler) {
        on(name, new EventMeta<T>(eventHandler, eventType));
    }

    @Override
    public <T> void on(final String name, final TypeReference<T> eventType, 
            final EventHandler<T> eventHandler) {
        on(name, new EventMeta<T>(eventHandler, eventType));
    }

    @Override
    public void emit(final String name, final Object event) {
        SocketIOMessage.Emit msg = new SocketIOMessage.Emit(name, event);
        mWriter.forward(msg);
    }
    
    private void startHeartbeat() {
        // TODO does this need to be its own thread? can we reuse an existing one?
        new Thread() {

            @Override
            public void run() {
                while (isConnected()) {
                    try {
                        Thread.sleep(mHeartbeat);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final SocketIOMessage.Heartbeat hbeat = new SocketIOMessage.Heartbeat();
                    mWriter.forward(hbeat);
                }
            };
        }.start();
    }

}