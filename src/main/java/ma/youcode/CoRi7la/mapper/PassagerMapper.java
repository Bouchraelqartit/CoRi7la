package ma.youcode.CoRi7la.mapper;

import ma.youcode.CoRi7la.dto.PassagerRequest;
import ma.youcode.CoRi7la.dto.PassagerResponse;
import ma.youcode.CoRi7la.entity.Passager;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PassagerMapper {

    Passager toEntity(PassagerRequest request);

    PassagerResponse toResponse(Passager passager);
}