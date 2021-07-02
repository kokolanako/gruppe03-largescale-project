package client;

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import communication.Message;
import communication.ServerCommunicator;
import org.json.*;

import java.util.Timer;
import java.util.TimerTask;

import lombok.SneakyThrows;


public class Client {       //todo: tls socket
    private boolean connectionClosed;
    JSONObject jsonObject;
    private ServerCommunicator serverCommunicator;

    private String ownLastName;
    private String ownFirstName;
    private String id;

    public Client(String path) throws IOException {
        this.jsonObject = new JSONObject(FileReader.read(path));
        try {
            //Serverconnection
            Socket socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));
            System.out.println("Connected to Server:" + socket.getInetAddress());

            this.serverCommunicator = new ServerCommunicator(new ObjectInputStream(socket.getInputStream()),
                    new ObjectOutputStream(socket.getOutputStream()),
                    Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("retries")),
                    Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("timeout")),
                    this, jsonObject.getJSONObject("person").getJSONObject("keys").getString("private"));

            register();
            disconnectAfterDuration();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void register() throws IOException, ClassNotFoundException {
        Message answer = this.serverCommunicator.request(createRegistrationMessage(), "OK");
        if (answer != null) {
            System.out.println("Answer in thread: "+Thread.currentThread().getName()+" " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
                    + answer.getMessageText());
            this.connectionClosed = false;
          try {
            Thread.sleep(3000); // to wait until other client thread registers TT##//TODO otherwise null for public Key of receiver todo: try to send message multiple times
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
        },Integer.parseInt(jsonObject.getJSONObject("general").getString("duration"))*1000L);
    }



    public boolean isConnectionClosed() {
        return connectionClosed;
    }

    private Message createRegistrationMessage() {
        Message message = new Message();
        message.setTYPE("REGISTER");
        this.id=((JSONObject) jsonObject.get("person")).getString("id");
        message.setId(id);
        String[] name = (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
        this.ownLastName=name[0].trim().toLowerCase();
        message.setLastName(this.ownLastName);
        this.ownFirstName=name[1].trim().toLowerCase();
        message.setFirstName(this.ownFirstName);
        message.setPublicKey(((JSONObject) ((JSONObject) jsonObject.get("person")).get("keys")).getString("public"));
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
            System.out.println("Answer in Thread "+Thread.currentThread().getName()+" " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
                    + answer.getPublicKey());
            return answer.getPublicKey();
        } else {
            throw new NoKeyException("No public key received");
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
                sendMessage = createSendMessage(messageText, publicKey,messageType);
                sendMessage.setLastNameReceiver(splitName[0]);
                sendMessage.setFirstNameReceiver(splitName[1]);
                sendMessage(sendMessage);
            }catch (NoKeyException e){
                System.out.println("Can not get key from "+askKeyMessage.getFirstNameReceiver()+" "+askKeyMessage.getLastNameReceiver()
                        +", maybe (s)he does not exist");
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
              String logMsg="MESSAGE OK answer in Thread "+Thread.currentThread().getName()+" " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
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
                sendMessage = createSendMessage(messageText, publicKey,messageType);
                sendMessage.setIdReceiver(id);

                sendMessage(sendMessage);
            }catch (NoKeyException e){
                System.out.println("Can not get key from "+askKeyMessage.getId()+", maybe (s)he does not exist");
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
            String[] splitAction = action.toString().split("\\[");
            if (splitAction.length == 4) {
                if (splitAction[0].trim().equals("SEND")) {
                    String messageType = splitAction[3].trim().substring(0, splitAction[3].trim().length() - 1);
                    String message = splitAction[2].trim().substring(0, splitAction[2].trim().length() - 1);
                    String receiver = splitAction[1].trim().substring(0, splitAction[1].trim().length() - 1);
                  System.out.println("*********************");
                  System.out.println(message+"  "+receiver+" "+messageType);
                  String actualMessageType="MESSAGE";
                  if(messageType.equals("BUSINESS")){
                    actualMessageType= "MESSAGE_BUSINESS";
                  }else if(messageType.equals("PRIVATE")){
                    actualMessageType="MESSAGE_PRIVATE";        }
                    if (receiver.contains(",")) {
                        sendName(receiver, message,actualMessageType);
                    } else {
                        sendID(receiver, message,actualMessageType);
                    }
                }
            }
        }
    }

    public String getName() {
        return ((JSONObject) this.jsonObject.get("person")).getString("name");
    }

    public void closeConnection() {
        Message message = new Message();
        message.setTYPE("CLOSE_CONNECTION");

        Message answer = this.serverCommunicator.request(message, "CLOSE_CONNECTION");
        if (answer != null) {
            System.out.println("Answer in Thread "+Thread.currentThread().getName()+" " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
                    + answer.getMessageText());
            this.connectionClosed = true;
            System.out.println("Connection closed");
        } else {
            System.out.println("Connection not closed");
        }
    }
}
