package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterResponse {

    private String masterUrl; // https://194.249.0.45
    private String clientPublicCred; // the client public credential
    private String clientPrivateCred; // the client private credential
    private String credentialsType; // PASSWORD
    private String certificateAuthority; // the caCert

    public ClusterResponse(){

    }

    public ClusterResponse(String masterUrl, String clientPublicCred, String clientPrivateCred, String
            credentialsType, String certificateAuthority) {
        this.masterUrl = masterUrl;
        this.clientPublicCred = clientPublicCred;
        this.clientPrivateCred = clientPrivateCred;
        this.credentialsType = credentialsType;
        this.certificateAuthority = certificateAuthority;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getClientPublicCred() {
        return clientPublicCred;
    }

    public void setClientPublicCred(String clientPublicCred) {
        this.clientPublicCred = clientPublicCred;
    }

    public String getClientPrivateCred() {
        return clientPrivateCred;
    }

    public void setClientPrivateCred(String clientPrivateCred) {
        this.clientPrivateCred = clientPrivateCred;
    }

    public String getCredentialsType() {
        return credentialsType;
    }

    public void setCredentialsType(String credentialsType) {
        this.credentialsType = credentialsType;
    }

    public String getCertificateAuthority() {
        return certificateAuthority;
    }

    public void setCertificateAuthority(String certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }
}
