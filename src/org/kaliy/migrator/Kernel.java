package org.kaliy.migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class Kernel {
    private static Logger logger = LoggerFactory.getLogger(Kernel.class);
    public static void main(String[] args) {
        logger.info("PostgreSQL-migrator is starting...");

        LogAnalyzer logAnalyzer = null;
        try {
            logAnalyzer = new LogAnalyzer("postgresql.log");
        } catch (FileNotFoundException e) {
            System.exit(1);
            logger.error("Could not find log file.");
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Could not found PostgreSQL JDBC Driver. Exiting...");
            System.exit(1);
        }

        modifyNewDatabase(logAnalyzer);

    }

    private static void modifyNewDatabase(LogAnalyzer logAnalyzer) {
        DatabaseOperator databaseOperator = new DatabaseOperator(Config.getCurrentPostgreSQLHost());
        databaseOperator.executeQueryList(logAnalyzer.getQueries());
    }

}
