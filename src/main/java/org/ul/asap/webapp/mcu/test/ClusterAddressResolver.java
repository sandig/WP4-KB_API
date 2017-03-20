package org.ul.asap.webapp.mcu.test;

import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Obtain external IPs of worker nodes from Kubernetes clusters.
 * NodeAddress::getType determines the type of the IP address. It can be set to one of the following:
 * <ul>
 *     <li>LegacyHostIP</li>
 *     <li>InternalIP</li>
 *     <li>ExternalIP</li>
 *     <li>Hostname</li>
 * </ul>
 *
 * The meaning of these parameters as well as which of those are set depends on the cloud provider. Thus, there is no
 * standard way to read an external IP from. Only some providers, e.g. OpenStack and AWS (Amazon Web Services) even set
 * InternalIP, while they also set ExternalIP, as does GCE (Google Cloud Engine). In some cases, LegacyHostIP
 * could mean external IP address while in others it is not even present. However, LegacyHostIP is marked for
 * deprecation and will be removed in future versions of Kubernetes API.
 *
 * @author uros
 */
public class ClusterAddressResolver {

    /**
     * Default ping timeout in milliseconds.
     * @see #filterReachable(int)
     */
    public static final int DEFAULT_PING_TIMEOUT = 5000; // i.e. 5s

    // The order of address types to look for an external ip.
    private static final List<String> ADDRESS_TYPE_ORDER = Arrays.asList("ExternalIP", "LegacyHostIP", "Hostname", "InternalIP");

    private final ConfigSelector configSelector;
    private final Map<String, List<NodeAddress>> mapContextsToExternalIPs;

    //FIXME: The same cluster node could appear in many contexts. Thus, the values should be lists of strings (context names).
    private final Map<String, String> mapExternalIPtoContext;

    /**
     * Constructor for ClusterAddressResolver.
     * @param configSelector - An object containing all possible configurations.
     */
    public ClusterAddressResolver(final ConfigSelector configSelector) {
        this.configSelector = configSelector;
        mapContextsToExternalIPs = new HashMap<>();

        init();

        mapExternalIPtoContext = new HashMap<>();
        mapContextsToExternalIPs.forEach((context, addrs) -> addrs.forEach(addr -> mapExternalIPtoContext.put(addr.getAddress(), context)));
    }

    /**
     * For a given context name, retrieve a list of nodes with external IPs.
     * @param contextName - Context name, grouping together cluster and authorization information.
     * @return - List of node addresses of the context with external IPs.
     */
    public List<NodeAddress> getNodeAddressList(String contextName) {
        return mapContextsToExternalIPs.get(contextName);
    }

    /**
     * For a given host name or address, retrieve the context it appears in.
     * @param host - IP address or hostname of the node in a cluster.
     * @return - Context name
     */
    public String getContextName(String host) {
        //FIXME: This method only returns a single context name while the same cluster node could appear in many contexts!
        return mapExternalIPtoContext.get(host);
    }

    private void init() {
        for (final NamedContext context : configSelector.getContexts()) {
            final String contextName = context.getName();
            final Config config = configSelector.configForContext(contextName);
            final KubernetesClient client = new DefaultKubernetesClient(config);

            for (final Node node : client.nodes().list().getItems()) {
                final Optional<NodeAddress> externalIP = node.getStatus().getAddresses()
                        .stream()
                        .min((address1, address2) -> {
                            int idxAddress1 = ADDRESS_TYPE_ORDER.indexOf(address1.getType());
                            int idxAddress2 = ADDRESS_TYPE_ORDER.indexOf(address2.getType());
                            if (idxAddress1 < 0) idxAddress1 = Integer.MAX_VALUE;
                            if (idxAddress2 < 0) idxAddress2 = Integer.MAX_VALUE;
                            return idxAddress1 - idxAddress2;
                        });

                if (externalIP.isPresent()) {
                    final List<NodeAddress> currentIPs = mapContextsToExternalIPs.getOrDefault(contextName, new ArrayList<>());
                    currentIPs.add(externalIP.get());
                    mapContextsToExternalIPs.put(contextName, currentIPs);
                }
            }
        }
    }

    /**
     * Remove unreachable cluster nodes from the mapping by performing ping against all the nodes.
     * @param timeout - Timeout for the ping command.
     */
    public void filterReachable(int timeout) {
        mapContextsToExternalIPs.values()
                .forEach(addr -> addr
                        .parallelStream()
                        .filter(ip -> isReachable(ip, timeout))
                        .collect(Collectors.toList()));
    }

    /**
     * Perform <code>filterReachable</code> with the default timeout.
     * @see #filterReachable(int)
     * @see #DEFAULT_PING_TIMEOUT
     */
    public void filterReachable() {
        filterReachable(DEFAULT_PING_TIMEOUT);
    }

    /**
     * Test whether address is reachable within specified timeout.
     * @param address - Address of the node.
     * @param timeout - Time constraint for the ping.
     * @return - True if node on the given address is reachable and false otherwise.
     */
    public static boolean isReachable(final NodeAddress address, final int timeout) {
        try {
            final boolean reachable = InetAddress.getByName(address.getAddress()).isReachable(timeout);
            System.err.printf("%s is reachable: %b%n", address.getAddress(), reachable);
            return reachable;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
