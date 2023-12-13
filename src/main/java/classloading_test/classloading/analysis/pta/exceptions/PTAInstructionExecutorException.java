package classloading_test.classloading.analysis.pta.exceptions;

import classloading_test.classloading.analysis.InstructionLocation;

public class PTAInstructionExecutorException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private InstructionLocation exceptionLocation;
	private String message;
	public PTAInstructionExecutorException(String message, InstructionLocation location) {
		this.exceptionLocation = location;
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return String.format("Exception location: %s. Exception message: %s", exceptionLocation, message);
		
	}
	
}
