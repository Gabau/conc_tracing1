package classloading_test.classloading.added;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import classloading_test.classloading.models.TreeId;

/**
 * Class used by application
 * @author Gabau
 *
 */
public class MasterRecorder {
	// the read count vector
	public static ConcurrentHashMap<Long, ConcurrentHashMap<Long, Integer>> rcVector = new ConcurrentHashMap<>(); 
	// the lastwrite location
	public static ConcurrentHashMap<Long, Event> lw = new ConcurrentHashMap<>();
	// write->read event list
	public static ConcurrentHashMap<Long, ArrayList<Event>> WR = new ConcurrentHashMap<Long, ArrayList<Event>>();
	// stores the max id map.
	public static ConcurrentHashMap<Long, TreeId> threadMaxId = new ConcurrentHashMap<>();
	

	// used to assign unique id to all shared objects
	public static long maxObjectId = 0;
	
	public static void onGetField(String value) {
		
	}
	
	
	public static void onGetStatic() {
		
	}

	public static void addThreadId(long newThreadId) {
		long currId = Thread.currentThread().getId(); // parent id
		TreeId parent = threadMaxId.get(currId);
		if (parent == null) {
			parent = new TreeId();
			threadMaxId.put(currId, parent);
		}
		TreeId child = parent.createChildTreeId();
		threadMaxId.put(newThreadId, child);
	}
	
	/**
	 * Gets the tree id for current thread.
	 * @return
	 */
	public static TreeId getCurrentThreadTreeId() {
		TreeId current = threadMaxId.get(Thread.currentThread().getId());
		if (current == null) {
			current = new TreeId();
			synchronized (threadMaxId) {
				threadMaxId.put(Thread.currentThread().getId(), current);
			}
		}
		return current;
	}
	
	public static void increment() {
		maxObjectId += 1;
	}
	
	public static class Event {
		public final long currThread;
		public final Integer value;
		public final Integer counter;
		public final String field;
		public final Long variable; // the unique id of the variable
		public Event(long currThread, Integer value, Integer counter, Long variable, String field) {
			this.value = value;
			this.currThread = currThread;
			this.counter = counter;
			this.variable = variable;
			this.field = field;
		}
	}
	
	
	
	public static Event onRead(Event event) {
		long thread = Thread.currentThread().getId();
		if (rcVector.get(thread) == null) {
			rcVector.put(thread, new ConcurrentHashMap<>());
		}
		rcVector.get(thread).put(event.variable, rcVector.get(thread).get(event.variable) + 1);
		Event prevWrite = null;
		synchronized (lw) {
			prevWrite = lw.get(event.variable);
		}
		return prevWrite;
	}
	
	public void updateWR(Event e) {
		synchronized (WR.get(e.variable)) {
			WR.get(e.variable).add(e);
		}
	}
	
	public static void onWrite(Event e) {
		
	}
	
	// write to file logs
	// should execute even when threads are deadlocked (basically kill all other threads other
	// than master
	public static void end() {
		
	}
	
	/**
	 * To call at the end of execution where we write the output to some output file.
	 */
	public void log() {
		
	}
}
