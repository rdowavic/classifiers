import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class NaiveBayesClassifier {
	
	public static ArrayList<String[]> data;
		
		public static double prob1(String classed, String[] attributes) {
			double prob = 1;
			for(int i = 0; i < attributes.length; i++) {
				// this is included, because when we do 10 fold validation
				// we our attributes come with the class, whereas normally they wouldn't.
				if (attributes[i].equals("yes") || attributes[i].equals("no")){
					break;
				}
				prob = prob * prob2(i, classed, Double.parseDouble(attributes[i]));
			}
			return prob;
		}
		
		public static double prob2(int attribute, String classed, double value) {
			double mean = mean(attribute, classed);
			double sd = sd(attribute, classed, mean);
			double power = -Math.pow(value-mean, 2)/(2*Math.pow(sd, 2));
			return 1/(sd*Math.sqrt(2*Math.PI))*Math.exp(power);
		}
		
		public static double mean(int attribute, String classed) {
			int count = 0;
			double sum = 0;
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i)[data.get(i).length - 1].equals(classed)) {
					sum += Double.parseDouble(data.get(i)[attribute]);
					count++;
				}
			}
			return sum/count;
		}
		
		public static double sd(int attribute, String classed, double mean) {
			int count = 0;
			double sum = 0;
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i)[data.get(i).length - 1].equals(classed)) {
					sum += Math.pow((Double.parseDouble(data.get(i)[attribute]) - mean), 2);
					count++;
				}
			}
			return Math.sqrt(sum/(count-1));
		}
		
		// if calcAcc is on, then we return the accuracy of the test
		// if calcAcc is off, then we return -1;
		public static double naiveBayes(ArrayList<String[]> testing, boolean calcAcc) {
			
			int correctlyClassified = 0;
			
			for (int i = 0; i < testing.size(); i++) {
				// do probability of yes given E
				double yesProb = prob1("yes", testing.get(i));
				// do probability of no given E
				double noProb = prob1("no", testing.get(i));
				
				if (yesProb >= noProb) {
					if (calcAcc) {
						if (testing.get(i)[testing.get(i).length-1].equals("yes")){
							correctlyClassified++;
						}
					} else {
						System.out.println("yes");
					}
				} else {
					if (calcAcc) {
						if (testing.get(i)[testing.get(i).length-1].equals("no")){
							correctlyClassified++;
						}
					} else {
						System.out.println("no");
					}
				}	
			}
			
			if (calcAcc) {
				double acc = correctlyClassified/(double) testing.size();
				return acc;
			}
			return -1;
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
