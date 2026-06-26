package bf.annuaire.artisans.auth.security;

import java.util.Set;

/**
 * Identité authentifiée extraite du JWT, exposée comme principal dans le {@code SecurityContext}.
 * Accessible dans les controllers via {@code @AuthenticationPrincipal AuthPrincipal principal}.
 */
public record AuthPrincipal(Long userId, String phone, Set<String> roles) {}
