package client;

import communication.Message;
import communication.ServerCommunicator;
import lombok.Getter;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class Client {
  @Getter
  private String personOrOrganisation;
  private boolean connectionClosed;
  JSONObject jsonObject;
  private ServerCommunicator serverCommunicator;

  private String ownLastName;
  private String ownFirstName;
  @Getter
  private String id;
  private String path;
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

      //todo: soll das geändert werden
      if (jsonObject.keySet().contains("person")) {
        this.personOrOrganisation = "person";
      } else if (jsonObject.keySet().contains("organisation")) {
        this.personOrOrganisation = "organisation";
      } else {
        System.out.println("Config: " + path + " is invalid.");
        System.exit(1);
      }
      //TODO bei Subklasse Organisation: in message set typeInstance(ORGANIZATION)
      this.serverCommunicator = new ServerCommunicator(
          new ObjectOutputStream(socket.getOutputStream()),new ObjectInputStream(socket.getInputStream()),
          Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("retries")),
          Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("timeout")),
          this, jsonObject.getJSONObject(this.personOrOrganisation).getJSONObject("keys").getString("private"));

      this.serverCommunicator = createServerCommunicator(socket);

      this.register();

      this.disconnectAfterDuration();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }


  abstract String getPublicKey();
  abstract String getPrivateKey();
  abstract String getID();
  abstract String[] getName();


  private ServerCommunicator createServerCommunicator(Socket socket) throws IOException {
    return new ServerCommunicator(new ObjectInputStream(socket.getInputStream()),
            new ObjectOutputStream(socket.getOutputStream()),
            Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("retries")),
            Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("timeout")),
            this, getPrivateKey());
  }



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
    Message message = new Message();
    message.setTYPE("REGISTER");
    this.id = getID();
    message.setId(id);
    String[] name = getName();
    this.ownLastName = name[0].trim().toLowerCase();
    message.setLastName(this.ownLastName);
    this.ownFirstName = name[1].trim().toLowerCase();
    message.setFirstName(this.ownFirstName);
    message.setPublicKey(getPublicKey());
    message.setMessageText("");
    return message;
  }

  private String getReceiverPublicKey(Message message) throws IOException, ClassNotFoundException, NoKeyException {
    message.setTYPE("ASK_PUBLIC_KEY");
    if (connectionClosed) {
      throw new NoKeyException("Connection to Server already closed, stop key request");
    }
    Message answer = this.serverCommunicator.request(message, "ASK_PUBLIC_KEY");

    if (answer != null) {
      System.out.println("Answer in Thread " + Thread.currentThread().getName() + " " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
          + answer.getPublicKey());
      return answer.getPublicKey();
    } else {
      throw new NoKeyException("No public key received");
    }
  }

  private void sendTransaction(Message messageTransaction) {
    System.out.println("-------------------- Trans "+messageTransaction);
    if (!connectionClosed) {
      try {
        this.serverCommunicator.streamOut(messageTransaction);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void sendName(String name, String messageText, String messageType) {
    try {
      //get public key of receiver
      Message askKeyMessage = new Message();
      String[] splitName = name.split(",");
      splitName[0] = splitName[0].toLowerCase().trim();
      splitName[1] = splitName[1].toLowerCase().trim();

      askKeyMessage.setLastNameReceiver(splitName[0]);
      askKeyMessage.setFirstNameReceiver(splitName[1]);
      try {
        String publicKey = getReceiverPublicKey(askKeyMessage);

        //encrypt message
        Message sendMessage;
        sendMessage = createSendMessage(messageText, publicKey, messageType);
        sendMessage.setLastNameReceiver(splitName[0]);
        sendMessage.setFirstNameReceiver(splitName[1]);
        sendMessage(sendMessage);
      } catch (NoKeyException e) {
        System.out.println("Can not get key from " + askKeyMessage.getFirstNameReceiver() + " " + askKeyMessage.getLastNameReceiver()
            + ", maybe (s)he does not exist");
        // e.printStackTrace();
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void sendMessage(Message message) throws IOException, ClassNotFoundException {
    //send message to receiver
    if (!connectionClosed) {
      Message answer = this.serverCommunicator.request(message, "OK");
      if (answer != null) {
        String logMsg = "MESSAGE OK answer in Thread " + Thread.currentThread().getName() + " " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
            + answer.getMessageText();
        System.out.println(logMsg);

      } else {
        System.out.println("Stop now retrying");
      }
    }
  }

  private void sendID(String id, String messageText, String messageType) {
    try {

      //get public key of receiver
      Message askKeyMessage = new Message();
      askKeyMessage.setIdReceiver(id);
      try {
        String publicKey = getReceiverPublicKey(askKeyMessage);

        //encrypt message
        Message sendMessage;
        sendMessage = createSendMessage(messageText, publicKey, messageType);
        sendMessage.setIdReceiver(id);

        sendMessage(sendMessage);
      } catch (NoKeyException e) {
        System.out.println("Can not get key from " + askKeyMessage.getId() + ", maybe (s)he does not exist");
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    // Beachte Timeout und Retry.
  }

  private Message createSendMessage(String messageText, String receiverPublicKey, String messageType) {
    Message message = new Message();
    message.setTYPE(messageType);
    message.setLastName(this.ownLastName);
    message.setFirstName(this.ownFirstName);
    message.setId(this.id);
    message.setMessageText(MessageHandler.encrypt(messageText, receiverPublicKey));
        return message;
  }

  public void runAllActions() {
    for (Object action : (JSONArray) jsonObject.get("actions")
    ) {
      if (connectionClosed) {
        break;
      }
      String fullAction = action.toString();
      String patternString = "\\[(.*?)\\]";//non-greedy match
      Pattern pattern = Pattern.compile(patternString);
      Matcher matcher = pattern.matcher(fullAction);
      List<String> allMatches = new ArrayList<String>();

      while(matcher.find()){
        allMatches.add(matcher.group(1).trim());
//        System.out.println(matcher.group(1));
      }


      if (fullAction.contains("SUB") || fullAction.contains("ADD")) {
        String messageType = null;
        Message messageTransaction = new Message();
        if (fullAction.contains("SUB")) {
          messageType = "TRANSACTION_SUB";
          messageTransaction.setIdReceiver(allMatches.get(0));
          messageTransaction.setIbanFrom(allMatches.get(1));
          messageTransaction.setAmount((this.convertToLong(allMatches.get(2))));

        } else if (fullAction.contains("ADD")) {
          messageType = "TRANSACTION_ADD";
          messageTransaction.setIbanFrom(allMatches.get(1));
          messageTransaction.setIbanTo(allMatches.get(2));
          messageTransaction.setAmount(this.convertToLong(allMatches.get(3)));

        }
        messageTransaction.setId(this.id);
        messageTransaction.setLastName(this.ownLastName);
        messageTransaction.setFirstName(this.ownFirstName);
        messageTransaction.setTYPE(messageType);
        this.sendTransaction(messageTransaction);

      } else {

        String messageType = "MESSAGE";
        if (allMatches.size() <= 3) {

          if (allMatches.size() == 3) {
            messageType = allMatches.get(2);
          }
          String message = allMatches.get(1);
          String receiver = allMatches.get(0);
          System.out.println("***********CHECK REGEX EXTRACTION BELOW**********");
          System.out.println(receiver + " " + messageType + " " + message);
          if (messageType.equals("BUSINESS")) {
            messageType = "MESSAGE_BUSINESS";
          } else if (messageType.equals("PRIVATE")) {
            messageType = "MESSAGE_PRIVATE";
          }
          if (receiver.contains(",")) {
            sendName(receiver, message, messageType);
          } else {
            sendID(receiver, message, messageType);
          }

        }
      }


    }
  }

  private long convertToLong(String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      try {
        this.serverCommunicator.getLogger().logString("Cannot convert " + s + " to long Format.");
      } catch (IOException ioException) {
        ioException.printStackTrace();//TODO
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

  public Long retrieveAmount(String iban, String id) {
    try {
      this.jsonObject = new JSONObject(FileHandler.read(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
    JSONArray accounts = this.jsonObject.getJSONObject("organisation").getJSONArray("accounts");
    for (int i = 0; i < accounts.length(); i++) {
      JSONObject account = accounts.getJSONObject(i);
      JSONArray customers = account.getJSONArray("customers");
      for (int k = 0; k < customers.length(); k++) {
        JSONObject customer = customers.getJSONObject(k);
        String idCustomer = customer.getString("id");
        String roleCustomer = customer.getString("role");
        if (idCustomer.equals(id) && roleCustomer.equals("CUSTOMER")) {
          String ibanCustomer = account.getString("iban");
          if (ibanCustomer.equals(iban)) {
            String previousAmount = account.getString("amount");
            return Long.parseLong(previousAmount);
          }

        }
      }
    }
    return null;
  }

  public void writeNewAmount(String iban, String idPerson, long amount) {
    JSONArray accounts = jsonObject.getJSONObject("organisation").getJSONArray("accounts");
    for (int i = 0; i < accounts.length(); i++) {
      JSONObject account = accounts.getJSONObject(i);
      JSONArray customers = account.getJSONArray("customers");
      for (int k = 0; k < customers.length(); k++) {
        JSONObject customer = customers.getJSONObject(k);
        String idCustomer = customer.getString("id");
        String roleCustomer = customer.getString("role");
        if (idCustomer.equals(idPerson) && roleCustomer.equals("CUSTOMER")) {
          String ibanCustomer = account.getString("iban");
          if (ibanCustomer.equals(iban)) {
            account.put("amount", "" + amount);

          }

        }
      }

    }
    try {
      FileHandler.write(this.path, this.jsonObject);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void startListeningToTransactions() {
    if (this.personOrOrganisation.equals("organisation")) {
        this.serverCommunicator.createAndStartTransactionsListener();

    }
  }
}
