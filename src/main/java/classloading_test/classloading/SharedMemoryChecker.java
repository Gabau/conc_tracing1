package classloading_test.classloading;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.MethodGen;

public interface SharedMemoryChecker {

	// todo: check if this shared memory should be instrumented
	// basically specify the classes that should be a shared memory
	public static boolean shouldInstrument(String objType, Instruction instr, MethodGen method, String className, ConstantPoolGen cpg) {
		return objType.equals("SomeType");
	}
}
