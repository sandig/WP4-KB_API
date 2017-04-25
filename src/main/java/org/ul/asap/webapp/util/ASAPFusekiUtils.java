package org.ul.asap.webapp.util;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.apache.jena.vocabulary.VCARD;
import org.joda.time.DateTime;
import org.ul.asap.webapp.entry.*;
import org.ul.common.monitoring.MonitoringMetric;
import org.ul.common.rest.IService;

import java.io.*;
import java.net.URL;
import java.util.*;

public class ASAPFusekiUtils {

    private static final String XSD_PREFIX = "xsd:<http://www.w3.org/2001/XMLSchema#>";
    private static final String RDF_PREFIX = "rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    public static String KB_PREFIX_SHORT = "http://www.semanticweb.org/project-entice/ontologies/2015/7/knowledgebase#";
    private static String KB_PREFIX = ":<http://www.switch-project.eu/ontology/switch-component#>";
    private static String OWL_PREFIX = "owl:<http://www.w3.org/2002/07/owl#>";

    public static String generateInsertVMStatement(String vmFilename, boolean isScript, String owner, boolean
            optimize) {
        String id = UUID.randomUUID().toString();
        return String.format("PREFIX dc: <http://purl.org/dc/elements/1.1/>" + "INSERT DATA" +
                "{ <http://example/%s>    dc:filename    \"%s\" ; dc:is_script \"%s\" ;  dc:owner  \"%s\" ; " +
                "dc:optimize \"%s\" .}   ", id, vmFilename, isScript, owner, optimize);
    }

    public static String generateInsertObjectStatement(Object obj) {
        try {
            // CREATE APPLICATION COMPONENT
            if (obj instanceof ApplicationComponent) {
                ApplicationComponent ac = (ApplicationComponent) obj;

                return String.format("PREFIX " + KB_PREFIX + " PREFIX " + OWL_PREFIX + " INSERT DATA {" +
                                "knowledgebase:%s  a knowledgebase:ApplicationComponent , owl:NamedIndividual ;" +
                                "        knowledgebase:install_script     \"%s\" ;\n" +
                                "        knowledgebase:stateful  %s ;\n" +
                                "        knowledgebase:execution_script     \"%s\" ;" +
                                "        knowledgebase:description     \"%s\" ;" +
                                "}", ac.getId(), ac.getInstallScript(), ac.isStateful(), ac.getExecutionScript(),
                        ac.getDescription());
                //ac.getInputInterface(), ac.getDevelopmentEvent(), ac.getOutputInterface(), ac.getQualityAttribute(),
            }
            // CREATE CLUSTER CREDENTIALS
            else if (obj instanceof ClusterCredentials) {
                ClusterCredentials cc = (ClusterCredentials) obj;

                return String.format("PREFIX " + KB_PREFIX + " PREFIX " + OWL_PREFIX + " INSERT DATA {" +
                        ":%s  a :ClusterCredentials, owl:NamedIndividual, :" +
                        (cc.getCredType().equals(CredentialsType.CERTIFICATES) ? "CERTIFICATES" : "PASSWORD") + " ;" +
                        "        :ClusterCredentials_MasterURL     \"%s\" ;\n" +
                        "        :ClusterCredentials_ClientPublicCredentials  \"%s\" ;\n" +
                        "        :ClusterCredentials_ClientPrivateCredentials     \"%s\" ;" +
                        "        :ClusterCredentials_CertificateAuthority     \"%s\" ;" +
                        "}", UUID.randomUUID().toString(), cc.getMasterURL(), cc.getClientPublicCredentials(), cc.getClientPrivateCredentials(), cc.getCertificateAuthority());
            }
            // CREATE MONITORING METRIC
            else if (obj instanceof MonitoringMetric) {
                MonitoringMetric mm = (MonitoringMetric) obj;

                return String.format(Locale.US, "PREFIX " + KB_PREFIX + " PREFIX " + OWL_PREFIX + " PREFIX " +
                                OWL_PREFIX + "PREFIX " + XSD_PREFIX + " INSERT DATA {" +
                                ":%s  a :MonitoringMetric, owl:NamedIndividual ;" +
                                "        :MonitoringMetric_CollectingInterval     %d ;\n" +
                                "        :MonitoringMetric_DataType  \"%s\" ;\n" +
                                "        :MonitoringMetric_Group     \"%s\" ;" +
                                "        :MonitoringMetric_LowerLimit     \"%f\"^^xsd:double ;" +
                                "        :MonitoringMetric_Name     \"%s\" ;" +
                                "        :MonitoringMetric_Threshold     \"%f\"^^xsd:double ;" +
                                "        :MonitoringMetric_Unit     \"%s\" ;" +
                                "        :MonitoringMetric_UpperLimit     \"%f\"^^xsd:double ;" +
                                "        :MonitoringMetric_RelationalOperator     \"%s\" ;" +
                                "}", UUID.randomUUID().toString(), mm.getCollectingInterval(), mm.getDataType(),
                        mm.getGroup(), mm.getLowerLimit(), mm.getName(), mm.getThreshold(), mm.getUnit(),
                        mm.getUpperLimit(), mm.getRelationalOperator());
            } else {
                throw new InvalidObjectException("The selected object type is not currently supported!");
            }
        } catch (InvalidObjectException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> String createRegixBasedTableString(T[][] objectives) {
        String objectivesStr = new String();
        for (int i = 0; i < objectives.length; i++) {
            for (int j = 0; j < objectives[i].length; j++) {
                objectivesStr += objectives[i][j] + ",";
            }
            objectivesStr = objectivesStr.substring(0, objectivesStr.length() - 1);
            objectivesStr += "//";
        }
        if (objectivesStr.length() > 0) objectivesStr = objectivesStr.substring(0, objectivesStr.length() - 2);

        return objectivesStr;
    }

    @Deprecated
    public static String getPassword(String username) {
        return "prefix knowledgebase: <http://www.semanticweb.org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                "\n" +
                "SELECT ?pass\n" +
                "WHERE { ?s a knowledgebase:User ; knowledgebase:User_UserName \"" + username + "\" ; " +
                "knowledgebase:User_Password ?pass }\n" +
                "LIMIT 25\n";
    }


    public static String performUserLogin(String username, String password) {
        return "prefix knowledgebase: <http://www.semanticweb.org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                "\n" +
                "SELECT ?s ?privilege \n" +
                "WHERE { ?s a knowledgebase:User ; knowledgebase:User_UserName \"" + username + "\" ; " +
                "knowledgebase:User_Password \"" + password + "\" ; knowledgebase:User_Privilege ?privilege }\n" +
                "LIMIT 25\n";
    }

    public static String getAllEntitiesQuery(String entityClass, String... queryFilterCondition) {
        if (entityClass.equals("ClusterCredentials") && queryFilterCondition.length > 0) {
            return "prefix : <http://www.switch-project.eu/ontology/switch-component#>\n" +
                    "\n" +
                    "SELECT ?s ?p ?o\n" +
                    "WHERE { :" + queryFilterCondition[0] + " a :" + entityClass + " ; ?p ?o }";
        } else return "prefix knowledgebase: <http://www.switch-project.eu/ontology/switch-component#>\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE { ?s a knowledgebase:" + entityClass + " ; ?p ?o }\n";
//                    "LIMIT 200";
    }

    public static String getAllEntitiesCount(String entityClass, String... queryFilterCondition) {
        if (entityClass.equals("DiskImage") && queryFilterCondition.length > 0)
            return "prefix knowledgebase: <http://www.semanticweb" +
                    ".org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "\n" +
                    "SELECT (COUNT(DISTINCT ?s) AS ?count)\n" +
                    "WHERE { ?s a knowledgebase:" + entityClass + " ; knowledgebase:DiskImage_hasOwner " +
                    "\"" + queryFilterCondition[0] + "\"^^xsd:anyURI }";
        else
            return "prefix knowledgebase: <http://www.semanticweb" +
                    ".org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                    "SELECT (COUNT(DISTINCT ?s) AS ?count)\n" +
                    "WHERE { ?s a knowledgebase:" + entityClass + "}";
    }

    public static String getEntityQuery(String entityClass, String... postfixCondition) {
        if (postfixCondition.length > 1)
            return "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "prefix knowledgebase: <http://www.semanticweb" +
                    ".org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                    "\n" +
                    "SELECT ?s ?p ?o\n" +
                    "WHERE { ?s a knowledgebase:" + entityClass + " " + postfixCondition[0] + " }\n" +
                    postfixCondition[1];

        return "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "prefix knowledgebase: <http://www.semanticweb" +
                ".org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                "\n" +
                "SELECT ?s ?p ?o\n" +
                "WHERE { ?s a knowledgebase:" + entityClass + " ; ?p ?o }\n" + postfixCondition[0];
    }

    public static String getIdBasedEntitiesQuery(String clazz, String id) {
        if (clazz.equals("Fragment")) return "prefix knowledgebase: <http://www.semanticweb" +
                ".org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                "SELECT ?s\n" +
                "WHERE { ?s a knowledgebase:Fragment ; knowledgebase:Fragment_hasReferenceImage ?o , " +
                "knowledgebase:" + id + " }\n";
//                "LIMIT 200";
        else throw new UnsupportedOperationException("not implemented for this class");
    }

    public static String getHistoryDataIDs(String fragmentId) {
        return "prefix knowledgebase: <http://www.semanticweb.org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                "prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "select ?o\n" +
                "where { knowledgebase:" + fragmentId + " knowledgebase:hasHistoryData ?o\n" +
                "}";
    }

    public static String getDiskImageIDs() {
        return "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "prefix knowledgebase: <http://www.semanticweb.org/project-entice/ontologies/2015/7/knowledgebase#>\n" +
                "\n" +
                "#SELECT count(*)\n" +
                "SELECT DISTINCT ?s\n" +
                "WHERE { ?s a knowledgebase:DiskImage . ?s a owl:NamedIndividual , ?o }\n" +
                "#FILTER(?o IN knowledgebase:DiskImage )";
    }

    public static String getAllAttributesMatchingSubjectID(String id) {
        return "SELECT ?p ?o\n" +
                "WHERE { <" + id + "> ?p  ?o . }\n" +
                "LIMIT 25";
    }

    public static String getFusekiDBSource(String sourceURL) {
        InputStream is = null;
        try {
            is = new URL(sourceURL).openStream();
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            // ugly hack but works. The original content have not valid format using "{" and "}"
            result = result.replace("{", "");
            result = result.replace("}", "");

            //write content to tmp file
            File temp = File.createTempFile("tempfile", ".tmp");

            //write it
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(result);
            bw.close();
            return temp.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void getAllReasoners() {
        Model reasoners = ReasonerRegistry.theRegistry().getAllDescriptions();
        ResIterator ri = reasoners.listSubjectsWithProperty(RDF.type, ReasonerVocabulary.ReasonerClass);
        while (ri.hasNext()) {
            System.out.println(" " + ri.next());
        }
    }

    public static final String myNamespacePrefix = "http://entice-project/jernej/test/";

    public static List<ResultObj> getResultObjectListFromResultSet(ResultSet results) {
        List<ResultObj> resultObjs = new ArrayList<ResultObj>();

        // For each solution in the result set
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            Iterator<Var> varIter = ((ResultBinding) qs).getBinding().vars();
            String x = null;
            String r = null;
            String y = null;
            while (varIter.hasNext()) {
                Var var = varIter.next();
                if (var.getVarName().equals("s")) {
                    x = ((ResultBinding) qs).getBinding().get(var).toString();
                    if (x.contains(KB_PREFIX_SHORT)) x = x.replace(KB_PREFIX_SHORT, "");
                } else if (var.getVarName().equals("p")) r = ((ResultBinding) qs).getBinding().get(var).toString();
                else if (var.getVarName().equals("o")) {
                    try {
                        String resStr = ((ResultBinding) qs).getBinding().get(var).toString();
                        if (resStr.contains("^^http://www.w3.org/2001/XMLSchema#dateTime"))
                            y = ((ResultBinding) qs).getBinding().get(var).getLiteral().getValue().toString();
                        else if (resStr.contains("^^http://www.w3.org/2001/XMLSchema#anyURI"))
                            y = resStr.replace("^^http://www.w3.org/2001/XMLSchema#anyURI", "").replaceAll("\"", "");
                        else if (resStr.contains("^^http://www.w3.org/2001/XMLSchema#integer"))
                            y = resStr.replace("^^http://www.w3.org/2001/XMLSchema#integer", "").replaceAll("\"", "");
                        else if (resStr.contains("-")) y = resStr.replaceAll("\"", "");
                        else if (resStr.contains("^^http://www.w3.org/2001/XMLSchema#double"))
                            y = resStr.replace("^^http://www.w3.org/2001/XMLSchema#double", "").replaceAll("\"", "");
                        else y = String.valueOf(((ResultBinding) qs).getBinding().get(var).getLiteral().getValue());

                        //additional filter of prefixes cannot be done here!
                        //if(resStr.startsWith(KB_PREFIX_SHORT)) y = resStr.replaceFirst(KB_PREFIX_SHORT,"");
                    } catch (Exception e) {
                        y = ((ResultBinding) qs).getBinding().get(var).toString();
                    }
                }
            }

            resultObjs.add(new ResultObj(x, r, y));
        }

        return resultObjs;
    }

    public static <T extends MyEntry> List<T> getAllEntityAttributes(Class<T> clazz, String ontologyURL, String... conditions) {
        String selectQuery = ASAPFusekiUtils.getAllEntitiesQuery(clazz.getSimpleName(), conditions);
        QueryExecution qe = QueryExecutionFactory.sparqlService(ontologyURL, selectQuery);
//        QueryExecution qe = QueryExecutionFactory.sparqlService("http://localhost:3030/entice/query", selectQuery);
        ResultSet results = qe.execSelect();
        List<ResultObj> resultObjs = ASAPFusekiUtils.getResultObjectListFromResultSet(results);

        //exception!!
        if (conditions.length > 0 && (clazz.getSimpleName().equals("ClusterCredentials") ||
                clazz.getSimpleName().equals("MonitoringMetric")) && resultObjs.size() > 0 && resultObjs.get(0).getS() == null) {
            for (ResultObj resultObj : resultObjs) {
                resultObj.setS(conditions[0]);
            }
        }


        return ASAPFusekiUtils.mapResultObjectListToEntry(clazz, resultObjs);
    }

    public static <T extends MyEntry> List<T> getAllEntityAttributes(String selectQuery, Class clazz, IService service) {

        QueryExecution qe = QueryExecutionFactory.sparqlService(service.getFusekiQuery(), selectQuery);
        long startTime = System.currentTimeMillis();
        ResultSet results = qe.execSelect();
        System.out.println("SUBSET: " + (System.currentTimeMillis() - startTime) + "ms");
        List<ResultObj> resultObjs = ASAPFusekiUtils.getResultObjectListFromResultSet(results);

        return ASAPFusekiUtils.mapResultObjectListToEntry(clazz, resultObjs);
    }

    public static <T extends MyEntry> List<T> mapResultObjectListToEntry(Class<T> clazz, List<ResultObj> resultObjs) {
        List<T> list = new ArrayList<T>();
        for (ResultObj resultObj : resultObjs) {
            ASAPCommonUtils.mapResultObjectToEntry(list, resultObj, clazz);
        }
        return list;
    }

    public static List<MonitoringMetric> getAllEntityAttributes(Class<MonitoringMetric> monitoringMetricClass, String property) {
        //TODO
        return null;
    }
}

