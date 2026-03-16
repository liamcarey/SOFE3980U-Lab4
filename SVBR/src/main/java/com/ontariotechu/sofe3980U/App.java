package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

	static class BinaryMetrics {
		double bce;
		int tp, tn, fp, fn;
		double accuracy, precision, recall, f1, auc;

		BinaryMetrics(double bce, int tp, int tn, int fp, int fn,
					  double accuracy, double precision, double recall,
					  double f1, double auc) {
			this.bce = bce;
			this.tp = tp;
			this.tn = tn;
			this.fp = fp;
			this.fn = fn;
			this.accuracy = accuracy;
			this.precision = precision;
			this.recall = recall;
			this.f1 = f1;
			this.auc = auc;
		}
	}

	static class Prediction {
		int actual;
		double predicted;

		Prediction(int actual, double predicted) {
			this.actual = actual;
			this.predicted = predicted;
		}
	}

	public static void main(String[] args) {
		String[] files = {"model_1.csv", "model_2.csv", "model_3.csv"};

		double bestBCE = Double.MAX_VALUE;
		double bestAccuracy = -1;
		double bestPrecision = -1;
		double bestRecall = -1;
		double bestF1 = -1;
		double bestAUC = -1;

		String bestBCEModel = "";
		String bestAccuracyModel = "";
		String bestPrecisionModel = "";
		String bestRecallModel = "";
		String bestF1Model = "";
		String bestAUCModel = "";

		for (String file : files) {
			BinaryMetrics m = evaluateModel(file);

			System.out.println("for " + file);
			System.out.println("\tBCE =" + m.bce);
			System.out.println("\tConfusion matrix");
			System.out.println("\t\t\t y=1\t y=0");
			System.out.println("\t\ty^=1\t" + m.tp + "\t" + m.fp);
			System.out.println("\t\ty^=0\t" + m.fn + "\t" + m.tn);
			System.out.println("\tAccuracy =" + m.accuracy);
			System.out.println("\tPrecision =" + m.precision);
			System.out.println("\tRecall =" + m.recall);
			System.out.println("\tf1 score =" + m.f1);
			System.out.println("\tauc roc =" + m.auc);

			if (m.bce < bestBCE) {
				bestBCE = m.bce;
				bestBCEModel = file;
			}
			if (m.accuracy > bestAccuracy) {
				bestAccuracy = m.accuracy;
				bestAccuracyModel = file;
			}
			if (m.precision > bestPrecision) {
				bestPrecision = m.precision;
				bestPrecisionModel = file;
			}
			if (m.recall > bestRecall) {
				bestRecall = m.recall;
				bestRecallModel = file;
			}
			if (m.f1 > bestF1) {
				bestF1 = m.f1;
				bestF1Model = file;
			}
			if (m.auc > bestAUC) {
				bestAUC = m.auc;
				bestAUCModel = file;
			}
		}

		System.out.println("According to BCE, The best model is " + bestBCEModel);
		System.out.println("According to Accuracy, The best model is " + bestAccuracyModel);
		System.out.println("According to Precision, The best model is " + bestPrecisionModel);
		System.out.println("According to Recall, The best model is " + bestRecallModel);
		System.out.println("According to F1 score, The best model is " + bestF1Model);
		System.out.println("According to AUC ROC, The best model is " + bestAUCModel);
	}

	public static BinaryMetrics evaluateModel(String filePath) {
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
			return new BinaryMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		}

		double epsilon = 1e-15;
		double bceSum = 0.0;

		int tp = 0, tn = 0, fp = 0, fn = 0;
		List<Prediction> predictions = new ArrayList<>();

		for (String[] row : allData) {
			int actual = Integer.parseInt(row[0]);
			double predicted = Double.parseDouble(row[1]);

			// prevent log(0)
			predicted = Math.max(epsilon, Math.min(1.0 - epsilon, predicted));

			bceSum += actual * Math.log(predicted) + (1 - actual) * Math.log(1 - predicted);

			int predictedClass = (predicted >= 0.5) ? 1 : 0;

			if (predictedClass == 1 && actual == 1) tp++;
			else if (predictedClass == 1 && actual == 0) fp++;
			else if (predictedClass == 0 && actual == 0) tn++;
			else if (predictedClass == 0 && actual == 1) fn++;

			predictions.add(new Prediction(actual, predicted));
		}

		int n = allData.size();
		double bce = -bceSum / n;

		double accuracy = (double)(tp + tn) / (tp + tn + fp + fn);
		double precision = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
		double recall = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
		double f1 = (precision + recall == 0) ? 0.0 : 2 * precision * recall / (precision + recall);
		double auc = calculateAUC(predictions);

		return new BinaryMetrics(bce, tp, tn, fp, fn, accuracy, precision, recall, f1, auc);
	}

	public static double calculateAUC(List<Prediction> predictions) {
		Collections.sort(predictions, Comparator.comparingDouble((Prediction p) -> p.predicted).reversed());

		int positives = 0;
		int negatives = 0;

		for (Prediction p : predictions) {
			if (p.actual == 1) positives++;
			else negatives++;
		}

		if (positives == 0 || negatives == 0) return 0.0;

		double tp = 0.0;
		double fp = 0.0;
		double prevTPR = 0.0;
		double prevFPR = 0.0;
		double auc = 0.0;

		for (Prediction p : predictions) {
			if (p.actual == 1) tp++;
			else fp++;

			double tpr = tp / positives;
			double fpr = fp / negatives;

			auc += (fpr - prevFPR) * (tpr + prevTPR) / 2.0;

			prevTPR = tpr;
			prevFPR = fpr;
		}

		return auc;
	}
}
