package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.dto.request.UpdateStatusRequestDTO;
import co.com.bancolombia.api.dto.response.ApplicationResponseDTO;
import co.com.bancolombia.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "id", expression = "java(application.getId().toString())")
    ApplicationResponseDTO toResponse(Application application);

    @Mapping(target = "id", ignore = true) // Se genera en la BD
    @Mapping(target = "statusId", constant = "1")
    Application toModel(ApplicationRequestDTO applicationRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "term", ignore = true)
    @Mapping(target = "loanTypeId", ignore = true)
    @Mapping(target = "documentId", ignore = true)
    Application toModel(UpdateStatusRequestDTO updateStatusRequestDTO);

    @Named("uuidToString")
    default String map(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    default UUID map(String uuid) {
        return uuid != null ? UUID.fromString(uuid) : null;
    }
}