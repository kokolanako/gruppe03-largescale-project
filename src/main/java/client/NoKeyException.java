package client;

public class NoKeyException extends Exception{
    public NoKeyException(String errorMessage){
        super(errorMessage);
    }
}
