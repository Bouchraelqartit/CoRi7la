package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.PointArretRequest;
import ma.youcode.CoRi7la.dto.TrajetPublicationRequest;
import ma.youcode.CoRi7la.dto.TrajetResponse;
import ma.youcode.CoRi7la.entity.Conducteur;
import ma.youcode.CoRi7la.entity.PointArret;
import ma.youcode.CoRi7la.entity.Trajet;
import ma.youcode.CoRi7la.entity.Vehicule;
import ma.youcode.CoRi7la.exception.ForbiddenActionException;
import ma.youcode.CoRi7la.exception.InvalidRequestException;
import ma.youcode.CoRi7la.exception.ResourceNotFoundException;
import ma.youcode.CoRi7la.mapper.TrajetMapper;
import ma.youcode.CoRi7la.repository.ConducteurRepository;
import ma.youcode.CoRi7la.repository.TrajetRepository;
import ma.youcode.CoRi7la.repository.VehiculeRepository;
import ma.youcode.CoRi7la.service.TrajetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TrajetServiceImpl implements TrajetService {

    private static final Logger log =
            LoggerFactory.getLogger(TrajetServiceImpl.class);

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper mapper;

    public TrajetServiceImpl(
            TrajetRepository trajetRepository,
            ConducteurRepository conducteurRepository,
            VehiculeRepository vehiculeRepository,
            TrajetMapper mapper
    ) {
        this.trajetRepository = trajetRepository;
        this.conducteurRepository = conducteurRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TrajetResponse publier(
            Long conducteurId,
            TrajetPublicationRequest request
    ) {
        Conducteur conducteur = conducteurRepository.findById(conducteurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'id " + conducteurId
                ));

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'id " + request.vehiculeId()
                ));

        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenActionException(
                    "Ce véhicule n'appartient pas à ce conducteur."
            );
        }

        if (request.placesProposees() == null
                || request.placesProposees() < 1
                || request.placesProposees() > vehicule.getCapacite()) {
            throw new InvalidRequestException(
                    "Le nombre de places doit être compris entre 1 et "
                            + vehicule.getCapacite() + "."
            );
        }

        List<PointArretRequest> points = request.pointsArret();
        verifierParcours(points);

        Trajet trajet = new Trajet(
                conducteur,
                vehicule,
                points.get(0).heurePassage(),
                request.placesProposees()
        );

        for (int i = 0; i < points.size(); i++) {
            PointArretRequest point = points.get(i);

            trajet.ajouterPointArret(new PointArret(
                    point.ville().strip(),
                    i + 1,
                    point.heurePassage(),
                    point.prixDepuisDepart()
            ));
        }

        Trajet trajetEnregistre = trajetRepository.save(trajet);

        log.info(
                "Publication du trajet id={} par le conducteur id={}, véhicule id={}",
                trajetEnregistre.getId(),
                conducteurId,
                vehicule.getId()
        );

        return mapper.toResponse(
                trajetEnregistre,
                trajetEnregistre.getPlacesProposees()
        );
    }

    private void verifierParcours(List<PointArretRequest> points) {
        if (points == null || points.size() < 2) {
            throw new InvalidRequestException(
                    "Le trajet doit contenir au moins deux points d'arrêt."
            );
        }

        Set<String> villes = new HashSet<>();
        LocalDateTime maintenant = LocalDateTime.now();

        for (int i = 0; i < points.size(); i++) {
            PointArretRequest point = points.get(i);

            if (point == null
                    || point.ville() == null
                    || point.ville().isBlank()
                    || point.heurePassage() == null
                    || point.prixDepuisDepart() == null) {
                throw new InvalidRequestException(
                        "Chaque point doit contenir une ville, une heure et un prix."
                );
            }

            String villeNormalisee =
                    point.ville().strip().toLowerCase(Locale.ROOT);

            if (!villes.add(villeNormalisee)) {
                throw new InvalidRequestException(
                        "Une ville ne peut pas apparaître plusieurs fois dans le parcours."
                );
            }

            if (i == 0) {
                if (!point.heurePassage().isAfter(maintenant)) {
                    throw new InvalidRequestException(
                            "L'heure de départ doit être dans le futur."
                    );
                }

                if (point.prixDepuisDepart().signum() != 0) {
                    throw new InvalidRequestException(
                            "Le prix du premier point doit être égal à zéro."
                    );
                }
            } else {
                PointArretRequest precedent = points.get(i - 1);

                if (!point.heurePassage().isAfter(precedent.heurePassage())) {
                    throw new InvalidRequestException(
                            "Les heures de passage doivent être strictement croissantes."
                    );
                }

                if (point.prixDepuisDepart()
                        .compareTo(precedent.prixDepuisDepart()) <= 0) {
                    throw new InvalidRequestException(
                            "Les prix depuis le départ doivent être strictement croissants."
                    );
                }
            }
        }
    }
}