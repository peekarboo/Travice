
package org.travice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.travice.Context;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return Context.getObjectMapper();
    }

}
