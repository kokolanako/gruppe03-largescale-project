package client;

import java.util.ArrayList;
import java.util.Map;

//todo: unmarshalling and marshalling
public class BankAccount {
    long amount;
    String iban;
    Map<String, ArrayList<BankAccountRoles>> customers;  //Map aus id und Rollen
}
