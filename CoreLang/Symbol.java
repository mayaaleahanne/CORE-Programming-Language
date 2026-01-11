package CoreLang;

import static CoreLang.Core.*;
import java.util.ArrayList;
import java.util.Iterator;
import CoreLang.CoreVar.*;

/* This interface defines the methods
 * that all the symbol classes
 * need to implement. 
 * The purpose of this interface
 * is to make it easier to create helper
 * methods that are applicable for all
 * symbol classes. */
public interface Symbol {
	
	// Symbol interface enums:
	
	/* Represents each non-terminal symbol type
	 * NOTE: Terminal is not non-terminal. */
	public enum SymbolType{
		Procedure,
		DeclSeq,
		StmtSeq,
		Decl,
		DeclInteger,
		DeclObj,
		Function,
		Parameters,
		Stmt,
		Call,
		Assign,
		Print,
		Read,
		If,
		Loop,
		Cond,
		Cmpr,
		Expr,
		Term,
		Factor,
		Terminal
	}
	
	// Symbol interface methods:
	
	// Get the type of the symbol
	public SymbolType getType();
	
	/* Returns the value of the symbol
	 * Will return the symbol type if
	 * the symbol is non-terminal and
	 * the value of the symbol if the
	 * symbol is terminal. */
	public String getNameOrValue();
	
	// Sets the parent node of the symbol
	public void setParent(Symbol parent);
	
	// Returns the parent node of the symbol
	public Symbol getParent();
	
	// Returns the symbol's InstructionSetManager
	public InstructionManager getInstructionManager();
	
	// Adds a terminal child to the parse tree
	public void addTerminalChild(CoreScanner scanner);
	
	// Adds a non-terminal child to the parse tree
	public void addNonTerminalChild(Symbol child);
	
	// Get the children of the symbol
	public ArrayList<Symbol> getChildren();
	
	/* Returns an ArrayList of all non-terminal 
	 * children of a given symbol. */
	public ArrayList<Symbol> getNonTerminalChildren();
	
	/* Returns an ArrayList of all terminal 
	 * children of a given symbol.
	 * NOTE: the ArrayList is of type string because
	 * we only want to store the values of the terminal
	 * children in the list rather than the symbols objects
	 * themselves because the only use that these objects
	 * have is to use them to access the string value of 
	 * the terminal symbol. Directly storing the string
	 * values of the symbol prevents us from having to
	 * call a method on a symbol object to access the
	 * value. */
	public ArrayList<String> getTerminalChildren();
	
	// Counts how many of a given terminal child the symbol has
	public int getTerminalChildCount(String value);
	
	// Parses a symbol
	public void parse(CoreScanner scanner);
	
	// Executes a symbol
	public void execute(CoreScanner scanner);
	
	// Executes a symbol
	public void executeWithFrame(Frame frame);
	
	// Executes a symbol
	public int executeReturnInt();
	
	// Executes a symbol
	public boolean executeReturnBool(boolean negateResult);
	
	// Prints the parse tree
	public void print();
	
	// Symbol interface classes:
	
	/* This class represents the non-terminal
	 * symbol, assign, from the Core language. */
	public class Assign extends BaseSymbol {
		// Constructor
		Assign(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			CoreVar var = CallStack.retrieveVar(this.getTerminalChildren().get(0));
			
			// Make sure var was declared before checking for proper assignment
			if(var == null) {
				return;
			}
			
			// Make assignment based on terminal children
			if(this.getTerminalChildren().contains("[")) {
				
				/* Check if reference is null 
				 * before making a key assignment. */
				if(var.refIsNull()) {
					ErrorHandler.handleError("ERROR: Cannot make a key assignment to object " + 
							var.getIdentifier() + " because it has a null reference value.");
				}
				var.updateVar(this.getTerminalChildren().get(2), this.getNonTerminalChildren().get(0).executeReturnInt(), false);
			}
			else if (this.getTerminalChildren().contains("(")) {
				
				// Create a local copy for the stack
				if(CallStack.numFrames() > 1) {
					CallStack.removeVar(this.getTerminalChildren().get(0));
					var = new ObjectVar(this.getTerminalChildren().get(0));
					CallStack.allocateMem(var);
				}
				var.updateVar(this.getTerminalChildren().get(5), this.getNonTerminalChildren().get(0).executeReturnInt(), true);
			}
			else if(this.getTerminalChildren().contains(":")) {
				CoreVar varToAlias = CallStack.retrieveVar(this.getTerminalChildren().get(2));
				var.alias(varToAlias);
			} else {
				
				// Update variable based on type
				if(var.getVarType().equals(INTEGER)) {
					var.updateVar(this.getNonTerminalChildren().get(0).executeReturnInt());
				} else {
					
					/* Check if reference is null 
					 * before making a key assignment. */
					if(var.refIsNull()) {
						ErrorHandler.handleError("ERROR: Cannot make a key assignment to object " + 
								var.getIdentifier() + " because it has a null reference value.");
					}
					var.updateVar(var.getDefaultKey(), this.getNonTerminalChildren().get(0).executeReturnInt(), false);
				}
			}
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, call, from the Core language. */
	public class Call extends BaseSymbol{
		// Constructor
		Call(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			Iterator<String> iterator = this.getNonTerminalChildren().get(0).getTerminalChildren().iterator();
			Frame frame = new Frame();
			
			// Add local memory to the frame
			frame.allocateScopeMem();
			
			// Get the function symbol from memory using address from the MMU
			Symbol function = MemManagementUnit.getProcedure(this.getTerminalChildren().get(1));
			Iterator<String> formalParams = function.getNonTerminalChildren().get(0).getTerminalChildren().iterator();
			
			// Create formal parameters
			function.getNonTerminalChildren().get(0).executeWithFrame(frame);
			
			// Copy the values of passed in arguments into formal parameters
			while(iterator.hasNext()) {
				String currId = iterator.next();
				String formalParam = formalParams.next();
				
				// Advance the iterator to the next identifier
				if(currId.equals(",")) {
					currId = iterator.next();
					formalParam = formalParams.next();
				}
				
				/* Get formal parameter from frame and copy the 
				 * values of the passed in arguments into these 
				 * parameter. */
				CoreVar formalParamVar = frame.retrieveVar(formalParam, CallStack.getGlobalMem());
				CoreVar passedInParam = CallStack.retrieveVar(currId);
				formalParamVar.alias(passedInParam);
			}
			
			// Push new frame onto the call stack
			CallStack.addFrame(frame);
			
			// Execute the function
			function.getNonTerminalChildren().get(1).execute(scanner);
			
			// Pop frame from the call stack
			CallStack.removeFrame();
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, cmpr, from the Core language. */
	public class Cmpr extends BaseSymbol{
		// Constructor
		Cmpr(SymbolType type){
			super(type);
		}
		
		@Override
		public boolean executeReturnBool(boolean negateResult) {
			boolean result = false;
			
			// Get numeric values to compare
			int expr1 = this.children.get(0).executeReturnInt();
			int expr2 = this.children.get(2).executeReturnInt();
			
			// Evaluate condition based on child of the symbol
			if(this.children.get(1).getNameOrValue().equals("==")) {
				result = expr1 == expr2;
			} else {
				result = expr1 < expr2;
			}
			
			// Negate the result if necessary
			if(negateResult) {
				result = !result;
			}
			return result;
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, cond, from the Core language. */
	public class Cond extends BaseSymbol{
		// Constructor
		Cond(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, decl, from the Core language. */
	public class Decl extends BaseSymbol{
		// Constructor
		Decl(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, decl-integer, from the Core language. */
	public class DeclInteger extends BaseSymbol{
		// Constructor
		DeclInteger(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			// Allocate variable to memory
			CallStack.allocateMem(new IntegerVar(this.children.get(1).getNameOrValue()));
		}
	}
	
	/* This class represents the non-terminal 
	 * symbol, decl-obj, from the Core language. */
	public class DeclObj extends BaseSymbol{
		// Constructor
		DeclObj(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			// Allocate variable to memory
			CallStack.allocateMem(new ObjectVar(this.children.get(1).getNameOrValue()));
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, decl-seq, from the Core language. */
	public class DeclSeq extends BaseSymbol{
		// Constructor
		DeclSeq(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, expr, from the Core language. */
	public class Expr extends BaseSymbol{	
		// Constructor
		Expr(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, factor, from the Core language. */
	public class Factor extends BaseSymbol{
		// Constructor
		Factor(SymbolType type){
			super(type);
		}
		
		@Override
		public int executeReturnInt() {
			int result = 0;
			
			// Get the variable from the map
			if(this.getTerminalChildren().contains("[")) {
				CoreVar var = CallStack.retrieveVar(this.getTerminalChildren().get(0));
				
				// Check if key exists
				if(!var.keyExists(this.getTerminalChildren().get(2))) {
					ErrorHandler.handleError("ERROR: key " + this.getTerminalChildren().get(2) + 
							" for object " + this.getTerminalChildren().get(0) + " does not exist.");
				}
				result = var.getValue(this.getTerminalChildren().get(2));
			}
			
			// Parse the const into an integer
			else if (Character.isDigit(this.getTerminalChildren().get(0).charAt(0))) {
				result = Integer.parseInt(this.getTerminalChildren().get(0));
			}
			
			// Execute another expression and return its result
			else if(this.getTerminalChildren().contains("(")) {
				result = this.getNonTerminalChildren().get(0).executeReturnInt();
			} else {
				CoreVar var = CallStack.retrieveVar(this.getTerminalChildren().get(0));
				
				// Access variable value based on its type
				if(var.getVarType().equals(INTEGER)) {
					result = var.getValue();
				} else {
					result = var.getValue(var.getDefaultKey());
				}
			}
			return result;
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, function, from the Core language. */
	public class Function extends BaseSymbol{
		// Constructor
		Function(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, if, from the Core language. */
	public class If extends BaseSymbol{
		// Constructor
		If(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			Symbol stmtSeqToExecute = null;
			
			// Check if the if clause if true
			if(this.getNonTerminalChildren().get(0).executeReturnBool(false)) {
				stmtSeqToExecute = this.children.get(3);
			}
			
			// Check if an else clause exists
			else if(this.getNonTerminalChildren().size() > 2) {
				stmtSeqToExecute = this.children.get(5);
			}
			
			// Check if there is a statement sequence to execute
			if(stmtSeqToExecute != null) {
				CallStack.allocateScopeMem();
				stmtSeqToExecute.execute(scanner);
				CallStack.deallocateScopeMem();
			}
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, loop, from the Core language. */
	public class Loop extends BaseSymbol{
		// Constructor
		Loop(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			CoreVar var = CallStack.retrieveVar(this.getChildren().get(2).getNameOrValue());
			int value = this.children.get(4).executeReturnInt();
			
			// Update variable based on variable type
			if(var.getVarType().equals(INTEGER)) {
				var.updateVar(value);
			} else {
				var.updateVar(var.getDefaultKey(), value, false);
			}
			boolean cond = this.children.get(6).executeReturnBool(false);
			
			// Execute stmtSeq as many times necessary
			while(cond) {
				
				// Execute statement sequence
				CallStack.allocateScopeMem();
				this.getChildren().get(11).execute(scanner);
				CallStack.deallocateScopeMem();
				
				// Compute expression
				value = this.children.get(8).executeReturnInt();
				
				// Update variable based on variable type
				if(var.getVarType().equals(INTEGER)) {
					var.updateVar(value);
				} else {
					var.updateVar(var.getDefaultKey(), value, false);
				}
				cond = this.children.get(6).executeReturnBool(false);
			}
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, parameters, from the Core language. */
	public class Parameters extends BaseSymbol{
		// Constructor
		Parameters(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, print, from the Core language. */
	public class Print extends BaseSymbol{
		// Constructor
		Print(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			System.out.println(this.children.get(2).executeReturnInt());
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, procedure, from the Core language. */
	public class Procedure extends BaseSymbol{
		// Constructor
		Procedure(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, read, from the Core language. */
	public class Read extends BaseSymbol{
		// Constructor
		Read(SymbolType type){
			super(type);
		}
		
		@Override
		public void execute(CoreScanner scanner) {
			CoreVar var = CallStack.retrieveVar(this.children.get(2).getNameOrValue());
			
			// Check if end of file has been reached
			if(scanner.currentToken().equals(EOS)) {
				ErrorHandler.handleError("ERROR: Couldn't read value into " + var.getIdentifier());
			}
			
			// Update variable's value based on its type
			if(var.getVarType().equals(INTEGER)) {
				var.updateVar(scanner.getConst());
			} else {
				var.updateVar(var.getDefaultKey(), scanner.getConst(), false);
			}
			
			// Advance to next value
			scanner.nextToken();
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, stmt, from the Core language. */
	public class Stmt extends BaseSymbol{
		// Constructor
		Stmt(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, stmt-seq, from the Core language. */
	public class StmtSeq extends BaseSymbol{
		// Constructor
		StmtSeq(SymbolType type){
			super(type);
		}
	}
	
	/* This class represents the non-terminal
	 * symbol, term, from the Core language. */
	public class Term extends BaseSymbol{
		// Constructor
		Term(SymbolType type){
			super(type);
		}
	}
	
	public class Terminal extends BaseSymbol{
		// Constructor
		Terminal(String value){
			super(value);
		}
	}
}
