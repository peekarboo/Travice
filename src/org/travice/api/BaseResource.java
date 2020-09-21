
package org.travice.api;

import javax.ws.rs.core.SecurityContext;

public class BaseResource {

    @javax.ws.rs.core.Context
    private SecurityContext securityContext;

    protected long getUserId() {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        if (principal != null) {
            return principal.getUserId();
        }
        return 0;
    }
}
