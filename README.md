<div align="center">

# 🥭 Mamão

### Baixar vídeo das redes nunca foi tão *mamão com açúcar*.

App Android pessoal e minimalista para baixar vídeo/áudio de YouTube, TikTok, Instagram e
mais de 1000 sites — motor [yt-dlp](https://github.com/yt-dlp/yt-dlp), rodando 100% no aparelho
(sem servidor).

</div>

---

A razão de existir do app é o **botão Compartilhar do Android**: em qualquer app, toque em
`Compartilhar → Mamão` e escolha **Vídeo** ou **Áudio** com um toque.

## Como rodar / instalar

- **Usuário:** baixe o APK na aba [Releases](../../releases) e instale (permita "fontes
  desconhecidas"). Use um aparelho **arm64** real (não roda em emulador x86).
- **Build do código:**
  ```bash
  # Android Studio gera o wrapper sozinho; via CLI use o ./gradlew já versionado.
  ./gradlew assembleDebug     # APK de teste
  ./gradlew assembleRelease   # APK assinado (precisa de keystore.properties — veja abaixo)
  ```

## Funcionalidades

- 📥 Colar link → **Vídeo** ou **Áudio** (MP3).
- 🔗 Receber link via **Compartilhar** → mini-seletor Vídeo / Áudio.
- 🔄 Botão para **atualizar o yt-dlp** (canal nightly) quando algum site quebrar.
- 💾 Arquivos salvos em `Download/Mamao` (via MediaStore, sem permissão de armazenamento).

## Build de release (assinado)

A assinatura usa um `keystore.properties` (ignorado pelo git) apontando para um `.jks` local —
nenhuma senha vai para o repositório. Veja [CLAUDE.md](CLAUDE.md) para o passo a passo.

## Limitações conhecidas

- Sites mudam e quebram o yt-dlp; use o botão de atualizar quando algum download começar a falhar.
- Instagram e conteúdos que exigem login são best-effort (suporte a cookies fica para depois).

## Stack

Kotlin · Jetpack Compose (Material 3) · single-activity · `youtubedl-android`.
Detalhes de arquitetura: [CLAUDE.md](CLAUDE.md).
