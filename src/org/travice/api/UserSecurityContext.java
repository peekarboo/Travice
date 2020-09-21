
package org.travice.api;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class UserSecurityContext implements SecurityContext {

    private UserPrincipal principal;

    public UserSecurityContext(UserPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return BASIC_AUTH;
    }

}
