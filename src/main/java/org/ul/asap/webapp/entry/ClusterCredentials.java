package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClusterCredentials extends MyEntry implements ASAPEntry {

    private CredentialsType credType; //enumerated type "password" or "certificates"
    private String masterURL; // contains the URL of the Kuberentes API server that is located on the Master host.
    private String clientPublicCredentials; //contains username or clientCert (the public part of the client credentials) -
    // depending on the type of credentials used for login
    private String clientPrivateCredentials; // contains password of clientKey (the private part of the client credentials)
    // - depending on the type of credentials used for login
    private String certificateAuthority; //contains the public key of the certificate authority that was used to sign client
    // credentials.

//    private CredType credType; //enumerated type "password" or "certificates"
//    private String masterUrl; //string  , this containes the URL of the Kuberentes API server that is located on the Master host.
//    private String clientPublicCred; //string , this contains username or clientCert (the public part of the client credentials) - depending on the type of credentials used for login
//    private String clientPrivateCred; //string , this contains password of clientKey (the private part of the client credentials) - depending on the type of credentials used for login
//    private String caCert; //string, this contains the public key of the certificate authority that was used to sign client credentials.


    //no arg constructor needed for JAXB
    public ClusterCredentials() {
        super("id");
    }

    public ClusterCredentials(String id) {
        super(id);
    }

    public ClusterCredentials(String id, CredentialsType credType, String masterUrl, String clientPublicCred, String
            clientPrivateCred, String certificateAuthority) {
        super(id);
        this.credType = credType;
        this.masterURL = masterUrl;
        this.clientPublicCredentials = clientPublicCred;
        this.clientPrivateCredentials = clientPrivateCred;
        this.certificateAuthority = certificateAuthority;
    }

    public CredentialsType getCredType() {
        return credType;
    }

    public void setCredType(CredentialsType credType) {
        this.credType = credType;
    }

    public String getMasterURL() {
        return masterURL;
    }

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public String getClientPublicCredentials() {
        return clientPublicCredentials;
    }

    public void setClientPublicCredentials(String clientPublicCredentials) {
        this.clientPublicCredentials = clientPublicCredentials;
    }

    public String getClientPrivateCredentials() {
        return clientPrivateCredentials;
    }

    public void setClientPrivateCredentials(String clientPrivateCredentials) {
        this.clientPrivateCredentials = clientPrivateCredentials;
    }

    public String getCertificateAuthority() {
        return certificateAuthority;
    }

    public void setCertificateAuthority(String certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }
}
