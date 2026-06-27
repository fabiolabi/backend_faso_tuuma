package bf.annuaire.artisans.auth.controller;

import bf.annuaire.artisans.auth.dto.AuthResponse;
import bf.annuaire.artisans.auth.dto.LoginRequest;
import bf.annuaire.artisans.auth.dto.LogoutRequest;
import bf.annuaire.artisans.auth.dto.PasswordResetConfirm;
import bf.annuaire.artisans.auth.dto.PasswordResetRequest;
import bf.annuaire.artisans.auth.dto.RefreshRequest;
import bf.annuaire.artisans.auth.dto.RegisterRequest;
import bf.annuaire.artisans.auth.service.AuthService;
import bf.annuaire.artisans.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints publics d'authentification ({@code /api/auth}). Aucun JWT requis : ce sont eux qui
 * délivrent les tokens consommés par le reste de l'API.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion, rafraîchissement, mot de passe")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "Inscription d'un nouvel utilisateur (rôle CLIENT ou ARTISAN)")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Connexion par téléphone et mot de passe")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Rafraîchissement de la paire de tokens (rotation du refresh token)")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @Operation(summary = "Déconnexion : révoque le refresh token fourni")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
    }

    @Operation(summary = "Demande d'un code OTP de réinitialisation (envoyé par SMS)")
    @PostMapping("/password/reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.phone());
    }

    @Operation(summary = "Confirmation de la réinitialisation avec le code OTP reçu par SMS")
    @PostMapping("/password/reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirm request) {
        passwordResetService.confirmReset(request.phone(), request.code(), request.newPassword());
    }
}
