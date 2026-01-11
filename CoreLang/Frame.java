package CoreLang;

import java.util.Iterator;
import java.util.Stack;

// This class represents a frame on the call stack
public class Frame{
	private Stack<Memory> frame;
	
	// Constructor
	public Frame() {
		this.frame = new Stack<>();
	}
	
	// Allocates memory for a scope of the program
	public void allocateScopeMem() {
		this.frame.push(new Memory());
	}
	
	// Deallocates memory for a scope of the program
	public void deallocateScopeMem() {
		
		// Update the references before exiting scope
		this.frame.peek().updateReferences();
		this.frame.pop();
	}
	
	// Allocates memory for a variable in the current scope
	public void allocateMem(CoreVar var) {
		this.frame.peek().allocateMem(var);
	}
	
	/* Retrieve a variable from memory in the frame
	 * Returns null if the variable isn't there. */
	public CoreVar retrieveVar(String identifier, Memory global) {
		CoreVar var = null;
		Iterator<Memory> iterator = this.frame.iterator();
		
		// Conduct search
		while(iterator.hasNext()) {
			Memory currScopeMem = iterator.next();
			
			// Check if variable was found
			if(currScopeMem.memContainsVar(identifier)) {
				var = currScopeMem.retrieveVar(identifier);
			}
		}
		
		// Check if variable is in global memory
		if(var == null && global.memContainsVar(identifier)) {
			var = global.retrieveVar(identifier);
		}
		
		return var;
	}
	
	// Removes a variable from the frame
	public void removeVar(String identifier) {
		this.frame.peek().removeVar(identifier);
	}
}
