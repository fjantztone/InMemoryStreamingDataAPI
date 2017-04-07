import exceptions.RequiresValidDateException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-03-31.
 */

public class Main {
    public static Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {

        try {
            App app = new App(new CacheMiddleWare());
            logger.log(Level.INFO, "Application is running!");
        } catch (RequiresValidDateException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
