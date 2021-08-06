package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Account {
    private long amount;
    private String iban;
    List<Customer> customers;
}
