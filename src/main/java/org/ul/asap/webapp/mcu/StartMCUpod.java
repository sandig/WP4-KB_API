package org.ul.asap.webapp.mcu;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class StartMCUpod
 * FIRST NON INTEGRATED VERSION
 */
@WebServlet("/StartMCUpod")
public class StartMCUpod extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected static KubernetesClient newClientForWatchingPods;
    protected static Watch mojStrazarPodov;

    //"semaphore" used to identify when the pod is "up and running"
    //TODO: I am not sure about this! This is global variable that is only one for thousands of HTTP request - so once the watcher will set this variable to "true"
    //all others will "think" that "their" Pod is up and running. You should test this - instantiate pod and then kill it with kubectl - and see if my code still
    //reports that the pod is up and running (because this variable remained set to "true").
    public static boolean thePodIsUpAndRunning = false;
    //this variable should contain the IP where the pod is running (when it reaches the "up and running" state, before that the value is null)
    public static String publicIpWherePodStarted = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public StartMCUpod() {
        super();
        // TODO Auto-generated constructor stub
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        KubernetesClient kube = null;
        //get the IP addresses (locations) of the clients that will be participating in the videoconference. This info is passed to Matej's "adaptation
        //module" and it should return the id of the cluster where to start the Pod.
        String clientLocations = request.getParameter("participants");
        List<String> clientIps = Utilities.parseAndValidateClientIPs(clientLocations);
        if (clientIps == null) {
            //then something when wrong while parsing the IPs and validating them --> so we should inform the user immediately - show him web form once again
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset=\"ISO-8859-1\">");
            out.println("<title>Enter IPs of participants</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<form action=\"./StartMCUpod\" method=\"post\">");
            out.println("<p>One or more of your IPs were in wrong format, or you provided an empty list! Do not expect adaptation module to work with that!</p>");
            out.println("<p>Please list videoconference participants IPs delimited with semicolon once again: </p>");
            out.println("<input type=\"text\" name=\"participants\" size=\"200\"><br>");
            out.println("<br>");
            out.println("<input type=\"submit\" value=\"Get it now!\">");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
            out.close();

        } else {
            try {
                DecisionMakerInput input = new DecisionMakerInput("MCU", clientIps);
                //if there is at least one IP specified by the user - then we should call the Adaptation module of Matej
//			String clusterId=Utilities.getTheClusterIdFromDecisionMaker(input);

                // GET request
                String clusterId = "a570ee11-eaeb-4e99-ab73-24ff44e82af4";
                URIBuilder builder = new URIBuilder("http://localhost:8080/SWITCH/rest/asap/get_credentials?cluster_id=" + clusterId);
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet httpGet = new HttpGet(builder.build());
                HttpResponse getResponse = httpClient.execute(httpGet);

                //            httpGet.addHeader("Content-Type", "application/json");

//            HttpEntity resEntity = response.getEntity();
                BufferedReader rd = new BufferedReader(new InputStreamReader(getResponse.getEntity().getContent()));
                StringBuffer result = new StringBuffer();

                //ClusterCredentials cred = (ClusterCredentials) credentialsResponse.getResultObject();

                result.toString();

                //we need the obtain appropriately configured Config object to access the right cluster and be able "start working" in the right Kubernates namespace
                //in our case everything will be done in the "default" Kubernetes namespace (no matter which cluster)
                Config config = Utilities.getKuberConfig(null, "default");
                kube = new DefaultKubernetesClient(config);

                String uniqueIdentifierOfPod = Utilities.generateKubernetesFriendlyShortUuid(24);


                //WATCHER OF PODs
                newClientForWatchingPods = new DefaultKubernetesClient(config);
//                mojStrazarPodov = newClientForWatchingPods.pods().watch(new Watcher<Pod>() {
//                    @Override
//                    public void eventReceived(Action action, Pod pod) {
//
//                    }
//
//                    @Override
//                    public void onClose(KubernetesClientException e) {
//
//                    }
//                });
                mojStrazarPodov = newClientForWatchingPods.pods().watch(new MojPodsWatcher(uniqueIdentifierOfPod));
                //TODO: instead of watching all the pods in the cluster (and their events) we should rather watch the individual Pod (the one with uniqueIdentifierOfPod)
                //see the commented line below - however it was not tested!
                //mojStrazarPodov=newClientForWatchingPods.pods().withName(uniqueIdentifierOfPod).watch(new MojPodsWatcher(uniqueIdentifierOfPod));
                newClientForWatchingPods.close();

                //create the pod now
                Utilities.createPod(kube, uniqueIdentifierOfPod);


                while (true) {
                    try {
                        Thread.sleep(500);
                        if (thePodIsUpAndRunning == true) {
                            out.println("<!DOCTYPE html>");
                            out.println("<html>");
                            out.println("<head>");
                            out.println("<meta charset=\"ISO-8859-1\">");
                            out.println("<title>MCU magic!</title>");
                            out.println("</head>");
                            out.println("<body>");
                            out.println("<p>Please start your videoconference <a href=\"https://" + publicIpWherePodStarted + ":444\" target=\"_blank\">here</a></p>");
                            out.println("<p>When finished click the below button to destroy the MCU instance!</p>");
                            out.println("<form action=\"./StopMCUpod\" method=\"post\">");
                            out.println("<input type=\"hidden\" name=\"idOfKubernetesCluster\" value=\"" + clusterId + "\">");
                            out.println("<input type=\"hidden\" name=\"podId\" value=\"" + uniqueIdentifierOfPod + "\">");
                            out.println("<input type=\"submit\" value=\"Kill it!\">");
                            out.println("</form>");


                            out.println("</body>");
                            out.println("</html>");
                            out.close();
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            try {
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kube.close();
                out.close();
            }
        }

    }


    //I am really not sure if this is a reliable way of checking when the pod is "up and running"
    public static class MojPodsWatcher implements Watcher<Pod> {
        private static String identifikatorOfPod;

        //constructor
        public MojPodsWatcher(String id) {
            MojPodsWatcher.identifikatorOfPod = id;
        }

        @Override
        public void eventReceived(Action action, Pod resource) {
            try {
                String podName = resource.getMetadata().getName();
                String podStatus = resource.getStatus().getPhase();
                if (podName != null
                        && podName.equals(MojPodsWatcher.identifikatorOfPod)
                        && !action.toString().equals("DELETED")
                        && podStatus != null
                        && podStatus.equals("Running")) {
                    StartMCUpod.thePodIsUpAndRunning = true;
                    // TODO: this is only working on Arnes cloud, in GCP it requires more effort
                    // to get the public IP where Pod started
                    StartMCUpod.publicIpWherePodStarted = resource.getStatus().getHostIP();
                    StartMCUpod.mojStrazarPodov.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(KubernetesClientException e) {
            if (e != null) {
                e.printStackTrace();
                System.out.println("Ma jebote exception!");
            }
        }
    }
}
