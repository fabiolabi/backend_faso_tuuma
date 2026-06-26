# FEATURES — Faso Tuuma

> Inventaire et état d'avancement des features.
> État : ☑ Fait · ◐ En cours · ☐ À faire

## Socle technique

| Feature | État | Notes |
|---------|------|-------|
| Squelette Maven (Spring Boot 3.5, Java 21, jar) | ☑ | web, jpa, validation, security, websocket, flyway, h2, postgresql, mapstruct, lombok, springdoc, testcontainers, firebase-admin |
| Structure package-by-feature | ☑ | `common/` + features `auth, user, media, metier, search, client, device, notification` |
| Profils Spring dev/prod (`.properties`) | ☑ | dev = H2 fichier + console + logs SQL ; prod = PostgreSQL via env ; `ddl-auto=validate` |
| Migration Flyway `V1__init.sql` | ☑ | toutes les tables de DIAGRAM.md, portable H2/Postgres, contraintes + index |
| Socle transverse | ☑ | `AbstractAuditingEntity`, `ApiError`, `GlobalExceptionHandler`, Jackson UTC ISO-8601 |
| Config sécurité JWT stateless | ☑ | header `Bearer`, pas de cookie/session ; filtre JWT branché (feature auth) |
| OpenAPI / Swagger UI | ☑ | schéma `bearerAuth` |

## Features métier

| Feature | État | Notes |
|---------|------|-------|
| Auth (register / login / refresh / logout / reset password) | ☑ | Login par téléphone ; JWT (JJWT HMAC) + refresh token persisté hashé (rotation, révocation) ; reset par code 6 chiffres email (Spring Mail). Migration V2 (rôles + `password_reset_code`). Packages `user/` + `auth/` |
| Media (upload/download de fichiers) | ☑ | Store générique `media_file` ; upload multipart → FS local, download servi par l'API ; images JPEG/PNG/WebP ≤ 5 Mo ; upload/delete JWT (auteur/ADMIN), download/info publics. Package `media/` |
| Metier (recherche géoloc, publication, agrégat) | ☑ | Enseigne `metier` + adresse, catégories (N–N), services, horaires, réseaux sociaux, galerie/couverture. Recherche Haversine SQL natif filtrée `is_published=true AND is_active=true` (filtres `q`/`categorySlug`/`minRating`/rayon). Publication, suppression logique. Catégories : seed `V3` + lecture publique + CRUD ADMIN. Notes en lecture seule (→ feature `client`). Package `metier/` |
| Search (recherche transverse + suggestions) | ☑ | `/api/search` : recherche `q` élargie (nom + description enseigne + prestations + localité) vs `/api/metiers` (nom seul), mêmes filtres `categorySlug`/`minRating`/rayon et **même tri Haversine SQL natif réutilisé**. Autocomplétion `/api/search/suggest` (catégories/enseignes/prestations). Lecture seule : réutilise l'agrégat `metier` (`MetierSummaryDto`, `MetierMapper`, `GeoUtils`), aucune table propre. Package `search/` |
| Client (demandes, notation, messagerie, espace) | ☑ | 4 sous-domaines : **demandes de prestation** (`service_order`, workflow de statut client/artisan) ; **notation** (`metier_rating` 1-5 + commentaire, upsert 1/client/enseigne, recalcul `metier.rating_avg`/`rating_count`) ; **messagerie** client↔artisan (`conversation`/`message`, persistée + REST) ; **espace client** (résumé d'activité). Réservé au rôle `CLIENT` ; transitions de demande réservées à l'artisan propriétaire. Aucune migration (tables déjà dans `V1`). Package `client/` (`order`, `rating`, `messaging`, `profile`) |
| ~~Comment (notation + Gemini)~~ | — | **Remplacé par `client` (notation).** `metier_rating` porté par `POST /api/metiers/{id}/ratings`. La table `Comment`/colonne `ai_rating` (notation auto Gemini) n'existe pas au schéma — piste à arbitrer. Stub `comment/` retiré |
| Messagerie temps-réel (WebSocket) | ◐ | Persistance + REST faits (feature `client/messaging`, `Conversation`/`Message`) ; reste la diffusion temps-réel WebSocket par-dessus |
| Device (tokens FCM) | ☐ | multi-appareil, purge des tokens invalides |
| Notification (push FCM) | ☐ | Firebase Admin ; déclencheurs naturels : demande créée → artisan, statut changé → client, nouveau message → destinataire |
