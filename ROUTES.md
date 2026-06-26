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
> lecture seule (alimentées par la feature `comment`).

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

## Comment (`/api/comments`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| _à compléter_ | | | | |

## Device (`/api/devices`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| _à compléter_ | | | | |

## Notification (`/api/notifications`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| _à compléter_ | | | | |
