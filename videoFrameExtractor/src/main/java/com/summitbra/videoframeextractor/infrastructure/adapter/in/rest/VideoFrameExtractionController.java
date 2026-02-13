package com.summitbra.videoframeextractor.infrastructure.adapter.in.rest;

import com.summitbra.videoframeextractor.application.port.in.ProcessVideoFramesUseCase;
import com.summitbra.videoframeextractor.application.port.in.GetVideoProcessingUseCase;
import com.summitbra.videoframeextractor.domain.model.VideoFile;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.dto.BatchVideoProcessingResponse;
import com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.dto.VideoProcessingResponseDto;
import com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.mapper.VideoProcessingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Video Frame Extraction", description = "API para extração de frames de vídeos")
public class VideoFrameExtractionController {

    private final ProcessVideoFramesUseCase processVideoFramesUseCase;
    private final GetVideoProcessingUseCase getVideoProcessingUseCase;
    private final VideoProcessingMapper mapper;

    @Operation(
        summary = "Extrair frames de um vídeo",
        description = "Processa um arquivo de vídeo e extrai frames em intervalos especificados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Frames extraídos com sucesso",
                content = @Content(schema = @Schema(implementation = VideoProcessingResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "413", description = "Arquivo muito grande (máximo 15MB)"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(value = "/extract-frames", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoProcessingResponseDto> extractFrames(
            @Parameter(description = "Arquivo de vídeo (máximo 15MB)", required = true)
            @RequestPart("video") MultipartFile videoFile,

            @Parameter(description = "Intervalo entre frames em segundos")
            @RequestParam(value = "intervalSeconds", defaultValue = "1")
            @Min(value = 0, message = "Intervalo deve ser positivo")
            @Max(value = 60, message = "Intervalo máximo é 60 segundos")
            double intervalSeconds,

            @Parameter(description = "Número máximo de frames a extrair")
            @RequestParam(value = "maxFrames", defaultValue = "100")
            @Min(value = 1, message = "Deve extrair pelo menos 1 frame")
            @Max(value = 1000, message = "Máximo 1000 frames")
            int maxFrames,

            @Parameter(description = "Formato de saída dos frames")
            @RequestParam(value = "outputFormat", defaultValue = "PNG") String outputFormat) {

        try {
            log.info("Recebida solicitação de extração de frames: arquivo={}, intervalo={}, maxFrames={}, formato={}",
                    videoFile.getOriginalFilename(), intervalSeconds, maxFrames, outputFormat);

            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(VideoProcessingResponseDto.error("Arquivo de vídeo é obrigatório"));
            }

            String contentType = videoFile.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                    .body(VideoProcessingResponseDto.error("Apenas arquivos de vídeo são aceitos"));
            }

            if (!outputFormat.matches("(?i)^(PNG|JPG|JPEG)$")) {
                return ResponseEntity.badRequest()
                    .body(VideoProcessingResponseDto.error("Formato de saída deve ser PNG, JPG ou JPEG"));
            }

            VideoFile domainVideoFile = VideoFile.builder()
                .filename(videoFile.getOriginalFilename())
                .content(videoFile.getBytes())
                .size(videoFile.getSize())
                .contentType(videoFile.getContentType())
                .build();

            ProcessVideoFramesUseCase.ProcessVideoCommand command =
                new ProcessVideoFramesUseCase.ProcessVideoCommand(intervalSeconds, maxFrames, outputFormat);

            VideoProcessingJob job = processVideoFramesUseCase.processVideo(domainVideoFile, command);

            VideoProcessingResponseDto response = mapper.toResponseDto(job);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao processar vídeo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(VideoProcessingResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Extrair frames de múltiplos vídeos",
        description = "Processa múltiplos arquivos de vídeo e extrai frames de cada um em intervalos especificados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processamento iniciado com sucesso",
                content = @Content(schema = @Schema(implementation = BatchVideoProcessingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "413", description = "Arquivos muito grandes (máximo 15MB cada)"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(value = "/extract-frames/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BatchVideoProcessingResponse> extractFramesBatch(
            @Parameter(description = "Arquivos de vídeo (máximo 15MB cada)", required = true)
            @RequestPart("videos") MultipartFile[] videoFiles,

            @Parameter(description = "Intervalo entre frames em segundos")
            @RequestParam(value = "intervalSeconds", defaultValue = "1")
            @Min(value = 0, message = "Intervalo deve ser positivo")
            @Max(value = 60, message = "Intervalo máximo é 60 segundos")
            double intervalSeconds,

            @Parameter(description = "Número máximo de frames a extrair por vídeo")
            @RequestParam(value = "maxFrames", defaultValue = "100")
            @Min(value = 1, message = "Deve extrair pelo menos 1 frame")
            @Max(value = 1000, message = "Máximo 1000 frames")
            int maxFrames,

            @Parameter(description = "Formato de saída dos frames")
            @RequestParam(value = "outputFormat", defaultValue = "PNG") String outputFormat) {

        try {
            log.info("Recebida solicitação de extração de frames em lote: {} vídeos, intervalo={}, maxFrames={}, formato={}",
                    videoFiles.length, intervalSeconds, maxFrames, outputFormat);

            if (videoFiles.length == 0) {
                return ResponseEntity.badRequest()
                    .body(BatchVideoProcessingResponse.error("Pelo menos um arquivo de vídeo é obrigatório"));
            }

            if (videoFiles.length > 10) {
                return ResponseEntity.badRequest()
                    .body(BatchVideoProcessingResponse.error("Máximo 10 vídeos por lote"));
            }

            if (!outputFormat.matches("(?i)^(PNG|JPG|JPEG)$")) {
                return ResponseEntity.badRequest()
                    .body(BatchVideoProcessingResponse.error("Formato de saída deve ser PNG, JPG ou JPEG"));
            }

            List<VideoProcessingResponseDto> results = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;

            for (MultipartFile videoFile : videoFiles) {
                try {
                    if (videoFile.isEmpty()) {
                        results.add(VideoProcessingResponseDto.error("Arquivo vazio: " + videoFile.getOriginalFilename()));
                        errorCount++;
                        continue;
                    }

                    String contentType = videoFile.getContentType();
                    if (contentType == null || !contentType.startsWith("video/")) {
                        results.add(VideoProcessingResponseDto.error("Apenas arquivos de vídeo são aceitos: " + videoFile.getOriginalFilename()));
                        errorCount++;
                        continue;
                    }

                    VideoFile domainVideoFile = VideoFile.builder()
                        .filename(videoFile.getOriginalFilename())
                        .content(videoFile.getBytes())
                        .size(videoFile.getSize())
                        .contentType(videoFile.getContentType())
                        .build();

                    ProcessVideoFramesUseCase.ProcessVideoCommand command =
                        new ProcessVideoFramesUseCase.ProcessVideoCommand(intervalSeconds, maxFrames, outputFormat);

                    VideoProcessingJob job = processVideoFramesUseCase.processVideo(domainVideoFile, command);
                    VideoProcessingResponseDto response = mapper.toResponseDto(job);

                    results.add(response);
                    successCount++;

                    log.info("Vídeo processado com sucesso: {}", videoFile.getOriginalFilename());

                } catch (Exception e) {
                    log.error("Erro ao processar vídeo {}: ", videoFile.getOriginalFilename(), e);
                    results.add(VideoProcessingResponseDto.error("Erro ao processar " + videoFile.getOriginalFilename() + ": " + e.getMessage()));
                    errorCount++;
                }
            }

            BatchVideoProcessingResponse batchResponse = new BatchVideoProcessingResponse();
            batchResponse.setBatchId(java.util.UUID.randomUUID().toString());
            batchResponse.setTotalVideos(videoFiles.length);
            batchResponse.setSuccessCount(successCount);
            batchResponse.setErrorCount(errorCount);
            batchResponse.setResults(results);
            batchResponse.setMessage(String.format("Processamento concluído: %d sucessos, %d erros", successCount, errorCount));

            return ResponseEntity.ok(batchResponse);

        } catch (Exception e) {
            log.error("Erro ao processar lote de vídeos: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BatchVideoProcessingResponse.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Obter status do processamento de um vídeo",
        description = "Consulta o status atual do processamento de um vídeo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status encontrado",
                content = @Content(schema = @Schema(implementation = VideoProcessingResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Vídeo não encontrado")
    })
    @GetMapping("/status/{videoId}")
    public ResponseEntity<VideoProcessingResponseDto> getProcessingStatus(@PathVariable String videoId) {
        return getVideoProcessingUseCase.getProcessingStatus(videoId)
            .map(job -> ResponseEntity.ok(mapper.toResponseDto(job)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Listar processamentos recentes",
        description = "Retorna os últimos processamentos de vídeos realizados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de processamentos",
                content = @Content(schema = @Schema(implementation = VideoProcessingResponseDto.class)))
    })
    @GetMapping("/recent")
    public ResponseEntity<List<VideoProcessingResponseDto>> getRecentProcessings() {
        List<VideoProcessingJob> jobs = getVideoProcessingUseCase.getRecentProcessings();
        List<VideoProcessingResponseDto> response = jobs.stream()
            .map(mapper::toResponseDto)
            .toList();
        return ResponseEntity.ok(response);
    }
}
