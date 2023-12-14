package conc_trace.instr.analysis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.junit.jupiter.api.Test;

import conc_trace.instr.analysis.pta.PTAIntValue;
import conc_trace.instr.analysis.pta.PTAMergeObject;
import conc_trace.instr.analysis.pta.PTAObject;

public class PTAMergeObjectTest {

	// className:methodName:methodSignature
	private static MethodGen getMethodFromString(String methodHash) throws 
		ClassNotFoundException {
		String[] tokens = methodHash.split(":");
		JavaClass jc = Repository.lookupClass(tokens[0]);
		ClassGen cg = new ClassGen(jc);
		for (Method method : jc.getMethods()) {
			if (method.getName().equals(tokens[1])
					&& method.getSignature().equals(tokens[2])) {
				return new MethodGen(method, cg.getClassName(), cg.getConstantPool());
			}
		}
		throw new RuntimeException("Cannot find");
	}
	
	@Test
	public void testPTAMethodReturnHashGeneration() throws ClassNotFoundException {
		String testInput = String.format(
				"%s:%s:%s",
				PTAMergeObjectTest.class.getName(),
				"testPTAMethodReturnHashGeneration",
				"()V");
		MethodGen generateTestMethod = getMethodFromString(testInput);
		PTAObject result = 
				PTAMergeObject.getMethodReturnObject(generateTestMethod);
		
	}
	
	@Test
	public void testMergingOfTypes() {
		PTAObject basicObject = new PTAObject();
		basicObject.addType(Type.BOOLEAN);
		PTAObject basicObject2 = new PTAObject();
		basicObject2.addType(Type.BYTE);
		
		PTAMergeObject mergeObject = new PTAMergeObject();
		mergeObject.union(basicObject);
		mergeObject.union(basicObject2);

		assertTrue(mergeObject.isType(Type.BYTE));
		assertTrue(mergeObject.isType(Type.BOOLEAN));
		assertFalse(mergeObject.isType(Type.INT));
	}
	
	@Test
	public void testGettingIntValue() {
		PTAIntValue intObject = new PTAIntValue(10);
		PTAMergeObject mergeObject = new PTAMergeObject();
		mergeObject.union(intObject);
		assertTrue(mergeObject.isType(Type.INT));
		assertEquals(mergeObject.getValue(), 10);
	}
	
	private PTAMergeObject createMergeObject(Collection<PTAObject> children) {
		PTAMergeObject result = new PTAMergeObject();
		for (PTAObject childObject : children) {
			result.union(childObject);
		}
		return result;
	}
	
	private PTAObject createTypedObject(Type type) {
		PTAObject stringObject = new PTAObject();
		stringObject.addType(type);
		return stringObject;
	}
	
	@Test
	public void testGetField() {
		// test that the field we get, when we update it
		// it will change.
		// only test the types of the field for now.
		ArrayList<PTAObject> childrenObjects = new ArrayList<>();
		childrenObjects.add(new PTAObject());
		childrenObjects.add(new PTAObject());
		childrenObjects.add(new PTAObject());
		childrenObjects.get(0).putField("name", createTypedObject(Type.STRING));
		childrenObjects.get(1).putField("name", createTypedObject(Type.INT));
		childrenObjects.get(2).putField("name", createTypedObject(Type.NULL));
		
		PTAMergeObject mergeObject = createMergeObject(childrenObjects);
		PTAObject fieldObject = mergeObject.getField("name");
		assertTrue(fieldObject.isType(Type.STRING));
		assertTrue(fieldObject.isType(Type.NULL));
		assertTrue(fieldObject.isType(Type.INT));
		assertTrue(fieldObject == mergeObject.getField("name"));
		
		// test putting to the merge object
		mergeObject.putField("name", createTypedObject(Type.NULL));
		assertTrue(childrenObjects.get(0).getField("name").isType(Type.NULL));
		assertTrue(childrenObjects.get(1).getField("name").isType(Type.NULL));
		fieldObject = mergeObject.getField("name");
		assertTrue(fieldObject.isType(Type.NULL));
		assertFalse(fieldObject.isType(Type.STRING));
	}
	
	@Test
	public void testChainOfMerge() {
		PTAObject rootObject = createTypedObject(Type.STRING);
		PTAMergeObject mergeObject1 = new PTAMergeObject();
		PTAMergeObject mergeObject2 = new PTAMergeObject();
		PTAMergeObject mergeObject3 = new PTAMergeObject();
		mergeObject1.union(rootObject);
		
	}
	
	
	
	
}
