import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.InvalidKeyException;
import exceptions.RequiresValidDateException;
import services.AppService;

import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-03-31.
 */

public class Main {
    public static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            AppService app = new AppService();
            logger.log(Level.INFO, "Application is running!");
        } catch (IOException | RequiresValidDateException | CacheAlreadyExistsException | CacheNotFoundException | InvalidKeyException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }


    }
}
