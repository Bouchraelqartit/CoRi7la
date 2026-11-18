package ma.youcode.CoRi7la.mapper;

import ma.youcode.CoRi7la.dto.ConducteurRequest;
import ma.youcode.CoRi7la.dto.ConducteurResponse;
import ma.youcode.CoRi7la.entity.Conducteur;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ConducteurMapper {

    Conducteur toEntity(ConducteurRequest request);

    ConducteurResponse toResponse(Conducteur conducteur);
}