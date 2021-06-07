import client.Client;

import java.io.File;
import java.io.IOException;

public class ApplicationStarter {

  public static void main(String[] args) throws IOException {
    String path="src/main/resources/configs/config_1.json";
    try{
      Client client=new Client(path);
      System.out.println("Client created and registered");
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}
