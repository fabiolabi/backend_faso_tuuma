# ROUTES — Faso Tuuma (API REST)

> Référence des endpoints exposés. Tous les chemins sont préfixés par `/api`.
> **Auth** : `Public` (aucun token) ou `JWT` (header `Authorization: Bearer <token>`).
> Toutes les listes sont paginées (`page` / `size`). Dates en UTC ISO-8601.

## Auth (`/api/auth`)

> Login par **téléphone**. L'access token (JWT) est court ; le refresh token (opaque, persisté hashé)
> est soumis à rotation à chaque rafraîchissement et révoqué au logout.

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| POST | `/api/auth/register` | Public | `{ firstname, lastname, phone, email?, password, role? }` | Inscription (rôle `CLIENT` par défaut, ou `ARTISAN`) → `201` + `AuthResponse` |
| POST | `/api/auth/login` | Public | `{ phone, password }` | Connexion → `200` + `AuthResponse` ; identifiants invalides → `401` |
| POST | `/api/auth/refresh` | Public | `{ refreshToken }` | Nouvelle paire de tokens (rotation) ; token invalide/expiré/révoqué → `401` |
| POST | `/api/auth/logout` | Public | `{ refreshToken }` | Révoque le refresh token → `204` |
| POST | `/api/auth/password/reset/request` | Public | `{ email }` | Envoie un code à 6 chiffres par email → `202` (réponse identique si l'email est inconnu) |
| POST | `/api/auth/password/reset/confirm` | Public | `{ email, code, newPassword }` | Vérifie le code et change le mot de passe → `204` ; code invalide/expiré → `400` |

**`AuthResponse`** : `{ accessToken, refreshToken, tokenType: "Bearer", expiresIn, user: { id, firstname, lastname, phone, email, roles } }`.

## Metier (`/api/metiers`)

> Annuaire géolocalisé des enseignes d'artisans. Une enseigne naît `is_published = false`
> (invisible en recherche) et appartient à un utilisateur `ARTISAN`. La recherche de proximité est
> un tri Haversine (SQL natif) sur les enseignes publiées et actives. La gestion (mutation) est
> réservée au **propriétaire** ou à un **ADMIN** ; les notes (`rating_avg`/`rating_count`) sont en
> lecture seule (alimentées par la feature `client` — notation, voir plus bas).

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| GET | `/api/metiers` | Public | query : `q?`, `categorySlug?`, `minRating?`, `lat?`, `lng?`, `radiusKm?`, `page`, `size` | Recherche paginée (publiées) → `200` + page de `MetierSummary`. Avec `lat`/`lng` : tri par distance croissante (`distanceKm` renseigné) |
| GET | `/api/metiers/mine` | JWT | query : `page`, `size` | Mes enseignes (publiées ou non) → `200` + page de `MetierSummary` |
| GET | `/api/metiers/{id}` | Public | — | Détail → `200` + `MetierDetail` ; non publiée et non propriétaire/ADMIN → `404` |
| POST | `/api/metiers` | JWT (ARTISAN) | `CreateMetier` | Création (`is_published=false`) → `201` + `MetierDetail` ; rôle insuffisant → `403` |
| PUT | `/api/metiers/{id}` | JWT (proprio/ADMIN) | `UpdateMetier` | Mise à jour → `200` ; autre utilisateur → `403` |
| POST | `/api/metiers/{id}/publish` | JWT (proprio/ADMIN) | — | Publie l'enseigne → `200` + `MetierDetail` |
| POST | `/api/metiers/{id}/unpublish` | JWT (proprio/ADMIN) | — | Dépublie l'enseigne → `200` + `MetierDetail` |
| DELETE | `/api/metiers/{id}` | JWT (proprio/ADMIN) | — | Suppression logique (`is_active=false`) → `204` |
| GET | `/api/metiers/{id}/services` | Public | — | Prestations → `200` + `[Service]` |
| POST | `/api/metiers/{id}/services` | JWT (proprio/ADMIN) | `{ name, description?, priceMin?, priceMax?, active? }` | Ajout d'une prestation → `201` + `Service` |
| PUT | `/api/metiers/{id}/services/{serviceId}` | JWT (proprio/ADMIN) | idem | Mise à jour → `200` + `Service` |
| DELETE | `/api/metiers/{id}/services/{serviceId}` | JWT (proprio/ADMIN) | — | Suppression → `204` |
| GET | `/api/metiers/{id}/hours` | Public | — | Horaires → `200` + `[Hourly]` |
| PUT | `/api/metiers/{id}/hours` | JWT (proprio/ADMIN) | `[{ day, openHour?, closeHour?, open }]` | Remplace tous les horaires (1 max/jour) → `200` + `[Hourly]` |
| GET | `/api/metiers/{id}/socials` | Public | — | Réseaux sociaux → `200` + `[SocialMedia]` |
| PUT | `/api/metiers/{id}/socials` | JWT (proprio/ADMIN) | `[{ platform, url }]` | Remplace tous les liens → `200` + `[SocialMedia]` |
| GET | `/api/metiers/{id}/gallery` | Public | — | Galerie → `200` + `[GalleryItem]` |
| POST | `/api/metiers/{id}/gallery` | JWT (proprio/ADMIN) | `{ fileId, position? }` | Ajoute une image (réf. `media`) → `201` + `GalleryItem` |
| DELETE | `/api/metiers/{id}/gallery/{galleryId}` | JWT (proprio/ADMIN) | — | Retire une image → `204` |

**`CreateMetier` / `UpdateMetier`** : `{ name, phone?, description?, addressDescription?, address?: { city, district?, sector?, street? }, gpsLat?, gpsLng?, categoryIds?: [..], coverFileId? }`.
**`MetierSummary`** : `{ id, name, phone, coverUrl, city, district, gpsLat, gpsLng, ratingAvg, ratingCount, categories: [slug], distanceKm, createdAt, updatedAt }`.
**`MetierDetail`** : `MetierSummary` enrichi de `{ ownerUserId, description, addressDescription, address, published, active, categories: [Category], services, hours, socials, gallery }`.

## Search (`/api/search`)

> Recherche transverse de l'annuaire (endpoints **publics**, sur les enseignes `is_published = true AND
> is_active = true`). Élargit la portée texte par rapport à `/api/metiers` : `q` matche le **nom** et la
> **description** de l'enseigne, ses **prestations** (`service.name`/`description`, actives) et sa
> **localité** (`address.city/district/sector/street`). Filtres et tri identiques à la recherche de
> proximité (Haversine SQL natif) : avec `lat`/`lng`, tri par distance croissante (`distanceKm`
> renseigné) ; sinon tri par note décroissante. `/api/metiers` reste inchangé.

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| GET | `/api/search` | Public | query : `q?`, `categorySlug?`, `minRating?`, `lat?`, `lng?`, `radiusKm?`, `page`, `size` | Recherche transverse paginée → `200` + page de `MetierSummary` (même format que `/api/metiers`) |
| GET | `/api/search/suggest` | Public | query : `q`, `limit?` (défaut 10, max 20) | Autocomplétion → `200` + `[SearchSuggestion]` ; `q` vide → liste vide |

**`SearchSuggestion`** : `{ type, label, value }` — `type` ∈ `CATEGORY` / `METIER` / `SERVICE`. Pour
`CATEGORY`, `value` = `slug` (à réinjecter en `categorySlug`) ; pour `METIER`/`SERVICE`, `value` = libellé
(à réinjecter en `q`). Suggestions plafonnées à `limit`, dédupliquées par libellé.

## Categories (`/api/categories`)

> Catégorisation des enseignes (2 niveaux : `parentId = null` = racine). Lecture publique ;
> gestion réservée au rôle `ADMIN`. Données initiales fournies par la migration `V3`.

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| GET | `/api/categories` | Public | — | Liste plate (avec `parentId`) → `200` + `[Category]` |
| GET | `/api/categories/{id}` | Public | — | Détail → `200` + `Category` ; inconnu → `404` |
| POST | `/api/categories` | JWT (ADMIN) | `{ name, slug?, parentId? }` | Création → `201` + `Category` ; non-ADMIN → `403` |
| PUT | `/api/categories/{id}` | JWT (ADMIN) | `{ name, slug?, parentId? }` | Mise à jour → `200` + `Category` |
| DELETE | `/api/categories/{id}` | JWT (ADMIN) | — | Suppression → `204` |

**`Category`** : `{ id, parentId, name, slug }`.

## Media (`/api/media`)

> Stockage générique de fichiers. Le binaire est uploadé en `multipart/form-data` vers le backend,
> écrit sur le système de fichiers local ; l'API renvoie une `url` de téléchargement
> (`/api/media/{id}`). Images uniquement (JPEG, PNG, WebP), max 5 Mo.

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| POST | `/api/media` | JWT | `multipart/form-data` : `file` | Upload d'un fichier → `201` + `MediaFile` ; type non autorisé ou vide → `400` ; > 5 Mo → `413` |
| GET | `/api/media/{id}` | Public | — | Télécharge le binaire (`Content-Type` d'origine, `inline`) ; id inconnu → `404` |
| GET | `/api/media/{id}/info` | Public | — | Métadonnées du fichier → `200` + `MediaFile` ; id inconnu → `404` |
| DELETE | `/api/media/{id}` | JWT | — | Supprime le fichier (auteur ou `ADMIN`) → `204` ; autre utilisateur → `403` |

**`MediaFile`** : `{ id, url, originalName, contentType, sizeBytes, createdAt }`.

## Client — Demandes de prestation (`/api/client/orders`, `/api/artisan/orders`)

> Un client (rôle `CLIENT`) adresse une demande de prestation à une enseigne **publiée**. La demande
> naît `PENDING`. L'artisan **propriétaire** de l'enseigne (ou un `ADMIN`) traite la demande
> (`accept`/`reject`/`complete`) ; le client peut l'annuler tant qu'elle est `PENDING` ou `ACCEPTED`.
> Statuts : `PENDING → ACCEPTED | REJECTED` (artisan), `ACCEPTED → COMPLETED` (artisan),
> `PENDING | ACCEPTED → CANCELLED` (client). Les autres transitions → `400`.

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| POST | `/api/client/orders` | JWT (CLIENT) | `CreateServiceOrder` | Dépôt d'une demande (`PENDING`) → `201` + `ServiceOrder` ; enseigne non publiée → `404` ; prestation hors enseigne → `400` |
| GET | `/api/client/orders` | JWT (CLIENT) | query : `status?`, `page`, `size` | Mes demandes → `200` + page de `ServiceOrder` |
| GET | `/api/client/orders/{id}` | JWT (CLIENT) | — | Détail d'une de mes demandes → `200` ; pas la mienne → `404` |
| POST | `/api/client/orders/{id}/cancel` | JWT (CLIENT) | — | Annulation → `200` ; statut non annulable → `400` |
| GET | `/api/artisan/orders` | JWT (proprio) | query : `status?`, `page`, `size` | Demandes reçues sur mes enseignes → `200` + page de `ServiceOrder` |
| POST | `/api/artisan/orders/{id}/accept` | JWT (proprio/ADMIN) | — | `PENDING → ACCEPTED` → `200` ; sinon `400` ; non-proprio → `403` |
| POST | `/api/artisan/orders/{id}/reject` | JWT (proprio/ADMIN) | — | `PENDING → REJECTED` → `200` ; sinon `400` ; non-proprio → `403` |
| POST | `/api/artisan/orders/{id}/complete` | JWT (proprio/ADMIN) | — | `ACCEPTED → COMPLETED` → `200` ; sinon `400` ; non-proprio → `403` |

**`CreateServiceOrder`** : `{ metierId, serviceId?, message?, requestedDate? }`.
**`ServiceOrder`** : `{ id, metierId, metierName, serviceId, serviceName, clientUserId, clientName, message, requestedDate, status, createdAt, updatedAt }`.

## Client — Notation des enseignes (`/api/metiers/{id}/ratings`, `/api/client/ratings`)

> Notation `metier_rating` portée par la feature `client` (remplace l'ancien modèle `comment`/Gemini).
> Un client note une enseigne **publiée** de 1 à 5 (+ commentaire), **une seule fois** par enseigne
> (upsert). Chaque écriture/suppression recalcule `metier.rating_avg`/`rating_count`. Les avis sont
> publics en lecture (sous le préfixe public `/api/metiers/**`). Noter sa propre enseigne → `400`.

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| GET | `/api/metiers/{id}/ratings` | Public | query : `page`, `size` | Avis publics d'une enseigne → `200` + page de `MetierRating` |
| POST | `/api/metiers/{id}/ratings` | JWT (CLIENT) | `Rating` | Dépose/met à jour ma note (upsert) → `200` + `MetierRating` ; auto-notation → `400` |
| DELETE | `/api/metiers/{id}/ratings/mine` | JWT (CLIENT) | — | Supprime ma note → `204` ; aucune note → `404` |
| GET | `/api/client/ratings` | JWT (CLIENT) | query : `page`, `size` | Mes notes → `200` + page de `MetierRating` |

**`Rating`** : `{ rating (1-5), comment? }`.
**`MetierRating`** : `{ id, metierId, metierName, clientUserId, clientName, rating, comment, createdAt }`.

## Client — Messagerie (`/api/conversations`)

> Messagerie client↔artisan persistée (REST ; le temps-réel WebSocket reste à venir). Au plus un fil
> par couple (client, enseigne). Seul un `CLIENT` démarre un fil ; les opérations sont réservées aux
> **participants** (le client, le propriétaire de l'enseigne, ou un `ADMIN`). Un fil/message non
> accessible répond `404` (pas de fuite d'existence).

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| POST | `/api/conversations` | JWT (CLIENT) | `StartConversation` | Démarre ou récupère le fil avec une enseigne (+ 1er message optionnel) → `200` + `Conversation` |
| GET | `/api/conversations` | JWT (participant) | query : `box?` (`client`\|`artisan`, défaut `client`), `page`, `size` | Mes fils, triés par dernier message → `200` + page de `Conversation` |
| GET | `/api/conversations/{id}` | JWT (participant) | — | Détail d'un fil → `200` ; non participant → `404` |
| GET | `/api/conversations/{id}/messages` | JWT (participant) | query : `page`, `size` | Messages (récent → ancien) → `200` + page de `Message` |
| POST | `/api/conversations/{id}/messages` | JWT (participant) | `SendMessage` | Envoie un message (maj `lastMessageAt`) → `201` + `Message` |
| POST | `/api/conversations/{id}/read` | JWT (participant) | — | Marque comme lus les messages reçus → `204` |

**`StartConversation`** : `{ metierId, firstMessage? }`. **`SendMessage`** : `{ body }`.
**`Conversation`** : `{ id, metierId, metierName, clientUserId, clientName, lastMessageAt, lastMessagePreview, unreadCount, createdAt }`.
**`Message`** : `{ id, conversationId, senderUserId, body, sentAt, readAt }`.

## Client — Espace (`/api/client/summary`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| GET | `/api/client/summary` | JWT (CLIENT) | — | Résumé d'activité → `200` + `ClientSummary` |

**`ClientSummary`** : `{ ordersPending, ordersAccepted, ordersCompleted, ordersRejected, ordersCancelled, ratingsCount, conversationsCount }`.

## Comment (`/api/comments`)

> **Remplacé par la feature `client` (notation).** La notation `metier_rating` est désormais portée par
> `POST /api/metiers/{id}/ratings` (voir « Client — Notation »). La piste « notation automatique Gemini »
> n'a pas de table dédiée dans le schéma et reste à arbitrer ; aucun endpoint `/api/comments`.

## Device (`/api/devices`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| _à compléter_ | | | | |

## Notification (`/api/notifications`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| _à compléter_ | | | | |
