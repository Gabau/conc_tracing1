package classloading_test.classloading.analysis;

import org.apache.bcel.classfile.Method;

/**
 * Represents an instruction location.
 * Unique for a given CFG instruction
 * @author Gabau
 *
 */
public class InstructionLocation {
	public static enum Type {
		STATIC_CREATION, // created as a static object
		METHOD, // in a method
		PARAMETER, // the parameter of the method -> only needed for the entry point.
		NEW_CREATION, // created by new instruction
		NEW_ARRAY // created via a new array
	}
	protected int instructionLocation;
	protected Method method;
	// class name used to access the location
	protected String className;
	protected Type instructionType;

	public static InstructionLocation fromCFGInstruction(JavaInstructionCFGNode node, int instruction) {
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InstructionLocation) {
			InstructionLocation otherLocation = (InstructionLocation) obj;
			return this.method.equals(otherLocation.method)
					&& this.instructionLocation == otherLocation.instructionLocation
					&& this.className.equals(otherLocation.className);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return instructionLocation ^ method.hashCode() ^ className.hashCode();
	}
}
