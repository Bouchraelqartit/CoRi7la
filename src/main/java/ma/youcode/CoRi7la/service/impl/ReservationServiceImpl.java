package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.ReservationRequest;
import ma.youcode.CoRi7la.dto.ReservationResponse;
import ma.youcode.CoRi7la.entity.*;
import ma.youcode.CoRi7la.exception.BusinessConflictException;
import ma.youcode.CoRi7la.exception.ForbiddenActionException;
import ma.youcode.CoRi7la.exception.InvalidRequestException;
import ma.youcode.CoRi7la.exception.ResourceNotFoundException;
import ma.youcode.CoRi7la.mapper.ReservationMapper;
import ma.youcode.CoRi7la.repository.*;
import ma.youcode.CoRi7la.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log =
            LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper mapper;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            PassagerRepository passagerRepository,
            TrajetRepository trajetRepository,
            PointArretRepository pointArretRepository,
            ReservationMapper mapper
    ) {
        this.reservationRepository = reservationRepository;
        this.passagerRepository = passagerRepository;
        this.trajetRepository = trajetRepository;
        this.pointArretRepository = pointArretRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReservationResponse reserver(
            Long passagerId,
            ReservationRequest request
    ) {
        if (request == null
                || request.trajetId() == null
                || request.pointMonteeId() == null
                || request.pointDescenteId() == null
                || request.nombrePlaces() == null
                || request.nombrePlaces() < 1
                || request.nombrePlaces() > 8) {
            throw new InvalidRequestException(
                    "Le trajet, les points et un nombre de places entre 1 et 8 sont obligatoires."
            );
        }

        Passager passager = passagerRepository.findById(passagerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'id " + passagerId
                ));

        // Le verrou est conservé jusqu'à la fin de la transaction.
        Trajet trajet = trajetRepository
                .findByIdForUpdate(request.trajetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable avec l'id " + request.trajetId()
                ));

        if (trajet.getStatut() != StatutTrajet.publie) {
            throw new BusinessConflictException(
                    "Seul un trajet publié peut être réservé."
            );
        }

        LocalDateTime maintenant = LocalDateTime.now();

        if (!trajet.getDateDepart().isAfter(maintenant)) {
            throw new BusinessConflictException(
                    "Il est impossible de réserver un trajet déjà commencé."
            );
        }

        boolean dejaReserve = reservationRepository
                .existsByPassagerIdAndTrajetIdAndStatut(
                        passagerId,
                        trajet.getId(),
                        StatutReservation.confirmee
                );

        if (dejaReserve) {
            throw new BusinessConflictException(
                    "Ce passager possède déjà une réservation confirmée sur ce trajet."
            );
        }

        PointArret montee = pointArretRepository
                .findByIdAndTrajetId(
                        request.pointMonteeId(),
                        trajet.getId()
                )
                .orElseThrow(() -> new InvalidRequestException(
                        "Le point de montée n'existe pas sur ce trajet."
                ));

        PointArret descente = pointArretRepository
                .findByIdAndTrajetId(
                        request.pointDescenteId(),
                        trajet.getId()
                )
                .orElseThrow(() -> new InvalidRequestException(
                        "Le point de descente n'existe pas sur ce trajet."
                ));

        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new InvalidRequestException(
                    "Le point de montée doit précéder le point de descente."
            );
        }

        long placesReservees = reservationRepository
                .compterPlacesReservees(
                        trajet.getId(),
                        StatutReservation.confirmee
                );

        long placesRestantes =
                (long) trajet.getPlacesProposees() - placesReservees;

        if (request.nombrePlaces() > placesRestantes) {
            throw new BusinessConflictException(
                    "Le nombre de places restantes est insuffisant."
            );
        }

        BigDecimal prixParPlace = descente.getPrixDepuisDepart()
                .subtract(montee.getPrixDepuisDepart());

        BigDecimal montantTotal = prixParPlace
                .multiply(BigDecimal.valueOf(request.nombrePlaces()))
                .setScale(2, RoundingMode.UNNECESSARY);

        // La colonne montantTotal accepte 8 chiffres entiers et 2 décimales.
        if (montantTotal.compareTo(
                new BigDecimal("99999999.99")
        ) > 0) {
            throw new InvalidRequestException(
                    "Le montant total dépasse la limite autorisée."
            );
        }

        Reservation reservation = new Reservation(
                passager,
                trajet,
                montee,
                descente,
                request.nombrePlaces(),
                montantTotal,
                maintenant
        );

        Reservation reservationEnregistree =
                reservationRepository.save(reservation);

        if (placesRestantes == request.nombrePlaces()) {
            trajet.marquerComplet();
        }

        log.info(
                "Réservation id={} créée : passager id={}, trajet id={}, places={}, montant={}",
                reservationEnregistree.getId(),
                passagerId,
                trajet.getId(),
                request.nombrePlaces(),
                montantTotal
        );

        return mapper.toResponse(reservationEnregistree);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> listerParPassager(Long passagerId) {
        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException(
                    "Passager introuvable avec l'id " + passagerId
            );
        }

        return reservationRepository
                .findByPassagerIdOrderByDateReservationDescIdDesc(passagerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> listerParTrajetConducteur(
            Long conducteurId,
            Long trajetId
    ) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable avec l'id " + trajetId
                ));

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenActionException(
                    "Ce trajet n'appartient pas à ce conducteur."
            );
        }

        return reservationRepository
                .findByTrajetIdOrderByDateReservationDescIdDesc(trajetId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservationResponse annuler(
            Long passagerId,
            Long reservationId
    ) {
        Long trajetId = reservationRepository
                .trouverTrajetIdParReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation introuvable avec l'id " + reservationId
                ));

        Trajet trajet = trajetRepository.findByIdForUpdate(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable avec l'id " + trajetId
                ));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation introuvable avec l'id " + reservationId
                ));

        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw new ForbiddenActionException(
                    "Cette réservation n'appartient pas à ce passager."
            );
        }

        if (reservation.getStatut() != StatutReservation.confirmee) {
            throw new BusinessConflictException(
                    "Cette réservation est déjà annulée."
            );
        }

        if (trajet.getStatut() != StatutTrajet.publie
                && trajet.getStatut() != StatutTrajet.complet) {
            throw new BusinessConflictException(
                    "Il est impossible d'annuler une réservation sur ce trajet."
            );
        }

        LocalDateTime limite = trajet.getDateDepart().minusHours(24);

        if (LocalDateTime.now().isAfter(limite)) {
            throw new BusinessConflictException(
                    "L'annulation est possible au plus tard 24 heures avant le départ."
            );
        }

        reservation.annuler();
        trajet.rouvrirApresAnnulation();

        log.info(
                "Annulation de la réservation id={} par le passager id={}, trajet id={}",
                reservationId,
                passagerId,
                trajetId
        );

        return mapper.toResponse(reservation);
    }


}