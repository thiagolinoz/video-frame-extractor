package com.summitbra.videoframeextractor.application.usecase;

import com.summitbra.videoframeextractor.application.port.in.CleanupVideoProcessingUseCase;
import com.summitbra.videoframeextractor.application.port.out.VideoProcessingRepository;
import com.summitbra.videoframeextractor.application.port.out.FileSystemService;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupVideoProcessingService implements CleanupVideoProcessingUseCase {

    private final VideoProcessingRepository repository;
    private final FileSystemService fileSystemService;

    @Override
    @Transactional
    public void cleanupOldProcessings(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        List<VideoProcessingJob> oldProcessings = repository
            .findOldProcessingRecords(cutoffTime, VideoProcessingJob.ProcessingStatus.COMPLETED);

        for (VideoProcessingJob job : oldProcessings) {
            try {
                if (job.getResult() != null && job.getResult().getZipFilename() != null) {
                    String zipFilename = job.getResult().getZipFilename();
                    Path zipPath = fileSystemService.getFilePath("output", zipFilename);

                    if (fileSystemService.fileExists(zipPath)) {
                        fileSystemService.deleteFile(zipPath);
                        log.info("Arquivo zip removido: {}", zipFilename);
                    }
                }

                repository.delete(job);
                log.info("Registro de processamento removido: {}", job.getVideoId());

            } catch (Exception e) {
                log.warn("Erro ao limpar processamento {}: {}", job.getVideoId(), e.getMessage());
            }
        }
    }
}
