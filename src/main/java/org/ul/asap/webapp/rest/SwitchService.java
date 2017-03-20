package org.ul.asap.webapp.rest;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.UUIDs;
import com.hp.hpl.jena.reasoner.ValidityReport;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.jclouds.javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ul.asap.webapp.entry.*;
import org.ul.asap.webapp.makers.ASAPModelMakerOLD;
import org.ul.asap.webapp.mcu.DecisionMakerInput;
import org.ul.asap.webapp.mcu.KBdummy;
import org.ul.asap.webapp.mcu.Utilities;
import org.ul.asap.webapp.mcu.test.ClusterAddressResolver;
import org.ul.asap.webapp.mcu.test.ConfigProvider;
import org.ul.asap.webapp.mcu.test.ConfigSelector;
import org.ul.asap.webapp.util.ASAPFusekiUtils;
import org.ul.common.monitoring.MonitoringMetric;
import org.ul.common.rest.IService;
import org.ul.entice.webapp.client.CassandraDB;
import org.ul.entice.webapp.entry.client.ResponseObj;

import javax.servlet.ServletException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//import javax.annotation.Nullable;

//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.authc.*;
//import org.apache.shiro.subject.Subject;

//import com.hp.hpl.jena.query.ResultSet;

//@Path("/images")
//@RequiresPermissions("protected:read")

@Path("/asap/")
public class SwitchService implements IService {

    // Allows to insert contextual objects into the class (e.g. ServletContext, Request, Response, UriInfo)
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    private String KB_PREFIX = "http://www.semanticweb.org/project-switch/ontologies/2015/7/knowledgebase#";
    private Map<String, String> mcuStateMap = new HashMap<>();

    private List<ASAPEntry> clustersList = new ArrayList<>();

    @Override
    public String getFusekiQuery() {
        return AppContextListener.prop.getProperty("fuseki.url.query");
    }

    /*
    * Method that returns all the available application components that can be drag into the canvas. Data will be
    * obtained from the KB.
    */
    @GET
    @Path("get_all_application_component_types")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> getAllApplicationComponentTypes() {
        List<Object> list = new ArrayList<>();
        return list;
    }

    // return: List of object names, descriptions and IDs
    /*
    * Method that returns the list of application component that match the name and/or type string pass as parameters.
    */
    @GET
    @Path("get_application_component_type")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> getApplicationComponentType(@QueryParam("name") String name, @Nullable String type) {
        return null;
    }

    /*
    *
    * EXAMPLE REQUEST:
    *   http://localhost:8080/SWITCH/rest/asap/get_monitoring_data
    */
    /*
    catascopia_user@cqlsh:jcatascopiadb> select * from  metric_value_table limit 10;
     metricid                                     | event_date | event_timestamp                      | mgroup | name
             | type    | units | value
    ----------------------------------------------+------------+--------------------------------------+--------+-------------+---------+-------+---------
     6bc762f1b1e94a6da5f8f5941f7cc30f:memSwapFree | 2016-11-23 | 0b150c2f-b169-11e6-7f7f-7f7f7f7f7f7f | Memory |
     memSwapFree | INTEGER |    KB | 2092372

     catascopia_user@cqlsh:jcatascopiadb> select * from agent_table limit 10;
     agentid                          | agentip        | agentname      | status     | tags | tstart
                   | tstop
    ----------------------------------+----------------+----------------+------------+------+--------------------------------------+--------------------------------------
     137a652ba93c4d75a9dfc52847c809a9 |  194.249.0.192 |  194.249.0.192 | TERMINATED | null |
     8e98fcf0-b15b-11e6-ae54-cb553cabb2f4 | 43c3c5b0-b15c-11e6-ae54-cb553cabb2f4
     */
    // >> metric_value_table
    // http://localhost:8080/SWITCH/rest/asap/get_monitoring_metric_values?from=2016-11-15%2022:15:52&to=2016-11-25
    // %2022:15:52

    @GET
    @Path("get_monitoring_metric_values")
    @Produces(MediaType.APPLICATION_JSON)
    public MyResponse getMonitoringData(@QueryParam("metric_id") String metricID, @QueryParam("from") String from,
                                        @QueryParam("to") String to) {
        List<ASAPEntry> list = new ArrayList<>();
        try {
            if (from == null || to == null)
                return new MyResponse(204, "Date intervals 'from' and 'to' must be specified! Example date format: "
                        + "yyyy-MM-dd HH:mm:ss", null, null);
            ValidityReport uv;
            // select * from metric_value_table where event_timestamp in(5ca338bf-b299-11e6-7f7f-7f7f7f7f7f7f,
            // 092fb23f-b299-11e6-7f7f-7f7f7f7f7f7f) and metricid='54dcc3c05f6941bba01bc680b55076b0:netBytesOUT'
            // allow filtering;
            //  select * from metric_value_table where event_timestamp in(5ca338bf-b299-11e6-7f7f-7f7f7f7f7f7f,
            // 092fb23f-b299-11e6-7f7f-7f7f7f7f7f7f) and metricid='54dcc3c05f6941bba01bc680b55076b0:netBytesOUT'
            // allow filtering;
//            to = "4343263" + to.substring(4,to.length());
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime dateFrom = formatter.parseDateTime(from);
            DateTime dateTo = formatter.parseDateTime(to);
            //            2010-05-30 22:15:52
            final String metricQueryPart = (metricID != null ? (" metricid = '" + metricID + "' and") : "");
            // UUIDs.startOf(new DateTime(from))
//            final String intervalQueryPart = " event_timestamp in("+UUIDs.startOf(dateFrom.getMillis())+","+UUIDs
// .startOf(dateTo.getMillis())+")";
//            final String intervalQueryPart = " event_timestamp in("+UUIDs.startOf(new DateTime(2016, 11, 1, 1, 59,
// 5).getMillis())+","+UUIDs.timeBased()+")";

            final String intervalQueryPart = " event_timestamp > " + UUIDs.startOf(dateFrom.getMillis()) + " and " +
                    "event_timestamp < " + UUIDs.startOf(dateTo.getMillis()) + "";
            ResultSet resultSet = CassandraDB.getSession().execute("select * from metric_value_table where " +
                    metricQueryPart + " " + intervalQueryPart + " limit 1000 allow filtering;");

            // new DateTime(UUIDs.unixTimestamp(UUIDs.timeBased())).toString()

            //////////
            //// new DateTime(UUID.fromString("5ca338bf-b299-11e6-7f7f-7f7f7f7f7f7f").timestamp()).toString()
            // RES: 4343111-12-25T01:27:19.999+01:00

            // new DateTime(UUID.fromString(UUIDs.startOf(new DateTime(2016, 11, 1, 1, 59, 5).getMillis()).toString()
            // ).timestamp()).toString()
            // RES:  4342457-03-28T09:13:20.000+02:00

            for (Row row : resultSet) {
                list.add(new MonitoringMetricValue(row.getString("metricid"), row.getString("event_date"), row
                        .getUUID("event_timestamp"), row.getString("mgroup"), row.getString("name"), row.getString
                        ("type"), row.getString("units"), row.getString("value")));
            }
            return new MyResponse(200, "OK", list, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new MyResponse(204, e.getMessage(), null, null);
        }
    }

    @GET
    @Path("get_agent_info")
    // CREATE INDEX status_at on agent_table(status)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Agent> getAgentInfo(@QueryParam("is_up") Boolean isUp) {
        List<Agent> list = new ArrayList<>();
        try {
            ResultSet resultSet = CassandraDB.getSession().execute("select * from agent_table " +
                    (isUp != null ? (isUp ? " where status = 'UP'" : " where status = 'TERMINATED'") : "") + ";");

            for (Row row : resultSet) {
                list.add(new Agent(row.getString("agentid"), row.getString("agentip"), row.getString("agentname"),
                        row.getString("status"), row.getString("tags"), row.getUUID("tstart"), row.getUUID("tstop")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return list;
        }
    }

    @GET
    @Path("get_kb_information")
    @Produces(MediaType.APPLICATION_JSON)
    // http://localhost:8080/SWITCH/rest/asap/get_kb_information?query=SELECT%20?s%20?p%20?o%20WHERE%20{%20?s%20?p%20?o%20}%20LIMIT%2025

    // http://193.2.72.90:7070/SWITCH/rest/asap/get_kb_information?query=SELECT%20?s%20?p%20?o%20WHERE%20{%20?s%20?p%20?o%20}%20LIMIT%2025
    public Map<String, String> getKnowledgeBaseInformation(@QueryParam("query") String query) {
        Map<String, String> resultMap = new HashMap<>();
        if (query == null) {
            resultMap.put("success", String.valueOf(false));
            resultMap.put("message", "argument 'query' is not defined!");
            return resultMap;
        }
        try {
            // validate query string using regular expressions

            // execute the query

            // return generic result
            QueryExecution qe = QueryExecutionFactory.sparqlService(
                    AppContextListener.prop.getProperty("fuseki.url.query"),
                    query);
            org.apache.jena.query.ResultSet rs = qe.execSelect();

            resultMap.put("success", String.valueOf(true));
            resultMap.put("message", ASAPFusekiUtils.getResultObjectListFromResultSet(rs).toString());
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("success", String.valueOf(false));
            resultMap.put("message", e.getMessage());
        } finally {
            return resultMap;
        }
    }


    /*
    catascopia_user@cqlsh:jcatascopiadb> select * from metric_table limit 10;
     agentid                          | metricid                                         | is_sub | mgroup     | name
                 | type    | units
    ----------------------------------+--------------------------------------------------+--------+------------+-----------------+---------+-------
     93730c72f6b34a6d8026edc88a069024 |  93730c72f6b34a6d8026edc88a069024:activeSessions |   null |    HAProxy |
     activeSessions | INTEGER |     #
     */
    @GET
    @Path("get_available_metric_definitions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MonitoringMetricDef> getAvailableMetricDefinition() {
        List<MonitoringMetricDef> list = new ArrayList<>();
        try {
            ResultSet resultSet = CassandraDB.getSession().execute("select * from metric_table;");

            for (Row row : resultSet) {
                list.add(new MonitoringMetricDef(row.getString("agentid"), row.getString("metricid"), row.getString
                        ("is_sub"), row.getString("mgroup"), row.getString("name"), row.getString("type"), row
                        .getString("units")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return list;
        }
    }


    // see component profile attributes:
    // https://docs.google.com/document/d/16OYHCANQQNdRAfoYGFcUUx75lVmXv6Xf2Bil91daKGU/edit
    /*
    * Method that returns the detail information (profile) of a given component.
    * EXAMPLE REQUEST:
    *   http://localhost:8080/SWITCH/rest/asap/get_application_component_profile
    */
    @GET
    @Path("get_application_component_profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getApplicationComponentProfile(@QueryParam("application_component_id") String
                                                         applicationComponentID) {
        return ASAPFusekiUtils.getAllEntityAttributes(ApplicationComponent.class,
                AppContextListener.prop.getProperty("fuseki.monitoring.url.query"));
    }

    // return:
    //      boolean: success, ID: <if successed>
    /*
    * Method that creates a new type of application component (eg: mcu server).
    */
    @POST
    @Path("create_component_type")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
//    @Nullable @QueryParam("id") String id, @QueryParam
//    ("install_script") String installScript, @QueryParam("stateful") boolean stateful, @QueryParam
//    ("execution_script") String executionScript, @QueryParam("input_interface") Object inputInterface,
//    @QueryParam("development_event") Object developmentEvent,
//    @QueryParam("output_interface") Object outputInterface,
//    @QueryParam("quality_attribute") Object qualityAttribute,
//    @QueryParam("description") String description
    public Map<String, String> createComponentType(ApplicationComponent applicationComponent) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            applicationComponent.setId(UUID.randomUUID().toString());
            String insertStatement = ASAPFusekiUtils.generateInsertObjectStatement(applicationComponent);
            UpdateProcessor upp = UpdateExecutionFactory.createRemote(UpdateFactory.create(insertStatement),
                    AppContextListener.prop.getProperty("fuseki.url.update"));
            upp.execute();
            resultMap.put("id", applicationComponent.getId());
            resultMap.put("success", String.valueOf(true));
        } catch (NullPointerException | IllegalStateException e) {
            resultMap.put("success", String.valueOf(false));
        }

        return resultMap;
    }

    // return: boolean: success, ID: <if successed>
    /*
    * SIDE GUI passes the provisioner result to the KB API.
    */
    @POST
    @Path("store_provisioner_result")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> storeProvisionerResult(Object provisioner) {
        return null;
    }

    /*
    * Return the list of VMs.
    */
    @GET
    @Path("get_virtual_infrastructure")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> getVirtualInfrastructure(@QueryParam("development_event") String runtimeAppId) {
        return null;
    }

    private static final ClusterCredentials arnesCredentials = new ClusterCredentials("111abc11111111111", CredentialsType.CERTIFICATES,
            AppContextListener.prop.getProperty("arnes.master.url"),
            AppContextListener.prop.getProperty("arnes.client.public.credentials"),
            AppContextListener.prop.getProperty("arnes.client.private.credentials"),
            AppContextListener.prop.getProperty("arnes.ca.cert"));


    private static final ClusterCredentials gcpTaiwanCredentials = new ClusterCredentials("222abc22222222222", CredentialsType.PASSWORD,
            AppContextListener.prop.getProperty("gcp.taiwan.master.url"),
            AppContextListener.prop.getProperty("gcp.taiwan.public.credentials"),
            AppContextListener.prop.getProperty("gcp.taiwan.private.credentials"),
            AppContextListener.prop.getProperty("gcp.taiwan.ca.cert"));


    //when a POST HTTP request requiring MediaType.APPLICATION_JSON will arrive at URL
    // http://localhost:8080/KubernetesFabric8RESTapi/AdaptationAPI/switch/getCredentials - the function
    // getCredentials() will be called
    //the request is carrying the JSON data (idOfCluster where to instantiate the pod)
    //the response should contain JSON data in the format like this: {"masterUrl":"https://194.249.0.45",
    // "clientPublicCred":"the client public credential","clientPrivateCred":"the client private credential",
    // "credentialsType":"PASSWORD","certificateAuthority":"the caCert"}

    //EXAMPLES:
    //    http://localhost:8080/SWITCH/rest/asap/get_credentials?cluster_id=8f0d0535-0c7e-4a03-b529-2b8730a182c4

    @GET
    @Path("get_credentials")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //the method takes parameter from the request and returns the required certificates
    public MyResponse getCredentials(@QueryParam("cluster_id") String clusterID) throws IOException {
        try {
//        if (clusterID.equals("111abc11111111111")) return arnesCredentials;
//        else if (clusterID.equals("222abc22222222222")) return gcpTaiwanCredentials;
            if (clusterID != null)
                return new MyResponse(200, "SUCCESS", null, ASAPFusekiUtils.getAllEntityAttributes(ClusterCredentials.class,
                        AppContextListener.prop.getProperty("fuseki.url.query"), clusterID).get(0));

            List<ASAPEntry> list = new ArrayList<>();
            list.addAll(ASAPFusekiUtils.getAllEntityAttributes(ClusterCredentials.class,
                    AppContextListener.prop.getProperty("fuseki.url.query")));
            return new MyResponse(200, "SUCCESS", list, null);
        } catch (Exception e) {
            return new MyResponse(400, "ERROR: " + e.getMessage(), null, null);
        }
    }

    @GET
    @Path("get_monitoring_rule_example")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //the method takes parameter from the request and returns the required certificates
    public String getRuleExample() throws IOException {

        return "";
    }

    @POST
    @Path("store_credentials")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //save the credentials in the KB
    public ResponseObj storeCredentials(ClusterCredentials cc) throws IOException {
        try {
            String insertStatement = ASAPFusekiUtils.generateInsertObjectStatement(cc);
            UpdateProcessor upp = UpdateExecutionFactory.createRemote(UpdateFactory.create(insertStatement),
                    AppContextListener.prop.getProperty("fuseki.url.update"));
            upp.execute();
            return new ResponseObj(200, "true");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }
    }

    @POST
    @Path("store_monitoring_metric")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //save the credentials in the KB
    public ResponseObj storeMonitoringMentric(MonitoringMetric monitoringMetric) throws IOException {
        try {
            String insertStatement = ASAPFusekiUtils.generateInsertObjectStatement(monitoringMetric);
            UpdateProcessor upp = UpdateExecutionFactory.createRemote(UpdateFactory.create(insertStatement),
                    AppContextListener.prop.getProperty("fuseki.monitoring.url.update"));
            upp.execute();
            return new ResponseObj(200, "true");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }
    }

    @GET
    @Path("get_monitoring_metrics")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //the method takes parameter from the request and returns the required certificates
    public List<MonitoringMetric> getMonitoringMetrics(@QueryParam("monitoring_metric_id") String mmID) throws IOException {
        return ASAPFusekiUtils.getAllEntityAttributes(MonitoringMetric.class,
                AppContextListener.prop.getProperty("fuseki.monitoring.url.query"));
    }

    // ------------------------------------------------------------------------------------------- 2nd review requests

    @POST
    @Path("store_monitoring_server_data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // POST - store monitoring server data (IDs)
    public ResponseObj storeMonitoringServerData(@QueryParam("monitoring_data") String monitoringData) {
        try {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }
    }

    @GET
    @Path("update_decision_maker")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // GET - MCU notifies DM with needed data (IPs,...)
    public ResponseObj updateDecisionMaker(@QueryParam("ips") String ips) throws IOException {
        try {
            return new ResponseObj(200, "ok response");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }

    }

    @POST
    @Path("store_dm_into_kb")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // POST - store model for DM into the KB
    public ResponseObj storeDMintoKB(String someModelFromDM) {
        try {
            return new ResponseObj(200, "ok response");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }
    }


    private List<ASAPModelObj> asapModelObjs = new ArrayList<>();

    @GET
    @Path("get_model")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // GET - get model in form of a vector
    public List<ASAPModelObj> getModel() throws IOException {
        try {
            // current solution. TODO ask Jernej and Salman for real data
            asapModelObjs.add(new ASAPModelObj(33, 20, 1000, 1, 12, 4));
            asapModelObjs.add(new ASAPModelObj(32, 20, 600, 1, 50, 4));
            asapModelObjs.add(new ASAPModelObj(12, 5, 200, 1, 30, 5));
            asapModelObjs.add(new ASAPModelObj(27, 15, 100, 2, 20, 4));
            asapModelObjs.add(new ASAPModelObj(12, 5, 100, 1, 100, 5));
            asapModelObjs.add(new ASAPModelObj(40, 25, 100, 2, 25, 3));
            asapModelObjs.add(new ASAPModelObj(44, 20, 500, 1, 60, 3));

            //todo: save into the KB: generate modelID + array


            ASAPModelMakerOLD mm = new ASAPModelMakerOLD();
            mm.MetricValues = asapModelObjs;

            return asapModelObjs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // http://localhost:8080/SWITCH/rest/asap/get_differential
    @GET
    @Path("get_differential")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // GET - get model in form of a vector
    public List<ASAPModelObj> getDifferential() throws IOException {
        try {
            ASAPModelMakerOLD mm = new ASAPModelMakerOLD();
            mm.MetricValues = asapModelObjs;

            return mm.calculateDifferential();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @GET
    @Path("get_asap_state")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // GET - get current state (HTML form periodically triggers this)  ; define all type of results
    public MyResponse getASAPState(@QueryParam("job_id") String jobID) throws IOException {
        try {
            return new MyResponse(200, "ok response", null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new MyResponse(400, e.getMessage(), null, null);
        }
    }

    @GET
    @Path("update_mcu_state")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // GET/POST - MCU notifies KB that has finished with QoE metric
    public ResponseObj updateMCUstate(@QueryParam("job_id") String jobID, @QueryParam("state") String state) throws IOException {
        try {
            return new ResponseObj(200, "kb store result");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }
    }

    @POST
    @Path("notify_model_maker")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // POST - MM must be notified with the following information:
    //  RTT ;  Hop Count ; Ram ; # of Cores ;  Architecture ; Star rating
    public ResponseObj notifyModelMaker(ModelMakerObj modelMakerObj) {
        try {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObj(400, e.getMessage());
        }
    }

    @GET
    @Path("get_kubernetes_pods")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //the method takes parameter from the request and returns the required certificates
    public MyResponse getKubernetesPods(@QueryParam("cluster_id") String clusterID) {
        KubernetesClient kube = null;
        MyResponse myResponse = null;
        List<ASAPEntry> podList = new ArrayList<>();
        try {
            for (ASAPEntry entry : clustersList) {
                final String clusterName = ((SimpleEntry) entry).getName();
                kube = initKubernetesClientByClusterId(clusterName);

                PodList pods = kube.pods().list();
                List<Pod> items = pods.getItems();
                if (items != null)
                    for (Pod item : items) {
//                System.out.println("Pod " + KubernetesHelper.getName(item) + " with ip: " + item.getStatus().getPodIP() + " created: " + item.getMetadata().getCreationTimestamp());
//                    podList.add(new SimpleEntry(KubernetesHelper.getName(item), KubernetesHelper.getLabels(item).get("app")));
                        podList.add(new SimpleEntry(clusterName + "|||" + KubernetesHelper.getName(item), "Cluster " + clusterName + " | Pod " + KubernetesHelper.getName(item) + " with ip: " + item.getStatus().getPodIP() + " created: " + item.getMetadata().getCreationTimestamp()));
                    }

            }

            myResponse = new MyResponse(200, "message", podList, null);
        } catch (Exception e) {
            e.printStackTrace();
            myResponse = new MyResponse(400, e.getMessage(), null, null);
        } finally {
            if (kube != null)
                kube.close();
            return myResponse;
        }
    }

    @GET
    @Path("get_available_clusters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //the method takes parameter from the request and returns the required certificates
    public MyResponse getAvailableClusters() {
        clustersList = new ArrayList<>();
        MyResponse myResponse = null;
        try {
            ConfigProvider configProvider = new ConfigProvider() {
            };

            Config config = configProvider.retrieveResource();
            ConfigSelector configSelector = new ConfigSelector(config);

            for (NamedContext context : configSelector.getContexts()) {
                clustersList.add(new SimpleEntry(context.getName(), context.getName()));
            }

            myResponse = new MyResponse(200, "message", clustersList, null);
        } catch (Exception e) {
            e.printStackTrace();
            myResponse = new MyResponse(400, e.getMessage(), null, null);
        } finally {
            return myResponse;
        }
    }


    private Map<String, JitsiObj> jitsiJobMap = new HashedMap();
    private int count = 0;

    @GET
    @Path("run_mcu_execution")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, String> runMCUexecution(@QueryParam("participants") String clientLocations) {
        //init start parameter states
        mcuStateMap.clear();
        count = 0;


        Map<String, String> resultMap = new HashMap<>();
        KubernetesClient client = null;

        try {
            List<String> clientIps = Utilities.parseAndValidateClientIPs(clientLocations);

            mcuStateMap.put("step" + (++count), "> IPs validated successfully.");
            if (clientIps == null) {
                final String validationErrorMessage = "One or more of your IPs were in wrong format, or you provided " +
                        "an empty list! Do not expect adaptation module to work with that! Please list videoconference " +
                        "participants IPs delimited with semicolon once again.";
                mcuStateMap.put("step" + (++count), validationErrorMessage);
                resultMap.put("error", validationErrorMessage);
                return resultMap;
            }

            final String currentJobID = UUIDs.timeBased().toString();
            jitsiJobMap.put(currentJobID, new JitsiObj(DateTime.now()));

            final String uniqueIdentifierOfPod = UUIDs.timeBased().toString();//UtilitiesBKP.generateKubernetesFriendlyShortUuid(24);
            mcuStateMap.put("step" + (++count), "> Credentials obtained from Knowledge Base.");

            String selectedClusterID = Utilities.getTheClusterIdFromDecisionMaker(new DecisionMakerInput("mcu", null));
            mcuStateMap.put("step" + (++count), "> Proposed cluster calculated from Decision Maker: " + selectedClusterID);

            client = initKubernetesClientByClusterId(selectedClusterID);
            mcuStateMap.put("step" + (++count), "> Credentials successfully applied to Kubernetes cloud system.");
            String mcuPodPath = new File(Paths.get(System.getenv("KUBEMANIFESTS"), "mcu001.yaml").toString()).toString();
			
            try (InputStream inputStream = (new FileInputStream(mcuPodPath))) {
                List<HasMetadata> metaList = client.load(inputStream).get();
                if (metaList != null && metaList.size() > 0 && metaList.get(0) instanceof Pod) {
                    Pod pod = (Pod) metaList.get(0);
                    System.out.println(pod);

                    Map<String, String> labels = new HashMap<String, String>();
                    labels.put("id", uniqueIdentifierOfPod);
                    labels.put("app", "testmcu");

                    // To add additional fields to pod, start with builder:
                    Pod pod2 = new PodBuilder(pod).withNewMetadata()
                            .withName(uniqueIdentifierOfPod).withLabels(labels).endMetadata().build();
                    client.pods().create(pod2);
                }
            }
            mcuStateMap.put("step" + (++count), "> Kubernetes Pod configuration provided and applied.");

            final CountDownLatch latch = new CountDownLatch(1);
            final KubernetesClient tempClient = client;
            client.pods().watch(new Watcher<Pod>() {
                @Override
                public void eventReceived(Action action, Pod resource) {
                    try {
                        String podName = resource.getMetadata().getName();
                        String podStatus = resource.getStatus().getPhase();
                        if (podName != null
                                && podName.equals(uniqueIdentifierOfPod)
                                && !action.toString().equals("DELETED")
                                && podStatus != null
                                && podStatus.equals("Running")) {

                            jitsiJobMap.get(currentJobID).setClusterId(selectedClusterID);
                            String probablyInternalIp = resource.getStatus().getHostIP();
                            final String externalIP = mapInternalIpToExternal(tempClient, probablyInternalIp);
                            final String publicUrl = "https://" + externalIP + ":444";

                            jitsiJobMap.get(currentJobID).setHostIP(publicUrl);
                            jitsiJobMap.get(currentJobID).setPodId(uniqueIdentifierOfPod);
                            resultMap.put("public_pod_url", publicUrl);
                            jitsiJobMap.get(currentJobID).setSuccess(true);
                            mcuStateMap.put("step" + (++count), ">> Kubernetes Pod started.");
                            latch.countDown();
                        } else
                            mcuStateMap.put("step" + (++count), ">> Kubernetes event: Action - " + action.name() + " | Phase - " + resource.getStatus().getPhase());
                    } catch (Exception e) {
                        jitsiJobMap.get(currentJobID).setSuccess(false);
                        mcuStateMap.put("step" + (++count), ">> An error occurred: " + e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void onClose(KubernetesClientException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    jitsiJobMap.get(currentJobID).setSuccess(false);
                    mcuStateMap.put("step" + (++count), ">> An error occurred: " + e.getMessage());
                }
            });

            mcuStateMap.put("step" + (++count), "> Kubernetes Pod starting.");
            latch.await(2, TimeUnit.MINUTES);  //main thread is waiting on CountDownLatch to finish

            if (jitsiJobMap.get(currentJobID).isSuccess()) {
                mcuStateMap.put("step" + (++count), "> PROCESS FINISHED SUCCESSFULLY");
                mcuStateMap.put("finished", String.valueOf(true));
                resultMap.put("pod_id", uniqueIdentifierOfPod);
                resultMap.put("job_id", currentJobID);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("error", e.getMessage());
        } finally {
            if (client != null)
                client.close();

            return resultMap;
        }

    }

    private String mapInternalIpToExternal(KubernetesClient client, String internalIp) {
        boolean found = false;
        for (Node n : client.nodes().list().getItems()) {
            String externalIP = null;
            for (NodeAddress na : n.getStatus().getAddresses()) {
                if (found && na.getType().equals("ExternalIP"))
                    return na.getAddress();
                else if (na.getAddress().equals(internalIp) && na.getType().equals("InternalIP")) {
                    if (externalIP != null)
                        return externalIP;
                    else
                        found = true;
                } else if (na.getAddress().equals(internalIp) && na.getType().equals("ExternalIP"))
                    return internalIp;  //internal and external IPs are the same (e.g. in case of arnes)
                else if (!na.getAddress().equals(internalIp) && na.getType().equals("ExternalIP"))
                    externalIP = na.getAddress();
            }
        }
        // if external IP is not explicitly listed inside the node list We should return its internal IP
        if (found)
            return internalIp;
        return null;
    }

    private KubernetesClient initKubernetesClientByClusterId(String selectedClusterID) {
        ConfigProvider configProvider = new ConfigProvider() {
        };
        io.fabric8.kubernetes.api.model.Config config = configProvider.retrieveResource();
        ConfigSelector configSelector = new ConfigSelector(config);
        return new DefaultKubernetesClient(configSelector.configForContext(selectedClusterID));
    }

    private Map<String, String> stopRunningKubernetesPods(KubernetesClient kube, String podId) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            final boolean deleteAll = (podId == null);
            int i = 0;
            if (deleteAll) {
                PodList pods = kube.pods().list();
                List<Pod> items = pods.getItems();

                for (Pod item : items) {
                    kube.pods().withName(item.getMetadata().getName()).delete();
                    System.out.println("pod stopped - " + item.getMetadata().getName());
                    i++;
                }
            } else
                kube.pods().withName(podId).delete();

            resultMap.put("success", "true");
            resultMap.put("count", i + "");
        } catch (Exception e) {
            resultMap.put("success", "false");
            e.printStackTrace();
        }
        return resultMap;
    }

    private Map<String, String> stopRunningKubernetesPods(String clusterName, String podId) {
        Map<String, String> resultMap = new HashMap<>();
        KubernetesClient client = null;

        try {
            client = initKubernetesClientByClusterId(clusterName);
            client.pods().withName(podId).delete();

            resultMap.put("success", "true");
        } catch (Exception e) {
            resultMap.put("success", "false");
            e.printStackTrace();
        } finally {
            if (client != null)
                client.close();
        }
        return resultMap;
    }

    private void stopAllPods(KubernetesClient kube) {
        stopRunningKubernetesPods(kube, null);
    }

    @GET
    @Path("stop_single_pod")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, String> stopSinglePod(@QueryParam("cluster_name") String clusterName, @QueryParam("pod_id") String podId) {
        return stopRunningKubernetesPods(clusterName, podId);
    }


    @GET
    @Path("stop_mcu_execution")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, String> stopMCUExecution(@QueryParam("job_id") String jobID) throws ServletException, IOException {
        //init start parameter states
        mcuStateMap.clear();
        count = 0;

        Map<String, String> resultMap = new HashMap<>();
        KubernetesClient client = null;
        try {
            client = initKubernetesClientByClusterId(jitsiJobMap.get(jobID).getClusterId());
            mcuStateMap.put("step" + (++count), "> Credentials successfully applied to Kubernetes cloud system.");
            final CountDownLatch latch = new CountDownLatch(1);

            client.pods().watch(new Watcher<Pod>() {
                @Override
                public void eventReceived(Action action, Pod resource) {
                    final String podName = resource.getMetadata().getName();
                    if (podName != null && podName.equals(jitsiJobMap.get(jobID).getPodId()) && action.toString().equals("DELETED")) {
                        System.out.println("Congratulations the pod with ID: " + jitsiJobMap.get(jobID).getPodId() +
                                " was sucesfully deleted! However Kubernetes will need some time to kill the container - so the app might still be operational for a while.");

                        resultMap.put("message", "Congratulations the pod with ID: " + jitsiJobMap.get(jobID).getPodId() +
                                " was sucesfully deleted! However Kubernetes will need some time to kill the container - so the app might still be operational for a while.");
                        mcuStateMap.put("finished", String.valueOf(true));
                        latch.countDown();
                    }
                    mcuStateMap.put("step" + (++count), ">> Kubernetes stop event: Action - " + action.name() + " | Phase - " + resource.getStatus().getPhase());
//                    System.out.println(">> Kubernetes stop event: Action - " + action.name() + " | Phase - " + resource.getStatus().getPhase());
                }

                @Override
                public void onClose(KubernetesClientException e) {
                    System.out.println(">> An error occurred: " + e.getMessage());
                    resultMap.put("error", e.getMessage());
                    mcuStateMap.put("step" + (++count), ">> An error occurred: " + e.getMessage());
                    latch.countDown();
                }
            });


            //delete the selected pod
            mcuStateMap.put("step" + (++count), "> Stopping pod.");
            client.pods().withName(jitsiJobMap.get(jobID).getPodId()).delete();
            latch.await(3, TimeUnit.MINUTES);  //main thread
            System.out.println("finished");
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("error", e.getMessage());
        } finally {
            if (client != null)
                client.close();
            return resultMap;
        }
    }

    @GET
    @Path("get_current_mcu_state")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, String> getCurrentMCUstate() throws ServletException, IOException {
        return mcuStateMap;
    }

}