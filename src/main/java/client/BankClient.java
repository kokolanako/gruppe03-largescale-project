package client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;


public class BankClient extends OrganisationClient{
    public BankClient(String path) throws IOException {
        super(path);
        this.serverCommunicator.createAndStartTransactionsListener(this);
    }

    Map<String, BankAccount> clientAccounts;

    /*
   public void writeNewAmount(String iban, String idPerson, long amount) {
        JSONArray accounts = jsonObject.getJSONObject("organisation").getJSONArray("accounts");
        for (int i = 0; i < accounts.length(); i++) {
            JSONObject account = accounts.getJSONObject(i);
            JSONArray customers = account.getJSONArray("customers");
            for (int k = 0; k < customers.length(); k++) {
                JSONObject customer = customers.getJSONObject(k);
                String idCustomer = customer.getString("id");
                String roleCustomer = customer.getString("role");
                if (idCustomer.equals(idPerson) && roleCustomer.equals("CUSTOMER")) {
                    String ibanCustomer = account.getString("iban");
                    if (ibanCustomer.equals(iban)) {
                        account.put("amount", "" + amount);

                    }
                }
            }
        }
        try {
            FileHandler.write(this.path, this.jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    */

    /*
    public Long retrieveAmount(String iban, String id) {
        try {
            this.jsonObject = new JSONObject(FileHandler.read(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray accounts = this.jsonObject.getJSONObject("organisation").getJSONArray("accounts");
        for (int i = 0; i < accounts.length(); i++) {
            JSONObject account = accounts.getJSONObject(i);
            JSONArray customers = account.getJSONArray("customers");
            for (int k = 0; k < customers.length(); k++) {
                JSONObject customer = customers.getJSONObject(k);
                String idCustomer = customer.getString("id");
                String roleCustomer = customer.getString("role");
                if (idCustomer.equals(id) && roleCustomer.equals("CUSTOMER")) {
                    String ibanCustomer = account.getString("iban");
                    if (ibanCustomer.equals(iban)) {
                        String previousAmount = account.getString("amount");
                        return Long.parseLong(previousAmount);
                    }

                }
            }
        }
        return null;
    } */

    public addAmount(long amount, String iban, String Id){

    }

    public removeAmount(long amount, String iban, String id){

    }

    public DeleteAccount(String iban, String id){

    }

    public TransferAmount(String iban, String id, String recieverIban){

    }

    boolean checkPermission(BankAccountActions action,String iban, String Id) throws Exception {
       switch (action) {
           case ADD: return hasAddPermissions(iban, id);
           case DELETE: return hasDeleteAccountPermissions(iban, id);
           case TRANSFERAL: return hasTransferalPermissions(iban, id);
           case REMOVE: return hasRemoveMoneyDecision(iban, id);
           default: throw new Exception("This action does not exist");
       }
    }

    private boolean hasTransferalPermissions(String iban, String id) {
    }

    private boolean hasRemoveMoneyDecision(String iban, String id) {
    }

    private boolean hasDeleteAccountPermissions(String iban, String id) {
    }

    private boolean hasAddPermissions(String iban, String id) {

    }

}
