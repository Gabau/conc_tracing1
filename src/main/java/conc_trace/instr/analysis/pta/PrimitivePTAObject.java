package conc_trace.instr.analysis.pta;

import java.util.HashMap;

import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Type;

import conc_trace.instr.analysis.InstructionLocation;

public class PrimitivePTAObject extends PTAObject {

	private static HashMap<BasicType, PrimitivePTAObject> primitiveObjects = new HashMap<>();
	
	private PrimitivePTAObject(BasicType type) {
		super.addType(type);
	}
	

	/**
	 * Get an objecct representing a void return.
	 * @return
	 */
	public static PTAObject getVoidReturn() {
		return getPrimitive(Type.VOID);
	}
	
	// flyweight pattern
	public static PrimitivePTAObject getPrimitive(BasicType type) {
		if (!primitiveObjects.containsKey(type)) {
			primitiveObjects.put(type, new PrimitivePTAObject(type));
		}
		return primitiveObjects.get(type);
	}
	public static PTAObject getUnknownIntObject() {
		return PrimitivePTAObject.getPrimitive(Type.INT);
	}
	
	@Override
	public void addAccessLocation(InstructionLocation location) {
		// TODO Auto-generated method stub
		super.addAccessLocation(location);
	}

	@Override
	public void addType(Type objectType) {
		// does nothing
	}

	@Override
	public void addSuperClass(String className) {
		// does nothing
	}
	
}
