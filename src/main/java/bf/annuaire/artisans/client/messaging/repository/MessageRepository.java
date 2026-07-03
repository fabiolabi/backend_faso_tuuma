package bf.annuaire.artisans.client.messaging.repository;

import bf.annuaire.artisans.client.messaging.entity.Message;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux messages ({@code message}) d'un fil de discussion. */
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    Optional<Message> findTop1ByConversationIdOrderBySentAtDesc(Long conversationId);

    @Query(
            """
            SELECT m FROM Message m
            WHERE m.conversation.id IN :ids
              AND m.sentAt = (
                  SELECT MAX(m2.sentAt) FROM Message m2 WHERE m2.conversation.id = m.conversation.id
              )
            """)
    List<Message> findLatestByConversationIdIn(@Param("ids") Collection<Long> ids);

    @Query(
            """
            SELECT m.conversation.id, COUNT(m) FROM Message m
            WHERE m.conversation.id IN :ids
              AND m.sender.id <> :readerId
              AND m.readAt IS NULL
            GROUP BY m.conversation.id
            """)
    List<Object[]> countUnreadByConversationIds(
            @Param("ids") Collection<Long> ids, @Param("readerId") Long readerId);

    /** Nombre de messages non lus pour un lecteur (ceux qu'il n'a pas envoyés et sans {@code read_at}). */
    long countByConversationIdAndSender_IdNotAndReadAtIsNull(Long conversationId, Long readerId);

    /** Marque comme lus les messages de l'autre partie dans un fil. Renvoie le nombre de lignes mises à jour. */
    @Modifying
    @Query("update Message m set m.readAt = :now "
            + "where m.conversation.id = :conversationId and m.sender.id <> :readerId and m.readAt is null")
    int markRead(
            @Param("conversationId") Long conversationId,
            @Param("readerId") Long readerId,
            @Param("now") Instant now);
}
