package com.magnux.iobahn.json.gson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.magnux.iobahn.json.JsonGenerator;

public class GsonGenerator implements JsonGenerator {

    private final OutputStreamWriter writer;
    private final JsonWriter json;
    private final Gson gson;

    public GsonGenerator(final Gson gson, final OutputStream out) {
        this.gson = gson;
        writer = new OutputStreamWriter(out);
        json = new JsonWriter(writer);
    }

    @Override
    public void writeString(final String arg0) throws IOException {
        json.value(arg0);
    }

    @Override
    public void writeStartObject() throws IOException {
        json.beginObject();
    }

    @Override
    public void writeStartArray() throws IOException {
        json.beginArray();
    }

    @Override
    public void writeRaw(final String arg0) throws IOException {
        writer.write(arg0);
    }

    @Override
    public void writeObject(final Object arg0) throws IOException {
        gson.toJson(arg0, writer);
    }

    @Override
    public void writeNumber(final int arg0) throws IOException {
        json.value(arg0);
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
        json.flush();
    }

    @Override
    public void close() throws IOException {
        json.close();
    }

}
