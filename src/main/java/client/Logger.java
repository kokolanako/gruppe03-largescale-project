package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    FileWriter fileWriter;

    public Logger(String filename) throws IOException {
        File file = new File(filename+".txt");
        this.fileWriter = new FileWriter(file, true);

    }

    void logString(String message) throws IOException {
        fileWriter.write(message);
    }

    protected void finalalize() throws IOException {
        this.fileWriter.close();
    }
}
