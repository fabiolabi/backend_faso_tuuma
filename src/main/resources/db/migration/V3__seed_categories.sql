-- ============================================================================
--  Faso Tuuma — V3 : catégories de métiers (données de référence)
--  Deux niveaux : catégories racines puis sous-catégories. Les enfants résolvent
--  leur parent par slug (pas d'id en dur) → portable H2 (dev) + PostgreSQL (prod).
--  Le schéma (table category) provient de V1 ; aucune modification de structure ici.
-- ============================================================================

-- ---------- Catégories racines ----------
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Mécanique', 'mecanique');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Couture & Mode', 'couture-mode');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Construction & Bâtiment', 'construction-batiment');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Bois & Ameublement', 'bois-ameublement');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Métal & Soudure', 'metal-soudure');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Beauté & Bien-être', 'beaute-bien-etre');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Alimentation', 'alimentation');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Électronique & Réparation', 'electronique-reparation');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Transport', 'transport');
INSERT INTO category (parent_id, name, slug) VALUES (NULL, 'Artisanat d''art', 'artisanat-art');

-- ---------- Sous-catégories ----------
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'mecanique'), 'Mécanicien auto', 'mecanicien-auto');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'mecanique'), 'Mécanicien moto', 'mecanicien-moto');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'mecanique'), 'Vulcanisateur', 'vulcanisateur');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'couture-mode'), 'Couturier / Tailleur', 'couturier');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'couture-mode'), 'Brodeur', 'brodeur');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'couture-mode'), 'Cordonnier', 'cordonnier');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'construction-batiment'), 'Maçon', 'macon');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'construction-batiment'), 'Plombier', 'plombier');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'construction-batiment'), 'Électricien bâtiment', 'electricien-batiment');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'construction-batiment'), 'Peintre en bâtiment', 'peintre-batiment');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'construction-batiment'), 'Carreleur', 'carreleur');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'bois-ameublement'), 'Menuisier', 'menuisier');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'bois-ameublement'), 'Ébéniste', 'ebeniste');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'metal-soudure'), 'Soudeur', 'soudeur');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'metal-soudure'), 'Ferronnier', 'ferronnier');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'beaute-bien-etre'), 'Coiffeur / Coiffeuse', 'coiffeur');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'beaute-bien-etre'), 'Esthéticienne', 'estheticienne');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'alimentation'), 'Boulanger', 'boulanger');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'alimentation'), 'Restaurateur', 'restaurateur');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'alimentation'), 'Boucher', 'boucher');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'electronique-reparation'), 'Réparateur téléphone', 'reparateur-telephone');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'electronique-reparation'), 'Frigoriste / Climatisation', 'frigoriste');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'transport'), 'Taxi', 'taxi');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'transport'), 'Livreur', 'livreur');

INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'artisanat-art'), 'Bijoutier', 'bijoutier');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'artisanat-art'), 'Sculpteur / Bronzier', 'sculpteur');
INSERT INTO category (parent_id, name, slug)
    VALUES ((SELECT id FROM category WHERE slug = 'artisanat-art'), 'Potier', 'potier');
