
package org.travice.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.netty.channel.ChannelHandler;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.travice.BaseDataHandler;
import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Attribute;
import org.travice.model.Device;
import org.travice.model.Position;

@ChannelHandler.Sharable
public class ComputedAttributesHandler extends BaseDataHandler {

    private JexlEngine engine;

    private boolean mapDeviceAttributes;

    public ComputedAttributesHandler() {
        engine = new JexlEngine();
        engine.setStrict(true);
        engine.setFunctions(Collections.singletonMap("math", (Object) Math.class));
        if (Context.getConfig() != null) {
            mapDeviceAttributes = Context.getConfig().getBoolean("processing.computedAttributes.deviceAttributes");
        }
    }

    private MapContext prepareContext(Position position) {
        MapContext result = new MapContext();
        if (mapDeviceAttributes) {
            Device device = Context.getIdentityManager().getById(position.getDeviceId());
            if (device != null) {
                for (Object key : device.getAttributes().keySet()) {
                    result.set((String) key, device.getAttributes().get(key));
                }
            }
        }
        Set<Method> methods = new HashSet<>(Arrays.asList(position.getClass().getMethods()));
        methods.removeAll(Arrays.asList(Object.class.getMethods()));
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                String name = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);

                try {
                    if (!method.getReturnType().equals(Map.class)) {
                        result.set(name, method.invoke(position));
                    } else {
                        for (Object key : ((Map) method.invoke(position)).keySet()) {
                            result.set((String) key, ((Map) method.invoke(position)).get(key));
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException error) {
                    Log.warning(error);
                }
            }
        }
        return result;
    }

    public Object computeAttribute(Attribute attribute, Position position) throws JexlException {
        return engine.createExpression(attribute.getExpression()).evaluate(prepareContext(position));
    }

    @Override
    protected Position handlePosition(Position position) {
        Collection<Attribute> attributes = Context.getAttributesManager().getItems(
                Context.getAttributesManager().getAllDeviceItems(position.getDeviceId()));
        for (Attribute attribute : attributes) {
            if (attribute.getAttribute() != null) {
                Object result = null;
                try {
                    result = computeAttribute(attribute, position);
                } catch (JexlException error) {
                    Log.warning(error);
                }
                if (result != null) {
                    try {
                        switch (attribute.getType()) {
                            case "number":
                                Number numberValue = (Number) result;
                                position.getAttributes().put(attribute.getAttribute(), numberValue);
                                break;
                            case "boolean":
                                Boolean booleanValue = (Boolean) result;
                                position.getAttributes().put(attribute.getAttribute(), booleanValue);
                                break;
                            default:
                                position.getAttributes().put(attribute.getAttribute(), result.toString());
                        }
                    } catch (ClassCastException error) {
                        Log.warning(error);
                    }
                }
            }
        }
        return position;
    }

}
