package classloading_test.classloading.analysis;

/**
 * Used to perform analysis by faking a stack with values to push and pop.
 * The idea behind this is to generate the tree of possible values.
 * @author Gabau
 *
 */
public class FakeStack {
	// the location that this stack was pushed
	private InstructionLocation pushLocation;
	// the type of item on the stack
	private String type;
	private FakeStack previous;
	
	// an empty FakeStack.
	public FakeStack() {
		
	}
	
	public FakeStack(InstructionLocation location, String type) {
		this.pushLocation = location;
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public boolean hasPrevious() {
		return this.previous != null;
	}
	
	// get the next fake stack
	public FakeStack pop() {
		return this.previous;
	}
	
	public FakeStack push(InstructionLocation location, String type) {
		FakeStack next = new FakeStack(location, type);
		next.previous = this;
		return next;
	}
}
