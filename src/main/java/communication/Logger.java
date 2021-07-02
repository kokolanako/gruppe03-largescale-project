package communication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    FileWriter fileWriter;

    public Logger(String filename) throws IOException {
        File file = new File(filename+".txt");
        this.fileWriter = new FileWriter(file, true);

    }

    public void logString(String message) throws IOException {
        fileWriter.write(message+"\n");
    }

    protected void finalalize() throws IOException {
        this.fileWriter.close();
    }
}
