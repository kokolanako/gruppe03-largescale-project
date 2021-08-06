package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Bank extends Organisation{
    List<Account> accounts;
    Bank organisation;
}
