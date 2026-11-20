package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.VehiculeRequest;
import ma.youcode.CoRi7la.dto.VehiculeResponse;
import ma.youcode.CoRi7la.entity.Conducteur;
import ma.youcode.CoRi7la.entity.Vehicule;
import ma.youcode.CoRi7la.exception.BusinessConflictException;
import ma.youcode.CoRi7la.exception.ResourceNotFoundException;
import ma.youcode.CoRi7la.mapper.VehiculeMapper;
import ma.youcode.CoRi7la.repository.ConducteurRepository;
import ma.youcode.CoRi7la.repository.VehiculeRepository;
import ma.youcode.CoRi7la.service.VehiculeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class VehiculeServiceImpl implements VehiculeService {

    private static final Logger log =
            LoggerFactory.getLogger(VehiculeServiceImpl.class);

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper mapper;

    public VehiculeServiceImpl(
            VehiculeRepository vehiculeRepository,
            ConducteurRepository conducteurRepository,
            VehiculeMapper mapper) {
        this.vehiculeRepository = vehiculeRepository;
        this.conducteurRepository = conducteurRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public VehiculeResponse ajouter(
            Long conducteurId,
            VehiculeRequest request) {

        Conducteur conducteur = conducteurRepository.findById(conducteurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'id " + conducteurId
                ));

        String immatriculation = request.immatriculation()
                .strip()
                .toUpperCase(Locale.ROOT);

        if (vehiculeRepository.existsByImmatriculationIgnoreCase(
                immatriculation)) {
            throw new BusinessConflictException(
                    "Cette immatriculation est déjà utilisée."
            );
        }

        Vehicule vehicule = mapper.toEntity(request);
        vehicule.setImmatriculation(immatriculation);
        vehicule.setConducteur(conducteur);

        Vehicule vehiculeEnregistre = vehiculeRepository.save(vehicule);

        log.info("Création du véhicule id={} pour le conducteur id={}",
                vehiculeEnregistre.getId(), conducteurId);

        return mapper.toResponse(vehiculeEnregistre);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse consulterParId(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'id " + id
                ));

        return mapper.toResponse(vehicule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> listerParConducteur(Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException(
                    "Conducteur introuvable avec l'id " + conducteurId
            );
        }

        return vehiculeRepository
                .findByConducteurIdOrderByIdAsc(conducteurId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VehiculeResponse modifier(Long id, VehiculeRequest request) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'id " + id
                ));

        String immatriculation = request.immatriculation()
                .strip()
                .toUpperCase(Locale.ROOT);

        if (vehiculeRepository.existsByImmatriculationIgnoreCaseAndIdNot(
                immatriculation, id)) {
            throw new BusinessConflictException(
                    "Cette immatriculation est déjà utilisée."
            );
        }

        vehicule.setMarque(request.marque());
        vehicule.setModele(request.modele());
        vehicule.setCouleur(request.couleur());
        vehicule.setImmatriculation(immatriculation);
        vehicule.setCapacite(request.capacite());

        Vehicule vehiculeModifie = vehiculeRepository.save(vehicule);

        log.info("Modification du véhicule id={}", id);

        return mapper.toResponse(vehiculeModifie);
    }
}