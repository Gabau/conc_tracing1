package conc_trace.instr.analysis.pta.model;

import java.util.HashMap;

import org.apache.bcel.generic.Type;

public class PrimitiveDefinition extends Definition {
	private Type type;
	private static HashMap<Type, PrimitiveDefinition> primitiveDefinitions = new HashMap<>();
	
	public static PrimitiveDefinition  getPrimitive(Type type) { 
		if (!primitiveDefinitions.containsKey(type)) {
			primitiveDefinitions.put(type, new PrimitiveDefinition(type));
		}
		return primitiveDefinitions.get(type);
	}
	
	private PrimitiveDefinition(Type type) {
		super(0);
		this.type = type;
	}
	
	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isDoubleLength() {
		return this.type.equals(Type.DOUBLE) || this.type.equals(Type.LONG);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return super.equals(obj) && isPrimitive()
				&& this.type == ((PrimitiveDefinition) obj).type;
	}
}
