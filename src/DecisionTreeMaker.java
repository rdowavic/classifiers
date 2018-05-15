import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;

public class DecisionTreeMaker {
	
	public void printTree(Node root, int depth) {
		Node current = root;
		
//		System.out.println("Splitting on " + root.data);
//		System.out.println("depth " + depth);
		for (Pair pair : current.children) {
			// print like label : then data
//			System.out.println(pair.label + ": " + pair.node.data);
			printTree(pair.node, depth+1);
		}
		System.out.println();
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
			System.out.println("Attribute: " + attribute);
			// choose next 'current' to go to
			for (Pair branch : current.children) {
				System.out.println("Branch label: " + branch.label);
				if (branch.label.equals(example[columnNum(attribute, attributes)])) {
					current = branch.node;
					break;
				}
			}
		}
		// has no children, so must be the answer
		// this should be yes or no, fucking fingers crossed bro
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
		return makeDecisionTree(examples, atts, atts, null);
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
		else if (attributes.isEmpty())
			return new Node(majorityClass(examples, allAttributes));
		else {
			String best = chooseAttribute(attributes, examples);
			/*Okay we're sort of recursively making this subtree thing.
			 *Once we have the best attribute to split on,
			 *we have to go through all the possible values of that attribute
			 *(they are like the branches) then they form their very own 
			 * DTL situations over there. Good for them.
			 */
			Node tree = new Node(best);
			int columnBest = columnNum(best, allAttributes);
			
			for (String value : allValues(columnBest, examples)) {
				ArrayList<String[]> childExamples = reducedExamples(examples, columnBest, value);
				ArrayList<String> attsWithBestRemoved = new ArrayList<String>(attributes);
				attsWithBestRemoved.remove(best);
				// making the recursive call here to construct the child subtree
				// shit
				Node child = makeDecisionTree(
						childExamples, 
						attsWithBestRemoved,
						allAttributes,
						majorityClass(examples, allAttributes)
				);
				
				// adding a branch to tree with label v_i and subtree `subtree` :)
				tree.addChild(value, child); 			
			}
			return tree;
		}
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
	private String chooseAttribute(ArrayList<String> attributes, ArrayList<String[]> examples) {
		String bestAttr = null;
		double bestGain = 0;
		
		for (String attr : attributes) {
			double attrGain = informationGain(columnNum(attr, attributes), examples, attributes);
			if (attrGain > bestGain || bestAttr == null) {
				bestAttr = attr;
				bestGain = attrGain;
			}
		}
		return bestAttr;
	}
	
	private double informationGain(int attrIndex, ArrayList<String[]> examples, ArrayList<String> attributes) {
		/*
		 * Gain(examples | attr) = information gain = I(
		 */
		return Entropy(examples, attributes) - Reminder(attrIndex, examples, attributes);
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
		
		for (String value : allValues(attrIndex, examples)) {
			double probValue = count(examples, attrIndex, value) / (double) examples.size();
			// get the list of examples such that example[attrIndex] == value
			ArrayList<String[]> onesWithThisValue = reducedExamples(examples, attrIndex, value);
			total += probValue * Entropy(onesWithThisValue, attributes);
		}
		return total;
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
			result += Pi * Math.log(Pi) / Math.log(2);
		}
		return -1 * result;
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
	private String majorityClass(ArrayList<String[]> examples, ArrayList<String> attributes) {
		ArrayList<String> classes = new ArrayList<String>();
		for (String[] example : examples)
			classes.add(example[columnNum("class", attributes)]);
		return mode(classes);
	}
	
	/**
	 * 
	 * @param values - an array list of strings
	 * @return the most frequently occurring string in the array list
	 */
	private String mode(ArrayList<String> values) {
		Map<String, Integer> valueCount = new HashMap<String, Integer>();
	    for (String value : values) {
	      if (!valueCount.containsKey(value))
	          valueCount.put(value, 0);
	      valueCount.put(value, valueCount.get(value) + 1);
	    }

	    // finding the most commonly occurring one
	    Map.Entry<String, Integer> mostFrequent = null;

	    for (Map.Entry<String, Integer> entry : valueCount.entrySet()) {
	      if (mostFrequent == null || entry.getValue() > mostFrequent.getValue())
	        mostFrequent = entry;
	    }
	    return mostFrequent.getKey();
	}
	
	
}
