

package com.magnux.iobahn.json;

import java.io.IOException;

public interface JsonGenerator {

    public abstract void writeString(final String arg0) throws IOException;

    public abstract void writeStartObject() throws IOException;

    public abstract void writeStartArray() throws IOException;

    public abstract void writeRaw(final String arg0) throws IOException;

    public abstract void writeObject(final Object arg0) throws IOException;

    public abstract void writeNumber(final int arg0) throws IOException;

    public abstract void writeFieldName(final String arg0) throws IOException;

    public abstract void writeEndObject() throws IOException;

    public abstract void writeEndArray() throws IOException;

    public abstract void flush() throws IOException;

    public abstract void close() throws IOException;
    
}
