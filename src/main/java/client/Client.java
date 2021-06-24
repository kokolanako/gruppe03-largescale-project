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


public class Client {       //todo: RSA verschlüsselung, tls socket
    private boolean connectionClosed;
    JSONObject jsonObject;
    private ServerCommunicator serverCommunicator;

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
                    Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("timeout")), this, jsonObject.getJSONObject("person").getJSONObject("keys").getString("private"));

            register();
            disconnectAfterDuration();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void register() throws IOException, ClassNotFoundException {
        Message answer = this.serverCommunicator.request(createRegistrationMessage(), "OK");
        if (answer != null) {
            System.out.println("Answer " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " " + answer.getMessageText());
            this.connectionClosed = false;
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
        message.setId(((JSONObject) jsonObject.get("person")).getString("id"));
        String[] name = (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
        message.setLastName(name[0].trim().toLowerCase());
        message.setFirstName(name[1].trim().toLowerCase());
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
            System.out.println("Answer " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " " + answer.getPublicKey());
            return answer.getPublicKey();
        } else {
            throw new NoKeyException("No public key received");
        }
    }

    private void sendName(String name, String messageText) {
        try {

            //get public key of receiver
            Message askKeyMessage = new Message();
            String[] splitName = name.split(",");
            splitName[0] = splitName[0].toLowerCase().trim();
            splitName[1] = splitName[1].toLowerCase().trim();

            askKeyMessage.setLastName(splitName[0]);
            askKeyMessage.setFirstName(splitName[1]);
            try {
                String publicKey = getReceiverPublicKey(askKeyMessage); //Todo: Error : wieso kann es vorkommen, dass es keinen KEY gibt (==null)

                //encrypt message
                Message sendMessage;
                sendMessage = createSendMessage(messageText, publicKey);
                sendMessage.setLastName(splitName[0]);
                sendMessage.setFirstName(splitName[1]);
                sendMessage(sendMessage);
            }catch (NoKeyException e){
                e.printStackTrace();
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
                System.out.println("Answer " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " " + answer.getMessageText());
            } else {
                System.out.println("Stop now retrying");
            }
        }
    }

    private void sendID(String id, String messageText) {
        try {

            //get public key of receiver
            Message askKeyMessage = new Message();
            askKeyMessage.setId(id);
            try {
                String publicKey = getReceiverPublicKey(askKeyMessage);

                //encrypt message
                Message sendMessage;
                sendMessage = createSendMessage(messageText, publicKey);
                sendMessage.setId(id);

                sendMessage(sendMessage);
            }catch (NoKeyException e){
                e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Beachte Timeout und Retry.
    }

    private Message createSendMessage(String messageText, String receiverPublicKey) {
        Message message = new Message();
        message.setTYPE("MESSAGE");
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
            if (splitAction.length == 3) {
                if (splitAction[0].trim().equals("SEND")) {
                    String message = splitAction[2].trim().substring(0, splitAction[2].trim().length() - 1);
                    String reciever = splitAction[1].trim().substring(0, splitAction[1].trim().length() - 1);
                    if (reciever.contains(",")) {
                        sendName(reciever, message);
                    } else {
                        sendID(reciever, message);
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
            System.out.println("Answer " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " " + answer.getMessageText());
            this.connectionClosed = true;
            System.out.println("Connection closed");
        } else {
            System.out.println("Connection not closed");
        }
    }
}
