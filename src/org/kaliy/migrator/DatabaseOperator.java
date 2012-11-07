package org.kaliy.migrator;

import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseOperator {

    private static Logger logger = LoggerFactory.getLogger(DatabaseOperator.class);

    private String host;

    private static final int BUFFER_SIZE = 4096;

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

    public Map<Long, byte[]> getBlobs(List<Long> ids) {
        Connection connection = null;
        Map<Long, byte[]> largeObjectMap = new HashMap<Long, byte[]>();
        byte[] buf = new byte[4096];
        try {
            connection = DriverManager.getConnection(getConnectionUrl(host),
                    Config.getConnectionUsername(),
                    Config.getConnectionPassword());
            connection.setAutoCommit(false);
            LargeObjectManager largeObjectManager = ((PGConnection)connection).getLargeObjectAPI();
            for(Long id: ids) {
                logger.debug("Opening large object {}", id);

                int readed;
                int offset = 0;
                LargeObject largeObject = largeObjectManager.open(id, LargeObjectManager.READ);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                while ((readed = largeObject.read(buf, offset, BUFFER_SIZE)) > 0) {
                    byteArrayOutputStream.write(buf, 0, readed);
                }
                largeObjectMap.put(id, byteArrayOutputStream.toByteArray());
                largeObject.close();

                logger.info("Added BLOB {} with byte[] size {}", id, byteArrayOutputStream.size());
            }
        } catch (SQLException e) {
            logger.error("Error while loading BLOBs: ", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error while closing connection: ", e);
                }
            }
        }
        return largeObjectMap;
    }

    private String getConnectionUrl(String host) {
        return "jdbc:postgresql://" + host  + "/" + Config.getDatabaseName();
    }


}
