package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.ConducteurRequest;
import ma.youcode.CoRi7la.dto.ConducteurResponse;
import ma.youcode.CoRi7la.entity.Conducteur;
import ma.youcode.CoRi7la.exception.BusinessConflictException;
import ma.youcode.CoRi7la.mapper.ConducteurMapper;
import ma.youcode.CoRi7la.repository.ConducteurRepository;
import ma.youcode.CoRi7la.service.ConducteurService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ConducteurServiceImpl implements ConducteurService {

    private static final Logger log =
            LoggerFactory.getLogger(ConducteurServiceImpl.class);

    private final ConducteurRepository repository;
    private final ConducteurMapper mapper;

    public ConducteurServiceImpl(
            ConducteurRepository repository,
            ConducteurMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ConducteurResponse creer(ConducteurRequest request) {

        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String numeroPermis = request.numeroPermis().strip();

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new BusinessConflictException(
                    "Cet email est déjà utilisé."
            );
        }

        if (repository.existsByNumeroPermis(numeroPermis)) {
            throw new BusinessConflictException(
                    "Ce numéro de permis est déjà utilisé."
            );
        }

        Conducteur conducteur = mapper.toEntity(request);
        conducteur.setEmail(email);
        conducteur.setNumeroPermis(numeroPermis);

        Conducteur conducteurEnregistre = repository.save(conducteur);

        log.info("Création du conducteur id={}", conducteurEnregistre.getId());

        return mapper.toResponse(conducteurEnregistre);
    }
}