# DJM2 - Player de Stems com IA Integrada

DJM2 é uma aplicação de desktop desenvolvida em Java que funciona como uma mesa de DJ virtual, permitindo a separação de qualquer música em suas faixas instrumentais (stems) e o controle individual de cada uma delas em tempo real.

O grande diferencial deste projeto é a sua integração direta com o **Demucs**, uma poderosa ferramenta de IA para separação de fontes de áudio. O usuário pode simplesmente selecionar um arquivo de música (MP3, WAV, etc.), e a aplicação orquestra todo o processo de separação em segundo plano, carregando as faixas resultantes (vocal, bateria, baixo, outros) automaticamente para mixagem.

Este projeto foi desenvolvido como parte da disciplina de Introdução a Multimídia.

## ✨ Funcionalidades Principais

  * **Separação Automática de Stems:** Selecione um arquivo de música e a aplicação chama o Demucs para separar as faixas de forma automática.
  * **Playback Multi-faixa Sincronizado:** Todas as faixas separadas tocam em perfeita sincronia.
  * **Controle Individual por Faixa:** Cada faixa (vocal, bateria, etc.) possui controles individuais de **Mute/Unmute** e **Volume**.
  * **Controles Mestres:** Botão de Play/Pause geral e uma barra de progresso para navegar pela música.
  * **Log de Processamento em Tempo Real:** Acompanhe a saída do Demucs diretamente na interface gráfica enquanto a separação acontece.
  * **Interface Reativa:** A aplicação não trava durante o processamento pesado da IA, graças ao uso de threads em segundo plano (`SwingWorker`).

## 🛠️ Tecnologias e Ferramentas Utilizadas

Este projeto foi construído utilizando as seguintes ferramentas de desenvolvimento:

  * **Linguagem:** **Java** (JDK 11 ou superior)
  * **Interface Gráfica (GUI):** **Java Swing**
  * **API de Áudio:** **Java Sound API (`javax.sound.*`)** para manipulação e reprodução dos arquivos de áudio (`.wav`).
  * **Motor de Separação de Áudio (IA):** **Demucs**, uma biblioteca baseada em Python e PyTorch.
  * **Concorrência:** **Java `SwingWorker`** para gerenciar a chamada do processo externo do Demucs sem congelar a Event Dispatch Thread (EDT) da interface.

## ⚙️ Arquitetura e Funcionamento

O projeto é organizado seguindo os princípios de Programação Orientada a Objetos, com as responsabilidades bem divididas:

1.  **`view (DJView)`**: Responsável por toda a interface gráfica. Ela captura as ações do usuário (como clicar em "Separar Música") e exibe o estado atual da aplicação (faixas, volumes, etc.).
2.  **`model (DJTable, AudioTrack)`**: Representa os dados e a lógica de negócio. A `DJTable` gerencia a lista de faixas de áudio (`AudioTrack`) e o estado da reprodução, mas não sabe nada sobre a interface.
3.  **`service (DemucsService)`**: Classe especialista que encapsula a lógica de chamar o processo de linha de comando do Demucs, monitorar sua saída e retornar os arquivos de áudio resultantes.

O fluxo principal acontece da seguinte forma:

1.  O usuário seleciona um arquivo de música na `DJView`.
2.  A `DJView` inicia um `SwingWorker` para não travar a UI.
3.  O `SwingWorker`, em uma thread de background, chama o `DemucsService`.
4.  O `DemucsService` monta e executa o comando `python -m demucs ...`. Ele captura o log do processo e o envia de volta para a `DJView` para ser exibido em tempo real.
5.  Ao final do processo, o `DemucsService` localiza os arquivos `.wav` gerados.
6.  O `SwingWorker` entrega esses arquivos para a `DJTable` (o modelo), que os carrega e prepara para a reprodução.
7.  A `DJView` é notificada e atualiza a interface para exibir os painéis de controle de cada nova faixa.

## 📋 Pré-requisitos

Para executar este projeto em sua máquina, é essencial ter o seguinte ambiente configurado:

  * **Java Development Kit (JDK):** Versão 11 ou mais recente.
  * **Python:** Versão 3.8 ou mais recente, adicionado ao PATH do sistema.
  * **Demucs:** Instalado via pip.
    ```bash
    pip install -U demucs
    ```
  * **FFmpeg:** Instalado e acessível através do PATH do sistema (o Demucs depende dele para processar diferentes formatos de áudio).

## ▶️ Como Executar o Projeto

1.  **Clone o repositório:**

    ```bash
    git clone https://github.com/PetersonNave/Player-de-Stems-com-IA-Integrada.git
    cd Player-de-Stems-com-IA-Integrada
    ```

2.  **Compile o código-fonte:**
    Navegue até a pasta `src` e execute o comando de compilação. Ele irá gerar os arquivos `.class` em uma nova pasta `out`.

    ```bash
    javac -d ../out com/m2corp/djm2/Application.java com/m2corp/djm2/model/*.java com/m2corp/djm2/view/*.java com/m2corp/djm2/service/*.java
    ```

3.  **Execute a aplicação:**
    A partir da pasta raiz do projeto, execute a classe principal que está no diretório `out`.

    ```bash
    java -cp out com.m2corp.djm2.Application
    ```

4.  **Utilize o programa:**
    Com a aplicação aberta, vá em `File > Separate and Load Song...` para começar\!



## 👨‍💻 Autor

  * **Peterson Jesus Feitosa de Melo** - [pjfm@cin.ufpe.br](mailto:pjfm@cin.ufpe.br)
