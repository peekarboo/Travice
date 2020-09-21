
package org.travice.database;

import org.travice.model.Device;
import org.travice.model.Position;

public interface IdentityManager {

    Device getById(long id);

    Device getByUniqueId(String uniqueId) throws Exception;

    Position getLastPosition(long deviceId);

    boolean isLatestPosition(Position position);

    boolean lookupAttributeBoolean(long deviceId, String attributeName, boolean defaultValue, boolean lookupConfig);

    String lookupAttributeString(long deviceId, String attributeName, String defaultValue, boolean lookupConfig);

    int lookupAttributeInteger(long deviceId, String attributeName, int defaultValue, boolean lookupConfig);

    long lookupAttributeLong(long deviceId, String attributeName, long defaultValue, boolean lookupConfig);

}
