package bf.annuaire.artisans.media.service;

import bf.annuaire.artisans.media.config.MediaProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Stockage des fichiers sur Cloudflare R2 via l'API S3-compatible.
 *
 * <p>Les objets sont adressés par la clé {@code storedPath} ({@code yyyy/MM/uuid.ext}).
 */
@Component
@ConditionalOnProperty(prefix = "app.media", name = "backend", havingValue = "r2")
@Slf4j
public class R2MediaStorage implements MediaStorageBackend {

    private static final DateTimeFormatter SHARD =
            DateTimeFormatter.ofPattern("yyyy/MM").withZone(ZoneOffset.UTC);

    private final S3Client s3Client;
    private final String bucket;

    public R2MediaStorage(S3Client s3Client, MediaProperties properties) {
        this.s3Client = s3Client;
        this.bucket = properties.getR2().getBucket();
    }

    @Override
    public String store(InputStream content, String extension, String contentType, long sizeBytes) {
        String key = SHARD.format(Instant.now()) + "/" + UUID.randomUUID() + extension;
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(content, sizeBytes));
            log.info("R2 putObject OK — bucket={}, key={}, size={}", bucket, key, sizeBytes);
        } catch (Exception e) {
            log.error("R2 putObject échoué — bucket={}, key={}, size={}", bucket, key, sizeBytes, e);
            throw new MediaStorageException("Échec de l'upload vers R2 : " + key, e);
        }
        return key;
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        try {
            var response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(relativePath)
                    .build());
            return new InputStreamResource(response);
        } catch (NoSuchKeyException e) {
            log.warn("R2 getObject — clé introuvable bucket={}, key={}", bucket, relativePath);
            throw new MediaStorageException("Fichier introuvable sur R2 : " + relativePath, e);
        } catch (S3Exception e) {
            log.error(
                    "R2 getObject échoué — bucket={}, key={}, awsError={}",
                    bucket,
                    relativePath,
                    e.awsErrorDetails(),
                    e);
            throw new MediaStorageException("Échec de la lecture depuis R2 : " + relativePath, e);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(relativePath)
                    .build());
        } catch (S3Exception e) {
            log.error(
                    "R2 deleteObject échoué — bucket={}, key={}, awsError={}",
                    bucket,
                    relativePath,
                    e.awsErrorDetails(),
                    e);
            throw new MediaStorageException("Échec de la suppression sur R2 : " + relativePath, e);
        }
    }

    /** Indique si l'objet existe déjà dans le bucket. */
    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new MediaStorageException("Échec de la vérification R2 : " + key, e);
        }
    }

    /** Upload un fichier local vers R2 en conservant la clé {@code storedPath}. */
    public void uploadFromPath(String key, Path localFile, String contentType, long sizeBytes) {
        if (exists(key)) {
            return;
        }
        try (InputStream input = Files.newInputStream(localFile)) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(input, sizeBytes));
        } catch (IOException e) {
            throw new MediaStorageException("Lecture locale impossible : " + localFile, e);
        } catch (S3Exception e) {
            throw new MediaStorageException("Échec de l'upload vers R2 : " + key, e);
        }
    }
}
