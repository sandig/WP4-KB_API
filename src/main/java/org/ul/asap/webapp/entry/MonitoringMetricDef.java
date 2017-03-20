package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
public class MonitoringMetricDef {
   private String agentid;
    private String metricid;
    private String is_sub;
    private String mgroup;
    private String name;
    private String type;
    private String units;

    public MonitoringMetricDef(){

    }

    public MonitoringMetricDef(String agentid, String metricid, String is_sub, String mgroup, String name, String type,
                               String units) {
        this.agentid = agentid;
        this.metricid = metricid;
        this.is_sub = is_sub;
        this.mgroup = mgroup;
        this.name = name;
        this.type = type;
        this.units = units;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getMetricid() {
        return metricid;
    }

    public void setMetricid(String metricid) {
        this.metricid = metricid;
    }

    public String getIs_sub() {
        return is_sub;
    }

    public void setIs_sub(String is_sub) {
        this.is_sub = is_sub;
    }

    public String getMgroup() {
        return mgroup;
    }

    public void setMgroup(String mgroup) {
        this.mgroup = mgroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
