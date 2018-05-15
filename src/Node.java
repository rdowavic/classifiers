import java.util.*;

public class Node {
	public String data;
	public ArrayList<Pair> children;
	
	public Node(String data) {
		this.data = data;
		this.children = new ArrayList<Pair>();
	}
	public void addChild(String label, Node child) {
		children.add(new Pair(label, child));
	}
}
