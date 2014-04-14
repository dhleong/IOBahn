package com.magnux.iobahn;

import com.magnux.iobahn.json.JsonAdapter;
import com.magnux.iobahn.json.jackson.JacksonJsonAdapter;

import de.tavendo.autobahn.WebSocketOptions;

public class SocketIOOptions extends WebSocketOptions {

    private JsonAdapter jsonAdapter;

    /**
     * The type of JsonAdapter to use; defaults to
     *  using Jackson, for backwards compatibility
     *  
     */
    public JsonAdapter getJsonAdapter() {
        if (jsonAdapter == null) {
            jsonAdapter = new JacksonJsonAdapter();
        }

        return jsonAdapter;
    }

    public SocketIOOptions setJsonAdapter(final JsonAdapter adapter) {
        jsonAdapter = adapter;
        return this;
    }

}
