package client;

import javax.net.SocketFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.concurrent.*;

import communication.Message;
import org.json.*;


public class Client {
    JSONObject jsonObject;
    private int id_counter = 0;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private int getNextID() {
        this.id_counter += 1;
        return this.id_counter;
    }

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
        System.out.println("Message " + message.getMessage_ID() + " send");

        System.out.println("Waiting for answer");
        Message answer = (Message) objectInputStream.readObject();
        System.out.println("Answer received");
        System.out.println(answer.getMessage_ID() + " " + answer.getTYPE() + " " + answer.getMessageText());
    }

    private Message createRegistrationMessage() {
        Message message = new Message();
        message.setTYPE("REGISTER");
        message.setMessage_ID(this.getNextID());
        message.setId(((JSONObject) jsonObject.get("person")).getString("id"));
        String[] name = (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
        message.setLastName(name[0].trim().toLowerCase());
        message.setFirstName(name[1].trim().toLowerCase());
        message.setPublicKey(((JSONObject) ((JSONObject) jsonObject.get("person")).get("keys")).getString("public"));
        message.setMessageText("");
        return message;
    }

    private void streamOut(Message message) throws IOException {
        this.objectOutputStream.writeObject(message);
        this.objectOutputStream.flush();
        System.out.println("Message " + message.getMessage_ID() + " send");
    }

    private String getReceiverPublicKey(Message message) throws IOException, ClassNotFoundException {
        message.setTYPE("ASK_PUBLIC_KEY");
        message.setMessage_ID(this.getNextID());
        System.out.println(((JSONObject) jsonObject.get("person")).getString("name") + " start sending ASK_PUBLIC_KEY_Message");
//        this.objectOutputStream.writeObject(message);
//        this.objectOutputStream.flush();
//        System.out.println("Message "+message.getMessage_ID()+" send");


        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Message> task = new Callable<>() {
            public Message call() throws InterruptedException, MalformedURLException, Exception {
                streamOut(message);
                return waitOnServerAnswer(message.getMessage_ID(), "ASK_PUBLIC_KEY");
            }
        };
        int retryCounter = 0;
        int maxRetries = Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("retries"));
        int timeout = Integer.parseInt(((JSONObject) jsonObject.get("general")).getString("timeout"));
        Message answer = null;
        System.out.println("Waiting for answerMessage");
        while (retryCounter < maxRetries) {
            Future<Message> future = executor.submit(task);
            try {
                answer = future.get(timeout, TimeUnit.SECONDS);
                break;

            } catch (TimeoutException ex) {
                retryCounter++;
                System.out.println("Timeout Occured");

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                break;
            } finally {
                future.cancel(true);
            }
//        Message answer = waitOnServerAnswer(message.getMessage_ID(), "ASK_PUBLIC_KEY");
        }
        if (answer != null) {
            System.out.println("Answer received");
            System.out.println(answer.getMessage_ID() + " " + answer.getTYPE() + " " + answer.getPublicKey());
            return answer.getPublicKey();
        } else {
            System.out.println("Answer not received, stop retrying");
            return null;
        }
    }

    private Message waitOnServerAnswer(int id, String type) throws IOException, ClassNotFoundException {
        while (true) {
            Object in = objectInputStream.readObject();
            if (in instanceof Message) {
                Message answer = (Message) in;
                if (answer.getMessage_ID() == id && answer.getTYPE().equals(type)) {
                    return answer;
                } else {
                    if (answer.getTYPE().equals("MESSAGE")) {
                        System.out.println(((JSONObject) jsonObject.get("person")).getString("name") +
                                " received Message from " + answer.getFirstName() + " " + answer.getLastName());
                        //TODO logging message
                    }
                }
            } else {
                System.out.println("in is not a Message and contains " + in.toString());
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
        System.out.println("Message " + message.getMessage_ID() + " send");

        //TODO Beachte Timeout und Retry, wenn Server throw Exception.
        System.out.println("Waiting for answerMessage");
        Message answerMessage = waitOnServerAnswer(message.getMessage_ID(), "OK");
        System.out.println("Answer received");
        System.out.println(answerMessage.getMessage_ID() + " " + answerMessage.getTYPE() + " " + answerMessage.getMessageText());

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
        message.setMessage_ID(this.getNextID());
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
        message.setMessage_ID(this.getNextID());
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        Message answer = waitOnServerAnswer(message.getMessage_ID(), "CLOSE_CONNECTION");
        System.out.println(answer.getMessage_ID() + " " + answer.getTYPE() + " " + answer.getMessageText());
    }
}
