import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;


public class Log {
    public Logger logger;
    FileHandler handler;

    public Log(String classname) throws IOException {
        File file = new File("./kalah.log");
        if (!file.exists() && !file.createNewFile())
        {
            throw new IOException("Unable to create file: " + file.getPath());
        }
        FileOutputStream out = new FileOutputStream(file);
        out.write("File is created.".getBytes());

        System.out.println("Log file is " + file.getName());
        System.out.flush();

        handler = new FileHandler(file.getName(), true);
        logger = Logger.getLogger(classname);
        logger.addHandler(handler);

        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
    }
}
