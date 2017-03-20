package org.ul.common.monitoring;

import org.ul.entice.webapp.entry.MyEntry;

public class MonitoringMetric extends MyEntry {

    // (seconds)
    private int collectingInterval;
    private String name;
    private String group;
    private String unit;
    private String dataType;
    private double upperLimit;
    private double lowerLimit;
    private double threshold;
    /*
    = equals
    ≠ not equal to
    > greater than
    < less than
    ≥ greater than or equal to
    ≤ less than or equal to
     */
    private String relationalOperator;

    public MonitoringMetric(String id, int collectingInterval, String name, String group, String unit, String dataType,
                            double upperLimit, double lowerLimit, double threshold, String relationalOperator) {
        super(id);
        this.collectingInterval = collectingInterval;
        this.name = name;
        this.group = group;
        this.unit = unit;
        this.dataType = dataType;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.threshold = threshold;
        this.relationalOperator = relationalOperator;
    }

    public MonitoringMetric() {
        super("");
    }

    public int getCollectingInterval() {
        return collectingInterval;
    }

    public void setCollectingInterval(int collectingInterval) {
        this.collectingInterval = collectingInterval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getRelationalOperator() {
        return relationalOperator;
    }

    public void setRelationalOperator(String relationalOperator) {
        this.relationalOperator = relationalOperator;
    }
}
