package pojo;

import client.BankAccountRoles;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Account {
    private long amount;
    private String iban;
    List<Customer> customers;
}
