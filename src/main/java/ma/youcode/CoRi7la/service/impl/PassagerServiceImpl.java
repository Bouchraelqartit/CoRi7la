package ma.youcode.CoRi7la.service.impl;

import ma.youcode.CoRi7la.dto.PassagerRequest;
import ma.youcode.CoRi7la.dto.PassagerResponse;
import ma.youcode.CoRi7la.entity.Passager;
import ma.youcode.CoRi7la.exception.ResourceNotFoundException;
import ma.youcode.CoRi7la.mapper.PassagerMapper;
import ma.youcode.CoRi7la.repository.PassagerRepository;
import ma.youcode.CoRi7la.service.PassagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class PassagerServiceImpl implements PassagerService {

    private static final Logger log =
            LoggerFactory.getLogger(PassagerServiceImpl.class);

    private final PassagerRepository repository;
    private final PassagerMapper mapper;

    public PassagerServiceImpl(
            PassagerRepository repository,
            PassagerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PassagerResponse creer(PassagerRequest request) {

        Passager passager = mapper.toEntity(request);
        passager.setEmail(
                request.email().strip().toLowerCase(Locale.ROOT)
        );

        Passager passagerEnregistre = repository.save(passager);

        log.info("Création du passager id={}", passagerEnregistre.getId());

        return mapper.toResponse(passagerEnregistre);
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponse consulterParId(Long id) {

        Passager passager = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'id " + id
                ));

        return mapper.toResponse(passager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> rechercher(String email) {

        List<Passager> passagers;

        if (email == null || email.isBlank()) {
            passagers = repository.findAll();
        } else {
            passagers = repository.findByEmailIgnoreCase(email.strip());
        }

        return passagers.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PassagerResponse modifier(Long id, PassagerRequest request) {

        Passager passager = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'id " + id
                ));

        passager.setNom(request.nom());
        passager.setPrenom(request.prenom());
        passager.setEmail(
                request.email().strip().toLowerCase(Locale.ROOT)
        );
        passager.setTelephone(request.telephone());

        Passager passagerModifie = repository.save(passager);

        log.info("Modification du passager id={}", id);

        return mapper.toResponse(passagerModifie);
    }
}