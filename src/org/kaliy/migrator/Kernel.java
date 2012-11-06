package org.kaliy.migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class Kernel {
    private static Logger logger = LoggerFactory.getLogger(Kernel.class);
    public static void main(String[] args) {
        logger.info("PostgreSQL-migrator is starting...");
        LogAnalyzer logAnalyzer;
        try {
            logAnalyzer = new LogAnalyzer("postgresql.log");
        } catch (FileNotFoundException e) {
            logger.error("Could not find log file.");
        }
    }
}
