package ma.youcode.CoRi7la.service;

import ma.youcode.CoRi7la.dto.PassagerRequest;
import ma.youcode.CoRi7la.dto.PassagerResponse;

import java.util.List;

public interface PassagerService {

    PassagerResponse creer(PassagerRequest request);

    PassagerResponse consulterParId(Long id);

    List<PassagerResponse> rechercher(String email);

    PassagerResponse modifier(Long id, PassagerRequest request);
}