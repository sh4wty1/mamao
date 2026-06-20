# Downloader

App Android pessoal e minimalista para baixar vídeo/áudio de YouTube, TikTok, Instagram e
mais de 1000 sites — motor de download = [yt-dlp](https://github.com/yt-dlp/yt-dlp), rodando
100% no aparelho (sem servidor).

A razão de existir do app é o **botão Compartilhar do Android**: em qualquer app, toque em
`Compartilhar → Downloader` e escolha **Vídeo** ou **Áudio** com um toque.

## Como rodar

1. Abra a pasta no **Android Studio** (ele gera o Gradle wrapper automaticamente).
   Ou, via linha de comando, com um Gradle ≥ 8.11 instalado:
   ```bash
   gradle wrapper
   ./gradlew installDebug   # instala no aparelho conectado
   ```
2. **Use um aparelho arm64 real.** O app compila só para `arm64-v8a` (config pessoal, APK
   menor), então não roda em emulador x86.

## Funcionalidades

- Colar link na tela principal → Vídeo / Áudio.
- Receber link via Share → mini-seletor Vídeo / Áudio.
- Áudio extraído em MP3, vídeo mesclado em MP4 (via ffmpeg embutido).
- Download em foreground service com notificação de progresso.
- Arquivos salvos em `Download/Downloader` (via MediaStore, sem permissão de armazenamento).
- Botão **atualizar yt-dlp** (canal nightly) na barra superior — conserta quebras de sites sem
  precisar atualizar o app.

## Limitações conhecidas

- Sites mudam e quebram o yt-dlp; use o botão de atualizar quando algum download começar a falhar.
- Instagram e conteúdos que exigem login são best-effort (suporte a cookies fica para depois).

## Stack

Kotlin · Jetpack Compose (Material 3) · single-activity · `youtubedl-android`.
Detalhes de arquitetura para contribuir: ver [CLAUDE.md](CLAUDE.md).
