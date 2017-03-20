package org.ul.asap.webapp.mcu;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class StopMCUpod.
 * FIRST NON INTEGRATED VERSION
 */
@WebServlet("/StopMCUpod")
public class StopMCUpod extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private KubernetesClient newClientForWatchingPods;
    public static Watch mojStrazarPodov;

    //"semaphore" used to identify when the pod is "up and running"
    public static boolean thePodIsDead = false;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public StopMCUpod() {
        super();
        // TODO Auto-generated constructor stub
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        //get the clusteID and the podID from request (in order to stop the pod)
        String clusterId = request.getParameter("idOfKubernetesCluster");
        String podId = request.getParameter("podId");

        //we need the obtain appropriately configured Config object to access the right cluster and be able "start working" in the right Kubernates namespace
        //in our case everything will be done in the "default" Kubernetes namespace (no matter which cluster)
        //TODO: send right credential:
        Config config = UtilitiesBKP.getKuberConfig(null, "default");
//		Config config = Utilities.getKuberConfig(clusterId, "default");
        KubernetesClient kube = new DefaultKubernetesClient(config);

        try {

            //WATCHER OF PODs
            newClientForWatchingPods = new DefaultKubernetesClient(config);
            mojStrazarPodov = newClientForWatchingPods.pods().watch(new MojPodsWatcher(podId));
            newClientForWatchingPods.close();

            //delete the pod now (the cluster and namespace where to delete pod are determined by the KubernetesClient object passed to the method)
            UtilitiesBKP.deletePodByName(kube, podId);


            while (true) {
                try {
                    Thread.sleep(500);
                    if (thePodIsDead == true) {
                        out.println("<!DOCTYPE html>");
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<meta charset=\"ISO-8859-1\">");
                        out.println("<title>MCU magic!</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<p>Congratulations the pod with ID: " + podId + " was sucesfully deleted! However Kubernetes will need some time to kill the container - so the app might still be operational for a while.</p>");
                        out.println("</body>");
                        out.println("</html>");
                        out.close();
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            kube.close();
            out.close();
        }
    }


    //I am really not sure if this is a reliable way of checking when the pod is "killed" sucesfully
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
                if (podName != null && podName.equals(MojPodsWatcher.identifikatorOfPod) && action.toString().equals("DELETED")) {
                    StopMCUpod.thePodIsDead = true;
                    StopMCUpod.mojStrazarPodov.close();
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
