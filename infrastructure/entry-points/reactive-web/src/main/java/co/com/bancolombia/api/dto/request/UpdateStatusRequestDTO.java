package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequestDTO(
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El correo electrónico no tiene un formato válido")
    String email,

    @NotNull(message = "El id del estado de la solicitud de prestamo que desea actualizar no puede ser nulo")
    Integer statusId
){

    }
