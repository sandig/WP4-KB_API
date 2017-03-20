package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
public class Agent {

    private String agentID;
    private String agentIP;
    private String agentName;
    private String status;
    private String tags;
    private UUID tstart;
    private UUID tstop;

    public Agent() {
    }

    public Agent(String agentID, String agentIP, String agentName, String status, String tags, UUID tstart, UUID
            tstop) {
        this.agentID = agentID;
        this.agentIP = agentIP;
        this.agentName = agentName;
        this.status = status;
        this.tags = tags;
        this.tstart = tstart;
        this.tstop = tstop;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getAgentIP() {
        return agentIP;
    }

    public void setAgentIP(String agentIP) {
        this.agentIP = agentIP;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public UUID getTstart() {
        return tstart;
    }

    public void setTstart(UUID tstart) {
        this.tstart = tstart;
    }

    public UUID getTstop() {
        return tstop;
    }

    public void setTstop(UUID tstop) {
        this.tstop = tstop;
    }
}
