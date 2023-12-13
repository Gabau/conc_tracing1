package conc_trace.instr;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Instruction;

public interface ToRecordStore {
	/**
	 * Uses the given context to determine if a particular variable in a class
	 * should be recorded
	 * Reference should be in byte code
	 * @return
	 */
	public boolean shouldRecord(String className, Method method, Instruction instruction);
	
}
