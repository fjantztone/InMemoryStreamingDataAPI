import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.RequiresValidDateException;
import services.AppService;

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
        } catch (RequiresValidDateException | CacheAlreadyExistsException | CacheNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }
}
