package client;

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;

import communication.Message;
import org.json.*;


public class Client {
    JSONObject jsonObject;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public Client(String path) throws IOException {
        this.jsonObject = new JSONObject(FileReader.read(path));
        try {
            //Serverconnection
            this.socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));
            System.out.println("Connected to Server:" + socket.getInetAddress());

            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            register(objectInputStream, objectOutputStream);

            System.out.println("Client " + ((JSONObject) jsonObject.get("person")).getString("name") + " created and registered");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void register(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        //objectInputStream enthaelt ACK wenn erfolgreich else Fehlermeldung

        //create message for registration
        Message message = createRegistrationMessage();
        //send to server
        System.out.println(((JSONObject) jsonObject.get("person")).getString("name") + " start sending Message");
        objectOutputStream.writeObject(message); //switched message and objectoutputstream //todo: soll ein dataOutputstream erzeugt werden
        objectOutputStream.flush();
        System.out.println("Message send");

        System.out.println("Waiting for answer");
        Message answer = (Message) objectInputStream.readObject();
        System.out.println("Answer received");
        System.out.println(answer.getTYPE() + " " + answer.getMessageText());
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

    private String getReceiverPublicKey(Message message) throws IOException, ClassNotFoundException {
        message.setTYPE("ASK_PUBLIC_KEY");
        System.out.println(((JSONObject) jsonObject.get("person")).getString("name") + " start sending ASK_PUBLIC_KEY_Message");
        this.objectOutputStream.writeObject(message);
        this.objectOutputStream.flush();
        System.out.println("Message send");

        System.out.println("Waiting for answerMessage");
        Message answer = waitOnServerAnswer("ASK_PUBLIC_KEY");
        System.out.println("Answer received");
        System.out.println(answer.getTYPE() + " " + answer.getPublicKey());
        return answer.getPublicKey();
    }

    private Message waitOnServerAnswer(String type) throws IOException, ClassNotFoundException {
        while (true) {
            Message answer = (Message) objectInputStream.readObject();
            if (answer.getTYPE().equals(type)) {
                return answer;
            } else {
                if (answer.getTYPE().equals("MESSAGE")) {
                    System.out.println("Received Message");
                    //TODO logging message
                }
            }
        }
    }

    private void sendName(String name, String messageText) {
        try {

            //get public key of receiver
            Message askKeyMessage = new Message();
            String[] splitName = name.split(",");
            splitName[0] = splitName[0].toLowerCase();
            splitName[1] = splitName[1].toLowerCase();

            askKeyMessage.setLastName(splitName[0].trim());
            askKeyMessage.setFirstName(splitName[1].trim());
            String publicKey = getReceiverPublicKey(askKeyMessage);

            //encrypt message
            Message sendMessage;
            sendMessage = createSendMessage(messageText, publicKey);
            sendMessage.setLastName(splitName[0]);
            sendMessage.setFirstName(splitName[1]);
            sendMessage(sendMessage);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message message) throws IOException, ClassNotFoundException {
        //send message to receiver
        System.out.println(((JSONObject) jsonObject.get("person")).getString("name") + " Start sending Message");
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        System.out.println("Message send");

        System.out.println("Waiting for answerMessage");
        Message answerMessage = waitOnServerAnswer("OK");
        System.out.println("Answer received");
        System.out.println(answerMessage.getTYPE() + " " + answerMessage.getMessageText());
        //TODO Beachte Timeout und Retry, wenn Server throw Exception.
    }

    private void sendID(String id, String messageText) {
        try {

            //get public key of receiver
            Message askKeyMessage = new Message();
            askKeyMessage.setId(id);
            String publicKey = getReceiverPublicKey(askKeyMessage);

            //encrypt message
            Message sendMessage;
            sendMessage = createSendMessage(messageText, publicKey);
            sendMessage.setId(id);

            sendMessage(sendMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Beachte Timeout und Retry.
    }

    private Message createSendMessage(String messageText, String recieverPublicKey) {
        Message message = new Message();
        message.setTYPE("MESSAGE");
        //TODO messageText verschluesseln
        String cryptedText = messageText;
        message.setMessageText(cryptedText);

        return message;
    }

    public void runAllActions() {
        for (Object action : (JSONArray) jsonObject.get("actions")
        ) {
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

    public int getDuration() {
        return ((JSONObject) this.jsonObject.get("general")).getInt("duration");
    }

    public void closeConnection() throws IOException, ClassNotFoundException {
        Message message = new Message();
        message.setTYPE("CLOSE_CONNECTION");
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        Message answer = waitOnServerAnswer("OK");
        System.out.println(answer.getTYPE() + " " + answer.getMessageText());
    }
}
