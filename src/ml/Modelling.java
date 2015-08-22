package ml;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import weka.classifiers.Classifier;
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
				int t = 0;
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
}