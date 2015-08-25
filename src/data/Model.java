package data;

import java.util.ArrayList;
import java.util.Calendar;

public class Model {

	public int id = -1;
	public String type;
	public String modelFile;
	public String algo;
	public String params;
	public BarKey bk;
	public ArrayList<String> metrics;
	public Calendar trainStart;
	public Calendar trainEnd;
	public Calendar testStart;
	public Calendar testEnd;
	public String sellMetric;
	public float sellMetricValue;
	public String stopMetric;
	public float stopMetricValue;
	public int numBars;
	
	public int trainDatasetSize;
	public int trainTrueNegatives;
	public int trainFalseNegatives;
	public int trainFalsePositives;
	public int trainTruePositives;
	public double trainTruePostitiveRate;
	public double trainFalsePositiveRate;
	public double trainCorrectRate;
	public double trainKappa;
	public double trainMeanAbsoluteError;
	public double trainRootMeanSquaredError;
	public double trainRelativeAbsoluteError;
	public double trainRootRelativeSquaredError;
	public double trainROCArea;
	
	public int testDatasetSize;
	public int testTrueNegatives;
	public int testFalseNegatives;
	public int testFalsePositives;
	public int testTruePositives;
	public double testTruePostitiveRate;
	public double testFalsePositiveRate;
	public double testCorrectRate;
	public double testKappa;
	public double testMeanAbsoluteError;
	public double testRootMeanSquaredError;
	public double testRelativeAbsoluteError;
	public double testRootRelativeSquaredError;
	public double testROCArea;
	
	public Model(String type, String modelFile, String algo, String params, BarKey bk, ArrayList<String> metrics,
			Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, String sellMetric,
			float sellMetricValue, String stopMetric, float stopMetricValue, int numBars, int trainDatasetSize,
			int trainTrueNegatives, int trainFalseNegatives, int trainFalsePositives, int trainTruePositives,
			double trainTruePostitiveRate, double trainFalsePositiveRate, double trainCorrectRate, double trainKappa,
			double trainMeanAbsoluteError, double trainRootMeanSquaredError, double trainRelativeAbsoluteError,
			double trainRootRelativeSquaredError, double trainROCArea, int testDatasetSize, int testTrueNegatives,
			int testFalseNegatives, int testFalsePositives, int testTruePositives, double testTruePostitiveRate,
			double testFalsePositiveRate, double testCorrectRate, double testKappa, double testMeanAbsoluteError,
			double testRootMeanSquaredError, double testRelativeAbsoluteError, double testRootRelativeSquaredError,
			double testROCArea) {
		super();
		this.type = type;
		this.modelFile = modelFile;
		this.algo = algo;
		this.params = params;
		this.bk = bk;
		this.metrics = metrics;
		this.trainStart = trainStart;
		this.trainEnd = trainEnd;
		this.testStart = testStart;
		this.testEnd = testEnd;
		this.sellMetric = sellMetric;
		this.sellMetricValue = sellMetricValue;
		this.stopMetric = stopMetric;
		this.stopMetricValue = stopMetricValue;
		this.numBars = numBars;
		this.trainDatasetSize = trainDatasetSize;
		this.trainTrueNegatives = trainTrueNegatives;
		this.trainFalseNegatives = trainFalseNegatives;
		this.trainFalsePositives = trainFalsePositives;
		this.trainTruePositives = trainTruePositives;
		this.trainTruePostitiveRate = trainTruePostitiveRate;
		this.trainFalsePositiveRate = trainFalsePositiveRate;
		this.trainCorrectRate = trainCorrectRate;
		this.trainKappa = trainKappa;
		this.trainMeanAbsoluteError = trainMeanAbsoluteError;
		this.trainRootMeanSquaredError = trainRootMeanSquaredError;
		this.trainRelativeAbsoluteError = trainRelativeAbsoluteError;
		this.trainRootRelativeSquaredError = trainRootRelativeSquaredError;
		this.trainROCArea = trainROCArea;
		this.testDatasetSize = testDatasetSize;
		this.testTrueNegatives = testTrueNegatives;
		this.testFalseNegatives = testFalseNegatives;
		this.testFalsePositives = testFalsePositives;
		this.testTruePositives = testTruePositives;
		this.testTruePostitiveRate = testTruePostitiveRate;
		this.testFalsePositiveRate = testFalsePositiveRate;
		this.testCorrectRate = testCorrectRate;
		this.testKappa = testKappa;
		this.testMeanAbsoluteError = testMeanAbsoluteError;
		this.testRootMeanSquaredError = testRootMeanSquaredError;
		this.testRelativeAbsoluteError = testRelativeAbsoluteError;
		this.testRootRelativeSquaredError = testRootRelativeSquaredError;
		this.testROCArea = testROCArea;
	}
	
}