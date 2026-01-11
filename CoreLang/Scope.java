package CoreLang;

import java.util.Map;
import java.util.TreeMap;
import static CoreLang.Core.*;

// This class represents a scope in a Core program.
public class Scope{
	private Map<String, Core> symbolTable;
	
	// Constructor
	public Scope() {
		this.symbolTable = new TreeMap<>();
	}
	
	// Adds a variable's identifier and its type to the symbol table
	public void registerVar(String identifier, Core varType) {
		
		/* If the same given identifier hasn't already 
		 * been declared, add it to the symbol table. */
		if(!this.symbolTable.containsKey(identifier)){
			this.symbolTable.put(identifier, varType);
		} else {
			/* Add error message in case of attempt
			 * at double variable declaration. */
			SemanticChecker.registerError("ERROR: identifier \'" + identifier + "\' already in use.");
		}
	}
	
	/* Returns the type of the symbol with 
	 * the given identifier. 
	 * Returns an ERROR token if the variable
	 * isn't in the symbol table. */
	public Core getVarType(String identifier) {
		
		// Checks if the variable is in the symbol table
		if(this.symbolTable.containsKey(identifier)) {
			return this.symbolTable.get(identifier);
		}
		return ERROR;
	}
	
	// See if a variable was registered in the symbol table
	public boolean varRegistered(String identifier) {
		return this.symbolTable.containsKey(identifier);
	}
}
