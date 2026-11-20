package ma.youcode.CoRi7la.mapper;

import ma.youcode.CoRi7la.dto.ReservationResponse;
import ma.youcode.CoRi7la.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = TrajetMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ReservationMapper {

    @Mapping(target = "passagerId", source = "passager.id")
    @Mapping(target = "trajetId", source = "trajet.id")
    ReservationResponse toResponse(Reservation reservation);
}