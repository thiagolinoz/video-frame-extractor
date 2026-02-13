package com.summitbra.videoframeextractor.infrastructure.config;

import com.summitbra.videoframeextractor.application.port.in.CleanupVideoProcessingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledCleanupService {

    private final CleanupVideoProcessingUseCase cleanupUseCase;

    @Scheduled(cron = "${app.cleanup.cron:0 0 2 * * ?}")
    public void cleanupOldProcessings() {
        log.info("Iniciando limpeza automática de processamentos antigos");
        try {
            cleanupUseCase.cleanupOldProcessings(7); // 7 dias
            log.info("Limpeza automática concluída com sucesso");
        } catch (Exception e) {
            log.error("Erro na limpeza automática", e);
        }
    }
}
