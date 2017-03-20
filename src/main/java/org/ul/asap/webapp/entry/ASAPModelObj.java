package org.ul.asap.webapp.entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ASAPModelObj {

    private int responseTime;
    private int numberOfHops;
    private int freeRam;
    private int numberOfCores;
    private int freeCPU;
    private int starRating;

    public ASAPModelObj(){

    }

    public ASAPModelObj(int responseTime, int numberOfHops, int freeRam, int numberOfCores, int freeCPU, int starRating) {
        this.responseTime = responseTime;
        this.numberOfHops = numberOfHops;
        this.freeRam = freeRam;
        this.numberOfCores = numberOfCores;
        this.freeCPU = freeCPU;
        this.starRating = starRating;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public int getNumberOfHops() {
        return numberOfHops;
    }

    public void setNumberOfHops(int numberOfHops) {
        this.numberOfHops = numberOfHops;
    }

    public int getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(int freeRam) {
        this.freeRam = freeRam;
    }

    public int getNumberOfCores() {
        return numberOfCores;
    }

    public void setNumberOfCores(int numberOfCores) {
        this.numberOfCores = numberOfCores;
    }

    public int getFreeCPU() {
        return freeCPU;
    }

    public void setFreeCPU(int freeCPU) {
        this.freeCPU = freeCPU;
    }

    public int getStarRating() {
        return starRating;
    }

    public void setStarRating(int starRating) {
        this.starRating = starRating;
    }

    public ASAPModelObj diff(ASAPModelObj asapModelObj) {
        return new ASAPModelObj(this.getResponseTime() - asapModelObj.getResponseTime(),
                this.getNumberOfHops() - asapModelObj.getNumberOfHops(),
                this.getFreeRam() - asapModelObj.getFreeRam(),
                this.getNumberOfCores() - asapModelObj.getNumberOfCores(),
                this.getFreeCPU() - asapModelObj.getFreeCPU(),
                this.getStarRating() - asapModelObj.getStarRating());
    }
}
