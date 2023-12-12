package classloading_test.classloading.analysis;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.junit.jupiter.api.Test;

public class JavaInstructionCFGTest {

	/**
	 * Method used to construct the CFG during testing
	 */
	private void testMethod() {
		int a = 0;
		int b = 1;
		int c = 1;
		int n = 10;
		for (int i = 0; i < n; ++i) {
			a = b;
			b = c;
			c = a + b;
		}
		System.out.println(c);
	}
	
	@Test
	public void testCFGConstruction() {
		/**
		 * Test construction of CFG using existing methods.
		 */
		JavaClass jc = null;
		try {
			jc = Repository.lookupClass(JavaInstructionCFGTest.class);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(jc);
		ClassGen cg = new ClassGen(jc);
		Method testMethod = null;
		for (Method method : cg.getMethods()) {
			if (method.getName().equals("testMethod") && method.getSignature().equals("()V")) {
				testMethod = method;
			}
		}
		assertNotNull(testMethod);
		HashMap<String, JavaInstructionCFG> result = JavaInstructionCFG.fromMethodRecursive(new MethodGen(testMethod, cg.getClassName(), cg.getConstantPool()));
		JavaInstructionCFG actualK = result.get("classloading_test.classloading.analysis.JavaInstructionCFGTest:testMethod:()V");
		System.out.println(result);
		System.out.println(result.get("JavaInstructionCFGTest:testMethod:()V"));
	}
}
