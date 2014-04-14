package com.magnux.iobahn.json.gson;

import java.io.IOException;
import java.io.OutputStream;

import com.google.gson.Gson;
import com.magnux.iobahn.SocketIOConnection.EventMeta;
import com.magnux.iobahn.json.JsonAdapter;
import com.magnux.iobahn.json.JsonGenerator;

public class GsonJsonAdapter implements JsonAdapter {

    private final Gson gson;

    public GsonJsonAdapter() {
        this(new Gson());
    }
    
    public GsonJsonAdapter(final Gson gson) {
        this.gson = gson;
    }

    @Override
    public JsonGenerator createJsonGenerator(final OutputStream out)
            throws IOException {
        
        return new GsonGenerator(gson, out);
    }

    @Override
    public <T> T readJson(final EventMeta<T> meta, final String json) throws IOException {
        return gson.fromJson(json, meta.mEventClass);
    }
}
