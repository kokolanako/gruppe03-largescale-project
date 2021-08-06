package communication;

import client.Client;
import client.PersonClient;
import client.bank.BankClient;
import pojo.Bank;

import java.io.IOException;
import java.util.ArrayList;


public class ApplicationStarter {

  public static void main(String[] args) throws IOException {
    String path1 = "src/main/resources/configs/config_1.json";
    String path2 = "src/main/resources/configs/config_2.json";
//    String path3 = "src/main/resources/configs/bank.json";
    try {
      ArrayList<Client> clientArrayList = new ArrayList<>();
      ArrayList<Thread> threadArrayList = new ArrayList<>();
//      clientArrayList.add(new BankClient(path3));
      clientArrayList.add(new PersonClient(path1));
      clientArrayList.add(new PersonClient(path2));

      for (Client client : clientArrayList
      ) {
        Thread thread = new Thread(() -> {
          if (!client.isConnectionClosed()) {
            if (client.getClass() == PersonClient.class) {
              ((PersonClient) client).runAllActions();
            }else if(client.getClass() == BankClient.class){
              ((BankClient) client).startListeningToTransactions(); //todo: was ist das
            }
          }
        });
        thread.setName("Thread " + client.getName());
        threadArrayList.add(thread);
        thread.start();
      }
      while (true) {
        clientArrayList.removeIf(Client::isConnectionClosed);
        if (clientArrayList.isEmpty()) {
          for (Thread thread : threadArrayList
          ) {
            thread.join(10);
          }
          System.exit(0);
          break;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
