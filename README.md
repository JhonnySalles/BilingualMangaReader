# BilingualMangaReader
> Leitor de mangas offline, no qual possui compatibilidade com textos extraido e processado através do programa [MangaExtractor](https://github.com/JhonnySalles/MangaExtractor)

<h4 align="center"> 
	🚧  PDV 🚀 Em construção...  🚧
</h4>

[![Build Status][travis-image]][travis-url]

<p align="center">
 <a href="#Sobre">Sobre</a> •
 <a href="#Bibliotecas-utilizadas">Bibliotecas utilizadas</a> • 
 <a href="#Estrutura-da-classe do arquivo de legenda">Estrutura do arquivo de legenda</a> • 
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


### Estrutura da classe do arquivo de legenda

O aplicativo é também compatível com legendas extraidas e pré processadas com o programa [MangaExtractor](https://github.com/JhonnySalles/MangaExtractor), onde após exportar para json as legendas com o formato abaixo, é possível carrega-los tanto imbutido no arquivo de manga (rar/zip), como também importado um arquivo de json solto.

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
- [X] Leitor com o minimo de recursos para visualização do manga
- [X] Listar arquivos na pasta de biblioteca
- [X] Guardar informações sobre progresso
- [X] Carregar legendas pré processadas
- [X] Localização da legenda em outro idioma
- [X] Popup flutuante da legenda para facilitar a leitura
- [X] Impressão na imagem das coordenadas de texto extraido para melhor localizar a legenda
- [X] Reconhecimento de furigana do texto em japonês
- [X] Reconhecimento do nivel jlpt do kanji
- [ ] Abertura de arquivos Tar
- [ ] Reconhecimento de pastas como um manga a ser aberto
- [ ] Ajustes no brilho e cores da imagem
- [ ] Luz azul para leitura norturna
- [ ] Melhorias e ajuste da interface
- [ ] Implementação de lista de favoritos
- [ ] Implementação de lista de histórico
- [ ] Utilização de vocabulário
- [ ] Significado do kanji
- [ ] Favoritar o kanji para estudo posterior
- [ ] Favoritar vocabulário para estudo posterior


## Contribuindo

1. Fork (<https://github.com/JhonnySalles/MangaExtractor/fork>)
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

![Leitor](https://i.imgur.com/KpbuMy7.png)

> Carregamento das legendas

![Legenda em português](https://i.imgur.com/9ru1amK.png)

![Texto flutuante](https://i.imgur.com/nTHFhCo.png)

![Imagem ampliada](https://i.imgur.com/TRaMZaS.png)

![Lista de legendas](https://i.imgur.com/tQkadpu.png)

![Legenda em japonês com as coordenadas impressas](https://i.imgur.com/hCbKhuP.png)

![Coordenadas dos textos](https://i.imgur.com/c6nqt87.png)

