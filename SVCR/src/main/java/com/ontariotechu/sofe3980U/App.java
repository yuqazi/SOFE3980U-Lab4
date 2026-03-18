package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App 
{
    static class Metrics {
        float mse, mae, mare;
        String name;

        Metrics(String name, float mse, float mae, float mare) {
            this.name = name;
            this.mse = mse;
            this.mae = mae;
            this.mare = mare;
        }
    }

    public static void main(String[] args)
    {
        Metrics m1 = evaluateModel("model_1.csv");
        Metrics m2 = evaluateModel("model_2.csv");
        Metrics m3 = evaluateModel("model_3.csv");

        // Find best models
        Metrics bestMSE = m1;
        Metrics bestMAE = m1;
        Metrics bestMARE = m1;

        Metrics[] models = {m1, m2, m3};

        for (Metrics m : models) {
            if (m.mse < bestMSE.mse) bestMSE = m;
            if (m.mae < bestMAE.mae) bestMAE = m;
            if (m.mare < bestMARE.mare) bestMARE = m;
        }

        // Print results
        System.out.println("\nAccording to MSE, The best model is " + bestMSE.name);
        System.out.println("According to MAE, The best model is " + bestMAE.name);
        System.out.println("According to MARE, The best model is " + bestMARE.name);
    }

    public static Metrics evaluateModel(String filePath)
    {
        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                                    .withSkipLines(1)
                                    .build();
            allData = csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Error reading: " + filePath);
            return null;
        }

        float mse = 0;
        float mae = 0;
        float mare = 0;
        int n = allData.size();

        for (String[] row : allData) {
            float y_true = Float.parseFloat(row[0]);
            float y_pred = Float.parseFloat(row[1]);

            float error = y_true - y_pred;

            mse += error * error;
            mae += Math.abs(error);

            if (y_true != 0) {
                mare += Math.abs(error / y_true);
            }
        }

        mse /= n;
        mae /= n;
        mare /= n;

        System.out.println("\nResults for " + filePath);
        System.out.println("MSE = " + mse);
        System.out.println("MAE = " + mae);
        System.out.println("MARE = " + mare);

        return new Metrics(filePath, mse, mae, mare);
    }
}