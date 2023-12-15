package conc_trace.instr.analysis.pta.model;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Array definition
 * @author Gabau
 *
 */
public class ArrayDefinition extends Definition {

	private HashMap<Integer, Definition> arrayDefinitions = new HashMap<>();
	private static int ArrayUID = 0;
	private int id;
	
	// each array definition has a unique id.
	public ArrayDefinition() {
		super(-1);
		this.id = ArrayUID;
		ArrayUID += 1;
	}
	
	




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(id);
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
		ArrayDefinition other = (ArrayDefinition) obj;
		return id == other.id;
	}

	private MergedDefinitions mergedDefinitions = new MergedDefinitions();
	
	public ArrayDefinition(int location) {
		super(location);
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	public void addDefinition(Definition def, Definition location) {
//		if (location.hasIntValue()) {
//			arrayDefinitions.put(location.getIntValue(), def);
//			return;
//		}
		mergedDefinitions.merge(def);
	}



	@Override
	public void addUse(int useLoc, Definition def) {
//		if (def.hasIntValue()) {
//			arrayDefinitions.get(def.getIntValue()).addUse(useLoc);
//			return;
//		}
		addUse(useLoc);
	}

	@Override
	public void addUse(int useLoc) {
		for (Entry<Integer, Definition> entry : arrayDefinitions.entrySet()) {
			entry.getValue().addUse(useLoc);
		}
	}
}
