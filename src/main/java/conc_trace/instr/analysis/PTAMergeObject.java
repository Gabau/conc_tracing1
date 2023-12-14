package conc_trace.instr.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import conc_trace.instr.analysis.pta.PTAObject;

/**
 * Represents a union of a group of objects.
 * @author Gabau
 *
 */
public class PTAMergeObject extends PTAObject {

	// Should not have any cycles!!!
	private LinkedList<PTAObject> childrenObjects = new LinkedList<>();
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
	
	/**
	 * Check if any processing needs to be done.
	 * @return
	 */
	private boolean hasChanged() {
		return childrenSize != childrenObjects.size();
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
	
	private void process() {
		if (childrenSize == childrenObjects.size()) {
			return;
		}
		if (listIterator == null) {
			listIterator = childrenObjects.listIterator();
		}
		while (listIterator.hasNext()) {
			PTAObject current = listIterator.next();
			process(current);
		}
		
	}
	
	public void union(PTAObject childObject) {
		assert(childObject != this);
		this.childrenObjects.add(childObject);
	}
	
	/**
	 * Puts a field and propogates the field to all child references.
	 */
	@Override
	public void putField(String fieldName, PTAObject field) {
		for (PTAObject child : childrenObjects) {
			child.putField(fieldName, field);
		}
		
		super.putField(fieldName, field);
	}

	/**
	 * Gets a field reference. Wrapper around all the child field references.
	 * TODO: Try out optimising this brute force getter
	 */
	@Override
	public PTAObject getField(String fieldName) {
		process();
		// todo: check if this is still needed after process
		// is added
		if (!this.fields.containsKey(fieldName)) {
			// create the new merge object
			PTAMergeObject generatedObject = new PTAMergeObject();
			for (PTAObject child: childrenObjects) {
				generatedObject.union(child.getField(fieldName));
			}
			// add the field to the parent object.
			super.putField(fieldName, generatedObject);
		}
		return super.getField(fieldName);
	}

	@Override
	public void addAccessLocation(InstructionLocation location) {
		// TODO Auto-generated method stub
		super.addAccessLocation(location);
	}

	@Override
	public boolean isType(Type objectType) {
		process();
		if (super.isType(objectType)) {
			return true;
		}
		for (PTAObject child: childrenObjects) {
			if (child.isType(objectType)) {
				super.addType(objectType);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isIntValue() {
		if (childrenObjects.size() == 0) return false;
		// two things needed -> all the fields are int values
		// and all the values are the same.
		boolean isIntVal = true;
		Integer current = null;
		// TODO: can add this to preprocessing
		// there is little chance that this is needed tho
		for (PTAObject objects: childrenObjects) {
			if (!objects.isIntValue()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getValue() {
		return childrenObjects.getFirst().getValue();
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

	
}
