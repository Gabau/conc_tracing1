package classloading_test.classloading.analysis;

import org.apache.bcel.classfile.Method;

public class InstructionLocation {
	private int instructionLocation;
	private Method method;
	// class name used to access the location
	private String className;
	
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
