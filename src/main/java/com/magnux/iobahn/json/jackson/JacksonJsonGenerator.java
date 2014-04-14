
package com.magnux.iobahn.json.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;

public class JacksonJsonGenerator implements com.magnux.iobahn.json.JsonGenerator {

    private final JsonGenerator generator;

    public JacksonJsonGenerator(final JsonGenerator generator) {
        this.generator = generator;
    }
    
    @Override
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    @Override
    public void writeFieldName(final String arg0) throws IOException {
        generator.writeFieldName(arg0);
    }

    @Override
    public void writeNumber(final int arg0) throws IOException {
        generator.writeNumber(arg0);
    }

    @Override
    public void writeObject(final Object arg0) throws IOException {
        generator.writeObject(arg0);
    }

    @Override
    public void writeRaw(final String arg0) throws IOException {
        generator.writeRaw(arg0);
    }

    @Override
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        generator.writeStartObject();
    }

    @Override
    public void writeString(final String arg0) throws IOException {
        generator.writeString(arg0);
    }

    @Override
    public void flush() throws IOException {
        generator.flush();
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }

}
