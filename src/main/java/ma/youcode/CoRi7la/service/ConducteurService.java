package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.ConducteurRequest;
import ma.youcode.CoRi7la.dto.ConducteurResponse;

public interface ConducteurService {

    ConducteurResponse creer(ConducteurRequest request);
    ConducteurResponse consulterParId(Long id);
}