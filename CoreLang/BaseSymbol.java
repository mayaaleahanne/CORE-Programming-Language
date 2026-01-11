package CoreLang;

import java.util.ArrayList;
import java.util.Iterator;
import CoreLang.CoreVar.*;
import static CoreLang.Core.*;
import static CoreLang.Symbol.SymbolType.*;

/* This class is a generalized implementation
 * of a symbol from the Core language grammar. */
public abstract class BaseSymbol implements Symbol{
	protected final SymbolType type;
	protected Symbol parent;
	protected InstructionManager manager;
	protected ArrayList<Symbol> children;
	protected StringBuilder value;
	
	// Constructor for non-terminal symbols
	public BaseSymbol(SymbolType type){
		
		// Register the root of the tree if the symbol is of type procedure
		if(type.equals(Procedure)) {
			MemManagementUnit.registerRoot(this);
		}
		
		// Initialize members
		this.type = type;
		this.parent = null;
		this.manager = new InstructionManager(this);
		SymbolFactory.createInstructionSet(this);
		this.children = new ArrayList<>();
	}
	
	// Overloaded constructor for a terminal symbol
	public BaseSymbol(String value) {
		this.type = Terminal;
		this.value = new StringBuilder().append(value);
	}
	
	@Override
	public final InstructionManager getInstructionManager() {
		
		// Terminal symbols do not have an instruction manager
		if(this.type.equals(Terminal)) {
			return null;
		}
		return this.manager;
	}
	
	@Override
	public final String getNameOrValue() {
		
		// Return value of a terminal symbol
		if(this.type.equals(Terminal)){
			return this.value.toString();
		}
		return this.type.toString().toLowerCase();
	}
	
	@Override
	public final SymbolType getType() {
		return this.type;
	}
	
	@Override
	public final ArrayList<Symbol> getChildren(){
		
		// Terminal symbols do not have children
		if(this.type.equals(Terminal)) {
			return null;
		}
		return this.children;
	}
	
	@Override
	public final void addNonTerminalChild(Symbol child) {
		
		// Terminal symbols do not have children
		if(this.type.equals(Terminal)) {
			return;
		}
		this.children.add(child);
		child.setParent(this);
	}
	
	@Override
	public final Symbol getParent() {
		return this.parent;
	}
	
	@Override
	public final void setParent(Symbol parent) {
		this.parent = parent;
	}
	
	@Override
	public final void parse(CoreScanner scanner) {
		
		/* Terminal symbols are not parsed because
		 * their values are initialized when they
		 * are constructed and added to the parse 
		 * tree and no further parsing is needed.*/
		if(this.type.equals(Terminal)) {
			return;
		}
		this.manager.executeParseInstructions(scanner);
	}
	
	@Override
	public void execute(CoreScanner reader) {
		
		// Terminal symbols do not execute
		if(this.type.equals(Terminal)) {
			return;
		}
		
		Iterator<Symbol> iterator = this.getNonTerminalChildren().iterator();
		
		// Execute all of a symbol's non-terminal children
		while(iterator.hasNext()) {
			Symbol currChild = iterator.next();
			
			/* Check if child needs to be executed
			 * Note: We do not want to execute the function
			 * children unless a procedure call has been made. */
			if(!currChild.getType().equals(Function)) {
				
				/* Allocate memory for local scope and change 
				 * where variables are allocated to.*/
				if(this.type.equals(Procedure) && currChild.getType().equals(StmtSeq)) {
					CallStack.allocateScopeMem();
					CallStack.stopGlobalMemAlloc();
				}
				currChild.execute(reader);
			}
		}
		
		/* Handle global variables after the 
		 * "main" procedure is finished executing. */
		if(this.type.equals(Procedure)) {
			
			// Update references in global memory
			CallStack.getGlobalMem().updateReferences();
		}
	}
	
	@Override
	public final void executeWithFrame(Frame frame) {
		Iterator<String> iterator = this.getTerminalChildren().iterator();
		
		// Allocate space for formal parameters
		while(iterator.hasNext()) {
			String currId = iterator.next();
			
			/* Only allocate space for identifiers
			 * NOTE: the last child will never be a
			 * comma as that violates the rules of
			 * the grammar. Thus, we can always
			 * assume that if the current child
			 * is a comma, then there will be a child
			 * to get after that and thus we will not
			 * call the .next() method when there isn't
			 * any remaining strings. */
			if(currId.equals(",")) {
				currId = iterator.next();
			}
			frame.allocateMem(new ObjectVar(currId));
		}
	}
	
	@Override
	public boolean executeReturnBool(boolean negateResult) {
		CondHelper helper = new CondHelper(this);
		boolean outerNegation = helper.leadingNegationPresent();
		Symbol currChild = this.children.get(helper.getCurrChildIndex());
		boolean result = currChild.executeReturnBool(false);
		helper.setCurrChildIndex(helper.getCurrChildIndex() + 1);
		
		// Conduct all comparisons
		while(helper.getCurrChildIndex() < this.children.size()) {
			boolean negationPresent = helper.leadingNegationPresent();
			
			// Short circuit evaluation if applicable
			if((result && helper.getOrTheResult()) || (!result && !helper.getOrTheResult())) {
				break;
			}
			currChild = this.children.get(helper.getCurrChildIndex());
			
			// Check if we need to "or" or "and" the result
			if(helper.getOrTheResult()) {
				result = result || currChild.executeReturnBool(negationPresent);
			} else {
				result = result && currChild.executeReturnBool(negationPresent);
			}
			helper.setCurrChildIndex(helper.getCurrChildIndex() + 1);
		}
		
		// Apply final negation
		if(outerNegation) {
			result = !result;
		}
		
		return result;
	}
	
	@Override
	public int executeReturnInt() {
		int result = this.children.get(this.children.size() - 1).executeReturnInt();
		
		// Perform computations
		for(int i = this.children.size() - 2; i > 0; i -= 2) {
			
			/* Get next symbol to execute and what 
			 * operation to perform on it and the 
			 * result.*/
			String currOp = this.children.get(i).getNameOrValue();
			Symbol currSymbol = this.children.get(i - 1);
			
			// Check which operation to perform
			if(currOp.equals("+")) {
				result += currSymbol.executeReturnInt();
			}
			else if (currOp.equals("-")) {
				result = currSymbol.executeReturnInt() - result;
			}
			else if (currOp.equals("*")) {
				result *= currSymbol.executeReturnInt();
			} 
			else if(currOp.equals("/") && result != 0){
				result =  currSymbol.executeReturnInt() / result;
			} else {
				// Report a divide by zero error
				ErrorHandler.handleError("ERROR: Cannot divide by zero.");
			}
		}
		return result;
	}
	
	@Override
	public final ArrayList<Symbol> getNonTerminalChildren(){
		
		// Terminal Symbols have no children
		if(this.type.equals(Terminal)) {
			return null;
		}
		
		Iterator<Symbol> iterator = this.children.iterator();
		ArrayList<Symbol> nonTerminalChildren = new ArrayList<>();
		
		// Get all terminal children
		while(iterator.hasNext()) {
			Symbol currChild = iterator.next();
			
			// Check if the current child is non-terminal
			if(!currChild.getType().equals(Terminal)) {
				
				// Add it to the list
				nonTerminalChildren.add(currChild);
			}
		}
		
		return nonTerminalChildren;
	}
	
	@Override
	public final ArrayList<String> getTerminalChildren(){
		
		// Terminal symbols have no children
		if(this.type.equals(Terminal)) {
			return null;
		}
		Iterator<Symbol> iterator = this.children.iterator();
		ArrayList<String> terminalChildren = new ArrayList<>();
		
		// Get all terminal children
		while(iterator.hasNext()) {
			Symbol currChild = iterator.next();
			
			// Check if the current child is terminal
			if(currChild.getType().equals(Terminal)) {
				
				// Add it to the list
				terminalChildren.add(currChild.getNameOrValue());
			}
		}
		
		return terminalChildren;
	}
	
	@Override
	public final void addTerminalChild(CoreScanner scanner) {
		
		// Terminal symbols do not have children
		if(this.type.equals(Terminal)) {
			return;
		}
		
		// Add terminal symbol to the parse tree based on its token type
		else if(scanner.currentToken().equals(ID)) {
			this.children.add(SymbolFactory.createSymbol(scanner.getId()));
			this.children.get(this.children.size() - 1).setParent(this);
			SemanticChecker.IDTokenSemanticCheck(scanner.getId(), this);
		}
		else if(scanner.currentToken().equals(STRING)) {
			this.children.add(SymbolFactory.createSymbol("\'" + scanner.getString() + "\'"));
			this.children.get(this.children.size() - 1).setParent(this);
		}
		else if(scanner.currentToken().equals(CONST)) {
			this.children.add(SymbolFactory.createSymbol(scanner.getConst() + ""));
			this.children.get(this.children.size() - 1).setParent(this);
		}
		
		// Check for a symbol token
		else if(CoreCollections.getSymbolsMap().containsKey(scanner.currentToken()) ||
				scanner.currentToken().equals(EQUAL)) {
			this.children.add(SymbolFactory.createSymbol(scanner.getSymbol()));
			this.children.get(this.children.size() - 1).setParent(this);
		}
		else if(!scanner.currentToken().equals(EOS)) {
			
			// Check if we're exiting a scope
			if(scanner.currentToken().equals(END)) {
				SemanticChecker.leaveScope();
			}
			this.children.add(SymbolFactory.createSymbol(scanner.currentToken().toString().toLowerCase()));
			this.children.get(this.children.size() - 1).setParent(this);
		}
	}
	
	@Override 
	public final int getTerminalChildCount(String value) {
		
		// Terminal symbols have no children
		if(this.type.equals(Terminal)) {
			return 0;
		}
		
		int count = 0;
		Iterator<Symbol> iterator = this.children.iterator();
		
		// Count how many times a terminal child appears
		while(iterator.hasNext()) {
			Symbol child = iterator.next();
			
			// Check if the child is terminal
			if(child.getType().equals(Terminal)&& child.getNameOrValue().equals(value)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public final void print() {
		Iterator<Symbol> iterator = this.children.iterator();
		
		/* Traverse through all of the symbol's children.
		 * Either print terminal children or call the
		 * print method of non-terminal children.*/
		while(iterator.hasNext()) {
			Symbol currChild = iterator.next();
			
			/* Check if the child is terminal or not.
			 * Terminal children are of type String
			 * and non-terminal children are of
			 * type Symbol. */
			if(currChild.getType().equals(Terminal)) {
				Formatter.manageIndents(this, currChild.getNameOrValue());
				
				// Check if the current child is "("
				if(currChild.getNameOrValue().equals("(") || currChild.getNameOrValue().equals("[")) {
					Formatter.setLeadingOpenParen(true);
				}
				
				// Print terminal symbols and format output
				Formatter.printLeadingSpaceOrIndent(this, currChild.getNameOrValue());
				System.out.print(currChild.getNameOrValue());
				Formatter.printNewline(this, currChild.getNameOrValue());
			} else {
				
				/* NOTE: null symbols simply aren't added 
				 * to the tree so we don't have to check 
				 * if the symbol is null before calling
				 * it's print method. */
				currChild.print();
			}
		}
	}
}
