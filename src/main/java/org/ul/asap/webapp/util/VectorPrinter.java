package org.ul.asap.webapp.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

/**
 * Created by mcigale on 12. 01. 2017.
 */
public class VectorPrinter {

    public void printValues(Vector<Vector<Double>> MetricValues)
    {

        System.out.println();
        for (Vector<Double> MeasurementInstance : MetricValues) {
            for (Double Value : MeasurementInstance) {
                System.out.printf(" %.2f \t", Value );
            }
            System.out.println();
        }

    }

    public void printDoubleValues(Vector<Double> MeasurementInstance)
    {

          for (Double Value : MeasurementInstance) {

              System.out.printf(" %,2f \t", Value );
            }
            System.out.println();

    }
    
    public void printStringValues(Vector<String> MeasurementInstance)
    {

        for (String Value : MeasurementInstance) {
            System.out.printf(Value + " \t");
        }
        System.out.println();

    }

    public void printIntValues(Vector<Integer> MeasurementInstance)
    {

        for (Integer Value : MeasurementInstance) {
            System.out.printf(Value + " \t");
        }
        System.out.println();

    }

    public void storeVectorValues(Vector<Vector<Double>> MetricValues, String Filename)
    {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(Filename, true), "utf-8"))) {
            for (Vector<Double> MeasurmentInstance : MetricValues) {
                for (Double Value : MeasurmentInstance) {
                    String s = String.format("%.2f\t", Value);
                    writer.write(s);
                }
                writer.newLine();
            }
        }
        catch (Exception e)
        {}
    }

    public void storeDoubleValues(Vector<Double> MeasurementInstance, String Filename)
    {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(Filename, true), "utf-8"))) {
                for (Double Value : MeasurementInstance) {
                    String s = String.format("%.2f\t", Value);
                    writer.write(s);
                }
                writer.newLine();
        }
        catch (Exception e)
        {}
    }
    public void storeStringValues(Vector<String> MeasurementInstance, String Filename)
    {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(Filename, true), "utf-8"))) {
            for (String Value : MeasurementInstance) {
                String s = String.format(Value + "\t");
                writer.write(s);
            }
            writer.newLine();
        }
        catch (Exception e)
        {}
    }


}
