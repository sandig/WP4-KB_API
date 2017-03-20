package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
public class MonitoringMetricValue implements ASAPEntry {
    private String metricid;
    private String event_date;
    private UUID event_timestamp;
    private String mgroup;
    private String name;
    private String type;
    private String units;
    private String value;

    public MonitoringMetricValue(){

    }

    public MonitoringMetricValue(String metricid, String event_date, UUID event_timestamp, String mgroup, String
            name, String type, String units, String value) {
        this.metricid = metricid;
        this.event_date = event_date;
        this.event_timestamp = event_timestamp;
        this.mgroup = mgroup;
        this.name = name;
        this.type = type;
        this.units = units;
        this.value = value;
    }

    public String getMetricid() {
        return metricid;
    }

    public void setMetricid(String metricid) {
        this.metricid = metricid;
    }

    public String getEvent_date() {
        return event_date;
    }

    public void setEvent_date(String event_date) {
        this.event_date = event_date;
    }

    public UUID getEvent_timestamp() {
        return event_timestamp;
    }

    public void setEvent_timestamp(UUID event_timestamp) {
        this.event_timestamp = event_timestamp;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
