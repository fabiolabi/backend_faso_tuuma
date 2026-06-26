# FEATURES — Faso Tuuma

> Inventaire et état d'avancement des features.
> État : ☑ Fait · ◐ En cours · ☐ À faire

## Socle technique

| Feature | État | Notes |
|---------|------|-------|
| Squelette Maven (Spring Boot 3.5, Java 21, jar) | ☑ | web, jpa, validation, security, websocket, flyway, h2, postgresql, mapstruct, lombok, springdoc, testcontainers, firebase-admin |
| Structure package-by-feature | ☑ | `common/` + features `auth, user, media, metier, search, client, ai, device, notification` |
| Profils Spring dev/prod (`.properties`) | ☑ | dev = H2 fichier + console + logs SQL ; prod = PostgreSQL via env ; `ddl-auto=validate` |
| Migration Flyway `V1__init.sql` | ☑ | toutes les tables de DIAGRAM.md, portable H2/Postgres, contraintes + index |
| Migration Flyway `V4__ai_rating_and_semantic.sql` | ☑ | table `service_rating` (champs IA + modération), note IA `service.rating_avg`/`rating_count`/`ai_summary`, `metier.search_embedding` ; retrait de `metier.rating_avg`/`rating_count` et de la table `metier_rating` |
| Socle transverse | ☑ | `AbstractAuditingEntity`, `ApiError`, `GlobalExceptionHandler`, Jackson UTC ISO-8601 |
| Config sécurité JWT stateless | ☑ | header `Bearer`, pas de cookie/session ; filtre JWT branché (feature auth) |
| OpenAPI / Swagger UI | ☑ | schéma `bearerAuth` |

## Features métier

| Feature | État | Notes |
|---------|------|-------|
| Auth (register / login / refresh / logout / reset password) | ☑ | Login par téléphone ; JWT (JJWT HMAC) + refresh token persisté hashé (rotation, révocation) ; reset par code 6 chiffres email (Spring Mail). Migration V2 (rôles + `password_reset_code`). Packages `user/` + `auth/` |
| Media (upload/download de fichiers) | ☑ | Store générique `media_file` ; upload multipart → FS local, download servi par l'API ; images JPEG/PNG/WebP ≤ 5 Mo ; upload/delete JWT (auteur/ADMIN), download/info publics. Package `media/` |
| Metier (recherche géoloc, publication, agrégat) | ☑ | Enseigne `metier` + adresse, catégories (N–N), services, horaires, réseaux sociaux, galerie/couverture. Recherche Haversine SQL natif filtrée `is_published=true AND is_active=true` (filtres `q`/`categorySlug`/rayon). Publication, suppression logique. Catégories : seed `V3` + lecture publique + CRUD ADMIN. Notation portée par les prestations (→ features `client.rating` + `ai`). Package `metier/` |
| Search (recherche transverse + suggestions + sémantique) | ☑ | `/api/search` : recherche `q` élargie (nom + description enseigne + prestations + localité) vs `/api/metiers` (nom seul), mêmes filtres `categorySlug`/rayon et **même tri Haversine SQL natif réutilisé**. `/api/search/semantic` : tri par sens (embeddings Gemini + cosinus en Java), repli mots-clés si IA indisponible. Autocomplétion `/api/search/suggest`. Lecture seule : réutilise l'agrégat `metier`, aucune table propre. Package `search/` |
| Client (demandes, notation, messagerie, espace) | ☑ | 4 sous-domaines : **demandes de prestation** (`service_order`, workflow de statut client/artisan) ; **notation** (`service_rating` : avis 1-5 + commentaire sur une **prestation**, upsert 1/client/prestation, statut `PENDING` puis analyse IA asynchrone) ; **messagerie** client↔artisan (`conversation`/`message`, persistée + REST) ; **espace client** (résumé d'activité). Réservé au rôle `CLIENT` ; transitions de demande réservées à l'artisan propriétaire. Package `client/` (`order`, `rating`, `messaging`, `profile`) |
| AI (notation automatique + recherche sémantique) | ☑ | Gemini via `RestClient`, derrière `app.ai.enabled` (off en dev/test ⇒ repli heuristique sans réseau). Traitement **asynchrone** (event `AFTER_COMMIT` + `@Async`) à chaque avis : analyse de sentiment, décalage note/texte, détection de faux avis + grossièreté/inapproprié, **note pondérée** du service (avis `REJECTED` masqués/exclus), **synthèse d'avis**. Embeddings d'enseigne pour la **recherche sémantique** (cosinus en Java). Migration `V4`. Package `ai/` |
| Messagerie temps-réel (WebSocket) | ◐ | Persistance + REST faits (feature `client/messaging`, `Conversation`/`Message`) ; reste la diffusion temps-réel WebSocket par-dessus |
| Device (tokens FCM) | ☐ | multi-appareil, purge des tokens invalides |
| Notification (push FCM) | ☐ | Firebase Admin ; déclencheurs naturels : demande créée → artisan, statut changé → client, nouveau message → destinataire |
