package client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileHandler {

    static String read(String path) throws IOException {
        File file = new File(path);
        String text = "";
        if (file.exists()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file.getPath()))));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
                text += line;
            }
        }
        System.out.print("\n");
            return text;
    }

  public static void changeOrganisationsAttribute(String path, String id, String iban, long amount) throws IOException {
    JSONObject jsonObject = new JSONObject(FileHandler.read(path));
    JSONArray accounts= jsonObject.getJSONObject("organisation").getJSONArray("accounts");
    for( int i=0; i<accounts.length(); i++){
      JSONObject account= accounts.getJSONObject(i);
      JSONArray customers= account.getJSONArray("customers");
      for( int k=0; k<customers.length(); k++){
        JSONObject customer= customers.getJSONObject(k);
        String idCustomer=customer.getString("id");
        String roleCustomer=customer.getString("role");
        if(idCustomer.equals(id) && roleCustomer.equals("CUSTOMER") ){
          String ibanCustomer=account.getString("iban");
          if(ibanCustomer.equals(iban)){
            String previousAmount= account.getString("amount");
            account.put("amount",""+amount);
        
          }

        }
      }

    }
    File file = new File(path);
    BufferedWriter output = new BufferedWriter(new FileWriter(file));
    output.write(jsonObject.toString());
    output.close();
  }
}
