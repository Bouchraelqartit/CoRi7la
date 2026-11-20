package ma.youcode.CoRi7la.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "point_arrets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_arrets_trajet_ordre",
                columnNames = {"trajet_id", "ordre"}
        )
)
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ville;

    @Column(nullable = false)
    private int ordre;

    @Column(nullable = false)
    private LocalDateTime heurePassage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixDepuisDepart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    protected PointArret() {
    }

    public PointArret(String ville, int ordre, LocalDateTime heurePassage,
                      BigDecimal prixDepuisDepart) {
        this.ville = ville;
        this.ordre = ordre;
        this.heurePassage = heurePassage;
        this.prixDepuisDepart = prixDepuisDepart;
    }

    void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }

    public Long getId() {
        return id;
    }

    public String getVille() {
        return ville;
    }

    public int getOrdre() {
        return ordre;
    }

    public LocalDateTime getHeurePassage() {
        return heurePassage;
    }

    public BigDecimal getPrixDepuisDepart() {
        return prixDepuisDepart;
    }
}