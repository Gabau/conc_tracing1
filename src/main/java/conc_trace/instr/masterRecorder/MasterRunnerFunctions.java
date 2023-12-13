package conc_trace.instr.masterRecorder;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;

import conc_trace.instr.added.MasterRecorder;

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
		// first load the method into the constant pool
		int mr = cpg.addMethodref(MasterRecorder.class.getName().replace(".", "/"), "onReadField", 
				"(Ljava/lang/Object;Ljava/lang/String;)V");
		InstructionList result = new InstructionList();
		result.append(new INVOKESTATIC(mr));
		return result;
	}
}
