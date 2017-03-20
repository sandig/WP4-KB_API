package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ModelMakerObj {

    private String rtt;
    private String hopCount;
    private String ram;
    private String noCores;
    private String architecture;
    private String starRating;

    public ModelMakerObj(){

    }

    public ModelMakerObj(String rtt, String hopCount, String ram, String noCores, String architecture, String starRating) {
        this.rtt = rtt;
        this.hopCount = hopCount;
        this.ram = ram;
        this.noCores = noCores;
        this.architecture = architecture;
        this.starRating = starRating;
    }

    public String getRtt() {
        return rtt;
    }

    public void setRtt(String rtt) {
        this.rtt = rtt;
    }

    public String getHopCount() {
        return hopCount;
    }

    public void setHopCount(String hopCount) {
        this.hopCount = hopCount;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getNoCores() {
        return noCores;
    }

    public void setNoCores(String noCores) {
        this.noCores = noCores;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getStarRating() {
        return starRating;
    }

    public void setStarRating(String starRating) {
        this.starRating = starRating;
    }
}
