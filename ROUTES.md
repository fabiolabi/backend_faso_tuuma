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

## Business (`/api/businesses`)

| Méthode | Chemin | Auth | Payload | Description |
|---------|--------|------|---------|-------------|
| _à compléter_ | | | | |

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
