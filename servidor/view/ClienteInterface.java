/***********************************************************************
 * Autor: Cassio Meira Silva
 * Matricula: 201610373
 * Inicio: 02/12/18
 * Ultima alteracao: 14/12/18
 * Nome: ClienteInterface
 * Funcao: Tela da interface do Cliente
 ***********************************************************************/

package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import socket.IReceberNet;
import socket.pdu.Apdu;
import view.controller.Conversa;
import view.controller.TelaCliente;
import view.controller.MenuSuperior;
import view.menuLateral.BoxMenu;
import view.menuLateral.MenuLateral;
import view.menuLateral.SubMenu;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class ClienteInterface extends BorderPane implements IReceberNet<DatagramPacket>, MenuLateral.TelaMenu, Apdu.IApdu, Conversa.IActions {
  private final String FXML = "view/telaPrincipal.fxml";//Caminho do FXML
  private final String titulo = "Cliente Chat";
  private Stage palco;

  private MenuSuperior menuSuperior;//Barra de Menu Superior
  private MenuLateral menuLateral;//Menu Lateral

  private BoxMenu menuPrincipal;

  private TelaCliente telaCliente;//Tela do Cliente

  private Map<String, Conversa> conversaMap = new HashMap<>();

  /*********************************************
   * Metodo: ClienteInterface - Construtor
   * Funcao: Constroi objetos da classe ClienteInterface
   * Parametros: void
   * Retorno: void
   *********************************************/
  public ClienteInterface(Stage palco) {
    this.palco = palco;
    try {
      this.palco.setTitle(titulo);
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
    this.menuSuperior = new MenuSuperior();//Instanciando MenuSuperior
    this.setTop(menuSuperior);//Adicionando na Interface

    this.menuLateral = new MenuLateral(this);//Instanciando MenuLateral
    this.setLeft(menuLateral);//Adicionando na Interface

    this.telaCliente = new TelaCliente();
    this.telaCliente.setPalco(palco);
    this.telaCliente.setInterfacePai(this);
    this.setCenter(telaCliente);

    menuPrincipal = new BoxMenu("Principal", menuLateral);
    menuLateral.adicionarMenu(menuPrincipal);
    SubMenu subMenu = new SubMenu("Cliente", menuPrincipal) {
      @Override
      public void onClick() {
        setCenter(telaCliente);
      }
    };
    menuPrincipal.onClick();
  }

  /*********************************************
   * Metodo: adicionarSala
   * Funcao: Adiciona uma nova sala no MenuLateral
   * Parametros: String sala
   * Retorno: void
   *********************************************/
  public void adicionarSala(String sala) {
    Conversa conversa = new Conversa();
    conversa.setNomeSala(sala);
    conversa.setEnviarNet(telaCliente.getClienteEnviarUDP());
    conversa.setActions(this);
    //conversa.mensagemCentral("Voce criou o Grupo " + sala);
    conversaMap.put(sala, conversa);
    menuLateral.adicionarMenu(conversa);
  }

  /*********************************************
   * Metodo: receberMensagem
   * Funcao: Recebe um pacote via Socket
   * Parametros: DatagramPacket pacote
   * Retorno: void
   *********************************************/
  @Override
  public void receberMensagem(DatagramPacket pacote) {
    String mensagem = new String(pacote.getData());
    System.out.println("\nCliente UDP Porta [" + pacote.getPort() + "] Recebeu: " + mensagem);

    Apdu apdu = new Apdu(this);
    apdu.tratarMensagem(mensagem, pacote.getAddress().getHostAddress());
  }

  /*********************************************
   * Metodo: setTela
   * Funcao: Adiciona uma nova interface no Centro
   * Parametros: Node node
   * Retorno: void
   *********************************************/
  @Override
  public void setTela(Node node) {
    this.setCenter(node);
  }

  /*********************************************
   * Metodo: mensagem
   * Funcao: Recebe uma nova Mensagem de uma sala
   * Parametros: String sala, String ip, String mensagem
   * Retorno: void
   *********************************************/
  @Override
  public void mensagem(String sala, String ip, String mensagem) {
    Conversa conversa = conversaMap.get(sala);

    if (conversa != null) {
      conversa.receberMensagem(mensagem, ip);
    }

  }

  /*********************************************
   * Metodo: criarSala
   * Funcao: Recebe uma mensagem pra criar uma sala
   * Parametros: String sala
   * Retorno: void
   *********************************************/
  @Override
  public void criarSala(String sala) {

  }

  /*********************************************
   * Metodo: entrarSala
   * Funcao: Recebe uma mensagem pra entrar numa sala
   * Parametros: String sala, String ip
   * Retorno: void
   *********************************************/
  @Override
  public void entrarSala(String sala, String ip) {
    Conversa conversa = conversaMap.get(sala);
    System.out.println("\nentrou aqui");
    System.out.println("IP: " + ip);
    System.out.println("Sala: " + sala);

    System.out.println(conversa);

    if (conversa != null) {
      System.out.println(ip + " entou no Grupo");
      conversa.mensagemCentral(ip  + " entrou no Grupo");
    }

  }

  /*********************************************
   * Metodo: sairSala
   * Funcao: Recebe uma mensagem que alguem saiu da sala
   * Parametros: String sala, String iṕ
   * Retorno: void
   *********************************************/
  @Override
  public void sairSala(String sala, String ip) {
    Conversa conversa = conversaMap.get(sala);
    if (conversa != null) {
      conversa.mensagemCentral(ip  + " saiu no Grupo");
    }
  }

  /*********************************************
   * Metodo: sairConversa
   * Funcao: Recebe uma mensagem pra sair da sala
   * Parametros: String sala
   * Retorno: void
   *********************************************/
  @Override
  public void sairConversa(String sala) {
    Conversa conversa = conversaMap.get(sala);

    if (conversa != null) {
      menuLateral.removerMenu(conversa);
    }

    menuPrincipal.onClick();
  }

}
