package conc_trace.instr.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.junit.jupiter.api.Test;

import conc_trace.instr.analysis.pta.DefUseInstructionExecutor;
import conc_trace.instr.analysis.pta.model.Definition;

public class DefUseExecutorTest {
	
	private static class TestClass {
		public int x = 0;
		public int y = 0;
	}
	
	private void testMethod() {
		// test the propogation of types
		TestClass tc = new TestClass();
		tc.x = 10;
		tc.y = 20;
		int m = tc.y;
		if (tc.x < 100) {
			tc.y = 1000;
		}
	}
	
	private MethodGen loadMethod(Class<?> clazz, String methodName) throws ClassNotFoundException {
		ClassGen cg = new ClassGen(Repository.lookupClass(clazz));
		Method[] methods = cg.getMethods();
		for (Method method: methods) {
			if (method.getName().equals(methodName)) {
				return new MethodGen(method, cg.getClassName(), cg.getConstantPool());
			}
		}
		assert(false);
		return null;
	}
	
	@Test
	public void testRunInstructions() throws ClassNotFoundException {
		DefUseInstructionExecutor executor = new DefUseInstructionExecutor();
		// load test method
		MethodGen m = loadMethod(this.getClass(), "testMethod");
		HashMap<String, JavaInstructionCFG> cfg = JavaInstructionCFG.fromMethodRecursive(m);
		JavaInstructionCFG methodCFG = cfg.get(JavaInstructionCFG.generateHash(m));
		executor.setCPG(m.getConstantPool());
		executor.initialiseParamSize(m.getMaxLocals());
		for (InstructionHandle handle : 
			methodCFG.getRoot().getInstructionHandles()) {
			executor.setCurrentOffset(handle.getPosition());
			handle.getInstruction().accept(executor);
		}
		HashMap<Integer, Definition> definitions = executor.getDefinitions();
		System.out.println(definitions);
	}
}
