
package org.travice.database;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.travice.model.User;

public class UsersManager extends SimpleObjectManager<User> {

    private Map<String, User> usersTokens;

    public UsersManager(DataManager dataManager) {
        super(dataManager, User.class);
        if (usersTokens == null) {
            usersTokens = new ConcurrentHashMap<>();
        }
    }

    private void putToken(User user) {
        if (usersTokens == null) {
            usersTokens = new ConcurrentHashMap<>();
        }
        if (user.getToken() != null) {
            usersTokens.put(user.getToken(), user);
        }
    }

    @Override
    protected void addNewItem(User user) {
        super.addNewItem(user);
        putToken(user);
    }

    @Override
    protected void updateCachedItem(User user) {
        User cachedUser = getById(user.getId());
        super.updateCachedItem(user);
        putToken(user);
        if (cachedUser.getToken() != null && !cachedUser.getToken().equals(user.getToken())) {
            usersTokens.remove(cachedUser.getToken());
        }
    }

    @Override
    protected void removeCachedItem(long userId) {
        User cachedUser = getById(userId);
        if (cachedUser != null) {
            String userToken = cachedUser.getToken();
            super.removeCachedItem(userId);
            if (userToken != null) {
                usersTokens.remove(userToken);
            }
        }
    }

    @Override
    public Set<Long> getManagedItems(long userId) {
        Set<Long> result = new HashSet<>();
        result.addAll(getUserItems(userId));
        result.add(userId);
        return result;
    }

    public User getUserByToken(String token) {
        return usersTokens.get(token);
    }

}
