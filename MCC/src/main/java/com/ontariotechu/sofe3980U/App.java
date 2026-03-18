package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App 
{
    public static void main(String[] args)
    {
        String filePath = "model.csv";
        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        }
        catch(Exception e){
            System.out.println("Error reading the CSV file");
            return;
        }

        float CE = 0;
        int n = allData.size();

        // 5x5 confusion matrix
        int[][] matrix = new int[5][5];

        for (String[] row : allData) {

            int y_true = Integer.parseInt(row[0]); // values: 1–5
            float[] y_predicted = new float[5];

            for (int i = 0; i < 5; i++) {
                y_predicted[i] = Float.parseFloat(row[i + 1]);
            }

            // --- Cross Entropy ---
            float prob = y_predicted[y_true - 1];

            // avoid log(0)
            prob = Math.max(1e-7f, prob);

            CE += -Math.log(prob);

            // --- Predicted class (argmax) ---
            int y_pred = 0;
            float maxProb = y_predicted[0];

            for (int i = 1; i < 5; i++) {
                if (y_predicted[i] > maxProb) {
                    maxProb = y_predicted[i];
                    y_pred = i;
                }
            }

            // fill confusion matrix
            matrix[y_pred][y_true - 1]++;
        }

        CE /= n;

        // --- Output ---
        System.out.println("CE =" + CE);
        System.out.println("Confusion matrix");
        System.out.println("\t\t\ty=1\t\ty=2\t\ty=3\t\ty=4\t\ty=5");

        for (int i = 0; i < 5; i++) {
            System.out.print("\ty^=" + (i+1) + "\t");
            for (int j = 0; j < 5; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }
}