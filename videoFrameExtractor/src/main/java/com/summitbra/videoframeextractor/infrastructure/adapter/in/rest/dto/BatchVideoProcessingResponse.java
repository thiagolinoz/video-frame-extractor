package com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta do processamento em lote de vídeos")
public class BatchVideoProcessingResponse {

    @Schema(description = "ID único do lote de processamento")
    private String batchId;

    @Schema(description = "Total de vídeos no lote")
    private int totalVideos;

    @Schema(description = "Quantidade de vídeos processados com sucesso")
    private int successCount;

    @Schema(description = "Quantidade de vídeos com erro")
    private int errorCount;

    @Schema(description = "Mensagem descritiva do lote")
    private String message;

    @Schema(description = "Resultados individuais de cada vídeo")
    @Builder.Default
    private List<VideoProcessingResponseDto> results = new ArrayList<>();

    @Schema(description = "Status geral do lote", example = "COMPLETED")
    private String status;

    @Schema(description = "Tempo de início do processamento")
    private String startTime;

    @Schema(description = "Tempo de fim do processamento")
    private String endTime;


    public static BatchVideoProcessingResponse error(String message) {
        return BatchVideoProcessingResponse.builder()
            .status("ERROR")
            .message(message)
            .totalVideos(0)
            .successCount(0)
            .errorCount(0)
            .results(new ArrayList<>())
            .build();
    }


    public double getSuccessRate() {
        if (totalVideos == 0) return 0.0;
        return (double) successCount / totalVideos * 100.0;
    }


    public boolean isFullySuccessful() {
        return errorCount == 0 && successCount == totalVideos;
    }


    public boolean hasErrors() {
        return errorCount > 0;
    }
}
