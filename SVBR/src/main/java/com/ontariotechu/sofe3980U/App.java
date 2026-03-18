package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.*;
import com.opencsv.*;

public class App 
{
    static class Metrics {
        String name;
        float bce, accuracy, precision, recall, f1, auc;
        int TP, FP, FN, TN;
    }

    public static void main(String[] args)
    {
        Metrics m1 = evaluate("model_1.csv");
        Metrics m2 = evaluate("model_2.csv");
        Metrics m3 = evaluate("model_3.csv");

        Metrics[] models = {m1, m2, m3};

        Metrics bestBCE = m1;
        Metrics bestAcc = m1;
        Metrics bestPrec = m1;
        Metrics bestRecall = m1;
        Metrics bestF1 = m1;
        Metrics bestAUC = m1;

        for (Metrics m : models) {
            if (m.bce < bestBCE.bce) bestBCE = m;
            if (m.accuracy > bestAcc.accuracy) bestAcc = m;
            if (m.precision > bestPrec.precision) bestPrec = m;
            if (m.recall > bestRecall.recall) bestRecall = m;
            if (m.f1 > bestF1.f1) bestF1 = m;
            if (m.auc > bestAUC.auc) bestAUC = m;
        }

        System.out.println("According to BCE, The best model is " + bestBCE.name);
        System.out.println("According to Accuracy, The best model is " + bestAcc.name);
        System.out.println("According to Precision, The best model is " + bestPrec.name);
        System.out.println("According to Recall, The best model is " + bestRecall.name);
        System.out.println("According to F1 score, The best model is " + bestF1.name);
        System.out.println("According to AUC ROC, The best model is " + bestAUC.name);
    }

    public static Metrics evaluate(String filePath)
    {
        Metrics m = new Metrics();
        m.name = filePath;

        FileReader filereader;
        List<String[]> allData;

        try{
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        }
        catch(Exception e){
            System.out.println("Error reading the CSV file");
            return null;
        }

        float bce = 0;
        int TP = 0, FP = 0, FN = 0, TN = 0;
        List<float[]> rocData = new ArrayList<>();

        for (String[] row : allData) {
            int y_true = Integer.parseInt(row[0]);
            float y_prob = Float.parseFloat(row[1]);

            // Avoid log(0)
            y_prob = Math.max(1e-7f, Math.min(1 - 1e-7f, y_prob));

            // BCE
            bce += -(y_true * Math.log(y_prob) + (1 - y_true) * Math.log(1 - y_prob));

            // Threshold = 0.5
            int y_pred = (y_prob >= 0.5) ? 1 : 0;

            if (y_true == 1 && y_pred == 1) TP++;
            else if (y_true == 0 && y_pred == 1) FP++;
            else if (y_true == 1 && y_pred == 0) FN++;
            else TN++;

            rocData.add(new float[]{y_prob, y_true});
        }

        int n = allData.size();
        bce /= n;

        float accuracy = (float)(TP + TN) / n;
        float precision = (float)TP / (TP + FP);
        float recall = (float)TP / (TP + FN);
        float f1 = 2 * precision * recall / (precision + recall);
        float auc = computeAUC(rocData);

        // store
        m.bce = bce;
        m.accuracy = accuracy;
        m.precision = precision;
        m.recall = recall;
        m.f1 = f1;
        m.auc = auc;
        m.TP = TP;
        m.FP = FP;
        m.FN = FN;
        m.TN = TN;

        // PRINT EXACT FORMAT
        System.out.println("for " + filePath);
        System.out.println("\tBCE =" + bce);
        System.out.println("\tConfusion matrix");
        System.out.println("\t\t\ty=1\t\ty=0");
        System.out.println("\t\ty^=1\t" + TP + "\t" + FP);
        System.out.println("\t\ty^=0\t" + FN + "\t" + TN);
        System.out.println("\tAccuracy =" + accuracy);
        System.out.println("\tPrecision =" + precision);
        System.out.println("\tRecall =" + recall);
        System.out.println("\tf1 score =" + f1);
        System.out.println("\tauc roc =" + auc);

        return m;
    }

    public static float computeAUC(List<float[]> data) {
        data.sort((a, b) -> Float.compare(b[0], a[0]));

        int P = 0, N = 0;
        for (float[] d : data) {
            if (d[1] == 1) P++;
            else N++;
        }

        float tp = 0, fp = 0;
        float prevTPR = 0, prevFPR = 0, auc = 0;

        for (float[] d : data) {
            if (d[1] == 1) tp++;
            else fp++;

            float TPR = tp / P;
            float FPR = fp / N;

            auc += (FPR - prevFPR) * (TPR + prevTPR) / 2;

            prevTPR = TPR;
            prevFPR = FPR;
        }

        return auc;
    }
}