package ma.youcode.CoRi7la.entity;

import jakarta.persistence.*;

@Entity
@Table (name ="conducteurs")
public class Conducteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false, unique = true)
    private String numeroPermis;

    protected Conducteur() {
    }

}
