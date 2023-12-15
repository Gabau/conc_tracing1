package conc_trace.instr.analysis.pta.model;

/**
 * Represents the set of fields that will be changed by a method.
 * Can kill the local variables or the 
 * @author Gabau
 *
 */
public interface KillSet {
	
	/**
	 * Returns true iff the given field
	 * access of the definition has been killed.
	 * 
	 * @param def
	 * @return
	 */
	public boolean containsDefinition(int index, String name);
	
}
