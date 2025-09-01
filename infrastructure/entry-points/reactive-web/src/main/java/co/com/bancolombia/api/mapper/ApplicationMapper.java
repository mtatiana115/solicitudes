package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.dto.response.ApplicationResponseDTO;
import co.com.bancolombia.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface ApplicationMapper {

    ApplicationResponseDTO toResponse(Application Application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusId", constant = "1")
    Application toModel(ApplicationRequestDTO applicationRequestDTO);
}