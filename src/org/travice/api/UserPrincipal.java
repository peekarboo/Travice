
package org.travice.api;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private long userId;

    public UserPrincipal(long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return null;
    }

}
