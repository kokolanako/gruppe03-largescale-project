package communication;

import client.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class ServerCommunicator {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Client client;
    private Logger logger;

    private int id_counter = 0;
    private final int timeout;
    private final int maxRetries;
    private Message serverAnswer;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public ServerCommunicator(ObjectInputStream inputStream, ObjectOutputStream outputStream, int maxRetries, int timeout, Client client) {
        this.objectOutputStream = outputStream;
        this.objectInputStream = inputStream;
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.client = client;
        try {
            this.logger = new Logger(client.getName() + " inbox");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message request(Message message, String responseType) {
        if (client.isConnectionClosed()) {
            throw new IllegalStateException("Connection to Server already closed");
        }
        message.setMessage_ID(getNextID());

        int retryCounter = 0;
        Message answer = null;
        while (answer == null && retryCounter < this.maxRetries) {
            try {
                streamOut(message);
                waitOnServerAnswer(message.getMessage_ID(), responseType);
                answer = getServerAnswer();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            if (answer != null && answer.getTYPE().equals("ERROR")) {
                answer = null;
                retryCounter++;
                try {
                    TimeUnit.SECONDS.sleep(this.timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return answer;
    }

    private int getNextID() {
        this.id_counter += 1;
        return this.id_counter;
    }

    private void streamOut(Message message) throws IOException {
        if (!client.isConnectionClosed()) {
            this.objectOutputStream.writeObject(message);
            this.objectOutputStream.flush();
            System.out.println(this.client.getName() + " send Message with id: " + message.getMessage_ID() + " with type: " + message.getTYPE());
            System.out.println("Waiting for answerMessage");
        }
    }

    private Message getServerAnswer() {
        while (true) {
            if (this.serverAnswer != null) {
                Message ret = this.serverAnswer;
                this.serverAnswer = null;
                return ret;
            }
        }
    }

    private void waitOnServerAnswer(int id, String type) throws IOException, ClassNotFoundException {
        while (true) {
            if (!client.isConnectionClosed()) {
                Object in = objectInputStream.readObject();
                if (in instanceof Message) {
                    Message answer = (Message) in;
                    if (answer.getMessage_ID() == id && answer.getTYPE().equals(type)) {
                        this.serverAnswer = answer;
                        if (answer.getTYPE().equals("CLOSE_CONNECTION")) {
                            this.objectInputStream.close();
                            this.objectOutputStream.close();
                            this.logger.finalalize();
                        }
                        break;
                    } else {
                        if (answer.getTYPE().equals("MESSAGE")) {
                            System.out.println(this.client.getName() + " received Message from " + answer.getFirstName() + " " + answer.getLastName());
                            //TODO Msg entschluesseln
                            LocalDateTime now = LocalDateTime.now();
                            this.logger.logString("Message from " + answer.getFirstName() + " " + answer.getLastName() + " at "+this.dtf.format(now)+": " + answer.getMessageText());
                        } else if (answer.getTYPE().equals("ERROR")) {
                            System.out.println("Server error detected: " + answer.getMessageText());
                            this.serverAnswer = answer;
                            break;
                        }
                    }
                } else {
                    System.out.println("in is not a Message");
                    if(in!=null){
                    System.out.print(" and contains " + in);}
                    else{
                        System.out.print(" and is null");
                    }
                    break;
                }
            } else {
                System.out.println("Connection to Server is closed");
                break;
            }
        }
    }
}