package CoreLang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import static CoreLang.Core.*;
import static CoreLang.Symbol.SymbolType.*;

// This class verifies that the program is semantically valid
public final class SemanticChecker {
	private static Stack<Scope> program = new Stack<>();
	private static Queue<String> errorMsgs = new LinkedList<>();
	
	// Private constructor to prevent instantiation
	private SemanticChecker() {}
	
	/* Checks for semantic errors and 
	 * handles them, if any exist. */
	public static void checkForErrors() {
		
		/* If errorMsgs isn't empty, it means that 
		 * semantic errors were found in the program. */
		if(!errorMsgs.isEmpty()) {
			
			// Handle semantic errors
			ErrorHandler.handleSemanticError(errorMsgs);
		}
	}
	
	// Stores an error message associated with a semantic error
	public static void registerError(String errorMsg) {
		// Add semantic error to the errorMsgs queue
		errorMsgs.add(errorMsg);
	}
	
	/* Checks if a variable has been 
	 * declared in any of the scopes. 
	 * Returns a null value if the
	 * not and returns a value of type
	 * Core if so.*/
	public static Core verifyVarDeclaration(String identifier) {
		Iterator<Scope> iterator = program.iterator();
		Core varType = null;
		
		// Search for variable declaration
		while(iterator.hasNext() && varType == null) {
			Scope currScope = iterator.next();
			
			/* Check if the given variable 
			 * was declared within the current 
			 * scope. */
			if(currScope.varRegistered(identifier)) {
				varType = currScope.getVarType(identifier);
			}
		}
		
		// Register error if variable was never declared
		if(varType == null) {

			// Ensure that duplicate messages aren't registered
			if(!errorMsgs.contains("ERROR: \'" + identifier + "\' is used but never declared.")) {
				registerError("ERROR: \'" + identifier + "\' is used but never declared.");
			}
		}
		
		return varType;
	}
	
	// Verify that the given variable is of the proper type
	public static void verifyVarType(String identifier, Core requiredType) {
		
		// Check if the error has already been caught by the semantic checker
		if(verifyVarDeclaration(identifier) == null) {
			return;
		}
		
		// Check if the variable is of the required type
		else if(!verifyVarDeclaration(identifier).equals(requiredType)) {
		registerError("ERROR: \'" + identifier + "\' must be of type " + requiredType.toString().toLowerCase() + ".");
		}
	}
	
	// Check if an assignment is invalid
	public static void verifyAssignment(ArrayList<String> terminalChildren) {
		
		// Check if the error has already been caught by the semantic checker
		if(verifyVarDeclaration(terminalChildren.get(0)) == null) {
			return;
		}
		
		/* Check for assignment cases in which the variable must have been 
		 * declared as an object. */
		else if((terminalChildren.contains(":") && (!verifyVarDeclaration(terminalChildren.get(0)).equals(OBJECT) || 
				!verifyVarDeclaration(terminalChildren.get(2)).equals(OBJECT)))
				|| (terminalChildren.contains("new") && !verifyVarDeclaration(terminalChildren.get(0)).equals(OBJECT)) || 
				(terminalChildren.contains("[") && !verifyVarDeclaration(terminalChildren.get(0)).equals(OBJECT))) {
			
			/* Register error messages based on how many 
			 * variables were supposed to be of type object. */
			if(terminalChildren.contains(":")) {
				
				/* Check which variable wasn't declared as an 
				 * OBJECT or if neither are of type OBJECT. */
				if(verifyVarDeclaration(terminalChildren.get(0)).equals(OBJECT)) {
					registerError("ERROR: Invalid assignment. \'" + terminalChildren.get(2) + "\' must be of type Object.");
				}
				else if(verifyVarDeclaration(terminalChildren.get(2)).equals(OBJECT)) {
					registerError("ERROR: Invalid assignment. \'" + terminalChildren.get(0) + "\' must be of type Object.");
				} else {
					registerError("ERROR: Invalid assignment. \'" + terminalChildren.get(0) + "\'" + 
				" and \'" + terminalChildren.get(2) + "\' must be of type Object.");
				}
			} else {
				registerError("ERROR: Invalid assignment. \'" + terminalChildren.get(0) + "\' must be of type Object.");
			}
		}
	}
	
	/* Leaves the current scope of the program that 
	 * the semantic checker is in. */
	public static void leaveScope() {
		program.pop();
	}
	
	/* Updates the semantic error checker each time
	 * we start parsing a new symbol. */
	public static void update(Symbol symbol) {
		
		/* Adds a new Scope object because we
		 * are entering a new scope within the
		 * program if we are parsing symbols of
		 * either type. */
		if(symbol.getType().equals(DeclSeq) || symbol.getType().equals(Function) || 
				(symbol.getType().equals(StmtSeq) && !symbol.getParent().getType().equals(Function))) {
			program.push(new Scope());
		}
	}
	
	// Checks if the correct amount of parameters were passed into the function
	public static void validateFunctionCall(Symbol symbol, String identifier) {
		
		/* We don't need to validate a function that's 
		 * already been proven to not exist. */
		if(!MemManagementUnit.procedureExists(identifier)) {
			return;
		}
		
		Symbol function = MemManagementUnit.getProcedure(symbol.getTerminalChildren().get(1));
		int requiredNumParams = function.getNonTerminalChildren().get(0).getTerminalChildren().size();
		int numParamsPassedIn = symbol.getNonTerminalChildren().get(0).getTerminalChildren().size();
		
		// Make sure the correct amount of parameters were passed into the function
		if (requiredNumParams != numParamsPassedIn) {
			// Remove commas from counts if present
			if(requiredNumParams > 1) {
				requiredNumParams = requiredNumParams - function.getNonTerminalChildren().get(0).getTerminalChildCount(",");
			}
			
			if(numParamsPassedIn > 1) {
				numParamsPassedIn = numParamsPassedIn - symbol.getNonTerminalChildren().get(0).getTerminalChildCount(",");
			}
			
			registerError("Error: procedure \'" + identifier + "\' requires " + requiredNumParams + 
					" parameter(s), but was called with " + numParamsPassedIn + ".");
		}
	}
	
	// This method performs a semantic check on all ID tokens
	public static void IDTokenSemanticCheck(String identifier, Symbol symbol) {
		
		// Register declared variables or formal parameters
		if(symbol.getType().equals(DeclInteger) || symbol.getType().equals(DeclObj) || 
				(symbol.getType().equals(Parameters) && symbol.getParent().getType().equals(Function))) {
			
			/* Register the variable based on
			 * its declaration type. */
			if(symbol.getType().equals(DeclInteger)) {
				program.peek().registerVar(identifier, INTEGER);
			} else {
				program.peek().registerVar(identifier, OBJECT);
			}
		}
		
		/* Register procedure IDs. No procedure may have identical
		 * names. */
		else if(symbol.getType().equals(Procedure) || symbol.getType().equals(Function)) {
			int address = -1;
			
			/* Register procedure ID and its address (-1 represents the 
			 * address of the root of the parse tree). */
			if(symbol.getType().equals(Function)) {
				address = symbol.getParent().getChildren().size() - 1;
			}
			MemManagementUnit.registerProcedure(identifier, address);
		}
		
		// Verify that the procedure being called has been declared
		else if(symbol.getType().equals(Call)) {
			
			// Register error message if necessary
			if(!MemManagementUnit.procedureExists(identifier)) {
				registerError("ERROR: Cannot call procedure \'" + identifier + "\'. It does not exist.");
				return;
			}
		}
		else {
			
			// Verify that the variable was declared
			verifyVarDeclaration(identifier);
		}
	}
}
