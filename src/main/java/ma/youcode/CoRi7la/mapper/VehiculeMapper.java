package ma.youcode.CoRi7la.mapper;

import ma.youcode.CoRi7la.dto.VehiculeRequest;
import ma.youcode.CoRi7la.dto.VehiculeResponse;
import ma.youcode.CoRi7la.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface VehiculeMapper {

    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequest request);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    VehiculeResponse toResponse(Vehicule vehicule);
}