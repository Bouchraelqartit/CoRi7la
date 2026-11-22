package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.*;
import ma.youcode.CoRi7la.entity.*;
import ma.youcode.CoRi7la.exception.*;
import ma.youcode.CoRi7la.mapper.TrajetMapper;
import ma.youcode.CoRi7la.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock TrajetRepository trajetRepository;
    @Mock ConducteurRepository conducteurRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock TrajetMapper mapper;

    @InjectMocks TrajetServiceImpl service;

    private final LocalDateTime depart =
            LocalDateTime.now().plusDays(3);

    private Conducteur conducteur() {
        Conducteur conducteur = new Conducteur();
        ReflectionTestUtils.setField(conducteur, "id", 1L);
        return conducteur;
    }

    private Trajet creerTrajet(LocalDateTime date) {
        Trajet trajet = new Trajet(
                conducteur(), new Vehicule(), date, 3
        );
        ReflectionTestUtils.setField(trajet, "id", 10L);
        return trajet;
    }

    private PointArretRequest point(
            String ville, LocalDateTime heure, String prix
    ) {
        return new PointArretRequest(
                ville, heure, new BigDecimal(prix)
        );
    }

    private List<PointArretRequest> parcoursValide() {
        return List.of(
                point("Tanger", depart, "0.00"),
                point("Tetouan", depart.plusHours(1), "40.00"),
                point("Chefchaouen", depart.plusHours(2), "80.00")
        );
    }

    private void preparerPublication() {
        Conducteur conducteur = conducteur();
        Vehicule vehicule = new Vehicule();
        vehicule.setConducteur(conducteur);
        vehicule.setCapacite(3);

        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(2L))
                .thenReturn(Optional.of(vehicule));
    }

    private void preparerTrajetVerrouille(Trajet trajet) {
        when(trajetRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(trajet));
    }

    @Test
    void publierCreeUnTrajetAvecSesArretsOrdonnes() {
        preparerPublication();

        when(trajetRepository.save(any(Trajet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.publier(
                1L, new TrajetPublicationRequest(2L, 3, parcoursValide())
        );

        ArgumentCaptor<Trajet> captor =
                ArgumentCaptor.forClass(Trajet.class);
        verify(trajetRepository).save(captor.capture());

        Trajet trajet = captor.getValue();

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        assertThat(trajet.getDateDepart()).isEqualTo(depart);
        assertThat(trajet.getPlacesProposees()).isEqualTo(3);
        assertThat(trajet.getPointsArret())
                .extracting(PointArret::getVille)
                .containsExactly("Tanger", "Tetouan", "Chefchaouen");
        assertThat(trajet.getPointsArret())
                .extracting(PointArret::getOrdre)
                .containsExactly(1, 2, 3);

        verify(mapper).toResponse(trajet, 3);
    }

    @Test
    void publierRefuseUnVehiculeDunAutreConducteur() {
        Conducteur proprietaire = conducteur();

        when(conducteurRepository.findById(2L))
                .thenReturn(Optional.of(new Conducteur()));

        Vehicule vehicule = new Vehicule();
        vehicule.setConducteur(proprietaire);

        when(vehiculeRepository.findById(2L))
                .thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> service.publier(
                2L, new TrajetPublicationRequest(2L, 2, parcoursValide())
        )).isInstanceOf(ForbiddenActionException.class);

        verify(trajetRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void publierRefuseUnNombreDePlacesInvalide(int places) {
        preparerPublication();

        assertThatThrownBy(() -> service.publier(
                1L, new TrajetPublicationRequest(2L, places, parcoursValide())
        )).isInstanceOf(InvalidRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "un-seul-arret",
            "ville-repetee",
            "depart-passe",
            "heures-egales",
            "prix-depart-non-nul",
            "prix-non-croissants"
    })
    void publierRefuseUnParcoursInvalide(String cas) {
        preparerPublication();

        List<PointArretRequest> points = switch (cas) {
            case "un-seul-arret" -> List.of(
                    point("Tanger", depart, "0.00")
            );
            case "ville-repetee" -> List.of(
                    point("Tanger", depart, "0.00"),
                    point(" tanger ", depart.plusHours(1), "40.00")
            );
            case "depart-passe" -> List.of(
                    point("Tanger", depart.minusDays(5), "0.00"),
                    point("Tetouan", depart, "40.00")
            );
            case "heures-egales" -> List.of(
                    point("Tanger", depart, "0.00"),
                    point("Tetouan", depart, "40.00")
            );
            case "prix-depart-non-nul" -> List.of(
                    point("Tanger", depart, "10.00"),
                    point("Tetouan", depart.plusHours(1), "40.00")
            );
            case "prix-non-croissants" -> List.of(
                    point("Tanger", depart, "0.00"),
                    point("Tetouan", depart.plusHours(1), "0.00")
            );
            default -> throw new IllegalArgumentException(cas);
        };

        assertThatThrownBy(() -> service.publier(
                1L, new TrajetPublicationRequest(2L, 3, points)
        )).isInstanceOf(InvalidRequestException.class);

        verify(trajetRepository, never()).save(any());
    }

    @Test
    void consulterCalculeLesPlacesRestantes() {
        Trajet trajet = creerTrajet(depart);

        when(trajetRepository.findWithDetailsById(10L))
                .thenReturn(Optional.of(trajet));
        when(reservationRepository.compterPlacesReservees(
                10L, StatutReservation.confirmee
        )).thenReturn(2L);

        service.consulterParId(10L);

        verify(mapper).toResponse(trajet, 1);
    }

    @Test
    void annulerAnnuleLeTrajetEtSesReservationsConfirmees() {
        Trajet trajet = creerTrajet(depart);
        preparerTrajetVerrouille(trajet);

        Reservation reservation = new Reservation(
                new Passager(), trajet,
                new PointArret("Tanger", 1, depart, BigDecimal.ZERO),
                new PointArret(
                        "Tetouan", 2, depart.plusHours(1),
                        new BigDecimal("40.00")
                ),
                1, new BigDecimal("40.00"), LocalDateTime.now()
        );

        when(reservationRepository.findByTrajetIdAndStatut(
                10L, StatutReservation.confirmee
        )).thenReturn(List.of(reservation));

        service.annuler(1L, 10L);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.annule);
        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.annule);
        verify(mapper).toResponse(trajet, 3);
    }

    @Test
    void annulerRefuseUnAutreConducteur() {
        Trajet trajet = creerTrajet(depart);
        preparerTrajetVerrouille(trajet);

        assertThatThrownBy(() -> service.annuler(2L, 10L))
                .isInstanceOf(ForbiddenActionException.class);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        verifyNoInteractions(reservationRepository, mapper);
    }

    @ParameterizedTest
    @EnumSource(
            value = StatutTrajet.class,
            names = {"termine", "annule"}
    )
    void annulerRefuseUnStatutFinal(StatutTrajet statut) {
        Trajet trajet = creerTrajet(depart);
        ReflectionTestUtils.setField(trajet, "statut", statut);
        preparerTrajetVerrouille(trajet);

        assertThatThrownBy(() -> service.annuler(1L, 10L))
                .isInstanceOf(BusinessConflictException.class);

        assertThat(trajet.getStatut()).isEqualTo(statut);
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void terminerAccepteUnTrajetApresSonDepart() {
        Trajet trajet = creerTrajet(LocalDateTime.now().minusHours(1));
        preparerTrajetVerrouille(trajet);

        service.terminer(1L, 10L);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.termine);
        verify(mapper).toResponse(trajet, 3);
    }

    @Test
    void terminerRefuseAvantLeDepart() {
        Trajet trajet = creerTrajet(depart);
        preparerTrajetVerrouille(trajet);

        assertThatThrownBy(() -> service.terminer(1L, 10L))
                .isInstanceOf(BusinessConflictException.class);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void terminerRefuseUnAutreConducteur() {
        Trajet trajet = creerTrajet(depart);
        preparerTrajetVerrouille(trajet);

        assertThatThrownBy(() -> service.terminer(2L, 10L))
                .isInstanceOf(ForbiddenActionException.class);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        verifyNoInteractions(reservationRepository, mapper);
    }

    @ParameterizedTest
    @EnumSource(
            value = StatutTrajet.class,
            names = {"termine", "annule"}
    )
    void terminerRefuseUnStatutFinal(StatutTrajet statut) {
        Trajet trajet = creerTrajet(LocalDateTime.now().minusHours(1));
        ReflectionTestUtils.setField(trajet, "statut", statut);
        preparerTrajetVerrouille(trajet);

        assertThatThrownBy(() -> service.terminer(1L, 10L))
                .isInstanceOf(BusinessConflictException.class);

        assertThat(trajet.getStatut()).isEqualTo(statut);
        verifyNoInteractions(reservationRepository, mapper);
    }
}