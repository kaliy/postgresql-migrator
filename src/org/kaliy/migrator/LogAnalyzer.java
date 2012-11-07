package org.kaliy.migrator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogAnalyzer {
    private static Logger logger = LoggerFactory.getLogger(LogAnalyzer.class);

    private List<String> queries = new ArrayList<String>();
    private List<Long> blobIDs = new ArrayList<Long>();
    private String filename;

    private FileInputStream fileInputStream;

    public LogAnalyzer(String filename) throws FileNotFoundException {
        this.filename = filename;
        analyze();
    }

    /**
     * Filling queries and BLOBs IDs Lists
     */
    private void analyze() {
        try {
            fileInputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            logger.error("File {} is not found: ", filename, e);
        }
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.containsIgnoreCase(line, "INSERT") ||
                        StringUtils.containsIgnoreCase(line, "DELETE") ||
                        StringUtils.containsIgnoreCase(line, "UPDATE")) {
                    String query;
                    if (!line.contains("$1"))
                        query = getUnparsedQueryFromLine(line);
                    else {
                        String parametersLine = bufferedReader.readLine();
                        if (StringUtils.containsIgnoreCase(line, "insert into common.file")) {
                            Long blobId = Long.parseLong(getParametersFromLine(parametersLine).get(1).replaceAll("[^0-9]", ""));
                            logger.debug("Adding BLOB ID to List: {}", blobId);
                            blobIDs.add(blobId);
                        }
                        query = getQueryFromTwoLines(line, parametersLine);
                    }
                    logger.debug("Adding query to List: {}", query);
                    queries.add(query);
                }
            }
        } catch (Exception e) {
            logger.error("Error occupied while reading {}:", filename, e);
        }

    }

    /**
     * Removing unnecessary info from line: <br />
     * <pre>2012-11-02 08:24:10 GMT ОТМЕТКА:  выполнение <unnamed>: UPDATE bpm.process SET checkTime = $1 WHERE (ID = $2)</pre><br />
     * converting to:
     * <pre>UPDATE bpm.process SET checkTime = $1 WHERE (ID = $2)</pre>
     * @param line - raw line from log
     * @return beautiful String
     */
    private String getUnparsedQueryFromLine(String line) {
        return StringUtils.substring(line,
                StringUtils.indexOfAny(line.toUpperCase(), "UPDATE", "INSERT", "DELETE"),
                line.length());
    }

    /**
     * Converting parameters String to parameters List: <br />
     * <pre>2012-11-02 08:25:12 GMT ПОДРОБНОСТИ:  параметры: $1 = NULL, $2 = '2012-11-02 08:24:02.334+00', $3 = '/Login', $4 = '0:0:0:0:0:0:0:1', $5 = '2012-11-02 08:24:02.369+00', $6 = '200'</pre><br />
     * converting to List:
     * [NULL, '2012-11-02 08:24:02.334+00', '/Login', '0:0:0:0:0:0:0:1', '2012-11-02 08:24:02.369+00', '200']
     * @param line - raw line with parameters
     * @return parameters List
     */
    private List<String> getParametersFromLine(String line) {
        List<String> parameters = new ArrayList<String>();
        List<String> unparsedParameters = new ArrayList<String>(Arrays.asList(line.split("\\$")));
        unparsedParameters.remove(0);
        for (String parameterString: unparsedParameters) {
            String parameterValue = StringUtils.substring(parameterString,
                    StringUtils.indexOf(parameterString, "=") + 2,
                    parameterString.length())
                    .trim();
            if (parameterValue.charAt(parameterValue.length() - 1) == ',')
                parameterValue = parameterValue.substring(0, parameterValue.length() - 1);
            parameters.add(parameterValue);
        }
        return  parameters;
    }

    /**
     * Creating query from two lines. Combining
     * <pre>UPDATE bpm.process SET checkTime = $1 WHERE (ID = $2)</pre> and
     * <pre>$1 = NULL, $2 = '20266'</pre> to SQL query: <br />
     * <pre>UPDATE bpm.process SET checkTime = NULL WHERE (ID = '20266')</pre>
     * @param line - raw line from log with the query
     * @param parameters - raw line from log with parameters
     * @return SQL query
     */

    private String getQueryFromTwoLines(String line, String parameters) {
        logger.trace("Log line: {}", line);
        logger.trace("Log paramaters: {}", parameters);
        int index = 1;
        String query = getUnparsedQueryFromLine(line);
        for (String parameter: getParametersFromLine(parameters)) {
            query = query.replaceFirst("\\$" + index++, parameter);
        }
        return query;
    }


    public List<String> getQueries() {
        return queries;
    }

    public List<Long> getBlobIDs() {
        return blobIDs;
    }
}
