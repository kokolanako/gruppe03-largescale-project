package communication;

import client.Client;
import client.MessageHandler;
import lombok.Getter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class ServerCommunicator {
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private Client client;
  @Getter
  private Logger logger;

  private int id_counter = 0;
  private final int timeout;
  private final int maxRetries;
  private Message serverAnswer;
  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
  String privateKey;

  private Thread bankTransactions;


  public ServerCommunicator( ObjectOutputStream outputStream, ObjectInputStream inputStream,int maxRetries, int timeout, Client client, String privateKey) {
    this.objectOutputStream = outputStream;
    this.objectInputStream = inputStream;
    this.maxRetries = maxRetries;
    this.timeout = timeout;
    this.privateKey = privateKey;
    this.client = client;
    try {
      this.logger = new Logger(client.getName() + " inbox");
    } catch (IOException e) {
      e.printStackTrace();
    }


  }
//TODO Encode/Decode , pull public key of requester by checking json config-> problem if multiple ids are registered for one account
  public void createAndStartTransactionsListener(ObjectInputStream os) {
    this.bankTransactions = new Thread(() -> {
      while (true) {
        try {
          Object in = os.readObject();
          if (in instanceof Message) {
            Message answer = (Message) in;

            if (answer.getTYPE().equals("TRANSACTION_SUB")) {
              //TODO check identity msg.getID whether it suits to account owner
              Long currentAmount = this.client.retrieveAmount(answer.getIbanFrom(), answer.getId());
              answer.setIdReceiver(answer.getId());
              answer.setId(this.client.getId());
              if (currentAmount == null) {
                answer.setTYPE("TRANSACTION_SUB_ERROR");
                answer.setMessageText("TRANSACTION_SUB could not take place due to missing account for id: " + answer.getIdReceiver());
                this.streamOut(answer);
                break;
              } else {
                long newAmount = currentAmount - answer.getAmount();
                if (newAmount < 0) {
                  answer.setTYPE("TRANSACTION_SUB_ERROR");
                  answer.setMessageText("TRANSACTION_SUB could not take place due to the negative resulting amount at account for id: " + answer.getIdReceiver());
                  this.streamOut(answer);
                  break;
                } else {
                  this.client.writeNewAmount(answer.getIbanFrom(), answer.getId(), newAmount);
                  answer.setTYPE("TRANSACTION_SUB_OK");
                  answer.setMessageText("TRANSACTION_SUB took place successfully for id " + answer.getIdReceiver());
                  this.streamOut(answer);
                  break;
                }
              }
            } else if (answer.getTYPE().equals("TRANSACTION_SUB_OK")) {
              this.logger.logString(answer.getMessageText());
            } else if (answer.getTYPE().equals("TRANSACTION_SUB_ERROR")) {
              System.out.println("Server error detected for msg-ID: : " + answer.getMessage_ID() + " Error message: " + answer.getMessageText());
              this.serverAnswer = answer;
              break;
            }}

          } catch(IOException e){
            e.printStackTrace();
          } catch(ClassNotFoundException e){
            e.printStackTrace();
          }
        }

    });
    this.bankTransactions.start();
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
    return ++this.id_counter;
  }

  public void streamOut(Message message) throws IOException {
    try {
      if (!client.isConnectionClosed()) {
        this.objectOutputStream.writeObject(message);
        this.objectOutputStream.flush();
        System.out.println("SEND: " + this.client.getName() + " send Message with id: " + message.getMessage_ID() + " with type: " + message.getTYPE());
        System.out.println(message);
//        System.out.println("Waiting for answerMessage");
      }

    } catch (SocketException e) {
      String logMsg = "The central server is disconnected from the network.";
      System.out.println(logMsg);
      this.logger.logString(logMsg);
      System.exit(1);
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
    try {
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
              if (answer.getTYPE().equals("MESSAGE") || answer.getTYPE().equals("MESSAGE_PRIVATE") || answer.getTYPE().equals("MESSAGE_BUSINESS")) {

                var messageText = MessageHandler.decrypt(answer.getMessageText(), privateKey);
                System.out.println("RECEIVED: " + this.client.getName() + " received Message from " + answer.getFirstName() + " " + answer.getLastName() + " message: " + messageText);

                LocalDateTime now = LocalDateTime.now();
                String prefix;
                if (answer.getTYPE().equals("MESSAGE_BUSINESS")) {
                  prefix = "business";
                } else {
                  prefix = "private";
                }
                this.logger.logString(prefix + " Message id " + answer.getMessage_ID() + " from " + answer.getFirstName() + " " + answer.getLastName() + " at " + this.dtf.format(now) + ": " + messageText);


              } else if (answer.getTYPE().equals("ERROR") || answer.getTYPE().equals("TRANSACTION_SUB_ERROR")) {
                System.out.println("Server error detected for msg-ID: : " + answer.getMessage_ID() + " Error message: " + answer.getMessageText());
                this.serverAnswer = answer;
                break;
              }
            }
          } else {
            System.out.print("in is not a Message");
            if (in != null) {
              System.out.println(" and contains " + in);
            } else {
              System.out.println(" and is null");
            }
            break;
          }
        } else {
          System.out.println("Connection to Server is closed");
          break;
        }
      }
    } catch (SocketException e) {
      System.out.println("Server has disconnected.");
      this.logger.logString("Server has disconnected");
    }
  }


}
