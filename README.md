# Teclado IA — protótipo inicial

## Compilar pelo celular com GitHub
1. Crie um repositório no GitHub.
2. Envie os arquivos mantendo exatamente a estrutura de pastas.
3. Abra a aba **Actions** e execute **Build APK** (ou faça um commit na branch main).
4. Abra a execução concluída e baixe o artefato **TecladoIA-debug**.
5. Extraia o ZIP e instale `app-debug.apk`.

## Configurar
1. Gere uma chave em https://aistudio.google.com/apikey.
2. Abra o app, cole a chave e salve.
3. Toque em **Ativar teclado**, habilite Teclado IA nas configurações.
4. Toque em **Selecionar teclado** e escolha Teclado IA.

## Segurança
Este é um protótipo pessoal. A chave é guardada nas preferências do app, mas chaves usadas diretamente por apps clientes podem ser extraídas. Não compartilhe APK com a sua chave configurada. Um produto público precisa de um backend/proxy seguro. A IA só recebe texto quando você toca em `✦ IA`; evite campos de senha e dados privados.

## Limitações
Teclado básico de demonstração. Ainda faltam autocorreção local mais completa, letras maiúsculas funcionais, layout numérico, sugestões e personalização avançada. Teste primeiro em um campo de texto comum.
