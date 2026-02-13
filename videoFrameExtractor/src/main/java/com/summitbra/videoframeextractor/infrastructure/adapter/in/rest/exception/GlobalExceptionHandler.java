package com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.exception;

import com.summitbra.videoframeextractor.application.usecase.ProcessVideoFramesService;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.filesystem.FileSystemAdapter;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.ffmpeg.FFmpegVideoFrameProcessor;
import com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.dto.VideoProcessingResponseDto;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String GENERIC_ERROR_MESSAGE = "Erro interno do servidor";
    private static final String FILE_SIZE_LIMIT_MESSAGE = "Arquivo muito grande. Limite máximo: 15MB";

    @ExceptionHandler(ProcessVideoFramesService.VideoProcessingException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleVideoProcessingException(
            ProcessVideoFramesService.VideoProcessingException ex) {
        log.error("Erro no processamento de vídeo: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(FFmpegVideoFrameProcessor.VideoProcessingException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleFFmpegException(
            FFmpegVideoFrameProcessor.VideoProcessingException ex) {
        log.error("Erro no FFmpeg: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro no processamento de vídeo: " + ex.getMessage());
    }

    @ExceptionHandler(FileSystemAdapter.FileSystemException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleFileSystemException(
            FileSystemAdapter.FileSystemException ex) {
        log.error("Erro no sistema de arquivos: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro no sistema de arquivos: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        log.warn("Validação falhou: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Dados inválidos: " + errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleConstraintViolation(
            ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));

        log.warn("Violação de constraint: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Parâmetros inválidos: " + errorMessage);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<VideoProcessingResponseDto> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex) {
        log.warn("Arquivo muito grande: tamanho máximo excedido");
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, FILE_SIZE_LIMIT_MESSAGE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("Recurso não encontrado: {}", ex.getResourcePath());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<VideoProcessingResponseDto> handleGenericException(Exception ex) {
        log.error("Erro não tratado: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);
    }

    private ResponseEntity<VideoProcessingResponseDto> buildErrorResponse(HttpStatus status, String message) {
        VideoProcessingResponseDto errorResponse = VideoProcessingResponseDto.error(message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
