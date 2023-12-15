package conc_trace.instr.analysis.pta.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Definition {
	private int location;
	private int argumentLocation = -1;
	private HashSet<Integer> useLocations = new HashSet<>();
	private boolean isNew = false;
	private HashMap<String, Definition> fieldsDefinition = new HashMap<>();
	
	public Definition(int location) {
		this(location, false);
	}
	
	public static Definition createArgument(int location) {
		// no location
		Definition result = new Definition(-1);
		result.argumentLocation = location;
		return result;
	}
	
	
	public Definition getField(String fieldName) {
		// when the definition is initialised during construction/return from function
		if (!fieldsDefinition.containsKey(fieldName)) {
			return this;
		}
		return fieldsDefinition.get(fieldName);
	}
	
	// puts the definition in the given field.
	public void putField(String fieldName, Definition def) {
		this.fieldsDefinition.put(fieldName, def);
	}
	
	private Definition(int location, boolean isNew) {
		this.location = location;
		this.isNew = isNew;
	}
	
	public static final Definition ofLocation(int location) {
		return new Definition(location);
	}
	
	public static final Definition ofNew(int location) {
		return new Definition(location, true);
	}
	
	public boolean hasIntValue() {
		return false;
	}
	
	public int getIntValue() {
		return 0;
	}
	
	
	public int getArgumentLocation() {
		assert(isArgument());
		return this.argumentLocation;
	}
	
	public boolean isArgument() {
		return argumentLocation == -1;
	}
	
	public void addDefinition(Definition def, Definition location) {
		
	}
	
	
	public void addUse(int useLoc, Definition def) {
		addUse(useLoc);
	}
	
	public void addUse(int useLoc) {
		useLocations.add(useLoc);
	}
	
	public boolean isPrimitive() {
		return false;
	}
	
	public boolean isDoubleLength() {
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(location);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Definition other = (Definition) obj;
		return location == other.location;
	}
	
	public Definition subtract(Definition other) {
		return this;
	}
	
	public Definition add(Definition other) {
		return this;
	}
	
	public Definition divide(Definition other) {
		return this;
	}
	
	public Definition mult(Definition other) {
		return this;
	}
	public Definition neg() {
		return this;
	}

	@Override
	public String toString() {
		return "Location: " + this.location;
	}
}
