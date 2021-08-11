package client.bank;

import client.Client;
import communication.ServerCommunicator;
import io.ConfigParser;
import pojo.Account;
import pojo.BankConfig;
import pojo.Customer;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class BankClient extends Client {
    private static final BankConfig config = ConfigParser.parse("src/main/resources/configs/bank.json");

    public BankClient() throws IOException, ClassNotFoundException {

        Socket socket = SocketFactory.getDefault().createSocket(config.getServer().getIp(), config.getServer().getPort());

        this.serverCommunicator = new ServerCommunicator(new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()),
                config.getGeneral().getRetries(), config.getGeneral().getTimeout(), this, config.getOrganisation().getKeys().getPrivateKey());
        this.id = getID();
        this.ownFirstName = config.getOrganisation().getName();
        this.ownLastName = " Bank";
        this.register();
    }

    @Override
    public String getPublicKey(){
        return config.getOrganisation().getKeys().getPublicKey();
    }

    @Override
    public String getPrivateKey() {
        return config.getOrganisation().getKeys().getPrivateKey();
    }

    @Override
    public String getID() {
        return config.getOrganisation().getId();
    }

    @Override
    public String[] getName() {
        return new String[]{config.getOrganisation().getName()};
    }

    @Override
    public String getTypeInstance() {
        return "ORGANIZATION";
    }

    public void startListeningToTransactions() throws IOException {
        // start transactions
        System.out.println("connected");
    }

    public Long retrieveAmount(String ibanFrom, String id) {
        for (Account account : config.getOrganisation().getAccounts()){
            if (account.getIban().equals(ibanFrom)) {
                for (Customer customer: account.getCustomers()) {
                    if (id.equals(customer.getId()) && customer.getRoles().stream().anyMatch(s -> s.equals("CUSTOMER"))) {
                        return account.getAmount();
                    }
                }
            }
        }
        throw new RuntimeException("No account found with iban: " + ibanFrom + " and id: " + id);
    }

    public void writeNewAmount(String ibanFrom, String id, long newAmount) {
        for (Account account : config.getOrganisation().getAccounts()){
            if (account.getIban().equals(ibanFrom)) {
                for (Customer customer: account.getCustomers()) {
                    if (id.equals(customer.getId()) && customer.getRoles().stream().anyMatch(s -> s.equals("CUSTOMER"))) {
                        account.setAmount(newAmount);
                    }
                }
            }
        }
        throw new RuntimeException("No account found with iban: " + ibanFrom + " and id: " + id);
    }

    /* todo
    public addAmount(long amount, String iban, String Id){
        return; //todo
    }

    public removeAmount(long amount, String iban, String id){
        return; //todo
    }

    public DeleteAccount(String iban, String id){
        return; //todo
    }

    public TransferAmount(String iban, String id, String recieverIban){
        return; //todo
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
*/
}
