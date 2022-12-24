# BilingualMangaReader
> Leitor de mangas offline, no qual possui compatibilidade com Tesseract e Google Vision para utilização de OCR, vínculo de dois arquivos em idiomas diferentes com facilidade na troca entre as páginas de ambos e compatibilidade com textos extraido e processado através do programa [MangaExtractor](https://github.com/JhonnySalles/MangaExtractor)

<h4 align="center"> 
	🚧  BilingualMangaReader 🚀 Em construção...  🚧
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

O aplicativo foi projetado para reconhecer arquivos cbr/rar e cbz/zip em uma pasta de biblioteca, onde irá listar todas os arquivos encontrados na pasta informada.

Também irá salvar algumas preferências e o progresso e tem suporte a furigana e nivel jlpt do kanji em cores.

Possui compatibilidade com o Tesseract e Google Vision para reconhecimento de caracteres em imagens, no qual comparado a versão de computador e em teste realizado é bem limitado, o que apenas faz jus o reconhecimento para kanjis e não palavras inteiras.

Vinculo entre dois arquivos diferentes para realizar a troca de página entre os arquivos de forma fácil com apenas um clique de botão, no qual pode auxiliar na leitura e aprendizado de um novo idioma.


### Bibliotecas utilizadas

<ul>
  <li><a href="https://github.com/square/picasso">Picasso</a> - Uma poderosa biblioteca de download e cache de imagens para Android.</li>
  <li><a href="https://github.com/google/gson">Gson</a> - Uma biblioteca para conversão de json em classe. </li>
  <li><a href="https://github.com/WorksApplications/Sudachi">Sudachi</a> - Uma excelente api para reconhecimento de vocabulário dentro de uma frase em japonês, como também sua leitura, forma de dicionário e afins. </li>
  <li> Room - Uma biblioteca nativa com vários recusos para gerenciar banco de dados SQLite. </li>
  <li> PageView - Implementado a estrutura de apresentação de imagens em carrocel. </li>
  <li><a href="https://github.com/junrar/junrar">Junrar</a> - Biblioteca para leitura e extração de arquivos rar e cbr. </li>
  <li><a href="https://github.com/0xbad1d3a5/Kaku]">Kaku</a> - Leitor OCR para android. <i>"Apenas chamada por dentro aplicativo, necessário estar instalado."</i> </li>
  <li><a href="https://www.atilika.org/">Kuromoji</a> - Analizador morfológico japonês. </li>
  <li><a href="https://github.com/WorksApplications/Sudachi">Sudachi</a> - Sudachi é um analisador morfológico japonês.</li>
  <li><a href="https://github.com/lofe90/FuriganaTextView">FuriganaTextView</a> - TextView personalizado para Android que renderiza texto em japonês com furigana. </li>
  <li><a href="https://github.com/adaptech-cz/Tesseract4Android">Tesseract4Android</a> - Poderosa biblioteca que faz a comunicação com o Tesseract OCR.</li>
  <li><a href="https://github.com/tony19/logback-android">LogBack</a> - Biblioteca que traz o poderoso logback para o android.</li>
  <li><a href="https://github.com/sarajmunjal/two-way-backport">TwoWayView</a> - Biblioteca para apresentação de lista, grids e afins com formato horizontal e vertical.</li>
  <li>Retrofit 2 - Popular biblioteca para HTTPs no Android.</li>
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
    │       ├── portugues      # Significado da palavra em português
    │	      ├── ingles         # Significado da palavra em inglês
    │	      ├── leitura        # Leitura em katakana
    │       └── revisado       # Flag sinalizadora que o vocabulário foi revisado ou não. 
    └── vocabulario            # Vocabulário do capitulo
        ├── palavra            
        ├── portugues          # Significado da palavra em português
	      ├── ingles             # Significado da palavra em inglês
	      ├── leitura            # Leitura em katakana
        └── revisado           # Flag sinalizadora que o vocabulário foi revisado ou não.
  
         
> Estrutura de classe com informações da página que possui a legenda pré processada, podendo ser obtida de uma raw ou traduzida de alguma fã sub. Com ele será possível apresentar a tradução ou significados dos kanjis presente na página.


## Histórico de Release

* 0.1.0
    * Uso em desenvolvimento.
* 0.2.0
    * Leitor de manga com suporte a rar/zip/cbr/cbz.
    * Recursos principais de um leitor de manga.
    * Adicionar favorito.
    * Exclusão do arquivo.
    * Ajuste de brilho e cor da imagem.
    * Reconhecimento de fugirana nos textos em japonês.
    * Carregar legendas externas e interna ao arquivo.
    * Detalhes sobre o kanji e nivel jlpt.
    * Primeira versão estável publicada. 
* 0.2.1
    * Localização do texto com clique longo no balão de fala.
    * Adicionado tela de ajuda com detalhes do funcionamento.
    * Adicionado controle de luz azul e sépia.
* 0.2.2
    * Ajustes de bugs e erros.
    * Novo calculo de posicionamento para a aplicação da legenda 
* 0.2.7
    * Adicionado método para vincular páginas de dois arquivos diferentes.
    * Adicionado método de arrasta e solta para a organização das imagens.
    * Adicionado método para vincular páginas duplas quando necessário no arquivo vinculado.
    * Implementado a correções de alguns bugs.
    * Correção para a leitura aproveitar a tela inteira em android recente.
    * Ajuste na busca da legenda e a implementação da busca pela página vinculada.
* 0.2.8
    * Implementado métodos para rastrear possiveis erros.
    * Ajuste de erros e bug.
    * Refatorado alguns icones.
    * Implementado a chamada ao App Kaku.
* 0.2.9
    * Otimização para o aplicativo utilizar menos memória.
    * Otimização no carregamento das capas.
    * Implementado a utilização da biblioteca LruCache para as imagens.
    * Implementação de novas funcionalidades de ordenação na tela de vinculo de arquivo.
* 0.2.10
    * Implementado nova funcionalidade de OCR, utilizando-se da tecnologia do Tesseract e Google Vision.
    * Adicionado nova funcionalidade e painel flutuante que irá recortar um pedaço da imagem para ser feito o reconhecimento de caractere no Tesseract.
    * Adicionado novo layout na janela flutuante para receber as informações do texto reconhecido, como também irá manter uma lista do que foi capturado em tempo de execução.
    * Adicionado para o Tesseract os data de treinamento nos idiomas Protuguês, Inglês e Japonês.
    * Implementado a possibilidade de ajustar a janela flutuante.
* 0.2.11
    * Otimização na biblioteca, tanto no carregamento dos itens quando na funcionalidade.
    * Refeito o layout para melhor aparência.
    * Adicionado nova tela com informações adicionais sobre o arquivo, com informações de capítulos e outros arquivos vinculados.
    * Adicionado animações nas transições de tela e nos arquivos da biblioteca.
    * Implementado um novo painel flutuante com botões essenciais para leitura, não sendo necessário sair da tela cheia para executar algum comando.
* 0.2.14
    * Refatorado alguns nomes de icones.
    * Ajuste de layout.
    * Correção em bug no scrool automático.
    * Ajuste de cores do projeto.
    * Implementado backup do banco de dados.
* 0.2.15
    * Ajuste na ordenação de páginas.
    * Criado tela para verificação do vocabulário.
* 0.2.16
    * Impelemntado novo tema de detalhe sobre o kanji.
    * Adicionado banco de dados do kanji.
    * Ajuste de layout.
    * Implemetado temas no aplicativo.
* 0.2.17
    * Implementado zoom na imagem com o toque longo.
    * Ajustes nas cores e layouts.
    * Adicionado funcionalidade copiar o vocabulário no toque longo.
    * Implementado para desfazer até 10 ultimas alterações na tela de vínculo de arquivos.
    * Correção de bug na ordenação das páginas na tela de vinculo de arquivos.
    * Implementado para o aplicativo realizar a abertura de qualquer arquivo cbr ou zip diretamente.
    * Adicionado atalhos para ultimos arquivos abertos.
* 0.2.18
    * Implementado popup na tela de vínculo de arquivos para visualizar a imagem em maior tamanho.
    * Otimizado o carregamento das imagens na tela de vinculo de arquivos.
    * Implementado função de zoom de pinça no popup de imagem.
    * Correção no vocabulário para aceitar a tradução em português e inglês.
    * Correção para a troca da página entre os arquivos vinculado esteja mantendo a posição da leitura.
* 0.2.19
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
- [X] Localizar a legenda pela cordenada do texto
- [X] Ajuste para a leitura aproveitar a tela inteira do celular
- [X] Adicionado nova tela para vincular as páginas de dois arquivos
- [X] Implementar a abertura de um segundo arquivo para leitura de multi idioma (com a troca de imagens entre o original e outro arquivo em outro idioma)
- [X] Implementar a troca de imagens na leitura para a página vinculada
- [X] Implementar a re-organização de outras páginas ao movimentar uma página em específica
- [X] Adicionar funcionalidade para páginas duplas
- [X] Ajustar a busca das legendas para utilizar da página vinculada
- [X] Implementar a organização dos arquivos vinculados por idioma, sendo possível o vinculo de mais de um arquivo e manter a organização feita
- [X] Implementar rastreamento de erros e logs.
- [X] Adicionado a chamada ao aplicativo Kaku
- [X] Implementar novas funcionalidades de ordenação na tela de vinculo de arquivo.
- [X] Implementar ordenação de páginas simples e dupla.
- [X] Implementar ordenação de páginas automática, utilizando o tamanho da imagem para identificar se é página dupla.
- [X] Implementar exclusão do vinculo do arquivo.
- [X] Ajustar o carregamento das imagens de capa, realizar otimização no aplicativo e menor consumo de memória.
- [X] Implementar o Tesseract OCR ao projeto e realizar identificação de palavras em imagens.
- [X] Ajustar o painel flutuante para receber palavras que foram reconhecidas pelo Tesseract.
- [X] Implementar a api Google Vision para reconhecimento de palavras em imagens.
- [X] Favoritar vocabulário para estudo posterior
- [X] Implementar temas no aplicativo.
- [X] Adição de dicionarios japonês.
- [X] Adição de várias pastas de biblioteca.
- [X] Implementar zoom nas imagens na tela de vinculo de arquivo.
- [X] Implementar novas ordenações na tela de vinculo de arquivo.
- [X] Abertura de arquivos diretamente pelo aplicativo.
- [X] Adicionado atalho para ultimos arquivos lidos.
- [ ] Implementar nova funcionalidade para leituras de novel.
- [ ] Implementar abertura de arquivos pdf.
- [ ] Implementar abertura de arquivos ebook.
- [ ] Implementar abertura de páginas da web.
- [ ] Exportar vocabularios ao Anki


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

![Vinculo de paginas em dois arquivos](https://i.imgur.com/uCvYPV6.png)

![Paginas não vinculadas](https://i.imgur.com/E1kyoQ4.png)

![Paginas duplas](https://i.imgur.com/adp9MwE.png)

![Popup flutuante](https://i.imgur.com/dIuQO9N.jpg)

![Localização do texto reconhecido com ocr](https://i.imgur.com/fMavbfI.jpg)

![Informações da legenda](https://i.imgur.com/t7LR4PV.jpg)

![Informações do kanji](https://i.imgur.com/j0Wzpsv.jpg)

![Informações do kanji](https://i.imgur.com/js2wlIb.jpg)

![Vocabulário](https://i.imgur.com/xG1jfYr.jpg)


> Estrutura do arquivo recomendada

![Estrutura de pasta](https://i.imgur.com/EZdlHGV.png)

> Recomendo embutir a legenda no arquivo e separar as pastas por capitulo para facilitar a localização da legenda correta quando não for possível o aplicativo encontrar a pagina correspondente.