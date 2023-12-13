package conc_trace.instr;

import java.util.HashSet;

public class Config {
	
	private static final Config config = new Config();
	
	private Config() {}
	
	public static Config v() {
		return config;
	}
	
	public void init() {
		
	}
	
	
	// todo: 
	// Gets the location of the object ref that we have to record
	public boolean shouldRecord(String className) {
		return false;
	}
	
}
