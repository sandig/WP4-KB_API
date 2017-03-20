package org.ul.asap.webapp.mcu;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.ul.asap.webapp.entry.ClusterCredentials;
import org.ul.asap.webapp.entry.CredentialsType;

public class Utilities {


    public static String getTheClusterIdFromDecisionMaker(DecisionMakerInput input) throws IOException {
        //pass the client locations to the "adaptation module" which should reposnd with the cluster ID where to start the pod
        //if you want to fake here and just return some valid clusterID - then you should check the cluster IDs that are currently in the knowledge base
        //if you intend to use the KnowledgeBaseDummy project (instead of real KB) then check what cluster IDs are defined there --> see KBDummy class
        //Arnes cluster --> ID="111abc111"
        //GCP Taiwan cluster --> ID="222abc222"
        //GCP US west --> ID="333abc333"
        //Flexiops cluster --> ID="444abc444" --> note that Flexiops cluster is not the most reliable cluster on this planet
        //return "111abc111";


        //but if you want to contact a real RESTful service that returns the clusterID - then you can make a call to the ASAPdecisionMaker project (service in that project)
        //so connect to the decision maker service and obtain the clusterId
        //the location (=URL) of the service is specified in the KubernetesMCUdeployer.properties properties file
        Properties properties = new Properties();

        //COULD HAVE PUT THE KubernetesMCUdeployer.properties file somewhere in the WEB APPLICATION FOLDER - AND BECAUSE THE FILE WILL BE IN CLASSPATH IT WILL BE FOUND AUTOMATICALLY
        //USUALLY THIS IS THE PREFERRED WAY OF ACCESSING THE PROPERTIES FILE --> SEE:
        //http://stackoverflow.com/questions/2161054/where-to-place-and-how-to-read-configuration-resource-files-in-servlet-based-app/2161583#2161583
        //http://javahonk.com/load-properties-file-servlet-java/
        //I could have put the file in the classpath - for example drop it in the src folder in my Eclipse project	and then load the file with line below
        //properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("KubernetesFabric8RESTapi.properties"));

        //HOWEVER IF I PUT THE PROPERTIES FILE IN THE WEB APPLICATION FOLDER THERE WILL BE PROBLEMS IF I WANT TO MAKE A DOCKER IMAGE OUT OF THIS WEB APP -->
        //THERE WILL BE PROBLEMS OF MODIFYING THE .properties FILE IN DOCKERFILE PRIOR THE TOMCAT IS STARTED (AND THE APPLICATION FOLDER IS EXTRACTED FROM A WAR FILE)!
        //SO INSTEAD OF PUTTING THE KubernetesMCUdeployer.properties ON THE CLASSPATH SOMEWHERE IN THE WEB APPLICATION FOLDER STRUCTURE - I WILL RATHER PUT IT
        //DIRECTLY IN THE "catalina.base" FOLDER --> if you run the server from within Eclipse then the "catalina.base" is
        //E:\workspace-FGG-switch\.metadata\.plugins\org.eclipse.wst.server.core\tmp0 --> so put the properties file there!
        //however if you are running in "normal" Tomcat and you did not configure in some other way - then the catalina.home and catalina.base are the same - see
        //http://stackoverflow.com/questions/3090398/tomcat-catalina-base-and-catalina-home-variables
        //TODO: this is probably a bad practise but will do the job for the application that will be delivered in Docker container (no issues with deployment and
        //location of the file in the disk)
        String catalinaBase = System.getProperty("catalina.base");
  
		//read the properties file
        //the file should have the line that determines where the decision maker service is. If we deployed the ASAPdecisionMaker project on the same Tomcat server as
        //this Servlet --> then the line in the properties file should be like the one below:
        //DMapiURL=http://localhost:8080/ASAPdecisionMaker/makedecision/decide/getClusterId
        InputStream in = new FileInputStream(catalinaBase + "/KubernetesMCUdeployer.properties");
        properties.load(in);


        //String uri="http://localhost:8080/ASAPdecisionMaker/makedecision/decide/getClusterId";
        String uri = properties.getProperty("DMapiURL");
        System.out.println("The decision maker endpoint to retrieve the cluster ID: " + uri);

        //TODO: I think that I should not make a new client for every call of this method (instead I should have one global client object which should of course be thread safe)
        /*
        ClientConfig config = new ClientConfig().register(LoggingFilter.class);
        Client client = ClientBuilder.newClient(config);
        WebTarget service = client.target(UriBuilder.fromUri(uri).build());
        Invocation.Builder invocationBuilder = service.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.json(input));
        String clusterId = response.readEntity(String.class);
        */
        String clusterId = "arnes";
        if (clusterId == null) {
            throw new IOException("The clusterID obtained from the decision maker service is null!");
        }
        return clusterId;


    }


    public static Config getKuberConfig(String clusterId, String namespace) {
        //call the KB and obtain the credentials
        Config kuberConf = null;
        try {
            ClusterCredentials cred = Utilities.getClusterCredentialsFromKB(clusterId);
            if (cred.getCredType().equals(CredentialsType.CERTIFICATES)) {
                kuberConf = new ConfigBuilder()
                        .withMasterUrl(cred.getMasterURL())
                        .withTrustCerts(true)
                        .withNamespace(namespace)
                        .withCaCertData(cred.getCertificateAuthority())
                        .withClientCertData(cred.getClientPublicCredentials())
                        .withClientKeyData(cred.getClientPrivateCredentials())
                        .build();
            } else if (cred.getCredType().equals(CredentialsType.PASSWORD)) {
                kuberConf = new ConfigBuilder()
                        .withMasterUrl(cred.getMasterURL())
                        .withTrustCerts(true)
                        .withNamespace(namespace)
                        .withCaCertData(cred.getCertificateAuthority())
                        .withUsername(cred.getClientPublicCredentials())
                        .withPassword(cred.getClientPrivateCredentials())
                        .build();
            } else {
                //nothing - we will return null object in this case
            }
        } catch (IOException e) {
            System.out.println("Well the KB cannot be accessed! Check the URL in the KubernetesMCUdeployer.properties file!");
            e.printStackTrace();
        }
        return kuberConf;
    }


    protected static ClusterCredentials getClusterCredentialsFromKB(String clusterId) throws IOException {
        //connect to the knowledge base and obtain the credentials

        Properties properties = new Properties();

        //COULD HAVE PUT THE KubernetesMCUdeployer.properties file somewhere in the WEB APPLICATION FOLDER - AND BECAUSE THE FILE WILL BE IN CLASSPATH IT WILL BE FOUND AUTOMATICALLY
        //USUALLY THIS IS THE PREFERRED LOCATION FOR PROPERTIES FILE --> SEE:
        //http://stackoverflow.com/questions/2161054/where-to-place-and-how-to-read-configuration-resource-files-in-servlet-based-app/2161583#2161583
        //http://javahonk.com/load-properties-file-servlet-java/
        //I could have put the file in the classpath - for example dropp it in the src folder in my Eclipse project	and then load the file with line below
        //properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("KubernetesFabric8RESTapi.properties"));


        //HOWEVER IF I PUT THE PROPERTIES FILE IN THE WEB APPLICATION FOLDER THERE WILL BE PROBLEMS IF I WANT TO MAKE A DOCKER IMAGE OUT OF THIS WEB APP -->
        //THERE WILL BE PROBLEMS OF MODIFYING THE .properties FILE IN DOCKERFILE PRIOR THE TOMCAT IS STARTED (AND THE APPLICATION FOLDER IS EXTRACTED FROM A WAR FILE)!
        //SO INSTEAD OF PUTTING THE KubernetesMCUdeployer.properties ON THE CLASSPATH SOMEWHERE IN THE WEB APPLICATION FOLDER STRUCTURE - I WILL RATHER PUT IT
        //DIRECTLY IN THE "catalina.base" FOLDER --> if you run the server from within Eclipse then the "catalina.base" is
        //E:\workspace-FGG-switch\.metadata\.plugins\org.eclipse.wst.server.core\tmp0 --> so put the properties file there!
        //however if you are running in "normal" Tomcat and you did not configure in some other way - then the catalina.home and catalina.base are the same - see
        //http://stackoverflow.com/questions/3090398/tomcat-catalina-base-and-catalina-home-variables
        //TODO: this is probably a bad practise but will do the job for the application that will be delivered in Docker container (no issues with deployment and
        //location of the file in the disk)
        String catalinaBase = System.getProperty("catalina.base");
        //System.out.println("Catalina bejs pa je: "+catalinaBase);
        //String catalinaHome=System.getProperty("catalina.home");
        //System.out.println("Catalina home pa je: "+catalinaHome);

        //read the properties file
        //the file should have the line that determines where the knowledge base is. If we deployed the KnowledgeBaseDummy project on the same Tomcat server as
        //this Servlet --> then this URL should be like the one below:
        //KBapiURL=http://localhost:8080/KnowledgeBaseDummy/kbdummy/dummy/getCredentials
        InputStream in = new FileInputStream(catalinaBase + "/KubernetesMCUdeployer.properties");
        properties.load(in);


        //String uri="http://localhost:8080/KubernetesFabric8RESTapi/AdaptationAPI/switch/getCredentials";
        String uri = properties.getProperty("KBapiURL");
        System.out.println("The KB endpoint to retrieve credentials is: " + uri);

        //TODO: I think that I should not make a new client for every call of this method (instead I should have one global client object which should of course be thread safe)
        ClientConfig config = new ClientConfig().register(LoggingFilter.class);
        Client client = ClientBuilder.newClient(config);
        WebTarget service = client.target(UriBuilder.fromUri(uri).build());
        Invocation.Builder invocationBuilder = service.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.json(clusterId));
        ClusterCredentials cred = response.readEntity(ClusterCredentials.class);
        if (cred == null) {
            throw new IOException("The credentials obtained from the KB are null!");
        }
        return cred;

    }


    protected static String generateKubernetesFriendlyShortUuid(int numberOfCharsInOutputString) {
        UUID uuid = UUID.randomUUID();
        String orig = uuid.toString();
        String str = orig.replaceAll("-", "");//remove the unnecessary hyphens
        char[] strip = str.toCharArray();
        char[] shorten = new char[numberOfCharsInOutputString];
        for (int i = 0; i < numberOfCharsInOutputString; i++) {
            //the first character in the string has to be a letter not a number
            if (i == 0) {
                char curr;
                do {
                    Random r = new Random();
                    int ranNum = r.nextInt(32);//there should be 32 characters in the "stripped version" of UUID string, and we pick char in random position - so we need numbers from 0...31
                    curr = strip[ranNum];
                    shorten[i] = curr;
                }
                while (!Character.isLetter(curr));
            } else {
                Random r = new Random();
                int ranNum = r.nextInt(32);//there should be 32 characters in the "stripped version" of UUID string, and we pick char in random position - so we need numbers from 0...31
                char curr = strip[ranNum];
                shorten[i] = curr;
            }
        }
        return new String(shorten);
    }


    public static void createPod(KubernetesClient kub, String podId) {
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("id", podId);
        labels.put("app", "testmcu");

        Map<String, Quantity> resourceRequests = new HashMap<String, Quantity>();
        resourceRequests.put("cpu", new Quantity("100m"));
        resourceRequests.put("memory", new Quantity("100Mi"));
        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(podId)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName("***")
                .withImage("***")
                .withNewResources().withRequests(resourceRequests).endResources()
                .addNewEnv().withName("***").withValue("***").endEnv()
                .addNewPort().withName("***").withContainerPort(111).withProtocol("***").withHostPort(111).endPort()
                .addNewPort().withName("***").withContainerPort(111).withProtocol("***").withHostPort(111).endPort()
                .endContainer()
                .endSpec()
                .build();
        kub.pods().create(pod);
    }


    /**
     * This method deletes the Pod with the given name. The Kubernetes cluster and the namespace are determined by the KubernetesClient object passed to the method.
     *
     * @param kube the {@link KubernetesClient} object used to access the Kubernetes API
     * @param id   the name that was used when creating the Pod we want to delete now.
     */
    protected static void deletePodByName(KubernetesClient kube, String id) {
        kube.pods().withName(id).delete();
    }


    public static List<String> parseAndValidateClientIPs(String ipsToAdd) {
        //remove any accidental whitespaces from the String received in request
        //the regex, \s will remove anything that is a space character (including space, tab characters etc). You need to escape the backslash in Java so the regex turns into \\s.
        ipsToAdd = ipsToAdd.replaceAll("\\s", "");
        String[] newIpsArray = ipsToAdd.split(",");
        ArrayList<String> newIps = new ArrayList<String>(Arrays.asList(newIpsArray));


        //now validate that all of the received IPs are valid IP addresses - if they are not we should return null object (to indicate something went wrong)
        boolean allIpsValid = true;
        System.out.println("The parsed array list is: " + newIps.toString());
        for (int k = 0; k < newIps.size(); k++) {
            InetAddressValidator inetValidator = InetAddressValidator.getInstance();
            boolean ipIsValid = inetValidator.isValidInet4Address(newIps.get(k));
            if (!ipIsValid) {
                allIpsValid = false;
            }
        }
        if (!allIpsValid) {
            newIps = null;
            System.out.println("Not all IPs are valid so the returned object will be null!");
        }

        return newIps;
    }


    //this method uses the kubernetes client (which is of course already connected to the right cluster) to obtain the IP of host machine where a Pod with given
    //name is running.
    //TODO: this method is "provider specific" - which means that results will depend on cluster provider. See also comments below in the code. To make it "provider-generic"
    //we should invest more effort (and I do not know if it is possible at all). More here - https://github.com/kubernetes/kubernetes/issues/9267 - citing:
    //Only Openstack and AWS set InternalIP. Openstack and AWS also set ExternalIP, as does GCE. LegacyHostIP is set in a variety of ways in different cloudprovider
    //implementations. AWS sets LegacyHostIP to the InternalIP. In the case of Mesos, Ovirt, Vagrant, and Rackspace, they only set LegacyHostIP. In the case of no
    //cloudprovider, the --hostname-override flag in Kubelet sets LegacyHostIP. In most of these cases, we don't know whether the LegacyHostIP is internal or external.
    //TODO: could be made more efficient - instead of looping through the lists....
    protected static String getPodHostExternalIP(KubernetesClient kubecl, String podName) {
        String publicallyRoutableIP = null;
        //first check if the pod with this name exists
        if (kubecl.pods().withName(podName).get() != null) {
            //then check if the pod is in the phase "running" - if not then there is no point returning the IP - and we will rather return null
            if (kubecl.pods().withName(podName).get().getStatus().getPhase().equals("Running")) {
                //obtain the IP where the pod is running. But depending on the cluster this will return different things. In GCP this will return the private IP of the host
                //machine (they have private networking besides public). In Arnes this will return public IP of host machine (because on Arnes we cannot configure private
                //network) - and so on. So it is "provider specific" again...
                String hostIP = kubecl.pods().withName(podName).get().getStatus().getHostIP();

                //OK - we need to find public IP of the host machine where the pod is running. Obviously we cannot get this info from the "Pod". So we need to take the IP
                //that we obtained above and try to find it in the metadata information about all the nodes constituting the cluster. When we find it - then we can try to
                //obtain the public IP from the list.
                List<Node> nodes = kubecl.nodes().list().getItems();
                for (int i = 0; i < nodes.size(); i++) {
                    Node node = (Node) nodes.get(i);
                    //Each node in the cluster can have many addresses associated. These are of various Types - The official documentation http://kubernetes.io/docs/admin/node/#addresses
                    //says that the Types could be Hostname, ExternalIP or InternalIP. However Arnes rather returns the type LegacyHostIP for all the addresses associated with node.
                    //Here you can read that if this happens the cluster is actually misconfigured - https://github.com/kubernetes/kubernetes/issues/9267
                    List<NodeAddress> nodeAdresses = node.getStatus().getAddresses();
                    for (int k = 0; k < nodeAdresses.size(); k++) {
                        NodeAddress nodeAddress = nodeAdresses.get(k);
                        String curAdressValue = nodeAddress.getAddress();

                        //if the current address equals the address that we obtained from the pod - then immediately loop through the list of addresses again and try to find
                        //an address with type of ExternalIP that belongs to this node. But if the address of type ExternalIP cannot be found then tra to find the
                        //address of type LegacyHostIP
                        if (curAdressValue.equals(hostIP)) {
                            String externalIP = null;
                            String legacyHostIP = null;

                            for (int j = 0; j < nodeAdresses.size(); j++) {
                                NodeAddress nAddress = nodeAdresses.get(j);
                                String cAdressType = nAddress.getType();
                                String cAdressValue = nAddress.getAddress();
                                if (cAdressType.equals("LegacyHostIP")) {
                                    legacyHostIP = cAdressValue;
                                } else if (cAdressType.equals("ExternalIP")) {
                                    externalIP = cAdressValue;
                                }
                            }
                            if (externalIP != null) {
                                publicallyRoutableIP = externalIP;
                            } else if (legacyHostIP != null) {
                                publicallyRoutableIP = legacyHostIP;
                            } else {
                                //just do nothing and we will return the null value in publicallyRoutableIP
                            }
                            break;
                        }

                    }
                    if (publicallyRoutableIP != null) {
                        break;
                    }
                }
            }
        }
        return publicallyRoutableIP;
    }

}
