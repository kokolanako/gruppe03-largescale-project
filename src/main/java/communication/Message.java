package communication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

//todo: Message nur noch per referenz aufrufen
@NoArgsConstructor
public class Message implements Serializable {
  private static final long serialVersionUID = 7829136421241571165L;
  @Setter
  @Getter
  private String TYPE;  //for client: REGISTER, MESSAGE, CLOSE_CONNECTION // for server: ASK_PUBLIC_KEY, OK, ERROR
  @Setter
  @Getter
  private String id;
  @Setter
  @Getter
  private String firstName;
  @Setter
  @Getter
  private String lastName;
  @Setter
  @Getter
  private String publicKey;
  @Setter
  @Getter
  private String messageText;
  @Getter
  @Setter
  private int message_ID;

  @Setter
  @Getter
  private String firstNameReceiver;
  @Setter
  @Getter
  private String lastNameReceiver;

  @Setter
  @Getter
  private String idReceiver;

  @Setter
  @Getter
  private String ibanFrom;
  @Setter
  @Getter
  private String ibanTo;

  @Setter
  @Getter
  private long amount; //TODO in CENTS?

  @Setter
  @Getter
  private String typeInstance;


  @Override
  public String toString(){
    return "Message: "+TYPE+ " id: "+id+" firstName: "+firstName+" lastName "+lastName+" messageText "+messageText+" msg id "+message_ID
        +" to receiver "+firstNameReceiver+" "+lastNameReceiver;
  }
}
