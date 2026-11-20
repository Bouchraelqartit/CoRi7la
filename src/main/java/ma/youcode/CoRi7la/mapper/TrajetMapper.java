package ma.youcode.CoRi7la.mapper;

import ma.youcode.CoRi7la.dto.PointArretResponse;
import ma.youcode.CoRi7la.dto.TrajetResponse;
import ma.youcode.CoRi7la.entity.PointArret;
import ma.youcode.CoRi7la.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {
                ConducteurMapper.class,
                VehiculeMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TrajetMapper {

    PointArretResponse toResponse(PointArret pointArret);

    @Mapping(target = "placesRestantes", source = "placesRestantes")
    TrajetResponse toResponse(Trajet trajet, int placesRestantes);
}