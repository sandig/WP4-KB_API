package org.ul.asap.webapp.makers;

import java.util.ArrayList;


/**
 * A class that keeps clusterID connected to the Metrics and metrics Names.
 * It also takes care of reading data from TSDB. (For no particular reason.)
 * Fields are public, because reasons.
 *
 * @author Matej Cigale
 */
public class ASAPClusterMetric {
    public String clusterID;
    public ArrayList<Double> clusterMetrics;
    public ArrayList<String> metricNames;
    public Integer numberOfMetrics;


    //Constructors
    public ASAPClusterMetric(String ClusterIDInput, ArrayList<String> MetricNamesInput) {
        clusterID = ClusterIDInput;
        metricNames = MetricNamesInput;
        numberOfMetrics = metricNames.size();
        clusterMetrics = getMetricsFromCluster(clusterID, metricNames);
    }

    //Public functions
    public Integer size() {
        return numberOfMetrics;
    }


    //Helper functions.
    private ArrayList<Double> getMetricsFromCluster(String ClusterID, ArrayList<String> MetricNames) {
        ArrayList<Double> MetricData = new ArrayList<Double>();
        //Placeholder. Just dummy data. But could work. This will be in TSDB?
        /*
        Metric names - in this dummy set.
        "RTT"
        "hopCount"
        "memFree"
        "cpuNum"
        "diskFree"
        */
        switch (ClusterID) {
            case "Arnes":
                MetricData.add(2.0);
                MetricData.add(5.0);
                MetricData.add(1000.0);
                MetricData.add(1.0);
                MetricData.add(1000.0);
                break;
            case "GoogleWest":
                MetricData.add(200.0);
                MetricData.add(25.0);
                MetricData.add(2000.0);
                MetricData.add(2.0);
                MetricData.add(10000.0);
                break;
            case "GoogleAsia":
                MetricData.add(200.0);
                MetricData.add(35.0);
                MetricData.add(2500.0);
                MetricData.add(2.0);
                MetricData.add(10000.0);
                break;
            default:
                MetricData.add(1000.0);
                MetricData.add(200.0);
                MetricData.add(500.0);
                MetricData.add(0.0);
                MetricData.add(0.0);
        }

        return MetricData;
    }


}
