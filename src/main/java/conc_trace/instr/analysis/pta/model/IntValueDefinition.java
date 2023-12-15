package conc_trace.instr.analysis.pta.model;

import java.util.HashMap;
import java.util.Objects;

public class IntValueDefinition extends Definition {

	private int value = 0;
	private static HashMap<Integer, IntValueDefinition> intValues = new HashMap<>();
	public IntValueDefinition(int location, int value) {
		super(location);
		// TODO Auto-generated constructor stub
		this.value = 0;
	}
	
	public static IntValueDefinition getValue(int value) {
		if (!intValues.containsKey(value)) {
			intValues.put(value, new IntValueDefinition(-1, value));
		}
		return intValues.get(value);
	}
	@Override
	public boolean hasIntValue() {
		return true;
	}
	@Override
	public int getIntValue() {
		return value;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(value);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntValueDefinition other = (IntValueDefinition) obj;
		return value == other.value;
	}

	
	
}
