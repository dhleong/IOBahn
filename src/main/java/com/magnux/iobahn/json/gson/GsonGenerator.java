package com.magnux.iobahn.json.gson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.magnux.iobahn.json.JsonGenerator;

public class GsonGenerator implements JsonGenerator {

    private final OutputStreamWriter writer;
    private final GsonJsonAdapter gson;

    private JsonWriter json;

    public GsonGenerator(final GsonJsonAdapter gson, final OutputStream out) {
        this.gson = gson;
        writer = new OutputStreamWriter(out);
    }

    @Override
    public void writeRawNumber(final int arg0) throws IOException {
        writer.write(String.valueOf(arg0));
        writer.flush();
    }

    @Override
    public void writeRaw(final String arg0) throws IOException {
        writer.write(arg0);
        writer.flush();
    }

    @Override
    public void writeStartObject() throws IOException {
        if (json == null)
            json = new JsonWriter(writer);

        json.beginObject();
    }

    @Override
    public void writeStartArray() throws IOException {
        if (json == null)
            json = new JsonWriter(writer);

        json.beginArray();
    }

    @Override
    public void writeString(final String arg0) throws IOException {
        json.value(arg0);
    }

    @Override
    public void writeObject(final Object arg0) throws IOException {
        gson.write(arg0, json);
    }

    @Override
    public void writeFieldName(final String arg0) throws IOException {
        json.name(arg0);
    }

    @Override
    public void writeEndObject() throws IOException {
        json.endObject();
    }

    @Override
    public void writeEndArray() throws IOException {
        json.endArray();
    }

    @Override
    public void flush() throws IOException {
        if (json != null)
            json.flush();
        else
            writer.flush();
    }

    @Override
    public void close() throws IOException {
        if (json != null)
            json.close();
        else
            writer.close();
    }

}
