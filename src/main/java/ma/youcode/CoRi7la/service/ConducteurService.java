package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.ConducteurRequest;
import ma.youcode.CoRi7la.dto.ConducteurResponse;
import java.util.List;


public interface ConducteurService {

    ConducteurResponse creer(ConducteurRequest request);
    ConducteurResponse consulterParId(Long id);
    List<ConducteurResponse> rechercher(String nom, String email);
    ConducteurResponse modifier(Long id, ConducteurRequest request);
}