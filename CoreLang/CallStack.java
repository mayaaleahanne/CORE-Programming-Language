package CoreLang;

import java.util.Stack;

// This class simulates a program's call stack
public final class CallStack{
	private static Memory global = new Memory();
	private static Stack<Frame> callStack = new Stack<>();
	private static boolean allocateToGlobal = true;
	
	// Private constructor to prevent instantiation
	private CallStack() {}
	
	// Adds a frame to the call stack
	public static void addFrame(Frame frame) {
		callStack.push(frame);
	}
	
	// Removes a frame from the call stack
	public static void removeFrame() {
		
		// Deallocate local scope of the current frame
		callStack.peek().deallocateScopeMem();
		callStack.pop();
	}
	
	// Add memory for a new scope in the current stack frame
	public static void allocateScopeMem() {
		callStack.peek().allocateScopeMem();
	}
	
	// Deallocate memory for a scope in the current stack frame
	public static void deallocateScopeMem() {
		callStack.peek().deallocateScopeMem();
	}
	
	// Stops variables from being allocated to global space
	public static void stopGlobalMemAlloc() {
		allocateToGlobal = false;
	}
	
	// Allocate memory for a given variable in the current stack frame
	public static void allocateMem(CoreVar var) {
		
		/* Allocate the memory to the global scope or local scope
		 * of the current frame.*/
		if(allocateToGlobal) {
			global.allocateMem(var);
		} else {
			callStack.peek().allocateMem(var);
		}
	}
	
	/* Retrieve a variable from memory in the current stack frame
	 * Returns null if the variable isn't there. */
	public static CoreVar retrieveVar(String identifier) {
		return callStack.peek().retrieveVar(identifier, global);
	}
	
	// Removes a variable from memory in the current stack frame
	public static void removeVar(String identifier) {
		callStack.peek().removeVar(identifier);
	}
	
	// Tells the size of the call stack
	public static int numFrames() {
		return callStack.size();
	}
	
	// Returns the global memory scope
	public static Memory getGlobalMem() {
		return global;
	}
}
