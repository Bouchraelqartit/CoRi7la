package ma.youcode.CoRi7la.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trajets")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(nullable = false)
    private LocalDateTime dateDepart;

    @Column(nullable = false)
    private int placesProposees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTrajet statut;

    @OneToMany(
            mappedBy = "trajet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("ordre ASC")
    private List<PointArret> pointsArret = new ArrayList<>();

    protected Trajet() {
    }

    public Trajet(Conducteur conducteur, Vehicule vehicule,
                  LocalDateTime dateDepart, int placesProposees) {
        this.conducteur = conducteur;
        this.vehicule = vehicule;
        this.dateDepart = dateDepart;
        this.placesProposees = placesProposees;
        this.statut = StatutTrajet.publie;
    }

    public void ajouterPointArret(PointArret point) {
        pointsArret.add(point);
        point.setTrajet(this);
    }

    public Long getId() {
        return id;
    }

    public Conducteur getConducteur() {
        return conducteur;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public LocalDateTime getDateDepart() {
        return dateDepart;
    }

    public int getPlacesProposees() {
        return placesProposees;
    }

    public StatutTrajet getStatut() {
        return statut;
    }

    public List<PointArret> getPointsArret() {
        return List.copyOf(pointsArret);
    }

    public void marquerComplet() {
        if (statut != StatutTrajet.publie) {
            throw new IllegalStateException(
                    "Seul un trajet publié peut devenir complet."
            );
        }

        statut = StatutTrajet.complet;
    }

    public void rouvrirApresAnnulation() {
        if (statut == StatutTrajet.complet) {
            statut = StatutTrajet.publie;
        }
    }

    public void annuler() {
        if (statut != StatutTrajet.publie
                && statut != StatutTrajet.complet) {
            throw new IllegalStateException(
                    "Seul un trajet publié ou complet peut être annulé."
            );
        }

        statut = StatutTrajet.annule;
    }
}