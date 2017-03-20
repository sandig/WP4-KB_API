package org.ul.asap.webapp.mcu;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DecisionMakerInput {
    private String appType; //this identifies the application type. Because there could be an infinite number of apps - this is not an enumerated type. But how does the decision maker module know about the value of these strings - I have no idea
    private List<String> clientIps; //this containes the list of client IPs (so decision maker can Search Results strategically place the server)

    //no arg constructor needed for JAXB
    public DecisionMakerInput() {
    }

    public DecisionMakerInput(String appType, List<String> clientIps) {
        this.appType = appType;
        this.clientIps = clientIps;
    }

    public String getAppType() {
        return this.appType;
    }

    public List<String> getClientIps() {
        return this.clientIps;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public void setClientIps(List<String> clientIps) {
        this.clientIps = clientIps;
    }
}
