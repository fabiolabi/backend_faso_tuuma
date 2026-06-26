/**
 * Feature <strong>media</strong> : stockage générique de fichiers (table {@code media_file}).
 *
 * <p>Upload <em>multipart</em> vers le backend, écriture sur le système de fichiers local, et
 * exposition d'une URL de téléchargement servie par l'API. Store réutilisable par les autres
 * features (photo de profil {@code Person}, couverture {@code Metier}, galerie) qui rattacheront
 * leurs colonnes {@code *_file_id} à un {@code MediaFile}.
 */
package bf.annuaire.artisans.media;
