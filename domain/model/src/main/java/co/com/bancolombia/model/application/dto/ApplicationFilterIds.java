package co.com.bancolombia.model.application.dto;

public record ApplicationFilterIds(
        Integer statusId,
        Integer loanTypeId,
        String documentId
) {}