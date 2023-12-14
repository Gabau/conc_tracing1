package conc_trace.instr.analysis.pta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;

import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang3.NotImplementedException;

import conc_trace.instr.analysis.InstructionLocation;
import conc_trace.instr.analysis.pta.exceptions.PTAInstructionExecutorException;

/**
 * Represents a generic object on the stack.
 * Can be a primitive.
 * @author Gabau
 */
public class PTAObject {

	private static class PrimitivePTAObject extends PTAObject {
		
	}
	
	// todo: determine if the void return is needed, doesn't seem like 
	// jvm pushes void return onto stack.
	private static final PTAObject VOID_RETURN = new PTAObject();
	// represents an unknown int value
	private static PTAObject EMPTY_INT_VALUE;
	
	public static enum State {
		TYPE_UNKNOWN, // when we haven't determined the type of this object
		TYPE_DETERMINED // when we determined the type.
	}
	// the signature string for the type
	protected HashSet<Type> type = new HashSet<>();
	// todo: create a factory method for this -> for see alot of duplicate
	// superclasses. Can do the same for type to severely reduce the heap use
	protected HashSet<String> superClass = new HashSet<>();
	
	// access locations
	protected LinkedList<InstructionLocation> accessLocations = new LinkedList<>();
	
	/**
	 * The location where this object was instantiated.
	 * Should be either a new instruction or an invoke instruction
	 * Invoke iff the object was created by a function,
	 * new iff the object was created directly.
	 */
	protected InstructionLocation creationLocation;
	protected State state = State.TYPE_DETERMINED;
	protected HashMap<String, PTAObject> fields = new HashMap<>();
	
	
	
	public PTAObject() {
		
	}
	
	/**
	 * Puts the given object into the field.
	 * @param fieldName
	 * @param field
	 */
	public void putField(String fieldName, PTAObject field) {
		fields.put(fieldName, field);
	}
	
	/**
	 * Gets the object ref at a given field.
	 * @param fieldName
	 * @return
	 */
	public PTAObject getField(String fieldName) {
		return fields.get(fieldName);
	}
	
	
	/**
	 * Adds an access location to the object.
	 * @param location
	 */
	public void addAccessLocation(InstructionLocation location) {
		accessLocations.add(location);
	}
	
	
	public boolean isVoid() {
		return this.isType(Type.VOID);
	}
	
	protected PTAObject(Type objectType) {
		this.type.add(objectType);
	}
	
	/**
	 * Creates a type to add to the object.
	 * @param objectType
	 */
	public void addType(Type objectType) {
		this.type.add(objectType);
	}
	
	public void addSuperClass(String className) {
		if (this.isVoid()) {
			throw new PTAInstructionExecutorException("Adding super class to wrong loc", creationLocation);
		}
		this.superClass.add(className);
	}
	
	public boolean isType(Type objectType) {
		return this.type.contains(objectType);
	}
	
	public int getValue() {
		throw new NotImplementedException();
	}
	
	public boolean isIntValue() {
		return false;
	}
	
	/**
	 * Generic PTAnalysis object.
	 */
	public PTAObject(InstructionLocation location) {
		type = null;
		superClass = null;
		state = State.TYPE_UNKNOWN;
		this.creationLocation = location;
	}
	
	public boolean isDoubleOrLong() {
		if (type == null || state == State.TYPE_UNKNOWN) {
			return false;
		}
		return type.contains(Type.LONG.getSignature()) || type.contains(Type.DOUBLE.getSignature());
	}
	
	public boolean isArrayRef() {
		return false;
	}
	
	// might not be needed
	@SuppressWarnings("unchecked")
	@Override
	protected PTAObject clone() throws CloneNotSupportedException {
		PTAObject result = new PTAObject(creationLocation);
		result.type = (HashSet<Type>) this.type.clone();
		result.superClass = (HashSet<String>) this.superClass.clone();
		return result;
	}
	@Override
	public int hashCode() {
		return Objects.hash(creationLocation, state, superClass, type);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PTAObject other = (PTAObject) obj;
		return Objects.equals(creationLocation, other.creationLocation)
				&& state == other.state
				&& Objects.equals(superClass, other.superClass) 
				&& Objects.equals(type, other.type)
				// TODO: not needed to equals
				&& Objects.equals(fields, other.fields);
	}
}
