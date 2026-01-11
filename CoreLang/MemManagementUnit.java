package CoreLang;

import java.util.Map;
import java.util.TreeMap;

// This class represents the memory management unit
public final class MemManagementUnit{
	private static Map<String, Integer> procedureSignatures = new TreeMap<>();
	private static Symbol root;
	
	// Private constructor to prevent instantiation
	private MemManagementUnit() {}
	
	// Registers the name of a procedure and its address
	public static void registerProcedure(String procedureId, int address) {
		
		// Register procedure if the ID isn't already taken
		if(!procedureSignatures.containsKey(procedureId)) {
			procedureSignatures.put(procedureId, address);
		} else {
			SemanticChecker.registerError("ERROR: Procedure ID \'" + procedureId + "\' is already in use.");
		}
	}
	
	// Tells whether a given procedure ID exists
	public static boolean procedureExists(String procedureId) {
		return procedureSignatures.containsKey(procedureId);
	}
	
	// Registers the root of the parse tree
	public static void registerRoot(Symbol treeRoot) {
		root = treeRoot;
	}
	
	// Returns the root of the parse tree
	public static Symbol getRoot() {
		return root;
	}
	
	// Returns a procedure of a given identifier from memory
	public static Symbol getProcedure(String identifier) {
		Symbol procedure = null;
		
		// Make sure procedure exists
		if(procedureExists(identifier)) {
			procedure = root.getNonTerminalChildren().get(0).getChildren().get(procedureSignatures.get(identifier));
		}
		
		return procedure;
	}
}
