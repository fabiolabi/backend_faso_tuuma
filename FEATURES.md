# FEATURES — Faso Tuuma

> Inventaire et état d'avancement des features.
> État : ☑ Fait · ◐ En cours · ☐ À faire

## Socle technique

| Feature | État | Notes |
|---------|------|-------|
| Squelette Maven (Spring Boot 3.5, Java 21, jar) | ☑ | web, jpa, validation, security, websocket, flyway, h2, postgresql, mapstruct, lombok, springdoc, testcontainers, firebase-admin |
| Structure package-by-feature | ☑ | `common/` + features `auth, business, comment, device, notification` |
| Profils Spring dev/prod (`.properties`) | ☑ | dev = H2 fichier + console + logs SQL ; prod = PostgreSQL via env ; `ddl-auto=validate` |
| Migration Flyway `V1__init.sql` | ☑ | toutes les tables de DIAGRAM.md, portable H2/Postgres, contraintes + index |
| Socle transverse | ☑ | `AbstractAuditingEntity`, `ApiError`, `GlobalExceptionHandler`, Jackson UTC ISO-8601 |
| Config sécurité JWT stateless | ☑ | header `Bearer`, pas de cookie/session ; filtre JWT branché (feature auth) |
| OpenAPI / Swagger UI | ☑ | schéma `bearerAuth` |

## Features métier

| Feature | État | Notes |
|---------|------|-------|
| Auth (register / login / refresh / logout / reset password) | ☑ | Login par téléphone ; JWT (JJWT HMAC) + refresh token persisté hashé (rotation, révocation) ; reset par code 6 chiffres email (Spring Mail). Migration V2 (rôles + `password_reset_code`). Packages `user/` + `auth/` |
| Business (recherche géoloc, publication, photos) | ☐ | Haversine SQL natif, filtré `published=true` ; URLs Cloudinary |
| Comment (dépôt + notation Gemini) | ☐ | `aiRating` 1-5 via Gemini, recalcul `avgRating` |
| Device (tokens FCM) | ☐ | multi-appareil, purge des tokens invalides |
| Notification (push FCM) | ☐ | Firebase Admin |
| Messagerie temps-réel (WebSocket) | ☐ | `Conversation` / `Message` — prévu, hors socle |
