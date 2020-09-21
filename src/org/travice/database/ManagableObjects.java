
package org.travice.database;

import java.util.Set;

public interface ManagableObjects {

    Set<Long> getUserItems(long userId);

    Set<Long> getManagedItems(long userId);

}
