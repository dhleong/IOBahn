package com.magnux.iobahn;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.magnux.iobahn.SocketIOConnection.EventMeta;
import com.magnux.iobahn.json.JsonAdapter;

import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketMessage;
import de.tavendo.autobahn.WebSocketReader;

public class SocketIOReader extends WebSocketReader {

    private static final boolean DEBUG = true;
    private static final String TAG = SocketIOReader.class.getName();

    /** New JsonAdapter */
    private final JsonAdapter mJsonAdapter;

    // / Holds reference to event subscription map created on master.
    private final ConcurrentHashMap<String, EventMeta<?>> mEvents;

    /**
     * A reader object is created in SocketIOConnection.
     * 
     * @param events
     *            The event subscription map created on master.
     * @param master
     *            Message handler of master (used by us to notify the master).
     * @param socket
     *            The TCP socket.
     * @param options
     *            WebSockets connection options.
     * @param threadName
     *            The thread name we announce.
     */
    public SocketIOReader(final ConcurrentHashMap<String, EventMeta<?>> events, 
            final Handler master, final SocketChannel socket,
            final SocketIOOptions options, final String threadName) {
        super(master, socket, options, threadName);
        mEvents = events;

        mJsonAdapter = options.getJsonAdapter();

        if (DEBUG)
            Log.d(TAG, "created");
    }

    @Override
    protected void onTextMessage(final String payload) {
        // TODO: make error propagation consistent
        notify(new WebSocketMessage.Error(new WebSocketException("non-raw receive of text message")));
    }

    @Override
    protected void onBinaryMessage(final byte[] payload) {
        // TODO: make error propagation consistent
        notify(new WebSocketMessage.Error(new WebSocketException("received binary message")));
    }

    /**
     * Unwraps a SocketIO message which is a WebSockets text message with JSON
     * payload conforming to SocketIO.
     */
    @Override
    protected void onRawTextMessage(final byte[] payload) {

        try {
            final String message = new String(payload, "UTF8");
            final String[] parts = message.split(":", 4);
            final int msgType = Integer.parseInt(parts[0]);

            switch (msgType) {

            case SocketIOMessage.MESSAGE_TYPE_CONNECT:
                notify(new SocketIOMessage.Connect("", ""));
                break;
            case SocketIOMessage.MESSAGE_TYPE_HEARTBEAT:
                break;
            case SocketIOMessage.MESSAGE_TYPE_TEXT_MESSAGE:
                if (DEBUG)
                    Log.d(TAG, "text message currently unsupported");
                break;
            case SocketIOMessage.MESSAGE_TYPE_JSON_MESSAGE:
                if (DEBUG)
                    Log.d(TAG, "json message currently unsupported");
                break;
            case SocketIOMessage.MESSAGE_TYPE_EVENT:
                final String id = parts[1];
                final String endpoint = parts[2];
                final String dataString = parts[3];
                final JSONObject data = new JSONObject(dataString);
                final String name = data.getString("name");

                if (mEvents.containsKey(name)) {
                    final JSONArray args = data.optJSONArray("args");

                    final Object event;
                    if (args != null && args.length() > 0) {
                        final String arg = args.getString(0); // This only supports
                                                            // sending one argument!!!

                        final EventMeta<?> meta = mEvents.get(name);
                        
                        event = mJsonAdapter.readJson(meta, arg);
                    } else {
                        event = null;
                    }

                    notify(new SocketIOMessage.Event(id, endpoint, name, event));

                } else {

                    if (DEBUG)
                        Log.d(TAG, "SocketIO event for not-subscribed topic received");
                }
                break;
            case SocketIOMessage.MESSAGE_TYPE_ACK:
                break;
            case SocketIOMessage.MESSAGE_TYPE_ERROR:
                if (DEBUG)
                    Log.d(TAG, "Error, message: " + message);
                break;
            case SocketIOMessage.MESSAGE_TYPE_NOOP:
                break;
            default:
                if (DEBUG)
                    Log.d(TAG, "unknown code");
            }

        } catch (final JSONException e) {
            if (DEBUG)
                e.printStackTrace();

        } catch (final IOException e) {

            if (DEBUG)
                e.printStackTrace();

        }
    }

}
