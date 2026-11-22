package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.entity.*;
import ma.youcode.CoRi7la.exception.BusinessConflictException;
import ma.youcode.CoRi7la.mapper.ReservationMapper;
import ma.youcode.CoRi7la.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PassagerRepository passagerRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private ReservationMapper mapper;

    @InjectMocks
    private ReservationServiceImpl service;

    @Test
    void reserverCalculeLeMontantEtPasseLeTrajetAComplet() {
        Passager passager = mock(Passager.class);
        Trajet trajet = mock(Trajet.class);

        PointArret montee = new PointArret(
                "Tetouan", 2,
                LocalDateTime.now().plusDays(3),
                new BigDecimal("40.00")
        );

        PointArret descente = new PointArret(
                "Chefchaouen", 3,
                LocalDateTime.now().plusDays(3).plusHours(1),
                new BigDecimal("80.00")
        );

        ReservationRequest request =
                new ReservationRequest(10L, 20L, 30L, 2);

        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(passager));

        when(trajetRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(trajet));

        when(trajet.getId()).thenReturn(10L);
        when(trajet.getStatut()).thenReturn(StatutTrajet.publie);
        when(trajet.getDateDepart())
                .thenReturn(LocalDateTime.now().plusDays(3));
        when(trajet.getPlacesProposees()).thenReturn(3);

        when(pointArretRepository.findByIdAndTrajetId(20L, 10L))
                .thenReturn(Optional.of(montee));
        when(pointArretRepository.findByIdAndTrajetId(30L, 10L))
                .thenReturn(Optional.of(descente));

        when(reservationRepository.compterPlacesReservees(
                10L, StatutReservation.confirmee
        )).thenReturn(1L);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.reserver(1L, request);

        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(reservationRepository).save(captor.capture());

        Reservation reservation = captor.getValue();

        assertThat(reservation.getMontantTotal())
                .isEqualByComparingTo("80.00");
        assertThat(reservation.getNombrePlaces()).isEqualTo(2);
        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.confirmee);
        assertThat(reservation.getPointMontee()).isSameAs(montee);
        assertThat(reservation.getPointDescente()).isSameAs(descente);

        verify(trajet).marquerComplet();
        verify(mapper).toResponse(reservation);
    }

    @Test
    void reserverRefuseUnPassagerDejaConfirmeSurLeTrajet() {
        Passager passager = mock(Passager.class);
        Trajet trajet = mock(Trajet.class);

        ReservationRequest request =
                new ReservationRequest(10L, 20L, 30L, 1);

        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(passager));

        when(trajetRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(trajet));

        when(trajet.getId()).thenReturn(10L);
        when(trajet.getStatut()).thenReturn(StatutTrajet.publie);
        when(trajet.getDateDepart())
                .thenReturn(LocalDateTime.now().plusDays(3));

        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                1L, 10L, StatutReservation.confirmee
        )).thenReturn(true);

        assertThatThrownBy(() -> service.reserver(1L, request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("déjà une réservation confirmée");

        verify(reservationRepository, never())
                .save(any(Reservation.class));
        verify(trajet, never()).marquerComplet();
    }
}