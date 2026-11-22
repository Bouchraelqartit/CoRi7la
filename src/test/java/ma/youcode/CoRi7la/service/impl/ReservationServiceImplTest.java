package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.entity.*;
import ma.youcode.CoRi7la.exception.*;
import ma.youcode.CoRi7la.mapper.ReservationMapper;
import ma.youcode.CoRi7la.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock ReservationRepository reservationRepository;
    @Mock PassagerRepository passagerRepository;
    @Mock TrajetRepository trajetRepository;
    @Mock PointArretRepository pointArretRepository;
    @Mock ReservationMapper mapper;

    @InjectMocks ReservationServiceImpl service;

    private final LocalDateTime depart =
            LocalDateTime.now().plusDays(3);

    private Trajet creerTrajet() {
        Trajet trajet = new Trajet(
                new Conducteur(), new Vehicule(), depart, 3
        );
        ReflectionTestUtils.setField(trajet, "id", 10L);
        return trajet;
    }

    private PointArret point(String ville, int ordre, String prix) {
        return new PointArret(
                ville, ordre, depart.plusHours(ordre),
                new BigDecimal(prix)
        );
    }

    private void preparerReservation(Trajet trajet) {
        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(new Passager()));
        when(trajetRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(trajet));
    }

    private void preparerPoints() {
        when(pointArretRepository.findByIdAndTrajetId(20L, 10L))
                .thenReturn(Optional.of(point("Tetouan", 2, "40.00")));
        when(pointArretRepository.findByIdAndTrajetId(30L, 10L))
                .thenReturn(Optional.of(point("Chefchaouen", 3, "80.00")));
    }

    private Reservation preparerAnnulation(Trajet trajet) {
        Passager passager = new Passager();
        ReflectionTestUtils.setField(passager, "id", 1L);

        Reservation reservation = new Reservation(
                passager, trajet,
                point("Tetouan", 2, "40.00"),
                point("Chefchaouen", 3, "80.00"),
                1, new BigDecimal("40.00"), LocalDateTime.now()
        );

        when(reservationRepository.trouverTrajetIdParReservationId(100L))
                .thenReturn(Optional.of(10L));
        when(trajetRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(trajet));
        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        return reservation;
    }

    @Test
    void reserverCalculeLePrixEtCompleteLeTrajet() {
        Trajet trajet = creerTrajet();
        preparerReservation(trajet);
        preparerPoints();

        when(reservationRepository.compterPlacesReservees(
                10L, StatutReservation.confirmee
        )).thenReturn(1L);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 2)
        );

        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(reservationRepository).save(captor.capture());

        assertThat(captor.getValue().getMontantTotal())
                .isEqualByComparingTo("80.00");
        assertThat(captor.getValue().getNombrePlaces()).isEqualTo(2);
        assertThat(captor.getValue().getStatut())
                .isEqualTo(StatutReservation.confirmee);
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.complet);
        verify(mapper).toResponse(captor.getValue());
    }

    @Test
    void reserverGardeLeTrajetPublieSilResteDesPlaces() {
        Trajet trajet = creerTrajet();
        preparerReservation(trajet);
        preparerPoints();

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 1)
        );

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void reserverRefuseUnDoublonConfirme() {
        preparerReservation(creerTrajet());

        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                1L, 10L, StatutReservation.confirmee
        )).thenReturn(true);

        assertThatThrownBy(() -> service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 1)
        )).isInstanceOf(BusinessConflictException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserverRefuseLesPlacesInsuffisantes() {
        Trajet trajet = creerTrajet();
        preparerReservation(trajet);
        preparerPoints();

        when(reservationRepository.compterPlacesReservees(
                10L, StatutReservation.confirmee
        )).thenReturn(2L);

        assertThatThrownBy(() -> service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 2)
        )).isInstanceOf(BusinessConflictException.class);

        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        verify(reservationRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = StatutTrajet.class,
            names = {"complet", "termine", "annule"}
    )
    void reserverRefuseUnTrajetNonPublie(StatutTrajet statut) {
        Trajet trajet = creerTrajet();
        ReflectionTestUtils.setField(trajet, "statut", statut);
        preparerReservation(trajet);

        assertThatThrownBy(() -> service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 1)
        )).isInstanceOf(BusinessConflictException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserverRefuseUnTrajetDejaParti() {
        Trajet trajet = creerTrajet();
        ReflectionTestUtils.setField(
                trajet, "dateDepart", LocalDateTime.now().minusHours(1)
        );
        preparerReservation(trajet);

        assertThatThrownBy(() -> service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 1)
        )).isInstanceOf(BusinessConflictException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserverRefuseUnPointAbsentDuTrajet() {
        preparerReservation(creerTrajet());

        when(pointArretRepository.findByIdAndTrajetId(20L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 1)
        )).isInstanceOf(InvalidRequestException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserverRefuseLaMonteeApresLaDescente() {
        preparerReservation(creerTrajet());

        when(pointArretRepository.findByIdAndTrajetId(20L, 10L))
                .thenReturn(Optional.of(point("Chefchaouen", 3, "80.00")));
        when(pointArretRepository.findByIdAndTrajetId(30L, 10L))
                .thenReturn(Optional.of(point("Tetouan", 2, "40.00")));

        assertThatThrownBy(() -> service.reserver(
                1L, new ReservationRequest(10L, 20L, 30L, 1)
        )).isInstanceOf(InvalidRequestException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void annulerLibereLaReservationEtRouvreLeTrajet() {
        Trajet trajet = creerTrajet();
        trajet.marquerComplet();
        Reservation reservation = preparerAnnulation(trajet);

        service.annuler(1L, 100L);

        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.annule);
        assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.publie);
        verify(mapper).toResponse(reservation);
    }

    @Test
    void annulerRefuseUnAutrePassager() {
        Trajet trajet = creerTrajet();
        Reservation reservation = preparerAnnulation(trajet);

        assertThatThrownBy(() -> service.annuler(2L, 100L))
                .isInstanceOf(ForbiddenActionException.class);

        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.confirmee);
        verifyNoInteractions(mapper);
    }

    @Test
    void annulerRefuseMoinsDeVingtQuatreHeuresAvantDepart() {
        Trajet trajet = creerTrajet();
        ReflectionTestUtils.setField(
                trajet, "dateDepart", LocalDateTime.now().plusHours(12)
        );
        Reservation reservation = preparerAnnulation(trajet);

        assertThatThrownBy(() -> service.annuler(1L, 100L))
                .isInstanceOf(BusinessConflictException.class);

        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.confirmee);
        verifyNoInteractions(mapper);
    }

    @Test
    void annulerRefuseUneReservationDejaAnnulee() {
        Reservation reservation = preparerAnnulation(creerTrajet());
        reservation.annuler();

        assertThatThrownBy(() -> service.annuler(1L, 100L))
                .isInstanceOf(BusinessConflictException.class);

        verifyNoInteractions(mapper);
    }
}