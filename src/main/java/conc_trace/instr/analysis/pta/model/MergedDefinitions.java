package conc_trace.instr.analysis.pta.model;

import java.util.HashSet;
import java.util.Objects;

public class MergedDefinitions extends Definition {

	HashSet<Definition> definitions = new HashSet<>();
	
	public void merge(Definition def) {
		this.definitions.add(def);
	}
	
	public void merge(MergedDefinitions defs) {
		this.definitions.addAll(defs.definitions);
	}
	
	public MergedDefinitions() {
		super(-1);
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	public boolean hasIntValue() {
		Integer val = null;
		for (Definition child : definitions) {
			if (!child.hasIntValue()) {
				return false;
			}
			if (val != null && child.getIntValue() != val) {
				return false;
			}
			val = child.getIntValue();
		}
		return true;
	}

	@Override
	public int getIntValue() {
		for (Definition def : definitions) {
			return def.getIntValue();
		}
		throw new RuntimeException("Invalid value access");
	}

	@Override
	public void addUse(int useLoc, Definition def) {
		for (Definition child : definitions) {
			child.addUse(useLoc, def);
		}
		super.addUse(useLoc, def);
	}

	@Override
	public boolean isDoubleLength() {
		// TODO Auto-generated method stub
		return super.isDoubleLength();
	}

	@Override
	public boolean isPrimitive() {
		// TODO Auto-generated method stub
		return super.isPrimitive();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(definitions);
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
		MergedDefinitions other = (MergedDefinitions) obj;
		return Objects.equals(definitions, other.definitions);
	}

	@Override
	public void addUse(int useLoc) {
		for (Definition definition: definitions){ 
			definition.addUse(useLoc);
		}
	}

}


