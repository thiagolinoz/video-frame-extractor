# Video Frame Extractor

Microsserviço responsável por processar vídeos de forma assíncrona: consome eventos de um tópico Kafka, baixa o vídeo do AWS S3, extrai frames usando FFmpeg/JavaCV, compacta os frames em um arquivo `.zip` e faz o upload do resultado de volta ao S3. O status de cada etapa é publicado em um tópico Kafka dedicado.

---

## Sumário

- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Fluxo de Processamento](#fluxo-de-processamento)
- [Configuração](#configuração)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Executando Localmente](#executando-localmente)
- [Executando com Docker Compose](#executando-com-docker-compose)
- [Deploy no Kubernetes](#deploy-no-kubernetes)
- [Testes](#testes)

---

## Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)**, separando claramente o domínio da aplicação dos adaptadores de infraestrutura.

```
com.fiap.videoframeextractor
├── domain/
│   ├── model/              → VideoMessage, VideoStatusMessage, VideoMetadata
│   ├── ports/
│   │   ├── in/             → FrameExtractionServicePort (caso de uso)
│   │   └── out/            → FrameExtractorPort, VideoStoragePort
│   ├── services/           → FrameExtractionService (lógica de negócio)
│   ├── enums/              → FrameFormatEnum, VideoStatusEnum
│   └── exceptions/         → Exceções de domínio
│
├── infrastructure/
│   └── adapter/
│       ├── in/messaging/   → VideoMessageConsumer (Kafka @KafkaListener)
│       └── out/
│           ├── ffmpeg/     → FFmpegFrameExtractor (extração de frames)
│           ├── messaging/  → VideoStatusProducer (Kafka producer)
│           └── s3/         → S3StorageAdapter (AWS S3)
│
└── configuration/          → Beans de configuração (AWS, FFmpeg, Kafka)
```

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem base |
| Spring Boot | 3.2.2 | Framework principal |
| Spring Kafka | gerenciado | Consumer/Producer Kafka |
| Spring Boot Actuator | gerenciado | Health checks |
| AWS SDK v1 (`aws-java-sdk-s3`) | 1.12.565 | Integração com Amazon S3 |
| JavaCV Platform | 1.5.9 | Extração de frames via FFmpeg |
| FFmpeg Java Wrapper (bramp) | 0.8.0 | Wrapper adicional para FFmpeg |
| Jackson Databind + JSR310 | gerenciado | Serialização JSON |
| Lombok | gerenciado | Redução de boilerplate |
| Apache Commons IO | 2.15.1 | Utilitários de I/O |
| Docker | — | Containerização |
| Kubernetes | — | Orquestração |
| LocalStack | — | Emulação local do AWS S3 |

---

## Fluxo de Processamento

```
[Serviço de Upload (video-app)]
         │
         │  Publica JSON em Kafka
         ▼
  Tópico: received-videos
         │
         ▼
VideoMessageConsumer.handleReceivedVideo()
  ├─ Deserializa JSON → VideoMessage
  ├─ Publica status PROCESSING → Tópico: process-status-videos
  └─ FrameExtractionService.processVideo()
        ├─ Valida formato do vídeo (mp4, avi, mov, mkv)
        ├─ Verifica existência no S3
        ├─ Valida tamanho máximo (100 MB)
        ├─ Download do vídeo (S3)
        ├─ Extrai frames via JavaCV/FFmpeg (1 frame/segundo, máx. 100 frames)
        ├─ Empacota frames em ZIP
        ├─ Upload do ZIP para S3 (frames/{videoId}.zip)
        ├─ Publica status COMPLETED → Tópico: process-status-videos
        └─ (Se erro) Publica status PROCESS_ERROR
```

### Modelo de Mensagem de Status (`VideoStatusMessage`)

| Campo | Descrição |
|---|---|
| `idVideoSend` | ID do vídeo |
| `cdVideoStatus` | `PROCESSING`, `COMPLETED` ou `PROCESS_ERROR` |
| `nmPersonEmail` | E-mail do usuário |
| `nmVideo` | Nome do vídeo |
| `nmVideoPathZip` | Caminho do ZIP no S3 (apenas em `COMPLETED`) |
| `errorMessage` | Mensagem de erro (apenas em `PROCESS_ERROR`) |

---

## Configuração

As configurações padrão estão em [src/main/resources/application.properties](src/main/resources/application.properties):

| Propriedade | Valor Padrão |
|---|---|
| `spring.kafka.bootstrap-servers` | `kafka.video.internal:9092` |
| `kafka.topic.video-uploaded` | `received-videos` |
| `kafka.topic.video-status` | `process-status-videos` |
| `kafka.consumer.group-id` | `video-frame-extractor-group` |
| `aws.s3.bucket-name` | `postech-fiap-bucket-videos` |
| `aws.s3.videos-prefix` | `videos/` |
| `aws.s3.frames-prefix` | `frames/` |
| `aws.region` | `us-east-1` |
| `ffmpeg.path` | `/usr/bin/ffmpeg` |
| `frame.extraction.output-dir` | `./frames` |
| `frame.extraction.fps` | `1` |
| `frame.extraction.max-frames` | `100` |
| `server.port` | `8080` |

---

## Variáveis de Ambiente

| Variável | Descrição |
|---|---|
| `AWS_REGION` | Região AWS (ex: `us-east-1`) |
| `AWS_ACCESS_KEY_ID` | Chave de acesso AWS |
| `AWS_SECRET_ACCESS_KEY` | Chave secreta AWS |
| `FFMPEG_PATH` | Caminho do binário FFmpeg |
| `FRAME_OUTPUT_DIR` | Diretório temporário para os frames |
| `FRAME_FPS` | Frames por segundo a extrair (padrão: `1`) |
| `KAFKA_BOOTSTRAP_SERVERS` | Endereço dos brokers Kafka |
| `S3_BUCKET_NAME` | Nome do bucket S3 |
| `LOCALSTACK_ENDPOINT` | Endpoint do LocalStack (apenas para desenvolvimento) |

---

## Executando Localmente

### Pré-requisitos

- Java 21+
- Maven 3.8+
- FFmpeg instalado (`apt install ffmpeg` / `brew install ffmpeg`)
- Kafka e AWS S3 (ou LocalStack) em execução

### Build e execução

```bash
# Compilar o projeto
./mvnw clean package -DskipTests

# Executar
java -jar target/video-frame-extractor-*.jar
```

---

## Executando com Docker Compose

O `docker-compose.yml` sobe o ambiente completo de desenvolvimento, incluindo:

- **app** — Aplicação (porta `8085`)
- **broker** — Kafka Confluent 7.6.0 (porta `9092`)
- **zookeeper** — Confluent 7.6.0
- **kafka-ui** — Interface Kafka (porta `8081`)
- **localstack** — Emulação do AWS S3 (porta `4566`)

```bash
docker compose up --build
```

Acesse o Kafka UI em: `http://localhost:8081`

---

## Deploy no Kubernetes

Os manifests estão no diretório [k8s/](k8s/):

| Arquivo | Descrição |
|---|---|
| [k8s/configmap.yaml](k8s/configmap.yaml) | ConfigMap com configurações da aplicação |
| [k8s/application/application-deployment.yaml](k8s/application/application-deployment.yaml) | Deployment (1 réplica, imagem no ECR) |
| [k8s/application/application-service.yaml](k8s/application/application-service.yaml) | Service LoadBalancer (porta 80 → 8080) |

```bash
# Aplicar os manifests
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/application/
```

> As credenciais AWS são fornecidas via Secret `aws-academy-credentials` no cluster.

---

## Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com relatório de cobertura
./mvnw test jacoco:report
```

Os relatórios de teste são gerados em `target/surefire-reports/`.
