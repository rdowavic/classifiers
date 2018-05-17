import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;
import java.util.Map.Entry;

public class DecisionTreeMaker {
	
	private ArrayList<String[]> allExamples;
	
	public void printTree(Node root) {
		System.out.println("--------------------------------------------------------------------");
		System.out.println("========================== Decision Tree ===========================");
		System.out.println("--------------------------------------------------------------------");
		printTree(root, 0);
		System.out.println();
	}
	
	private void printTree(Node root, int indent) {
		String offset = "";
		for (int i = 0; i < indent; i++) offset += "|  ";
		
		if (root.children.isEmpty())
			System.out.print(": " + root.data);
		else {
			for (Pair pair : root.children) {
				System.out.print("\n" + offset + root.data + " = " + pair.label);
				printTree(pair.node, indent + 1);
			}
		}
	}
	
	/**
	 * Given a list of examples, will print 'yes' or 'no' for what the DT classifies them as.
	 * @param DT
	 * @param examples
	 * @param attributes
	 */
	public void classifyExamples(Node DT, ArrayList<String[]> examples, ArrayList<String> attributes) {
		for (String[] example : examples) {
			System.out.println(classifyExample(DT, example, attributes));
		}
	}
	
	public String classifyExample(Node DT, String[] example, ArrayList<String> attributes) {
		Node current = DT;
		
		while (!current.children.isEmpty()) {
			//attribute to follow = current.data
			String attribute = current.data;
			// choose next 'current' to go to
			for (Pair branch : current.children) {
				if (branch.label.equals(example[columnNum(attribute, attributes)])) {
					current = branch.node;
					break;
				}
			}
		}
		// has no children, so must be the answer
		return current.data;
	}
	
	/**
	 * @param attr - the String attribute (eg "times_pregnant") that we need
	 * the index of
	 * @return the column number of the given attribute attr
	 */
	private int columnNum(String attr, ArrayList<String> attributes) {
		return attributes.indexOf(attr);
	}
	
	public Node makeDecisionTree(ArrayList<String[]> examples, ArrayList<String> atts) {
		allExamples = new ArrayList<String[]>(examples);
		// get rid of class from attributes
		ArrayList<String> attributesWithoutClass = new ArrayList<String>(atts);
		attributesWithoutClass.remove("class");
		return makeDecisionTree(examples, attributesWithoutClass, atts, "yes");
	}

	/**
	 * @brief this returns the root of the made decision tree
	 */
	private Node makeDecisionTree(ArrayList<String[]> examples,
			ArrayList<String> attributes, ArrayList<String> allAttributes, String defaultValue) {
		// examples is the training data
		// attributes is this string thing
		// default value is one of these
		if (examples.isEmpty())
			return new Node(defaultValue);
		// else if all examples have the same class, return the classification
		String classification = allSameClass(examples);
		if (classification != null) {
			return new Node(classification);
		}
		else if (attributes.isEmpty()) {
			return new Node(majorityClass(examples));
		}
		else {
			String best = chooseAttribute(attributes, allAttributes, examples);
			/*Okay we're sort of recursively making this subtree thing.
			 *Once we have the best attribute to split on,
			 *we have to go through all the possible values of that attribute
			 *(they are like the branches) then they form their very own 
			 * DTL situations over there. Good for them.
			 */
			Node tree = new Node(best);
			int columnBest = columnNum(best, allAttributes);
			
			for (String value : allValues(columnBest, allExamples)) {
				ArrayList<String[]> childExamples = reducedExamples(examples, columnBest, value);
				ArrayList<String> attsWithBestRemoved = new ArrayList<String>(attributes);
				attsWithBestRemoved.remove(best);
				// making the recursive call here to construct the child subtree
				// shit
				Node child = makeDecisionTree(
						childExamples, 
						attsWithBestRemoved,
						allAttributes,
						majorityClass(examples)
				);
				
				// adding a branch to tree with label v_i and subtree `subtree` :)
				tree.addChild(value, child); 			
			}
			return tree;
		}
	}
	
	/**
	 * 
	 * @param examples - the list of examples that we hope have the same class (are all yes or all no)
	 * @return the classification "yes"/"no" if they are all the same. If they aren't, return null.
	 */
	private String allSameClass(ArrayList<String[]> examples) {
		// the length-1 is the class line
		int CLASS = examples.get(0).length - 1;
		// the first one's class
		String classifiedAs = examples.get(0)[CLASS];
		
		for (String[] example : examples) {
			if (!example[CLASS].equals(classifiedAs))
				return null;
		}		
		return classifiedAs;
	}
	
	/**
	 * So examples is folds.get(i)
	 * 
	 * @param attributes
	 * @param examples
	 * @return
	 */
	public double testAccuracy(Node DT, ArrayList<String> attributes, ArrayList<String[]> examples) {
		double numCorrectlyClassified = 0;
		
		for (String[] example : examples) {
			String classifiedAs = classifyExample(DT, example, attributes);
			if (classifiedAs.equals(example[columnNum("class", attributes)]))
				numCorrectlyClassified++;
		}
		
		return numCorrectlyClassified / (double) examples.size();
	}
	
	
	/**
	 * Using Information gain to decide which attribute is the best one to use (to split on)
	 * @param attributes
	 * @param examples
	 * @return
	 */
	private String chooseAttribute(ArrayList<String> attributes, ArrayList<String> allAttributes, ArrayList<String[]> examples) {
		String bestAttr = null;
		double bestGain = 0;
		
		for (String attr : attributes) {
			double attrGain = informationGain(columnNum(attr, allAttributes), examples, allAttributes);
			if (attrGain > bestGain || bestAttr == null) {
				bestAttr = attr;
				bestGain = attrGain;
			}
		}
		return bestAttr;
	}
	
	private double informationGain(int attrIndex, ArrayList<String[]> examples, ArrayList<String> attributes) {
		/*
		 * Gain(examples | attr) = information gain = H(S) - H(S | A = v_j)
		 */
		double entropy = Entropy(examples, attributes);
		double reminder = Reminder(attrIndex, examples, attributes);
		
		return entropy - reminder;
	}
	
	/**
	 * The reminder is just this whole thing:  sum (x/y)H(S_i) for S_i as in
	 * okay this S_i could be a possible value of that attribute.
	 * Ie,
	 * Reminder = H(S | outlook) = (x1/y) * H(outlook=sunny) + ... + (x3/y) * H(outlook=rainy)
	 * So you're just kind of narrowing down the set of examples and then calculating
	 * their respective entropies and then adding back up. Holy fuck,
	 * that makes heaps of sense
	 * @param attrIndex
	 * @param examples
	 * @return
	 */
	private double Reminder(int attrIndex, ArrayList<String[]> examples, ArrayList<String> attributes) {
		double total = 0;
		double numExamples = examples.size();
		
		for (String value : allValues(attrIndex, examples)) {
			// get the list of examples such that example[attrIndex] == value
			ArrayList<String[]> onesWithThisValue = reducedExamples(examples, attrIndex, value);
			double count = onesWithThisValue.size();
			double entropyOnThisValue = Entropy(onesWithThisValue, attributes);
			if (Double.isNaN(entropyOnThisValue)) {
				System.out.println("Entropy on " + value + ": " + entropyOnThisValue);
			}
			total += count * entropyOnThisValue;
		}
		return total / numExamples;
	}
	
	/**
	 * 
	 * @param examples - the full set of examples
	 * @param attrIndex - the attribute to check whether it's value or not
	 * @param value - the value we want
	 * @return a reduced set of examples where the given attribute has the value `value`
	 */
	private ArrayList<String[]> reducedExamples(ArrayList<String[]> examples, int attrIndex, String value) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		for (String[] example : examples) {
			if (example[attrIndex].equals(value))
				result.add(example);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param examples - the examples from the training data
	 * @return the Entropy of the set of examples
	 * That is,
	 * H(Examples) = negative of the sum of (number of times a value occurs/total number examples) log(x/y)
	 * where x and y are just the previous things mentioned respectively.
	 * I use this to compute {@link #informationGain(int, ArrayList)}
	 * since I can see the entropy before and the entropy after splitting
	 * on that attribute.. Yah
	 * 
	 */
	private double Entropy(ArrayList<String[]> examples, ArrayList<String> attributes) {
		String[] classes = new String[] { "yes", "no" };
		double total = examples.size();
		double result = 0;
		
		for (String value : classes) {
			int numWithClass = count(examples, columnNum("class", attributes), value);
			double Pi = numWithClass / (double) total;
			/**
			 * A direct analogy to the formula
			 * H(M) = -Sigma P_i * log2(P_i) = entropy(M)
			 * 
			 */
			if (Pi == 0.0)
				continue;
			
			result -= (Pi * Math.log(Pi) / Math.log(2));
		}
		return result;
	}
	
	/**
	 * Find how many examples have the given value for this attribute
	 * @param examples
	 * @param attrIndex
	 * @param value
	 * @return
	 */
	private int count(ArrayList<String[]> examples, int attrIndex, String value) {
		int total = 0;
		
		for (String[] example : examples) {
			if (example[attrIndex].equals(value))
				total++;
		}
		return total;
	}
	
	/**
	 * 
	 * @param attrIndex - the column (or the attribute) we are looking over
	 * @param examples - the set of examples from the training data
	 * @return all the values associated with a column
	 */
	private Set<String> allValues(int attrIndex, ArrayList<String[]> examples) {
		Set<String> result = new HashSet<String>();
		
		for (String[] example : examples) {
			result.add(example[attrIndex]);
		}
		return result;
	}
	
	/**
	 * return the majority class of the given list of examples
	 * @param examples - its last column tells us whether it's a yes or no,
	 * this basically just grabs out that column and sends it to `mode` to find the most
	 * frequently occurring class, will be either yes or no.
	 * @return
	 */
	private String majorityClass(ArrayList<String[]> examples) {
		int CLASS = examples.get(0).length - 1;
		int countYes = 0, countNo = 0;
		
		for (String[] example : examples) {
			if (example[CLASS].equals("yes"))
				countYes++;
			else
				countNo++;
		}

		return (countYes >= countNo) ? "yes" : "no";
	}
}
