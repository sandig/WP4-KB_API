package org.ul.asap.webapp.mcu.test;

import io.fabric8.kubernetes.api.model.AuthInfo;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import io.fabric8.kubernetes.client.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ConfigSelector extends io.fabric8.kubernetes.api.model.Config implements Iterable<Map.Entry<String, Config>> {

    private final Map<String, Config> mapContextNameToConfig;

    public ConfigSelector(io.fabric8.kubernetes.api.model.Config config) {
        super(config.getApiVersion(), config.getClusters(), config.getContexts(), config.getCurrentContext(),
                config.getExtensions(), config.getKind(), config.getPreferences(), config.getUsers());

        mapContextNameToConfig = new HashMap<>();

        for (NamedContext context : config.getContexts())
            mapContextNameToConfig.put(context.getName(), createConfig(context.getName(), config));
    }

    public Config configForContext(String contextName) {
        return mapContextNameToConfig.get(contextName);
    }

    private static Config createConfig(String contextName, io.fabric8.kubernetes.api.model.Config config) {
        String prevContextName = config.getCurrentContext();

        config.setCurrentContext(contextName);
        Context currentContext = KubeConfigUtils.getCurrentContext(config);
        Cluster currentCluster = KubeConfigUtils.getCluster(config, currentContext);

        Config conf = new Config();

        if (currentCluster != null) {
            conf.setMasterUrl(currentCluster.getServer());
//            conf.setNamespace(currentContext.getNamespace());
            conf.setNamespace("default");   // mandatory
            conf.setTrustCerts(currentCluster.getInsecureSkipTlsVerify() != null && currentCluster.getInsecureSkipTlsVerify().booleanValue());
            conf.setCaCertFile(toAbsolutePath(currentCluster.getCertificateAuthority()));
            conf.setCaCertData(currentCluster.getCertificateAuthorityData());
            AuthInfo currentAuthInfo = KubeConfigUtils.getUserAuthInfo(config, currentContext);
            if (currentAuthInfo != null) {
                conf.setClientCertFile(toAbsolutePath(currentAuthInfo.getClientCertificate()));
                conf.setClientCertData(currentAuthInfo.getClientCertificateData());
                conf.setClientKeyFile(toAbsolutePath(currentAuthInfo.getClientKey()));
                conf.setClientKeyData(currentAuthInfo.getClientKeyData());
                conf.setOauthToken(currentAuthInfo.getToken());
                conf.setUsername(currentAuthInfo.getUsername());
                conf.setPassword(currentAuthInfo.getPassword());
                if (Utils.isNullOrEmpty(conf.getOauthToken())
                        && currentAuthInfo.getAuthProvider() != null
                        && ! Utils.isNullOrEmpty((String) currentAuthInfo.getAuthProvider().getConfig().get("access-token"))) {
                    conf.setOauthToken((String) currentAuthInfo.getAuthProvider().getConfig().get("access-token"));
                }
                conf.getErrorMessages().put(Integer.valueOf(401), "Unauthorized! Token may have expired! Please log-in again.");
                conf.getErrorMessages().put(Integer.valueOf(403), "Forbidden! User " + currentContext.getUser() + " doesn\'t have permission.");
            }
        }
        config.setCurrentContext(prevContextName);

        return conf;
    }

    private static String toAbsolutePath(String filename) {
        return (filename == null) ? null : new File(filename).getAbsolutePath();
    }

    @Override
    public Iterator<Map.Entry<String, Config>> iterator() {
        return mapContextNameToConfig.entrySet().iterator();
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<String, Config>> action) {
        for (Map.Entry<String, Config> entry : mapContextNameToConfig.entrySet())
            action.accept(entry);
    }

    @Override
    public Spliterator<Map.Entry<String, Config>> spliterator() {
        return mapContextNameToConfig.entrySet().spliterator();
    }
}
