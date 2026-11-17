package ma.youcode.CoRi7la.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicules")

public class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false)
    private String couleur;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    @Column(nullable = false)
    private int capacite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    protected Vehicule(){

    }
}
