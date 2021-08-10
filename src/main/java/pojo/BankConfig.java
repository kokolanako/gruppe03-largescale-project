package pojo;

import lombok.Data;

import java.util.List;

@Data
public class BankConfig {
    private General general;
    private Server server;
    private Organisation organisation;
}
