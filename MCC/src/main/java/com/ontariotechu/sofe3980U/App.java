package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

	public static void main(String[] args) {

		String filePath = "model.csv";

		FileReader filereader;
		List<String[]> allData;

		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader)
					.withSkipLines(1)
					.build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading CSV");
			return;
		}

		double epsilon = 1e-15;
		double ceSum = 0.0;

		// 5 classes
		int[][] confusionMatrix = new int[5][5];

		for (String[] row : allData) {

			int actual = Integer.parseInt(row[0]);

			double[] probs = new double[5];

			for (int i = 0; i < 5; i++) {
				probs[i] = Double.parseDouble(row[i + 1]);
			}

			// cross entropy
			double p = probs[actual - 1];
			p = Math.max(epsilon, Math.min(1 - epsilon, p));
			ceSum += -Math.log(p);

			// predicted class
			int predicted = 1;
			double maxProb = probs[0];

			for (int i = 1; i < 5; i++) {
				if (probs[i] > maxProb) {
					maxProb = probs[i];
					predicted = i + 1;
				}
			}

			confusionMatrix[predicted - 1][actual - 1]++;
		}

		double ce = ceSum / allData.size();

		System.out.println("CE =" + ce);
		System.out.println("Confusion matrix");
		System.out.println("\t\t y=1\t y=2\t y=3\t y=4\t y=5");

		for (int i = 0; i < 5; i++) {
			System.out.print("\ty^=" + (i + 1));
			for (int j = 0; j < 5; j++) {
				System.out.print("\t" + confusionMatrix[i][j]);
			}
			System.out.println();
		}
	}
}
