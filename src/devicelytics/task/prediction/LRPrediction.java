package devicelytics.task.prediction;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.LinearRegression;

public class LRPrediction
{
	public static void main(String args[]) throws Exception
	{
		// Load Data set
		final DataSource source = new DataSource("D:\\DataScienceCollection\\Weka\\house.arff");
		final Instances dataset = source.getDataSet();
		// set class index to the last attribute
		dataset.setClassIndex(dataset.numAttributes() - 1);

		// Build model
		LinearRegression model = new LinearRegression();
		model.buildClassifier(dataset);
		// output model
		System.out.println("LR FORMULA : " + model);

		// Now Predicting the cost
		Instance myHouse = dataset.lastInstance();
		double price = model.classifyInstance(myHouse);
		System.out.println("-------------------------");
		System.out.println("PRECTING THE PRICE : " + price);
	}
}