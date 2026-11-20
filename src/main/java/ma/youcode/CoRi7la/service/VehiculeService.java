package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.VehiculeRequest;
import ma.youcode.CoRi7la.dto.VehiculeResponse;

public interface VehiculeService {

    VehiculeResponse ajouter(Long conducteurId, VehiculeRequest request);
}