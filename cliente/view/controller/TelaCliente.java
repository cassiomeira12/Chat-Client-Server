/***********************************************************************
* Autor: Cassio Meira Silva
* Matricula: 201610373
* Inicio: 24/08/18
* Ultima alteracao: 14/12/18
* Nome: TelaCliente
* Funcao: Controler da Interface do TelaCliente JavaFX
***********************************************************************/

package view.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import socket.cliente.tcp.ClienteEnviarTCP;
import socket.cliente.tcp.ClienteReceberTCP;
import socket.cliente.udp.ClienteEnviarUDP;
import socket.cliente.udp.ClienteReceberUDP;
import socket.pdu.Apdu;
import util.Formatter;
import view.ClienteInterface;
import view.viewAlerta.Alerta;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

public class TelaCliente extends BorderPane {
  private final String FXML = "view/viewCliente/telaCliente.fxml";//Caminho do FXML
  private Stage palco;
  private ClienteInterface interfacePai;

  @FXML
  private TextField ipServidorText, portaText;
  @FXML
  private Button conectarButton;
  @FXML
  private Label statusClienteLabel;
  @FXML
  private StackPane conexaoStackPane;
  private ProgressIndicator progressIndicator;

  @FXML
  private TextField novaSalaText, entrarSalaText;
  @FXML
  private Button criarButton, entrarButton;
  @FXML
  private AnchorPane salaPane;
  @FXML
  private StackPane salaStack;
  private ProgressIndicator progressIndicatorSala;

  private boolean clienteDesconectado = true;

  private int portaServidorTCP = 1604;
  private int portaServidorUDP = 1605;
  private int portaClienteUDP = 1606;

  //Conexao TCP
  private ClienteReceberTCP clienteReceberTCP;
  private ClienteEnviarTCP clienteEnviarTCP;
  //Conexao UDP
  private ClienteReceberUDP clienteReceberUDP;
  private ClienteEnviarUDP clienteEnviarUDP;

  /*********************************************
  * Metodo: TelaCliente - Construtor
  * Funcao: Constroi objetos da classe TelaCliente
  * Parametros: Stage PALCO
  * Retorno: void
  *********************************************/
  public TelaCliente() {
    try {
      //Cria o carregador de arquivos FXML
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(FXML));
      fxmlLoader.setRoot(this);//Atribui essa classe como o Root da View
      fxmlLoader.setController(this);//Atribui essa classe como o Controller da View
      fxmlLoader.load();//Carrega a View FXML
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /*********************************************
  * Metodo: Initialize
  * Funcao: Metodo para carregar a Interface
  * Parametros: void
  * Retorno: void
  *********************************************/
  @FXML
  public void initialize() {
    portaText.setTextFormatter(Formatter.NUMERICO());

    this.progressIndicator = new ProgressIndicator();
    this.conexaoStackPane.getChildren().add(progressIndicator);
    this.progressIndicator.setVisible(false);

    this.progressIndicatorSala = new ProgressIndicator();
    this.salaStack.getChildren().add(progressIndicatorSala);
    this.progressIndicatorSala.setVisible(false);

    novaSalaText.setDisable(true);
    entrarSalaText.setDisable(true);
    criarButton.setDisable(true);
    entrarSalaText.setDisable(true);
    entrarButton.setDisable(true);

    conectarButton.setOnAction((Event) -> {
      if (clienteDesconectado) {//Se o TelaCliente estiver Desconectado
        conectar();//O TelaCliente Conecta
      } else {//Se o TelaCliente estiver Conectado
        desconectar();//O TelaCliente Desconecta
      }
    });

  }

  /*********************************************
   * Metodo: setPalco
   * Funcao: Atribui um referencia ao palco da interface
   * Parametros: Stage palco
   * Retorno: void
   *********************************************/
  public void setPalco(Stage palco) {
    this.palco = palco;
  }

  /*********************************************
   * Metodo: setInterfacePai
   * Funcao: Atribui um referencia a Interface Pai
   * Parametros: ClienteInterface interfacePai
   * Retorno: void
   *********************************************/
  public void setInterfacePai(ClienteInterface interfacePai) {
    this.interfacePai = interfacePai;
  }

  /*********************************************
   * Metodo: conectar
   * Funcao: Inicia uma conexao com o Servidor
   * Parametros: void
   * Retorno: void
   *********************************************/
  private void conectar() {
    String ip = ipServidorText.getText();
    String porta = portaText.getText();

    if (ip.isEmpty() || porta.isEmpty()) {
      Alerta.ERRO("Dados insuficentes para Conectar no Servidor", palco, interfacePai);
      return;
    }

    this.portaServidorTCP = Integer.parseInt(porta);
    this.portaServidorUDP = portaServidorTCP + 1;
    this.portaClienteUDP = portaServidorUDP + 1;

    new Thread(() -> {

      interfaceConectando();

      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }

      try {

        Socket socketTCP = new Socket(ip, portaServidorTCP);
        clienteReceberTCP = new ClienteReceberTCP(socketTCP);
        clienteEnviarTCP = new ClienteEnviarTCP(socketTCP);

        DatagramSocket socketUDP = new DatagramSocket(portaClienteUDP);
        clienteReceberUDP = new ClienteReceberUDP(socketUDP, interfacePai);
        clienteEnviarUDP = new ClienteEnviarUDP(socketUDP, ip, portaServidorUDP);

        clienteDesconectado = false;
        interfaceConectado();

        novaSalaText.setDisable(false);
        entrarSalaText.setDisable(false);
        criarButton.setDisable(false);
        entrarSalaText.setDisable(false);
        entrarButton.setDisable(false);

      } catch (Exception ex) {
        Alerta.EXCEPTION(ex, palco, interfacePai);
        interfaceDesconectado();
      }

    }).start();

  }

  /*********************************************
   * Metodo: desconectar
   * Funcao: Fecha uma conexao com o Servidor
   * Parametros: void
   * Retorno: void
   *********************************************/
  private void desconectar() {

    new Thread(() -> {

      interfaceDesconectando();

      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }

      clienteReceberTCP.close();
      clienteReceberUDP.close();

      clienteReceberTCP = null;
      clienteEnviarTCP = null;

      clienteReceberUDP = null;
      clienteEnviarUDP = null;

      clienteDesconectado = true;

      interfaceDesconectado();

    }).start();
  }

  /*********************************************
   * Metodo: interfaceConectando
   * Funcao: Altera os componentes para Conectando
   * Parametros: void
   * Retorno: void
   *********************************************/
  private void interfaceConectando() {
    Platform.runLater(() -> {
      ipServidorText.setDisable(true);
      portaText.setDisable(true);
      conectarButton.setDisable(true);
      statusClienteLabel.setVisible(false);
      progressIndicator.setVisible(true);
    });
  }

  /*********************************************
   * Metodo: interfaceConectado
   * Funcao: Altera os componentes para Conectado
   * Parametros: void
   * Retorno: void
   *********************************************/
  private void interfaceConectado() {
    Platform.runLater(() -> {
      statusClienteLabel.setText("Cliente Conectado!");
      statusClienteLabel.setTextFill(Color.web("#127c29"));
      statusClienteLabel.setVisible(true);
      conectarButton.setText("Desconectar");
      conectarButton.setDisable(false);
      progressIndicator.setVisible(false);
    });
  }

  /*********************************************
   * Metodo: interfaceDesconectando
   * Funcao: Altera os componentes para Desconectando
   * Parametros: void
   * Retorno: void
   *********************************************/
  private void interfaceDesconectando() {
    Platform.runLater(() -> {
      ipServidorText.setDisable(true);
      portaText.setDisable(true);
      conectarButton.setDisable(true);
      statusClienteLabel.setVisible(false);
      progressIndicator.setVisible(true);
    });
  }

  /*********************************************
   * Metodo: interfaceDesconectado
   * Funcao: Altera os componentes para Desconectado
   * Parametros: void
   * Retorno: void
   *********************************************/
  private void interfaceDesconectado() {
    Platform.runLater(() -> {
      statusClienteLabel.setText("Cliente Desconectado!");
      statusClienteLabel.setTextFill(Color.web("#f80000"));
      statusClienteLabel.setVisible(true);
      ipServidorText.setDisable(false);
      portaText.setDisable(false);
      conectarButton.setText("Conectar");
      conectarButton.setVisible(true);
      conectarButton.setDisable(false);
      progressIndicator.setVisible(false);
    });
  }

  /*********************************************
   * Metodo: criarSala
   * Funcao: Cria uma nova sala na interface
   * Parametros: void
   * Retorno: void
   *********************************************/
  @FXML
  public void criarSala() {
    String sala = novaSalaText.getText();

    if (sala.isEmpty()) {
      Alerta.ERRO("Dados insuficentes para Criar uma Sala no Servidor", palco, interfacePai);
      return;
    }

    new Thread(() -> {

      setDisableSala(true);

      //Enviando mensagem TCP para Servidor para Criar Sala
      clienteEnviarTCP.enviarMensagem(Apdu.CREATE(sala));

      interfacePai.adicionarSala(sala);

      setDisableSala(false);

    }).start();

  }

  /*********************************************
   * Metodo: entrarSala
   * Funcao: Inicia uma conexao com o Servidor
   * Parametros: void
   * Retorno: void
   *********************************************/
  @FXML
  public void entrarSala() {
    String sala = entrarSalaText.getText();

    if (sala.isEmpty()) {
      Alerta.ERRO("Dados insuficentes para Entrar uma Sala no Servidor", palco, interfacePai);
      return;
    }

    new Thread(() -> {

      setDisableSala(true);

      clienteEnviarTCP.enviarMensagem(Apdu.JOIN(sala));//Enviando mensagem TCP para Criar Sala

      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }

      interfacePai.adicionarSala(sala);

      setDisableSala(false);

    }).start();

  }

  /*********************************************
   * Metodo: setDisableSala
   * Funcao: Desabilita a interacao com a interface
   * Parametros: boolean valor
   * Retorno: void
   *********************************************/
  private void setDisableSala(Boolean valor) {
    Platform.runLater(() -> {
      novaSalaText.setDisable(valor);
      entrarSalaText.setDisable(valor);
      criarButton.setDisable(valor);
      entrarSalaText.setDisable(valor);
      entrarButton.setDisable(valor);
      progressIndicatorSala.setVisible(valor);
    });
  }

  /*********************************************
   * Metodo: getClienteReceberUDP
   * Funcao: Retorna a classe que recebe mensagens UDP
   * Parametros: void
   * Retorno: ClienteReceberUDP
   *********************************************/
  public ClienteReceberUDP getClienteReceberUDP() {
    return clienteReceberUDP;
  }

  /*********************************************
   * Metodo: getClienteEnviarUDP
   * Funcao: Retorna a classe que envia mensagens UDP
   * Parametros: void
   * Retorno: ClienteEnviarUDP
   *********************************************/
  public ClienteEnviarUDP getClienteEnviarUDP() {
    return clienteEnviarUDP;
  }

}//Fim class