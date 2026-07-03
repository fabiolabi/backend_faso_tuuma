-- ============================================================================
--  Faso Tuuma — V8 : index de performance (requêtes fréquentes)
--  Portable H2 + PostgreSQL.
-- ============================================================================

-- Filtre commun : enseignes visibles en recherche
CREATE INDEX idx_metier_published_active ON metier (is_published, is_active);

-- Favoris par utilisateur (GET /api/favorites)
CREATE INDEX idx_favorite_client ON user_favorite_metier (client_user_id);

-- Compteur de notifications non lues
CREATE INDEX idx_notification_recipient_read ON notification (recipient_user_id, read_at);

-- Boîte de réception triée par dernier message
CREATE INDEX idx_conversation_client_last_msg ON conversation (client_user_id, last_message_at);
CREATE INDEX idx_conversation_metier_last_msg ON conversation (metier_id, last_message_at);

-- Dernier message et comptage non-lus par fil
CREATE INDEX idx_message_conv_sent ON message (conversation_id, sent_at);
CREATE INDEX idx_message_conv_read ON message (conversation_id, read_at);
