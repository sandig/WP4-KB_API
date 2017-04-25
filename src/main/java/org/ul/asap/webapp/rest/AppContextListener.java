package org.ul.asap.webapp.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppContextListener implements ServletContextListener {

    private Logger logger;
    public static Properties prop = new Properties();
//    private CassandraService cassandraService;

    /**
     * CALLED ON SERVICE INIT
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
//        ResourceBundle rb = ResourceBundle.getBundle("language/bundle");
//        Enumeration<String> keys = rb.getKeys();
//        while (keys.hasMoreElements()) {
//            String key = keys.nextElement();
//            String value = rb.getString(key);
//            System.out.println(key + ": " + value);
//        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            // init logger
            logger = Logger.getLogger(this.getClass().getName());
            // init db.property list
            //CommonUtils.initProperties(prop,"asap.properties");
            logger.log(Level.INFO, "ASAP properties successfully initialized.");

            //new CassandraDB(logger, "jcatascopiadb", prop.getProperty("monitoring.server"), 9042);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*  FOR INTERNAL USE ONLY
    private void initCassandraConnectionToService() {
        List<String> clusters = new ArrayList<String>();
        clusters.add(prop.getProperty("apache.cassandra.ip"));
        cassandraService = new CassandraService(new CassandraParamsObj(Integer.parseInt(prop.getProperty("apache" + "" +
                ".cassandra.port")), prop.getProperty("apache.cassandra.keyspace"), clusters));
        logger.log(Level.INFO, "Cassandra connected");


        CassandraService.initCassandraPropertyTables();
        logger.log(Level.INFO, "Metric table data loaded.");
    }
    */

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
