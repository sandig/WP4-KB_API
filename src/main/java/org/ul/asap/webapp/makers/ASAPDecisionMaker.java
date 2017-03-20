package org.ul.asap.webapp.makers;

/**
 * Created by mcigale on 6. 03. 2017.
 */

import java.util.ArrayList;
import java.util.List;

// THIS CLASS NEEDS TO BE UPDATED, JUST FOR TESTING
public class ASAPDecisionMaker {



    private static ArrayList<String> getMetricNamesFromKB(String AppName)
    {
        //Placeholder. Just dummy data. But could work. This will be in KB
        ArrayList<String> metricNames = new ArrayList<String>();
        switch(AppName) {
            case "MCU":
                metricNames.add("RTT");
                metricNames.add("hopCount");
                metricNames.add("memFree");
                metricNames.add("cpuNum");
                metricNames.add("diskFree");
                break;
            default:

    }

        return metricNames;
    }

    private static ArrayList<Double> getModelFromKB(String AppName)
    {
        //Placeholder. Just dummy data. But could work. This will be in KB
        ArrayList<Double> model = new ArrayList<Double>();
        switch(AppName) {
            case "MCU":
                model.add(0.9);
                model.add(-0.8);
                model.add(0.3);
                model.add(0.2);
                model.add(0.2);
                break;
            case "test":
                model.add(-0.1);
                model.add(-0.2);
                model.add(0.5);
                model.add(0.3);
                model.add(0.4);
           }
        return model;
    }


    private static List<String> getClustetrID()
    {
        //Placeholder. Just dummy data. But could work. This will be in KB?
        List<String> clusterID = new ArrayList<String>();
        //TODO: Uroš and me will return clusters from KB or TOSCA
        clusterID.add("Arnes");
        clusterID.add("GoogleWest");
        clusterID.add("GoogleAsia");
        return clusterID;
    }

    private static ArrayList<Integer> getMetricOrder(ArrayList<Double> model)
    {
        //This curves my brains... I cant get this to a logical manner... Fuck.
        //But this seems to work. Good enough for today.

        ArrayList<Integer> orderMap = new ArrayList<Integer>();
           for (int i = 0; i < model.size(); i++) {
            Double aDouble = model.get(i);
            for (Integer anInteger : orderMap) {
                if (Math.abs(model.get(anInteger)) > Math.abs(aDouble))
                {
                  //don't ask...
                }
                else
                {
                    orderMap.add(orderMap.indexOf(anInteger),i);
                    break;
                }
            }
            if(orderMap.indexOf(i) < 0)
            orderMap.add(model.indexOf(aDouble));
        }
        return orderMap;
    }



    public static ASAPClusterMetric compareClusters(ASAPClusterMetric acm1, ASAPClusterMetric acm2, ArrayList<Integer> order, ArrayList<Double> model)
    {
        //Grade acm1 BS. But i guess it's ok.
        Integer MostRelevantMetric = order.get(0);

        if((model.get(MostRelevantMetric) * acm1.clusterMetrics.get(MostRelevantMetric)) > (model.get(MostRelevantMetric) * acm2.clusterMetrics.get(MostRelevantMetric)))
        {return acm1; }
        else if ((model.get(MostRelevantMetric) * acm1.clusterMetrics.get(MostRelevantMetric)) < (model.get(MostRelevantMetric) * acm2.clusterMetrics.get(MostRelevantMetric)))
        {return acm2; }
        else
        {
            if (order.size() > 0)
            {
                ArrayList<Integer> tempOrder = order;
                tempOrder.remove(0);
                ASAPClusterMetric Temp = compareClusters(acm1,acm2,tempOrder,model);
                return Temp;
            }
            else
            {
                return acm1;
                //They are identical as far as collected metrics are concerned. So might as well return acm1.
            }
        }
    }

    public static void main(String[] args){
        String ID = makeDecision("test","IP");

    }
    public static String makeDecision(String appName, String ip)
    {
        ArrayList<String> metricNames = getMetricNamesFromKB(appName);
        List<String> clustersID = getClustetrID();
        ArrayList<Double> model = getModelFromKB(appName);
        ArrayList<Integer> order = getMetricOrder(model);

        //This should be a loop, but I just had a mind-bending session with a loop that I do not care to reproduce.
        //So thiss will have to wait for the end of review.

        ASAPClusterMetric Cluster1 = new ASAPClusterMetric(clustersID.get(0),metricNames);
        ASAPClusterMetric Cluster2 = new ASAPClusterMetric(clustersID.get(1),metricNames);
        ASAPClusterMetric Cluster3 = new ASAPClusterMetric(clustersID.get(2),metricNames);

        ASAPClusterMetric TempCluster = compareClusters(Cluster1,Cluster2,order,model);
        ASAPClusterMetric bestCluster = compareClusters(TempCluster,Cluster3,order,model);



        System.out.printf(bestCluster.clusterID);
        return bestCluster.clusterID;
    }
}
