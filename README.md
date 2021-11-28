# BilingualMangaReader
> Leitor de mangas offline, no qual possui compatibilidade com textos extraido e processado através do programa [MangaExtractor](https://github.com/JhonnySalles/MangaExtractor)

<h4 align="center"> 
	🚧  PDV 🚀 Em construção...  🚧
</h4>

[![Build Status][travis-image]][travis-url]

<p align="center">
 <a href="#Sobre">Sobre</a> •
 <a href="#Bibliotecas-utilizadas">Bibliotecas utilizadas</a> • 
 <a href="#Json-processado">Json processado</a> • 
 <a href="#Estrutura-da-classe-do-arquivo-de-legenda">Estrutura do arquivo de legenda</a> • 
 <a href="#Histórico-de-Release">Histórico de Release</a> • 
 <a href="#Features">Features</a> • 
 <a href="#Contribuindo">Contribuindo</a> • 
 <a href="#Instalação">Instalação</a> • 
 <a href="#Exemplos">Exemplos</a>
</p>


## Sobre

Programa foi criado em Kotlin, onde foi utilizado algumas bibliotecas que estarão listadas mais abaixo para carregamento das imagens e leitura de arquivos jsons.

O aplicativo foi projetado para reconhecer arquivos cbr/rar e cbz/zip em uma pasta de biblioteca, onde irá listar todas os arquivos encontrados.

Também irá salvar algumas preferências e o progresso e tem suporte a furigana e nivel jlpt do kanji em cores.


### Bibliotecas utilizadas

<ul>
  <li><a href="https://github.com/square/picasso">Picasso</a> - Uma poderosa biblioteca de download e cache de imagens para Android.</li>
  <li><a href="https://github.com/google/gson">Gson</a> - Uma biblioteca para conversão de json em classe. </li>
  <li><a href="https://github.com/WorksApplications/Sudachi">Sudachi</a> - Uma excelente api para reconhecimento de vocabulário dentro de uma frase em japonês, como também sua leitura, forma de dicionário e afins. </li>
  <li> Room - Uma biblioteca nativa com vários recusos para gerenciar banco de dados SQLite. </li>
  <li> PageView - Implementado a estrutura de apresentação de imagens em carrocel. </li>
  <li><a href="https://github.com/junrar/junrar">Junrar</a> - Biblioteca para leitura e extração de arquivos rar e cbr. </li>
</ul>


## Json processado

Caso tenha alguma dúvida sobre como processar o json favor entrar em contato. Em breve estarei disponibilizando as legendas que já processadas.

[Legendas do manga de exemplo.](https://drive.google.com/drive/folders/1RGVVoyrLfT6qZO9mYChkmWPc1Gm0s3YJ?usp=sharing)

### Estrutura da classe do arquivo de legenda

O aplicativo é também compatível com legendas extraidas e pré processadas com o programa [MangaExtractor](https://github.com/JhonnySalles/MangaExtractor), onde após exportar para json as legendas com o formato abaixo, é possível carrega-los tanto embutido no arquivo de manga (rar/zip/tar), como também importado um arquivo de json solto localizado em alguma pasta no celular.

    List<Class> capitulos          # Lista de classes de capitulos
    ├── id
    ├── manga                  # Nome do manga
    ├── volume              
    ├── capitulo
    ├── linguagem              # Atualmente é suportado em linguagem Inglês, Japonês e Português.
    ├── scan
    ├── isExtra
    ├── isRaw
    ├── isProcessado
    ├── List<Class> paginas    # Array de classes páginas
    │   ├── nome               # Nome da imagem que está sendo processado
    │   ├── numero             # Um contador sequencial das imagens que estão no diretório
    │   ├── hashPagina
    │   ├── isProcessado
    │   ├── List<Class> Textos # Array de classe dos textos da página
    │   │   ├── sequencia
    │   │   ├── posX1          # Coordenadas da fala na imagem
    │   |   ├── posY1              
    │   |   ├── posX2              
    │   |   └── posY2 
    |   ├── hashPagina         # Hash md5 da imagem que foi processada, para que possa localizar a legenda desta página
    │   └── vocabulario        # Vocabulário da página
    │       ├── palavra       
    │       ├── significado   
    │       └── revisado       # Flag sinalizadora que o vocabulário foi revisado ou não. 
    └── vocabulario            # Vocabulário do capitulo
        ├── palavra       
        ├── significado   
        └── revisado           # Flag sinalizadora que o vocabulário foi revisado ou não.
  
         
> Estrutura de classe com informações da página que possui a legenda pré processada, podendo ser obtida de uma raw ou traduzida de alguma fã sub. Com ele será possível apresentar a tradução ou significados dos kanjis presente na página.


## Histórico de Release

* 0.0.1
    * Em progresso.


### Features

- [X] Abertura de arquivos cbr/rar
- [X] Abertura de arquivos cbz/zip
- [X] Abertura de arquivos Tar
- [X] Leitor com o minimo de recursos para visualização do manga
- [X] Listar arquivos na pasta de biblioteca
- [X] Guardar informações sobre progresso
- [X] Carregar legendas pré processadas
- [X] Localização da legenda em outro idioma
- [X] Popup flutuante da legenda para facilitar a leitura
- [X] Impressão na imagem das coordenadas de texto extraido para melhor localizar a legenda
- [X] Reconhecimento de furigana do texto em japonês
- [X] Reconhecimento do nivel jlpt do kanji
- [X] Ajustes no brilho e cores da imagem
- [X] Melhorias e ajuste da interface
- [X] Implementação de lista de histórico
- [X] Utilização de vocabulário
- [X] Significado do kanji
- [X] Favoritar o kanji para estudo posterior
- [X] Interface para modo tablet
- [X] Localizar no vocabulário na ao toque na palavra
- [X] Troca das legendas entre idiomas selecionados (devido a diferenças de paginas entre versões pode não localizar de forma satisfatória)
- [X] Luz azul para leitura norturna
- [ ] Favoritar vocabulário para estudo posterior
- [ ] Adição de dicionarios japonês
- [ ] Exportar vocabularios ao Anki
- [X] Localizar a legenda pela cordenada do texto


## Contribuindo

1. Fork (<https://github.com/JhonnySalles/BilingualMangaReader/fork>)
2. Crie sua branch de recurso (`git checkout -b feature/fooBar`)
3. Faça o commit com suas alterações (`git commit -am 'Add some fooBar'`)
4. Realize o push de sua branch (`git push origin feature/fooBar`)
5. Crie um novo Pull Request

<!-- Markdown link & img dfn's -->

[travis-image]: https://img.shields.io/travis/dbader/node-datadog-metrics/master.svg?style=flat-square
[travis-url]: https://travis-ci.org/dbader/node-datadog-metrics
[wiki]: https://github.com/yourname/BilingualMangaReader/wiki

## Instalação

> Para executar o projeto é necessário ter o android studio instalado junto com uma versão do emulador.

> Abra então no android studio a pasta do projeto e aguarde o gradle processar as dependências 

> Após a instalação das dependências compile e execute o projeto, no qual será então aberto no emulador.


## Exemplos

> Algumas imagens do aplicativo

![Biblioteca](https://i.imgur.com/roLmu9C.png)

![Leitor](https://i.imgur.com/r4hhAzj.jpg)

![Leitor](https://i.imgur.com/Awwcjyc.jpg)


> Recursos

![Popup flutuante](https://i.imgur.com/dIuQO9N.jpg)

![Localização do texto reconhecido com ocr](https://i.imgur.com/fMavbfI.jpg)

![Informações da legenda](https://i.imgur.com/t7LR4PV.jpg)

![Informações do kanji](https://i.imgur.com/j0Wzpsv.jpg)

![Informações do kanji](https://i.imgur.com/js2wlIb.jpg)

![Vocabulário](https://i.imgur.com/xG1jfYr.jpg)


> Estrutura do arquivo recomendada

![Estrutura de pasta](https://i.imgur.com/EZdlHGV.png)

> Recomendo embutir a legenda no arquivo e separar as pastas por capitulo para facilitar a localização da legenda correta quando não for possível o aplicativo encontrar a pagina correspondente.
