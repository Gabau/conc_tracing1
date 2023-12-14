package conc_trace.instr.analysis.pta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import conc_trace.instr.analysis.InstructionLocation;
import conc_trace.instr.analysis.pta.exceptions.PTAInstructionExecutorException;

/**
 * Represents a union of a group of objects.
 * @author Gabau
 *
 */
public class PTAMergeObject extends PTAObject {
	
	private HashSet<PTAObject> childrenObjectSet = new HashSet<>();
	private Iterator<PTAObject> listIterator = null;
	// used to keep track of any changes
	// only update once processing is done
	private int childrenSize = 0;
	/**
	 * All the method returns
	 * Method ref -> return object
	 */
	private static HashMap<String, PTAMergeObject> methodReturnObjects
		= new HashMap<>();
	
	public static String generateHash(MethodGen method) {
		return String.format("%s:%s:%s", method.getClassName(), 
				method.getName(),
				method.getSignature());
	}
	
	// TODO:
	// Add types optimisation.
	private void process(PTAObject childObject) {
		// add to all the fields in the current field map
		for (String key : fields.keySet()) {
			PTAMergeObject mergeField = (PTAMergeObject) fields.get(key);
			mergeField.union(childObject.getField(key));
		}
		
	}
	

	
	/**
	 * Instances when union is called:
	 *  * 
	 * @param childObject
	 */
	public void union(PTAObject childObject) {
		assert(!(childObject instanceof PTAMergeObject));
		this.childrenObjectSet.add(childObject);
	}
	
	// todo -> make use of union find to flatten this tree.
	public void union(PTAMergeObject childObject) {
		this.childrenObjectSet.addAll(childObject.childrenObjectSet);
//		this.childrenObjects.addAll(childObject.childrenObjects);
	}
	
	/**
	 * Puts a field and propogates the field to all child references.
	 */
	@Override
	public void putField(String fieldName, PTAObject field) {
		for (PTAObject child : childrenObjectSet) {
			child.putField(fieldName, field);
		}
		PTAMergeObject newMergeObject = new PTAMergeObject();
		newMergeObject.union(field);
		super.putField(fieldName, newMergeObject);
	}

	/**
	 * Gets a field reference. Wrapper around all the child field references.
	 * TODO: Try out optimising this brute force getter
	 */
	@Override
	public PTAObject getField(String fieldName) {
		// todo: check if this is still needed after process
		// is added
		if (!this.fields.containsKey(fieldName)) {
			// create the new merge object
			PTAMergeObject generatedObject = new PTAMergeObject();
			for (PTAObject child: childrenObjectSet) {
				generatedObject.union(child.getField(fieldName));
			}
			// add the field to the parent object.
			super.putField(fieldName, generatedObject);
		}
		return super.getField(fieldName);
	}

	@Override
	public void addAccessLocation(InstructionLocation location) {
		// propogate access location to children
		for (PTAObject childObject: childrenObjectSet) {
			childObject.addAccessLocation(location);
		}
		// only store the access locations of the merge object
		// TODO: consider not storing this location.
		super.addAccessLocation(location);
	}

	@Override
	public boolean isType(Type objectType) {
		for (PTAObject child: childrenObjectSet) {
			if (child.isType(objectType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isIntValue() {
		if (childrenObjectSet.size() == 0) return false;
		// two things needed -> all the fields are int values
		// and all the values are the same.
		boolean isIntVal = true;
		// TODO: can add this to preprocessing
		// there is little chance that this is needed tho
		for (PTAObject objects: childrenObjectSet) {
			if (!objects.isIntValue()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getValue() {
		for (PTAObject object : childrenObjectSet) {
			return object.getValue();
		}
		throw new PTAInstructionExecutorException("Invalid value", creationLocation);
	}

	public PTAMergeObject() {
	}
	
	public static PTAMergeObject getMethodReturnObject(MethodGen method) {
		String methodHash = generateHash(method);
		if (!methodReturnObjects.containsKey(methodHash)) {
			methodReturnObjects.put(methodHash, new PTAMergeObject());
		}
		return methodReturnObjects.get(methodHash);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(childrenObjectSet);
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
		PTAMergeObject other = (PTAMergeObject) obj;
		return Objects.equals(childrenObjectSet, other.childrenObjectSet)
				&& Objects.equals(fields, other.fields);
	}

	
}
