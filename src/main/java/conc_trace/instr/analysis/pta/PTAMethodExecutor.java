package conc_trace.instr.analysis.pta;

import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import conc_trace.instr.analysis.InstructionLocation;
import conc_trace.instr.analysis.JavaInstructionCFG;
import conc_trace.instr.analysis.JavaInstructionCFGNode;

public interface PTAMethodExecutor {

	/**
	 * Executes given method with parameters
	 * Plan -> execute until there is no change
	 * Initial state of worklist algorithm -> the algorithm propogates the stack
	 * Once the stack has no change, we do one more iteration.
	 * - have to run methods inside that have not been visited.
	 * - after which, the correct def-use pairs should have been obtained.
	 * 
	 * node1 out: FakeStack
	 * node2 in: new FakeStack
	 * 
	 * @param method
	 * @param paramList
	 * @return
	 */
	public static PTAObject methodExecutor(JavaInstructionCFG cfg,
			List<PTAObject> paramList) {
		JavaInstructionCFGNode root = cfg.getRoot();
		List<InstructionHandle> instructions = root.getInstructionHandles();
		PTAInstructionExecutor executor = new PTAInstructionExecutor();
		executor.setCPG(cfg.getCPG());
		executor.setCurrentStack(new FakeStack());
		executor.setLocalVariables(paramList);

		return executor.getOutput();
	}
	
	/**
	 * Used to merge two stacks. The new stack size is the
	 * max of the old stack size -- ideally the two stacks should be
	 * the same size.
	 * @param f1
	 * @param f2
	 * @return
	 */
	static FakeStack mergeStacks(FakeStack f1, FakeStack f2) {
		FakeStack newFakeStack = new FakeStack();
		List<PTAObject> values = new LinkedList<>();
		while (f1.hasValues() && f2.hasValues()) {
			PTAObject n1 = f1.getObject();
			PTAObject n2 = f2.getObject();
			f1.pop();
			f2.pop();
			PTAMergeObject newObject = new PTAMergeObject();
			newObject.union(n1);
			newObject.union(n2);
			values.add(newObject);
		}
		while (f1.hasValues()) {
			values.add(f1.getObject());
		}
		while (f2.hasValues()) {
			values.add(f2.getObject());
		}
		for (PTAObject value :  values) {
			newFakeStack.push(value);
		}
		return newFakeStack;
	}
	
	static void executeNode(JavaInstructionCFGNode node, 
			JavaInstructionCFG cfg,
			PTAInstructionExecutor executor) {
		List<InstructionHandle> instructions = node.getInstructionHandles();
		for (InstructionHandle instr: instructions) {
			int position = instr.getPosition();
			InstructionLocation location = 
					InstructionLocation.fromCFGInstruction(cfg, position);
			executor.setInstructionLocation(location);
			instr.getInstruction().accept(executor);
		}
		// duplicate the stack to run on the other instructions.
	}
}
