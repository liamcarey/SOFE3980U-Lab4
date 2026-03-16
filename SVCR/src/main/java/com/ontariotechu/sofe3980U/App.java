package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App 
{
	static class Metrics {
		double mse;
		double mae;
		double mare;

		Metrics(double mse, double mae, double mare) {
			this.mse = mse;
			this.mae = mae;
			this.mare = mare;
		}
	}

	public static void main(String[] args)
	{
		String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};

		double bestMSE = Double.MAX_VALUE;
		double bestMAE = Double.MAX_VALUE;
		double bestMARE = Double.MAX_VALUE;

		String bestMSEModel = "";
		String bestMAEModel = "";
		String bestMAREModel = "";

		for (String file : files) {
			Metrics m = evaluateModel(file);

			System.out.println("for " + file);
			System.out.println("\tMSE =" + m.mse);
			System.out.println("\tMAE =" + m.mae);
			System.out.println("\tMARE =" + m.mare);

			if (m.mse < bestMSE) {
				bestMSE = m.mse;
				bestMSEModel = file;
			}
			if (m.mae < bestMAE) {
				bestMAE = m.mae;
				bestMAEModel = file;
			}
			if (m.mare < bestMARE) {
				bestMARE = m.mare;
				bestMAREModel = file;
			}
		}

		System.out.println("According to MSE, The best model is " + bestMSEModel);
		System.out.println("According to MAE, The best model is " + bestMAEModel);
		System.out.println("According to MARE, The best model is " + bestMAREModel);
	}

	public static Metrics evaluateModel(String filePath) {
		FileReader filereader;
		List<String[]> allData;

		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader)
					.withSkipLines(1)
					.build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading file: " + filePath);
			return new Metrics(0, 0, 0);
		}

		double mse = 0.0;
		double mae = 0.0;
		double mare = 0.0;
		double epsilon = 1e-10;

		for (String[] row : allData) {
			double trueValue = Double.parseDouble(row[0]);
			double predictedValue = Double.parseDouble(row[1]);

			double error = trueValue - predictedValue;

			mse += error * error;
			mae += Math.abs(error);
			mare += Math.abs(error) / (Math.abs(trueValue) + epsilon);
		}

		int n = allData.size();
		mse /= n;
		mae /= n;
		mare /= n;

		return new Metrics(mse, mae, mare);
	}
}
