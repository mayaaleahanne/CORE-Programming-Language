package CoreLang;

import java.util.Map;
import java.util.TreeMap;
import static CoreLang.Core.*;

// This class represents memory.
public class Memory{
	private Map<String, CoreVar> storage;
	
	// Constructor
	public Memory() {
		this.storage = new TreeMap<>();
	}
	
	// Allocate memory for a given variable
	public void allocateMem(CoreVar var) {
		this.storage.put(var.getIdentifier(), var);
	}
	
	// Tells if a variable of a given identifier is in memory
	public boolean memContainsVar(String identifier) {
		return this.storage.containsKey(identifier);
	}
	
	// Retrieve a variable from memory
	public CoreVar retrieveVar(String identifier) {
		return this.storage.get(identifier);
	}
	
	// Remove a variable from memory
	public void removeVar(String identifier) {
		this.storage.remove(identifier);
	}
	
	// Updates references to variables in storage when leaving scope
	public void updateReferences() {
		
		// Search for any variables with references to them
		for(Map.Entry<String, CoreVar> entry : this.storage.entrySet()) {
			CoreVar currVar = entry.getValue();
			
			// Check if the variable is an object variable
			if(currVar.getVarType().equals(OBJECT) && currVar.getRefCount() > 0) {
				currVar.updateRefCount(false);
			}
		}
	}
}
