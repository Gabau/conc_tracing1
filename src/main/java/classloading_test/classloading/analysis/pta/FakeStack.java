package classloading_test.classloading.analysis.pta;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

import classloading_test.classloading.analysis.InstructionLocation;

/**
 * A stack used for generating values
 * @author Gabau
 *
 */
public class FakeStack {
	private Stack<PTAObject> stack = new Stack<>();
	// an empty FakeStack.
	public FakeStack() {
		
	}
	
	public FakeStack(PTAObject object) {
		stack.push(object);
	}
	
	public PTAObject getObject() {
		return stack.peek();
	}
	
	public boolean hasValues() {
		return !stack.isEmpty();
	}
	
	// get the next fake stack
	public FakeStack pop() {
		stack.pop();
		return this;
	}
	
	/**
	 * Pops words number of words from the stack, and returns the values
	 * @param words The number of words top get from the stack, without modifying the stack.
	 * @return
	 */
	public FakeStack popWords(int words) {
		FakeStack result = new FakeStack();
		int numOfWords = 0;
		while (numOfWords != words) {
			PTAObject topObject = getObject();
			if (topObject.isDoubleOrLong()) {
				numOfWords += 2;
			} else {
				numOfWords += 1;
			}
			result = result.push(topObject);
			pop();
		}
		
		return result;
	}
	
	public void addStack(FakeStack stack) {
		while (stack.hasValues()) {
			push(stack.getObject());
			stack.pop();
		}
	}

	
	public FakeStack push(PTAObject type) {
		stack.push(type);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(stack);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FakeStack other = (FakeStack) obj;
		return Objects.equals(stack, other.stack);
	}

	@Override
	public String toString() {
		return this.stack.toString();
	}

	
}
