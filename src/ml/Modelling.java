package ml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import constants.Constants;
import data.BarKey;
import dbio.QueryManager;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Modelling {

	private static FastVector metricBuckets = new FastVector();
	private static FastVector bullClassBuckets = new FastVector();
	private static FastVector sellClassBuckets = new FastVector();
	
	static {
		for (int a = 0; a <= 13; a++) {
			metricBuckets.addElement("BUCKET" + a);
		}
		
		bullClassBuckets.addElement("No");
		bullClassBuckets.addElement("Buy");
		
		sellClassBuckets.addElement("No");
		sellClassBuckets.addElement("Sell");
	}
	
	public static void main(String[] args) {

//		Classifier classifier = loadModel("NaiveBayes 0.model");
//		System.out.println(classifier.toString());
	}

	public static Classifier loadModel(String modelName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("weka\\models\\" + modelName));
			Classifier classifier = (Classifier)ois.readObject();
			ois.close();
			return classifier;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Instances loadData(ArrayList<String> featureNames, ArrayList<ArrayList<Object>> valuesList) {
		try {
			// Setup the attributes / features
			FastVector attributes = new FastVector();

			attributes.addElement(new Attribute("close"));
			attributes.addElement(new Attribute("hour"));
			
			for (String featureName : featureNames) {
				attributes.addElement(new Attribute(featureName, metricBuckets));
			}
			
			attributes.addElement(new Attribute("class", bullClassBuckets));
			
			// Setup the instances / values / data
			int capacity = 50000;
			String type = "Training";
			Instances instances = new Instances(type, attributes, capacity);
			
			for (ArrayList<Object> valueList : valuesList) {
				double[] values = new double[instances.numAttributes()];
				values[0] = Double.parseDouble(valueList.get(0).toString()); // close
				values[1] = (int)Double.parseDouble(valueList.get(1).toString()); // hour
				for (int i = 2; i < values.length; i++) {
					String featureBucket = valueList.get(i).toString().trim();
					values[i] = instances.attribute(i).indexOfValue(featureBucket);
				}
				Instance instance = new Instance(1, values);
				instances.add(instance);
			}
			
			instances.setClassIndex(attributes.size() - 1);
			
			return instances;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
	}
	
	public static void buildAndEvaluateModel(String algo, String params, Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, 
			float targetGain, float minLoss, int numPeriods, BarKey bk, ArrayList<String> metricNames, HashMap<String, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			String sellMetric = Constants.OTHER_SELL_METRIC_PERCENT_UP;
			float sellMetricValue = targetGain;
			String stopMetric = Constants.STOP_METRIC_PERCENT_DOWN;
			float stopMetricValue = minLoss;
			int numBars = 48;
			
			System.out.print("Creating Train & Test datasets...");
			ArrayList<ArrayList<Object>> trainValuesList = TrainingSetCreator.createWekaArffData(trainStart, trainEnd, sellMetricValue, stopMetricValue, numBars, bk, metricNames, metricDiscreteValueHash);
			ArrayList<ArrayList<Object>> testValuesList = TrainingSetCreator.createWekaArffData(testStart, testEnd, sellMetricValue, stopMetricValue, numBars, bk, metricNames, metricDiscreteValueHash);
			System.out.println("Complete.");
			
			// Training & Cross Validation Data
			System.out.print("Cross Validating...");
			Instances trainInstances = Modelling.loadData(metricNames, trainValuesList);
			Classifier classifier = null;
			if (algo.equals("NaiveBayes")) {
				classifier = new NaiveBayes();
			}
			else if (algo.equals("RandomForest")) {
				classifier = new RandomForest();
			}
			else if (algo.equals("J48")) {
				classifier = new J48();
			}
			else if (algo.equals("SimpleLogistic")) {
				classifier = new SimpleLogistic();
			}
			else if (algo.equals("Bagging")) {
				classifier = new Bagging();
			}
			else if (algo.equals("BayesNet")) {
				classifier = new BayesNet();
			}
			else {
				return;
			}
			Evaluation trainEval = new Evaluation(trainInstances);
			trainEval.crossValidateModel(classifier, trainInstances, 10, new Random(1));
			System.out.println("Complete.");
			
			int trainDatasetSize = trainInstances.numInstances();
			double[][] trainConfusionMatrix = trainEval.confusionMatrix();
			int trainTrueNegatives = (int)trainConfusionMatrix[0][0];
			int trainFalseNegatives = (int)trainConfusionMatrix[1][0];
			int trainFalsePositives = (int)trainConfusionMatrix[0][1];
			int trainTruePositives = (int)trainConfusionMatrix[1][1];
			double trainTruePositiveRate = trainTruePositives / (double)(trainTruePositives + trainFalseNegatives);
			double trainFalsePositiveRate = trainFalsePositives / (double)(trainFalsePositives + trainTrueNegatives);
			double trainCorrectRate = trainEval.pctCorrect();
			double trainKappa = trainEval.kappa();
			double trainMeanAbsoluteError = trainEval.meanAbsoluteError();
			double trainRootMeanSquaredError = trainEval.rootMeanSquaredError();
			double trainRelativeAbsoluteError = trainEval.relativeAbsoluteError();
			double trainRootRelativeSquaredError = trainEval.rootRelativeSquaredError();
			
			ThresholdCurve trainCurve = new ThresholdCurve();
			Instances trainCurveInstances = trainCurve.getCurve(trainEval.predictions(), 0);
			double trainROCArea = trainCurve.getROCArea(trainCurveInstances);

			// Test Data
			System.out.print("Evaluating Test Data...");
			Instances testInstances = Modelling.loadData(metricNames, testValuesList);
			classifier.buildClassifier(trainInstances);
			Evaluation testEval = new Evaluation(trainInstances);
			testEval.evaluateModel(classifier, testInstances);
			System.out.println("Complete.");
			
			int testDatasetSize = testInstances.numInstances();
			double[][] testConfusionMatrix = testEval.confusionMatrix();
			int testTrueNegatives = (int)testConfusionMatrix[0][0];
			int testFalseNegatives = (int)testConfusionMatrix[1][0];
			int testFalsePositives = (int)testConfusionMatrix[0][1];
			int testTruePositives = (int)testConfusionMatrix[1][1];
			double testTruePositiveRate = testTruePositives / (double)(testTruePositives + testFalseNegatives);
			double testFalsePositiveRate = testFalsePositives / (double)(testFalsePositives + testTrueNegatives);
			double testCorrectRate = testEval.pctCorrect();
			double testKappa = testEval.kappa();
			double testMeanAbsoluteError = testEval.meanAbsoluteError();
			double testRootMeanSquaredError = testEval.rootMeanSquaredError();
			double testRelativeAbsoluteError = testEval.relativeAbsoluteError();
			double testRootRelativeSquaredError = testEval.rootRelativeSquaredError();
			
			ThresholdCurve testCurve = new ThresholdCurve();
			Instances testCurveInstances = testCurve.getCurve(testEval.predictions(), 0);
			double testROCArea = testCurve.getROCArea(testCurveInstances);

			// Save model file
			System.out.print("Saving model file...");
			int modelID = QueryManager.getNextModelID();
			weka.core.SerializationHelper.write("weka/models/" + algo + modelID + ".model", classifier);
			System.out.println("Complete.");
						
			Model m = new Model(algo + modelID + ".model", algo, params, bk, metricNames, trainStart, trainEnd, testStart, testEnd, 
					sellMetric, sellMetricValue, stopMetric, stopMetricValue, numBars,
					trainDatasetSize, trainTrueNegatives, trainFalseNegatives, trainFalsePositives, trainTruePositives,
					trainTruePositiveRate, trainFalsePositiveRate, trainCorrectRate,
					trainKappa, trainMeanAbsoluteError, trainRootMeanSquaredError, trainRelativeAbsoluteError, trainRootRelativeSquaredError,
					trainROCArea,
					testDatasetSize, testTrueNegatives, testFalseNegatives, testFalsePositives, testTruePositives,
					testTruePositiveRate, testFalsePositiveRate, testCorrectRate,
					testKappa, testMeanAbsoluteError, testRootMeanSquaredError, testRelativeAbsoluteError, testRootRelativeSquaredError,
					testROCArea);
			
			System.out.print("Saving model to DB...");
			QueryManager.insertModel(m);
			System.out.println("Complete.");
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}