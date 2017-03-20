package org.ul.asap.webapp.mcu.test;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by uros on 12.3.2017.
 */
public interface ConfigProvider extends ResourceProvider<Config> {
    static final Object obj = null;

    enum DefaultSources {
        SystemPropertyFile      ( System.getProperty("kubernetes.config.file") ),
        EnvironmentVariable     ( System.getenv("KUBECONFIG") ),
        LocalConfigFile         ( ConfigProvider.class.getResource("kube-config.yaml").getFile() ),
        GlobalConfigFile        ( Paths.get(System.getenv("HOME"), ".kube", "config").toString() );

        private final String resourcePath;

        DefaultSources(String s) {
            resourcePath = s;
        }

        public String getResourcePath() {
            return resourcePath;
        }
    }

    public default Config retrieveResource() {
        System.out.println();
        final Optional<DefaultSources> source =
                Arrays.stream(DefaultSources.values())
                        .filter(path -> path.getResourcePath() != null && new File(path.getResourcePath()).exists())
                        .findFirst();
        if (! source.isPresent())
            return new Config();

        System.err.printf("Using config from %s.%n", source.get().getResourcePath());

        final File kubeConfigFile = new File(source.get().getResourcePath());

        Config config = null;
        try {
            config = KubeConfigUtils.parseConfig(kubeConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new Config();
        }
        return config;
    }
}
