# Video Frame Extractor

Um sistema para extrair frames de vídeos e compactá-los em arquivos ZIP.

## 🚀 Funcionalidades

- ✅ Upload de vídeos via interface web
- ✅ Extração de frames em intervalos configuráveis
- ✅ Suporte a múltiplos formatos de vídeo
- ✅ Compactação dos frames em arquivo ZIP
- ✅ Download direto do arquivo ZIP
- ✅ Interface web moderna e responsiva
- ✅ API REST para integração

## 📋 Pré-requisitos

### Opção 1: Com FFmpeg (Recomendado)
1. **Java 21** ou superior
2. **Maven 3.6+**
3. **FFmpeg** instalado no sistema

#### Instalação do FFmpeg:

**Windows:**
```powershell
# Usando Chocolatey
choco install ffmpeg

# Ou baixe diretamente de https://ffmpeg.org/download.html
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install ffmpeg
```

**macOS:**
```bash
# Usando Homebrew
brew install ffmpeg
```

### Opção 2: Sem FFmpeg
O sistema funcionará no modo básico, criando arquivos de exemplo ao invés de extrair frames reais.

## 🛠️ Instalação e Execução

1. **Clone o projeto:**
```bash
git clone <url-do-repositorio>
cd videoFrameExtractor
```

2. **Compile e execute:**
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

3. **Acesse a aplicação:**
Abra seu navegador e vá para: http://localhost:8080

## 🎯 Como Usar

### Interface Web
1. Acesse http://localhost:8080
2. Faça upload de um arquivo de vídeo (arraste e solte ou clique para selecionar)
3. Configure os parâmetros:
   - **Intervalo entre frames**: De quantos em quantos segundos extrair um frame
   - **Máximo de frames**: Quantos frames extrair no máximo
   - **Formato de saída**: PNG, JPG ou BMP
4. Clique em "Extrair Frames"
5. Aguarde o processamento
6. Baixe o arquivo ZIP com os frames

### API REST

#### Extrair Frames
```http
POST /api/video/extract-frames
Content-Type: multipart/form-data

Parâmetros:
- video: arquivo de vídeo (obrigatório)
- intervalSeconds: intervalo em segundos (padrão: 1)
- maxFrames: máximo de frames (padrão: 100)
- outputFormat: PNG, JPG ou BMP (padrão: PNG)
```

#### Baixar ZIP
```http
GET /api/video/download/{nomeDoArquivo.zip}
```

#### Status da API
```http
GET /api/video/status
```

### Exemplo usando curl
```bash
curl -X POST \
  http://localhost:8080/api/video/extract-frames \
  -F "video=@meu_video.mp4" \
  -F "intervalSeconds=2" \
  -F "maxFrames=50" \
  -F "outputFormat=PNG"
```

## 📁 Estrutura do Projeto

```
videoFrameExtractor/
├── src/main/java/com/summitbra/videoframeextractor/
│   ├── VideoFrameExtractorApplication.java    # Classe principal
│   ├── controller/
│   │   └── VideoFrameExtractionController.java # REST Controller
│   ├── service/
│   │   └── VideoFrameExtractionService.java   # Lógica de processamento
│   ├── model/
│   │   ├── VideoProcessingRequest.java        # Modelo de requisição
│   │   └── VideoProcessingResponse.java       # Modelo de resposta
│   └── config/
│       └── WebConfig.java                     # Configuração web
├── src/main/resources/
│   ├── application.properties                 # Configurações
│   └── static/
│       └── index.html                        # Interface web
└── temp/                                     # Diretório temporário (criado automaticamente)
└── output/                                   # Diretório de saída (criado automaticamente)
```

## ⚙️ Configuração

Edite o arquivo `application.properties` para personalizar:

```properties
# Porta do servidor
server.port=8080

# Tamanho máximo do arquivo
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Diretórios
app.video.temp-dir=temp
app.video.output-dir=output

# Caminhos do FFmpeg (se necessário)
app.ffmpeg.path=ffmpeg
app.ffprobe.path=ffprobe
```

## 🔧 Resolução de Problemas

### FFmpeg não encontrado
Se você receber erros relacionados ao FFmpeg:
1. Verifique se o FFmpeg está instalado: `ffmpeg -version`
2. Certifique-se de que está no PATH do sistema
3. Configure os caminhos no `application.properties`

### Erro de memória
Para vídeos grandes:
1. Aumente a memória da JVM: `-Xmx2g`
2. Reduza o número máximo de frames
3. Use intervalos maiores entre frames

### Formatos de vídeo suportados
O sistema suporta os formatos que o FFmpeg consegue processar:
- MP4, AVI, MOV, WMV, FLV, MKV, etc.

## 📝 Logs

Os logs são salvos no console e incluem:
- Informações de processamento
- Tempos de execução
- Erros e avisos
- Detalhes do FFmpeg (se habilitado)

## 🛡️ Segurança

- Validação de tipos de arquivo
- Limite de tamanho de upload (100MB)
- Validação de nomes de arquivo
- Limpeza automática de arquivos temporários
- Sanitização de parâmetros de entrada

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

## 🆘 Suporte

Se você encontrar problemas:
1. Verifique os logs da aplicação
2. Consulte a seção de resolução de problemas
3. Abra uma issue no repositório

## 🎬 Exemplo de Uso

1. **Prepare um vídeo**: Tenha um arquivo de vídeo (.mp4, .avi, etc.)
2. **Inicie a aplicação**: Execute `mvnw spring-boot:run`
3. **Acesse a interface**: http://localhost:8080
4. **Faça upload**: Arraste o vídeo para a área de upload
5. **Configure**: Defina intervalo de 2 segundos e máximo de 30 frames
6. **Processe**: Clique em "Extrair Frames"
7. **Baixe**: Clique no botão de download do arquivo ZIP

O resultado será um arquivo ZIP contendo 30 imagens extraídas do vídeo a cada 2 segundos!
