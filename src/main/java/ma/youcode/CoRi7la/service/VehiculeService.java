package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.VehiculeRequest;
import ma.youcode.CoRi7la.dto.VehiculeResponse;
import java.util.List;

public interface VehiculeService {

    VehiculeResponse ajouter(Long conducteurId, VehiculeRequest request);
    VehiculeResponse consulterParId(Long id);
    List<VehiculeResponse> listerParConducteur(Long conducteurId);
    VehiculeResponse modifier(Long id, VehiculeRequest request);
}