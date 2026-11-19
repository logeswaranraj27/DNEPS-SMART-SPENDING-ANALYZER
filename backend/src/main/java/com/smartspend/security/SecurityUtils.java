package com.smartspend.security;

import com.smartspend.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility to extract the verified user UID and information from Spring Security.
 */
public class SecurityUtils {

    /**
     * Retrieve the verified Firebase UID of the currently authenticated user.
     * Guaranteed not to trust any client-supplied body UID.
     */
    public static String getAuthenticatedUid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) principal).getUid();
        } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
            return (String) principal;
        }

        throw new UnauthorizedException("Invalid authentication credentials");
    }

    public static AuthenticatedUser getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) auth.getPrincipal();
        }
        return null;
    }
}
