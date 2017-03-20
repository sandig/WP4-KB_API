package org.ul.asap.webapp.mcu.test; /**
 * Created by uros on 19.12.2016.
 */

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        ConfigProvider configProvider = new ConfigProvider() {
        }; // implements an empty interface
        Config config = configProvider.retrieveResource();

        ConfigSelector configSelector = new ConfigSelector(config);
        ClusterAddressResolver resolver = new ClusterAddressResolver(configSelector);

        // Optional -- perform ping against clusters nodes and preserve reachable nodes only
        resolver.filterReachable();

        // Select config
        // To retrieve all possible values for configForContext(), use configSelector.getContexts() or simply iterate over it.
        KubernetesClient client = new DefaultKubernetesClient(configSelector.configForContext("arnes"));

        // Read MCU pod from yaml.
//        String mcuPodPath = Main.class.getResource("mcu001.yaml").getPath();
        String mcuPodPath = new File("local test path").toString();
        try (InputStream inputStream = (new FileInputStream(mcuPodPath))) {
            List<HasMetadata> metaList = client.load(inputStream).get();
            if (metaList != null && metaList.size() > 0 && metaList.get(0) instanceof Pod) {
                    Pod pod = (Pod) metaList.get(0);
                    System.out.println(pod);

                    // To add additional fields to pod, start with builder:
                    PodBuilder pb = new PodBuilder(pod);
                    pb.build();
            }
        }
    }
}
