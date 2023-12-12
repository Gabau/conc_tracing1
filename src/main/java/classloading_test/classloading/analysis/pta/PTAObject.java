package classloading_test.classloading.analysis.pta;

import java.util.HashSet;

import org.apache.bcel.generic.Type;

import classloading_test.classloading.analysis.InstructionLocation;

/**
 * Represents a generic object on the stack.
 * Can be a primitive.
 * @author Gabau
 */
public class PTAObject {


	public static enum State {
		TYPE_UNKNOWN, // when we haven't determined the type of this object
		TYPE_DETERMINED // when we determined the type.
	}
	// the signature string for the type
	protected HashSet<Type> type = new HashSet<>();
	// todo: create a factory method for this -> for see alot of duplicate
	// superclasses. Can do the same for type to severely reduce the heap use
	protected HashSet<String> superClass = new HashSet<>();

	/**
	 * The location where this object was instantiated.
	 * Should be either a new instruction or an invoke instruction
	 * Invoke iff the object was created by a function,
	 * new iff the object was created directly.
	 */
	protected InstructionLocation creationLocation;
	
	protected State state = State.TYPE_DETERMINED;
	
	public PTAObject() {
		
	}
	
	public PTAObject(Type objectType) {
		this.type.add(objectType);
	}
	
	public void addType(Type objectType) {
		this.type.add(objectType);
	}
	
	public void addSuperClass(String className) {
		this.superClass.add(className);
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
}
