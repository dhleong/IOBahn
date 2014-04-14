
package com.magnux.iobahn.json.jackson;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.magnux.iobahn.SocketIOConnection.EventMeta;
import com.magnux.iobahn.json.JsonAdapter;
import com.magnux.iobahn.json.JsonGenerator;

/**
 * Jackson
 *
 * @author dhleong
 */
public class JacksonJsonAdapter implements JsonAdapter {

    final JsonFactory mFactory;

    public JacksonJsonAdapter() {
        this(generateDefaultMapper());
    }
    
    public JacksonJsonAdapter(final ObjectMapper mapper) {
        mFactory = mapper.getJsonFactory();
    }
    
    @Override
    public JsonGenerator createJsonGenerator(final OutputStream out) throws IOException {
        return new JacksonJsonGenerator(mFactory.createJsonGenerator(out));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T readJson(final EventMeta<T> meta, final String json) throws IOException {

        final Object event;
        final JsonParser parser = mFactory.createJsonParser(json);
        if (meta.mEventClass != null) {
            event = parser.readValueAs(meta.mEventClass);
        } else if (meta.mEventTypeRef != null) {
            event = parser.readValueAs(meta.mEventTypeRef);
        } else {
            event = null;
        }

        parser.close();
        return (T) event;
    }

    private static ObjectMapper generateDefaultMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

}
