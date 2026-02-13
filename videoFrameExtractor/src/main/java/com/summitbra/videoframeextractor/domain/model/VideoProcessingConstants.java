package com.summitbra.videoframeextractor.domain.model;

public final class VideoProcessingConstants {

    private VideoProcessingConstants() {
        // Utility class
    }

    public static final double DEFAULT_INTERVAL_SECONDS = 1.0;
    public static final int DEFAULT_MAX_FRAMES = 100;
    public static final String DEFAULT_OUTPUT_FORMAT = "PNG";
    public static final long DEFAULT_PROCESSING_TIME_MS = 0L;

    public static final int MAX_FRAMES_LIMIT = 1000;
    public static final double MAX_INTERVAL_SECONDS = 60.0;
    public static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15MB

    public static final class SupportedFormats {
        public static final String PNG = "PNG";
        public static final String JPG = "JPG";
        public static final String JPEG = "JPEG";

        private SupportedFormats() {}

        public static boolean isSupported(String format) {
            return PNG.equalsIgnoreCase(format) ||
                   JPG.equalsIgnoreCase(format) ||
                   JPEG.equalsIgnoreCase(format);
        }
    }

    public static final class ErrorMessages {
        public static final String INVALID_INTERVAL = "Intervalo deve ser positivo e menor que " + MAX_INTERVAL_SECONDS + " segundos";
        public static final String INVALID_MAX_FRAMES = "Número de frames deve ser positivo e menor que " + MAX_FRAMES_LIMIT;
        public static final String INVALID_OUTPUT_FORMAT = "Formato de saída deve ser PNG, JPG ou JPEG";
        public static final String FILE_TOO_LARGE = "Arquivo muito grande. Tamanho máximo: 15MB";
        public static final String EMPTY_FILE = "Arquivo não pode estar vazio";
        public static final String INVALID_VIDEO_TYPE = "Apenas arquivos de vídeo são aceitos";

        private ErrorMessages() {}
    }
}
