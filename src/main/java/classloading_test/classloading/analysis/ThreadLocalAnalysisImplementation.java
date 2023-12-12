package classloading_test.classloading.analysis;

import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

import classloading_test.classloading.analysis.pta.FakeStack;

public class ThreadLocalAnalysisImplementation implements ThreadLocalAnalysis {
	
	private int getNumOnStack(InvokeInstruction instruction, ConstantPoolGen cpg) {
		return 0;
	}
	
	
	
	/**
	 * Generic idea:
	 *  * Find the bytecode for the entry class
	 *  * Construct CFG for this class
	 *  * Look for any class that is created that is a subclass of Thread -> spawn a new CFG
	 *  * Have a hash set for the possible values that an object could be pointing to the new instructions.
	 *  * ignore java.lang.Thread
	 *  
	 * Simplified algorithm
	 *  * Find locations which this entry point reaches, ignoring threads. Indicate what objects could possibly be there
	 *  * Repeat the same for threads
	 *  * Check which ones overlap -> those are the shared memory locations. 
	 *  
	 *  
	 * @param entryMethod methodName:descriptor, e.g. public static void main(String[] args) -- main:([Ljava/lang/String;)V
	 */
	@Override
	public List<SharedMemoryLocation> sharedMemoryLocations(String entryClass, String entryMethod) 
			throws ClassNotFoundException {
		// to prevent recursion from causing infinite loop
		// --> although should require a new visited for 
		HashSet<String> visited = new HashSet<>();
		JavaClass jc;
		jc = Repository.lookupClass(entryClass);
		ClassGen cg = new ClassGen(jc);
		// get the method
		Method[] methods = cg.getMethods();
		String[] entryMethodDesc = entryMethod.split(":");
		assert(entryMethodDesc.length >= 2);
		Method actualEntryMethod = null;
		for (Method m : methods) {
			if (m.getName().equals(entryMethodDesc[0]) 
					&& m.getSignature().equals(entryMethodDesc[1])) {
				actualEntryMethod = m;
				break;
			}
		}
		if (actualEntryMethod == null) {
			throw new ClassNotFoundException();
		}
		// generate possible locations of objects
		// performing PTA on locations
		MethodGen mg = new MethodGen(actualEntryMethod, entryClass, cg.getConstantPool());
		FakeStack currentFakeStack = null;
		for (InstructionHandle instr : mg.getInstructionList()) {
			Instruction instruction = instr.getInstruction();
			int numAdded = instruction.produceStack(cg.getConstantPool());
			if (numAdded == Const.UNPREDICTABLE) {
				// try to predict -> invoke can make use of method descriptor
				// also have to run the invoke instruction and push the result to stack.
				if (instruction instanceof InvokeInstruction) {
					
				}
			}
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}
}
