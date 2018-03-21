package org.ul.asap.webapp.util;

import org.apache.jena.atlas.lib.Alarm;
import org.joda.time.DateTime;
import org.ul.asap.webapp.entry.*;
import org.ul.common.monitoring.MonitoringMetric;
import org.ul.common.rest.IService;
import org.ul.entice.webapp.entry.*;
import org.ul.entice.webapp.util.FusekiUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class ASAPCommonUtils implements IService {
    public static void initProperties(Properties properties, String propertyName) {
        try {
            // load a properties file
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void mapResultObjectToEntry(List<?> list, ResultObj resultObj, Class clazz) {
        try {
            int index = getIndex((List<MyEntry>) list, resultObj.getS(), clazz);

            if (list.get(index) instanceof ApplicationComponent) {
                List<ApplicationComponent> repositoryList = (List<ApplicationComponent>) list;
                if (resultObj.getP().endsWith("install_script"))
                    repositoryList.get(index).setInstallScript(resultObj.getO());
                else if (resultObj.getP().endsWith("description"))
                    repositoryList.get(index).setDescription(resultObj.getO());
                else if (resultObj.getP().endsWith("execution_script"))
                    repositoryList.get(index).setExecutionScript(resultObj.getO());
                else if (resultObj.getP().endsWith("stateful"))
                    repositoryList.get(index).setStateful(Boolean.valueOf(resultObj.getO()));
            } else if (list.get(index) instanceof ClusterCredentials) {
                List<ClusterCredentials> clusterCredList = (List<ClusterCredentials>) list;
                //TODO: use this generic invocation of methods instead of hardcoded ones!!
                try {
                    Method method = clazz.getMethod("set" + resultObj.getP().split("_")[1], String.class);
                    method.invoke(list.get(index), resultObj.getO());
                } catch (Exception e) {
                    //e.printStackTrace();

                    if (resultObj.getP().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        clusterCredList.get(index).setCredType(CredentialsType.valueOf(resultObj.getO().
                                replaceFirst("http://www.switch-project.eu/ontology/switch-component#", "")));
                }
            } else if (list.get(index) instanceof MonitoringMetric || list.get(index) instanceof AlarmTrigger) {
                try {
                    if (resultObj.getP().endsWith("AlarmTrigger_Time"))
                        ((AlarmTrigger)list.get(index)).setTime(new DateTime(Long.valueOf(resultObj.getO())));

                    try {
                        Method method = clazz.getMethod("set" + resultObj.getP().split("_")[1], Integer.TYPE);
                        method.invoke(list.get(index), Integer.valueOf(resultObj.getO()));
                        return;
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }

                    try {
                        Method method = clazz.getMethod("set" + resultObj.getP().split("_")[1], String.class);
                        method.invoke(list.get(index), resultObj.getO());
                        return;
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }

                    try {
                        Method method = clazz.getMethod("set" + resultObj.getP().split("_")[1], Double.TYPE);
                        method.invoke(list.get(index), Double.valueOf(resultObj.getO()));
                        return;
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getIndex(List<MyEntry> entryList, String id, Class clazz) {
        int i = 0;

        for (MyEntry entry : entryList) {
            if (entry.getId().contains(id))
                return i;
            i++;
        }
        entryList.add(EntryFactory.getInstance(clazz, id));
        return (entryList.size() - 1);
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static int getFileSize(String url) {
        try {
            final URL uri = new URL(url);
            URLConnection ucon;
            ucon = uri.openConnection();
            ucon.connect();
            final String contentLengthStr = ucon.getHeaderField("content-length");
            return (int) (Long.valueOf(contentLengthStr) / 1024);
        } catch (final IOException e1) {
            e1.getMessage();
            return -1;
        }
    }

    @Override
    public String getFusekiQuery() {
        return null;
    }
}
