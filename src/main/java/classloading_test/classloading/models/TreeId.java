package classloading_test.classloading.models;

import java.util.LinkedList;

// a representation of id using a list of ids
// root has the actualId be empty
public class TreeId {
	
	private TreeNode root;
	private int maxId = 0;
	public int internalHashCode = 100002301;
	
	// Josh Bloch's hashing
	// https://stackoverflow.com/questions/113511/best-implementation-for-hashcode-method-for-a-collection
	private void precomputeHashCode() {
		TreeNode temp = root;
		while (temp != null) {
			int k = temp.getValue();
			k = k & (k >>> 32);
			internalHashCode = 37 * internalHashCode + k;
			temp = temp.getParent();
		}
	}
	public TreeId() {
		root = new TreeNode(0);
	}
	
	public TreeId createChildTreeId() {
		TreeId child = new TreeId();
		child.root = new TreeNode(maxId, this.root);
		this.maxId ++;
		child.precomputeHashCode();
		return child;
	}
	
	
	
	@Override
	public String toString() {
		if (!root.hasParent()) {
			return "root";
		}
		StringBuilder result = new StringBuilder();
		TreeNode temp = root;
		while (temp != null) {
			result.append(temp.getValue());
			result.append(";");
			temp = temp.getParent();
		}
		return result.toString();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TreeId) {
			TreeId other = (TreeId) obj;
			return this.root.equals(other.root);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return internalHashCode;
	}	
}
