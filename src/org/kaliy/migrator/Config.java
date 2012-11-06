package org.kaliy.migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    private static Properties props = new Properties();
    private static Logger logger = LoggerFactory.getLogger(Config.class);

    static {
        try {
            FileInputStream fis = new FileInputStream("migrator.properties");
            props.load(fis);
            fis.close();
        } catch (Exception e) {
            logger.error("Can't load config file (migrator.properties). Migrator will exit now", e);
        }
    }

    public static String getPreviousPostgreSQLHost() {
//        previous.postgres.host=127.0.0.1:10090
        return strProp("previous.postgres.host");
    }

    public static String getCurrentPostgreSQLHost() {
//        current.postgres.host=127.0.0.1:10092
        return strProp("current.postgres.host");
    }

    public static String getConnectionUsername() {
//        connection.username=postgres
        return strProp("connection.username");
    }

    public static String getConnectionPassword() {
//        connection.password=postgres
        return strProp("connection.password");
    }

    public static String getDatabaseName() {
//        database.name=moloco
        return strProp("database.name");
    }


    private static String strProp(String name) {
        return props.getProperty(name);
    }

}
