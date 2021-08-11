package pojo;

import lombok.Data;

@Data
public class BankConfig {
    private General general;
    private Server server;
    private Organisation organisation;
}
