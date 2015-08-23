package ml;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.ThresholdCurve;
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
	
	public static void buildAndEvaluateModel(Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, float targetGain, float minLoss, int numPeriods, BarKey bk, ArrayList<String> metricNames) {
		try {
			ArrayList<ArrayList<Object>> trainValuesList = TrainingSetCreator.createWekaArffData(trainStart, trainEnd, 1.2f, .2f, 48, bk, metricNames);
			ArrayList<ArrayList<Object>> testValuesList = TrainingSetCreator.createWekaArffData(testStart, testEnd, 1.2f, .2f, 48, bk, metricNames);
			
			// Cross Validation
			Instances trainInstances = Modelling.loadData(metricNames, trainValuesList);
			NaiveBayes classifier = new NaiveBayes();
			Evaluation trainEval = new Evaluation(trainInstances);
			trainEval.crossValidateModel(classifier, trainInstances, 10, new Random(1));
			
			double[][] trainConfusionMatrix = trainEval.confusionMatrix();
			double trainTrueNegatives = trainConfusionMatrix[0][0];
			double trainFalseNegatives = trainConfusionMatrix[1][0];
			double trainFalsePositives = trainConfusionMatrix[0][1];
			double trainTruePositives = trainConfusionMatrix[1][1];
			double trainTruePositiveRate = trainTruePositives / (trainTruePositives + trainFalseNegatives);
			double trainFalsePositiveRate = trainFalsePositives / (trainFalsePositives + trainTrueNegatives);
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
			Instances testInstances = Modelling.loadData(metricNames, testValuesList);
			classifier.buildClassifier(trainInstances);
			Evaluation testEval = new Evaluation(trainInstances);
			testEval.evaluateModel(classifier, testInstances);
			
			double[][] testConfusionMatrix = testEval.confusionMatrix();
			double testTrueNegatives = testConfusionMatrix[0][0];
			double testFalseNegatives = testConfusionMatrix[1][0];
			double testFalsePositives = testConfusionMatrix[0][1];
			double testTruePositives = testConfusionMatrix[1][1];
			double testTruePositiveRate = testTruePositives / (testTruePositives + testFalseNegatives);
			double testFalsePositiveRate = testFalsePositives / (testFalsePositives + testTrueNegatives);
			double testCorrectRate = testEval.pctCorrect();
			double testKappa = testEval.kappa();
			double testMeanAbsoluteError = testEval.meanAbsoluteError();
			double testRootMeanSquaredError = testEval.rootMeanSquaredError();
			double testRelativeAbsoluteError = testEval.relativeAbsoluteError();
			double testRootRelativeSquaredError = testEval.rootRelativeSquaredError();
			
			ThresholdCurve testCurve = new ThresholdCurve();
			Instances testCurveInstances = testCurve.getCurve(testEval.predictions(), 0);
			double testROCArea = testCurve.getROCArea(testCurveInstances);

			
			System.out.println(testEval.toSummaryString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}