
package org.travice.api;

import java.sql.SQLException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.travice.Context;
import org.travice.database.BaseObjectManager;
import org.travice.model.BaseModel;

public class SimpleObjectResource<T extends BaseModel> extends BaseObjectResource<T> {

    public SimpleObjectResource(Class<T> baseClass) {
        super(baseClass);
    }

    @GET
    public Collection<T> get(
            @QueryParam("all") boolean all, @QueryParam("userId") long userId) throws SQLException {

        BaseObjectManager<T> manager = Context.getManager(getBaseClass());
        return manager.getItems(getSimpleManagerItems(manager, all, userId));
    }

}
