import client.Client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ApplicationStarter {

    public static void main(String[] args) throws IOException {
        String path1 = "src/main/resources/configs/config_1.json";
        String path2 = "src/main/resources/configs/config_2.json";
        try {
            ArrayList<Client> clientArrayList=new ArrayList<>();
            ArrayList<Thread> threadArrayList=new ArrayList<>();
            clientArrayList.add(new Client(path1));
            clientArrayList.add(new Client(path2));

            for (Client client:clientArrayList
                 ) {
                Thread thread=new Thread(() -> {
                    if(!client.isConnectionClosed()){
                        client.runAllActions();
                    }
                    while(true){
                        if(client.isConnectionClosed()){
                            clientArrayList.remove(client);
                            break;
                        }
                    }
                });
                threadArrayList.add(thread);
                thread.start();
            }
            while(true){
                if (clientArrayList.isEmpty()){
                    for (Thread thread:threadArrayList
                         ) {
                        thread.join();
                    }
                    //FIXME whyever it just work with debugging and breakpoint at row 27
                    System.exit(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
