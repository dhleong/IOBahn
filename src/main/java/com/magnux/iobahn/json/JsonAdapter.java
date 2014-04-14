
package com.magnux.iobahn.json;

import java.io.IOException;
import java.io.OutputStream;

import com.magnux.iobahn.SocketIOConnection.EventMeta;

/**
 * Provides a unified API for JSON serialization/deserialization
 *
 * @author dhleong
 */
public interface JsonAdapter {
    
    public JsonGenerator createJsonGenerator(OutputStream out) throws IOException;

    public <T> T readJson(EventMeta<T> meta, String json) throws IOException;
}
