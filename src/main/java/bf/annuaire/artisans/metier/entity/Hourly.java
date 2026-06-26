package bf.annuaire.artisans.metier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Horaire d'ouverture d'une enseigne pour un jour de la semaine (table {@code hourly}). Au plus une
 * ligne par {@code (metier, day)} (contrainte d'unicité en base). {@code day} est stocké en VARCHAR
 * via {@link DayOfWeek} (MONDAY…SUNDAY). Pas de colonnes d'audit.
 */
@Entity
@Table(name = "hourly")
@Getter
@Setter
@NoArgsConstructor
public class Hourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    // `day` est un mot réservé SQL : backticks → Hibernate le quote ("day") dans tout le SQL généré
    // (SELECT/WHERE/ORDER BY), comme la colonne créée par la migration V1.
    @Enumerated(EnumType.STRING)
    @Column(name = "`day`", nullable = false, length = 10)
    private DayOfWeek day;

    @Column(name = "open_hour")
    private LocalTime openHour;

    @Column(name = "close_hour")
    private LocalTime closeHour;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;
}
