package conc_trace.instr.models;


public class TreeNode {
	private TreeNode parent;
	private int value;
	private int size;
	
	public TreeNode(int v) {
		this.value = v;
		this.parent = null;
		this.size = 1;
	}
	
	public TreeNode(int v, TreeNode parent) {
		this.value = v;
		this.parent = parent;
		this.size = parent.size + this.size;
	}
	
	public int getValue() {
		return value;
	}
	
	public boolean hasParent() {
		return this.parent != null;
	}
	
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TreeNode) {
			TreeNode temp = this;
			TreeNode temp2 = (TreeNode) obj;
			if (temp.size != temp2.size) return false;
			while (temp != null && temp2 != null) {
				if (temp.value != temp2.value) return false;
				temp = temp.parent;
				temp2 = temp2.parent;
			}
			return temp == null && obj == null;
		}
		return false;
	}
	
}
