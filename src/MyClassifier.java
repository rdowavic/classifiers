import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class MyClassifier {
	
	/**
	 * These are just Globals for attributes and CFS attributes respectively (The CFS is a narrowed down version)
	 * 
	 */
	public static ArrayList<String> attributes = new ArrayList<String>(Arrays.asList(new String[]{
			"times_pregnant",
			"plasma_glucose_conc",
		    "blood_pressure",
		    "tricep_skin_fold_thickness",
		    "serum_insulin",
		    "body_mass_index",
		    "diabetes_pedigree_function",
		    "age",
		    "class"	
		}));
	
	public static ArrayList<String> CFSattributes = new ArrayList<String>(Arrays.asList(new String[]{
			"plasma_glucose_conc",
		    "serum_insulin",
		    "body_mass_index",
		    "diabetes_pedigree_function",
		    "age",
		    "class"	
		}));
	
	public static void main(String[] args) {

		if (Arrays.asList(args).contains("--accuracy")) {
			// the user would write
			// pima.csv NB --accuracy
			crossValidate(args[0], args[1]);
		} else {
			// normal execution
			String trainingFilePath = args[0];
			String testingFilePath = args[1];
			String mode = args[2];
			ArrayList<String[]> trainingExamples = readExamples(trainingFilePath);
			ArrayList<String[]> testingExamples = readExamples(testingFilePath);
			ArrayList<String> atts = trainingExamples.get(0).length < 8 ? CFSattributes : attributes;
			
			if (mode.equals("DT")) {
				DecisionTreeMaker d = new DecisionTreeMaker();
				Node decisionTree = d.makeDecisionTree(trainingExamples, atts);
				d.classifyExamples(decisionTree, testingExamples, atts);
			} else if (mode.equals("NB")) {
				NaiveBayesClassifier.naiveBayes(testingExamples, false);
			}
		}
	}
	
	public static ArrayList<String[]> readExamples(String filePath) {
		ArrayList<String[]> data = new ArrayList<String[]>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = reader.readLine()) != null) {
				data.add(line.split(","));
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
		}
		return data;
	}
	
	public static void crossValidate(String path, String classifier) {
		 ArrayList<ArrayList<String[]>> fold = readFolds(path);
		 double accSum = 0;
		 
		 // i indexes the testing data, the j's are used for training
		 for (int i = 0; i < 10; i++) {
			 ArrayList<String[]> training = new ArrayList<String[]>();
			 for (int j = 0; j < 10; j++) {
				 if (j == i) {
					 // skipping the i'th one because we'll use it for testing
					 continue;
				 }
				 training.addAll(fold.get(j));
			 }
			 // now the training/testing split has been established
			 double accuracy = 0;
			 ArrayList<String[]> testing = fold.get(i);
			 boolean CFS = testing.get(0).length < 8;
			 ArrayList<String> atts = CFS ? CFSattributes : attributes;
			 
			 // fold.get(i) is the testing ArrayList<String[]>
			 if (classifier.equals("NB")) {
				 NaiveBayesClassifier.data = training;
				 accuracy = NaiveBayesClassifier.naiveBayes(testing, true);
			 } else if (classifier.equals("DT")){
				 DecisionTreeMaker d = new DecisionTreeMaker();
				 Node decisionTree = d.makeDecisionTree(training, atts);
				 accuracy = d.testAccuracy(decisionTree, atts, testing);
			 } 
			 accSum += accuracy;
			 System.out.println("Fold " + (i+1) +" Accuracy: "+ accuracy);
		 }
		 System.out.println("Average Accuracy: " + (accSum/10));
	}
	
	/**
	 * Puts the folds into a list. Examples in Fold one can be retrieved by going result.get(0).
	 * The first example in fold 1 can be retrieved by going result.get(0).get(0), and its type is an Array of Strings.
	 * @param path
	 * @return
	 */
	public static ArrayList<ArrayList<String[]>> readFolds(String path) {
		ArrayList<ArrayList<String[]>> fold = new ArrayList<ArrayList<String[]>>(10);
		
		for(int i = 0; i < 10; i++) {
			fold.add(i, new ArrayList<String[]>());
		}
		int spot = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = "";
			while((line = reader.readLine()) != null) {
				
				if (Pattern.matches("fold.+", line)) {
					continue;
				}
				
				if (line.equals("")) {
					spot++;
					continue;
				}
				
				fold.get(spot).add(line.split(","));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fold;
	}
	public static void stratifyData(String training) {
		ArrayList<String[]> yesData = new ArrayList<String[]>();
		ArrayList<String[]> noData = new ArrayList<String[]>();
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(training));
			String line = "";
			while((line = reader.readLine()) != null) {
				String[] dataPoint = line.split(",");
				if (dataPoint[dataPoint.length-1].equals("yes")) { 
					yesData.add(dataPoint);
					//System.out.println("test");
				} else if (dataPoint[dataPoint.length-1].equals("no")) {
					noData.add(dataPoint);
					//System.out.println("test");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		ArrayList<ArrayList<String[]>>buckets = new ArrayList<ArrayList<String[]>>(10);
		for(int i = 0; i < 10; i++) {
			buckets.add(i, new ArrayList<String[]>());
		}
		
		// divy out yes data
		for (int i = 0; i < yesData.size(); i++) {
			buckets.get(i%10).add(yesData.get(i));
		}
		// divy out no data
		for (int i = 0; i < noData.size(); i++) {
			buckets.get(i%10).add(noData.get(i));
		}
		
		for (int i = 0; i < 10; i++) {
			System.out.println("fold" + (i+1));
			for (int j = 0; j < buckets.get(i).size(); j++) {
				for (int k = 0; k < buckets.get(i).get(j).length; k++) {
					if (k < buckets.get(i).get(j).length - 1) {
						System.out.print(buckets.get(i).get(j)[k] + ",");
					} else {
						System.out.println(buckets.get(i).get(j)[k]);
					}
				}
			}
			System.out.println();
		}
	}
}
