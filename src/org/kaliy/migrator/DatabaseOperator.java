package org.kaliy.migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseOperator {

    private static Logger logger = LoggerFactory.getLogger(DatabaseOperator.class);

    private String host;

    public DatabaseOperator(String host) {
        this.host = host;
    }

    public void executeQueryList(List<String> queries) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(getConnectionUrl(host),
                    Config.getConnectionUsername(),
                    Config.getConnectionPassword());
            Statement statement = connection.createStatement();
            connection.setAutoCommit(false);
            for (String query: queries) {
                logger.info("Adding Query {} to execute", query);
                statement.addBatch(query);
            }
            logger.info("Executing queries");
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error while executing queries: ", e);
            while(e != null) {
                logger.error("Exception: ", e);
                Throwable t = e.getCause();
                while(t != null) {
                    logger.error("Cause: ", t);
                    t = t.getCause();
                }
                e = e.getNextException();
            }

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error while closing connection: ", e);
                }
            }
        }

    }

    private String getConnectionUrl(String host) {
        return "jdbc:postgresql://" + host  + "/" + Config.getDatabaseName();
    }


}
