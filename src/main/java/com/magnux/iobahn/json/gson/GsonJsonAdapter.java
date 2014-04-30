package com.magnux.iobahn.json.gson;

import java.io.IOException;
import java.io.OutputStream;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import com.magnux.iobahn.SocketIOConnection.EventMeta;
import com.magnux.iobahn.json.JsonAdapter;
import com.magnux.iobahn.json.JsonGenerator;

public class GsonJsonAdapter implements JsonAdapter {

    public interface ObjectWriter {
        public void write(Gson gson, Object object, JsonWriter writer);
    }

    static final ObjectWriter sDummyWriter = new ObjectWriter() {
        
        public void write(final Gson gson, final Object object, final JsonWriter out) {
            gson.toJson(object, object.getClass(), out);
        }
    };

    final Gson gson;

    private ObjectWriter writer = sDummyWriter;

    public GsonJsonAdapter() {
        this(new Gson());
    }
    
    public GsonJsonAdapter(final Gson gson) {
        this.gson = gson;
    }

    @Override
    public JsonGenerator createJsonGenerator(final OutputStream out)
            throws IOException {
        
        return new GsonGenerator(this, out);
    }

    @Override
    public <T> T readJson(final EventMeta<T> meta, final String json) throws IOException {
        return gson.fromJson(json, meta.mEventClass);
    }

    public GsonJsonAdapter withWriter(ObjectWriter newWriter) {
        writer = (newWriter == null)
            ? sDummyWriter
            : newWriter;

        return this;
    }

    void write(Object object, JsonWriter out) {
        writer.write(gson, object, out);
    }
}
