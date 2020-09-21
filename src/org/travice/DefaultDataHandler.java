
package org.travice;

import org.travice.helper.Log;
import org.travice.model.Position;

public class DefaultDataHandler extends BaseDataHandler {

    @Override
    protected Position handlePosition(Position position) {

        try {
            Context.getDataManager().addObject(position);
        } catch (Exception error) {
            Log.warning(error);
        }

        return position;
    }

}
