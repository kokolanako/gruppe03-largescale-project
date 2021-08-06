package client;

import communication.Message;
import communication.ServerCommunicator;
import lombok.Getter;
import lombok.SneakyThrows;
import org.json.JSONObject;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public abstract class Client {
  @Getter
  private String personOrOrganisation;
  boolean connectionClosed;
  protected JSONObject jsonObject;
  protected ServerCommunicator serverCommunicator;

  String ownLastName;
  String ownFirstName;
  @Getter
  String id;
  protected String path;
  private Socket socket;

  public Client(String path) throws IOException {
    this.path = path;
    this.jsonObject = new JSONObject(FileHandler.read(path));
    try {
      //Serverconnection
      this.socket = SocketFactory.getDefault().createSocket(
              ((JSONObject) jsonObject.get("server")).getString("ip"),
              ((JSONObject) jsonObject.get("server")).getInt("port"));
      System.out.println("Connected to Server:" + socket.getInetAddress());


      this.serverCommunicator = new ServerCommunicator(
              new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()),
              Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("retries")),
              Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("timeout")),
              this, getPrivateKey());

      this.id = getID();
      String[] name = getName();
      this.ownFirstName = name[1].trim().toLowerCase();
      this.ownLastName = name[0].trim().toLowerCase();

      this.register();

      this.disconnectAfterDuration();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }


  abstract String getPublicKey();

  abstract String getPrivateKey();

  public abstract String getID();

  public abstract String[] getName();

  abstract String getTypeInstance();

  /**
   * registers on the Server given in the configs
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void register() throws IOException, ClassNotFoundException {
    Message answer = this.serverCommunicator.request(createRegistrationMessage(), "OK");
    if (answer != null) {
      System.out.println("Answer in thread: " + Thread.currentThread().getName() + " " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
              + answer.getMessageText());
      this.connectionClosed = false;
      try {
        Thread.sleep(3000); // to wait until other client thread registers TT##, otherwise public Key of receiver could be null
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Registration went wrong");
      this.connectionClosed = true;
    }
  }

  private void disconnectAfterDuration() {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @SneakyThrows
      @Override
      public void run() {
        closeConnection();
      }
    }, Integer.parseInt(jsonObject.getJSONObject("general").getString("duration")) * 1000L);
  }


  public boolean isConnectionClosed() {
    return connectionClosed;
  }

  private Message createRegistrationMessage() {
    Message message = createMessageWithClientInformation("REGISTER");
    message.setPublicKey(getPublicKey());
    message.setMessageText("");
    return message;
  }

  String getReceiverPublicKey(Message message) throws IOException, ClassNotFoundException, NoKeyException {
    message.setTYPE("ASK_PUBLIC_KEY");
    message.setTypeInstance(getTypeInstance());
    if (connectionClosed) {
      throw new NoKeyException("Connection to Server already closed, stop key request");
    }
    Message answer = this.serverCommunicator.request(message, "ASK_PUBLIC_KEY");
    if (answer != null) {
      answer.setTypeInstance(getTypeInstance());
      System.out.println("Answer in Thread " + Thread.currentThread().getName() + " " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
              + answer.getPublicKey());
      return answer.getPublicKey();
    } else {
      throw new NoKeyException("No public key received");
    }
  }









  Message createSendMessage(String messageText, String receiverPublicKey, String messageType) {
    Message message = createMessageWithClientInformation(messageType);
    message.setMessageText(MessageHandler.encrypt(messageText, receiverPublicKey));
    return message;
  }

  /**
   * Ein MessageObject wird erstellt.
   * Es wird der Type, der Name des Senders und dessen Id festgelegt.
   *
   * @param messageType
   * @return Message
   */
  private Message createMessageWithClientInformation(String messageType) {
    Message message = new Message();
    message.setTYPE(messageType);
    message.setLastName(this.ownLastName);
    message.setFirstName(this.ownFirstName);
    message.setId(this.id);
    message.setTypeInstance(getTypeInstance());
    return message;
  }

   long convertToLong(String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      try {
        this.serverCommunicator.getLogger().logString("Cannot convert " + s + " to long Format.");
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
    return -1;
  }


  public void closeConnection() {
    Message message = new Message();
    message.setTYPE("CLOSE_CONNECTION");

    Message answer = this.serverCommunicator.request(message, "CLOSE_CONNECTION");
    if (answer != null) {
      System.out.println("Answer in Thread " + Thread.currentThread().getName() + " " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
              + answer.getMessageText());
      this.connectionClosed = true;
      System.out.println("Connection closed");
    } else {
      System.out.println("Connection not closed");
    }
  }


}


