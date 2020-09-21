
package org.travice.database;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.travice.Context;
import org.travice.helper.Log;
import org.travice.model.Group;

public class GroupsManager extends BaseObjectManager<Group> implements ManagableObjects {

    private AtomicLong groupsLastUpdate = new AtomicLong();
    private final long dataRefreshDelay;

    public GroupsManager(DataManager dataManager) {
        super(dataManager, Group.class);
        dataRefreshDelay = Context.getConfig().getLong("database.refreshDelay",
                DeviceManager.DEFAULT_REFRESH_DELAY) * 1000;
    }

    private void checkGroupCycles(Group group) {
        Set<Long> groups = new HashSet<>();
        while (group != null) {
            if (groups.contains(group.getId())) {
                throw new IllegalArgumentException("Cycle in group hierarchy");
            }
            groups.add(group.getId());
            group = getById(group.getGroupId());
        }
    }

    public void updateGroupCache(boolean force) throws SQLException {
        long lastUpdate = groupsLastUpdate.get();
        if ((force || System.currentTimeMillis() - lastUpdate > dataRefreshDelay)
                && groupsLastUpdate.compareAndSet(lastUpdate, System.currentTimeMillis())) {
            refreshItems();
        }
    }

    @Override
    public Set<Long> getAllItems() {
        Set<Long> result = super.getAllItems();
        if (result.isEmpty()) {
            try {
                updateGroupCache(true);
            } catch (SQLException e) {
                Log.warning(e);
            }
            result = super.getAllItems();
        }
        return result;
    }

    @Override
    protected void addNewItem(Group group) {
        checkGroupCycles(group);
        super.addNewItem(group);
    }

    @Override
    public void updateItem(Group group) throws SQLException {
        checkGroupCycles(group);
        super.updateItem(group);
    }

    @Override
    public Set<Long> getUserItems(long userId) {
        if (Context.getPermissionsManager() != null) {
            return Context.getPermissionsManager().getGroupPermissions(userId);
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Set<Long> getManagedItems(long userId) {
        Set<Long> result = new HashSet<>();
        result.addAll(getUserItems(userId));
        for (long managedUserId : Context.getUsersManager().getUserItems(userId)) {
            result.addAll(getUserItems(managedUserId));
        }
        return result;
    }

}
