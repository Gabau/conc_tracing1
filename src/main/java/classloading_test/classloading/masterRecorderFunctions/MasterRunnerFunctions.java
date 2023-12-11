package classloading_test.classloading.masterRecorderFunctions;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;

/**
 * Utility class for adding instrumentation for the
 * master recorder.
 * @author Gabau
 *
 */
public interface MasterRunnerFunctions {
	/**
	 * Instructions needed to log read field
	 * Assumes the arguments are already on the stack.
	 * Object toLog, String desc -> empty
	 * @return
	 */
	public static InstructionList logReadField(ConstantPoolGen cpg) {
		return null;
	}
}
