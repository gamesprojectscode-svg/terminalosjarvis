# Terminal OS JARVIS

Aplicativo Android nativo em Kotlin + Jetpack Compose, com interface futurista original, terminal de comandos locais, editor de texto, gerenciamento básico de arquivos, lembretes locais e painel de diagnóstico.

## Versão simplificada

Esta versão não inclui Python, Chaquopy, JavaScript, HTML/CSS executáveis, compiladores ou runtimes de linguagens de programação. A aba Editor é apenas um editor de texto; não executa código. Isso reduz dependências e simplifica o build.

## Recursos e limitações

- Terminal com operações reais no workspace privado do app.
- Proteção contra caminhos fora do workspace nos comandos de arquivos.
- Editor para abrir, editar e salvar arquivos como texto.
- Informações do dispositivo acessíveis pelas APIs públicas do Android.
- Lembretes salvos localmente, mas sem alarmes ou notificações agendadas.
- Seletor de pastas externas presente, mas importação/exportação completa ainda não implementada.
- Sem root, shell Linux completo, IA externa ou execução de código.
- O comando `clear` ainda não limpa imediatamente o histórico visual.

## Compilar pelo GitHub Actions

1. Envie o conteúdo desta pasta para a raiz do repositório GitHub, incluindo `.github`.
2. Abra **Actions** e selecione **Build Android APK**.
3. Clique em **Run workflow** ou faça push na branch `main`.
4. Após sucesso, baixe o artefato `terminal-os-jarvis-debug-apk`.
5. Extraia o artefato e instale `app-debug.apk` no Android.

O workflow configura Java 17, Android SDK 35 e Gradle 8.11.1.

## Comandos

`help`, `clear`, `date`, `device`, `pwd`, `ls [caminho]`, `mkdir nome`, `touch arquivo`, `cat arquivo`, `write arquivo conteúdo`, `cp origem destino`, `mv origem destino`, `rm caminho`, `find [caminho]`, `grep texto arquivo`, `ipconfig`, `wifi`, `bluetooth`, `settings`, `reminder`.

Os comandos de arquivo operam no workspace privado. `rm` exclui arquivos/pastas; confira o caminho antes de usar. Python e outras linguagens de programação foram removidos desta versão.

## Testes e transparência

Os arquivos foram atualizados para remover Chaquopy e Python, mas não foi possível executar uma compilação Android neste ambiente. O build precisa ser confirmado no GitHub Actions e testado em um aparelho real.

## Licenças

Código original: MIT (ver `LICENSE`). AndroidX, Kotlin e Jetpack Compose seguem as licenças dos respectivos projetos.
