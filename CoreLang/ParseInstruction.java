package CoreLang;

import static CoreLang.Core.*;
import static CoreLang.ParseInstruction.Instruction.*;
import static CoreLang.Symbol.SymbolType.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import CoreLang.Symbol.SymbolType;

/* This interface defines the methods that
 * all parse instructions need to implement.
 * All of the implementations of the interface
 * are below the method prototypes. */
public interface ParseInstruction{
	
	/* Represents each kind of operation
	 * that the parse method can perform. */
	public enum Instruction{
		ValidateTokenOrExit,
		ValidateTokensOrExit,
		DetectInvalidInstructionSet,
		ParseIfTokenInvalid,
		UnconditionalParse,
		ParseOrExit,
		ParseIfStartTokenValid,
		ConsumeLeadingTerminals,
		ConsumeTrailingTerminals,
		StartNewPossibleInstructionSet,
		CheckIfEndReached,
		ValidateAssignment,
		ValidateVarType
	}
	
	// Returns what the parse instruction is
	public Instruction getInstruction();
	
	// Executes the parse instruction
	public void execute(CoreScanner scanner, Symbol symbol);
	
	// Classes that implement the ParseInstruction interface
	
	/* This parse instruction checks if the current
	 * token is equal to a given token and EXITS the 
	 * program if it isn't. This operation is used 
	 * when we already know what the parse sequence 
	 * is and aren't trying to decide between multiple 
	 * possible parse sequences based off of a single 
	 * token. */
	public class ValidateTokenOrExit implements ParseInstruction{
		private Core validToken;
		private boolean isFinalToken;
		
		// Constructor
		public ValidateTokenOrExit(Core validToken, boolean isFinalToken) {
			this.validToken = validToken;
			this.isFinalToken = isFinalToken;
		}
		
		@Override
		public Instruction getInstruction() {
			return ValidateTokenOrExit;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {

			// Check if token is valid
			if(scanner.currentToken().equals(this.validToken)) {
				
				// Add terminal child to the parse tree
				symbol.addTerminalChild(scanner);
				
				// Advance to the next token
				if(!scanner.currentToken().equals(EOS)) {
					scanner.nextToken();
				}
				
				/* Check if the token is the final token
				 * to be checked within the parse
				 * instruction set. */
				if(this.isFinalToken) {
					symbol.getInstructionManager().setDoneParsing(true);
				}
				
			} else {
				ErrorHandler.handleError("ERROR parsing " + symbol.getNameOrValue() + ": " + scanner.currentToken().toString() + 
						" token should be " + String.valueOf(this.validToken) + " token. ");
			}
		}
	}
	
	/* This parse instruction checks if
	 * multiple consecutive tokens from the 
	 * scanner are equal to a given Queue
	 * of tokens and EXITS the program
	 * if any of the consecutive tokens
	 * from the scanner don't match the
	 * tokens in the Queue. */
	public class ValidateTokensOrExit implements ParseInstruction{
		private Queue<Core> tokensToValidate;
		private boolean hasFinalToken;
		
		// Constructor
		public ValidateTokensOrExit(Queue<Core>tokensToValidate, boolean hasFinalToken) {
			this.tokensToValidate = tokensToValidate;
			this.hasFinalToken = hasFinalToken;
		}
		
		@Override
		public Instruction getInstruction() {
			return ValidateTokensOrExit;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			Iterator<Core> tokenSequence = this.tokensToValidate.iterator();
			
			// Validate each token in the sequence
			while(tokenSequence.hasNext()) {
				Core currToken = tokenSequence.next();
				
				// Handle case of invalid token
				if(!scanner.currentToken().equals(currToken)) {
					ErrorHandler.handleError("ERROR parsing " + symbol.getNameOrValue() + ": " + scanner.currentToken().toString() + 
							" token should be " + String.valueOf(currToken) + " token. ");
				}
				
				// Add terminal child to the parse tree
				symbol.addTerminalChild(scanner);
				
				// Advance to the next token
				if(!scanner.currentToken().equals(EOS)) {
					scanner.nextToken();
				}
			}
			
			/* Check if the set has the final
			 * token to check in the parse 
			 * instruction set. */
			if(this.hasFinalToken) {
				symbol.getInstructionManager().setDoneParsing(true);
				
				// Check the validity of a function call
				if(symbol.getType().equals(Call)) {
					SemanticChecker.validateFunctionCall(symbol, symbol.getTerminalChildren().get(1));
				}
			}
		}
	}
	
	/* This parse instruction checks if
	 * the current token matches the first
	 * token in a possible parse instruction 
	 * set and triggers the deletion of the 
	 * remaining instructions in that 
	 * instruction set if not. */
	public class DetectInvalidInstructionSet implements ParseInstruction{
		private Core firstToken;
		
		// Constructor
		public DetectInvalidInstructionSet(Core firstToken) {
			this.firstToken = firstToken;
		}

		@Override
		public Instruction getInstruction() {
			return DetectInvalidInstructionSet;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {

			// Check if instruction set is valid
			if(scanner.currentToken().equals(this.firstToken)) {
				
				// Add terminal child to the parse tree
				symbol.addTerminalChild(scanner);
				
				// Advance to next token
				scanner.nextToken();
			} else {
				symbol.getInstructionManager().setInstructionsInvalid(true);
			}
		}
	}
	
	/* This parse instruction parses a given
	 * type of non-terminal symbol if the 
	 * current token of the scanner doesn't 
	 * equal a given token. */
	public class ParseIfTokenInvalid implements ParseInstruction{
		private Core possibleNextToken;
		private SymbolType type;
		
		// Constructor
		public ParseIfTokenInvalid(Core possibleNextToken, SymbolType type) {
			this.possibleNextToken = possibleNextToken;
			this.type = type;
		}

		@Override
		public Instruction getInstruction() {
			return ParseIfTokenInvalid;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			
			// Check if current token isn't the target token
			if(!scanner.currentToken().equals(this.possibleNextToken)) {
				
				/* Build a symbol of the proper type, 
				 * add it to the parse tree, and parse it. */
				symbol.addNonTerminalChild(SymbolFactory.createSymbol(this.type));
				symbol.getNonTerminalChildren().get(symbol.getNonTerminalChildren().size() - 1).parse(scanner);
			}
		}
	}
	
	/* This parse instruction creates and
	 * parses a non-terminal symbol of a
	 * given type without first checking 
	 * for any instructions. */
	public class UnconditionalParse implements ParseInstruction{
		private SymbolType type;
		
		// Constructor
		public UnconditionalParse(SymbolType type) {
			this.type = type;
		}

		@Override
		public Instruction getInstruction() {
			return UnconditionalParse;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			
			// Add a symbol to the tree and parse it
			symbol.addNonTerminalChild(SymbolFactory.createSymbol(this.type));
			symbol.getNonTerminalChildren().get(symbol.getNonTerminalChildren().size() - 1).parse(scanner);
		}
	}
	
	/* This parse instruction parse a
	 * non-terminal symbol of a given 
	 * type if the current token of the 
	 * scanner can be found within any 
	 * of the "first sets" of any of the 
	 * symbols and EXITS the program if 
	 * the current token isn't within 
	 * any of the "first sets". */
	public class ParseOrExit implements ParseInstruction{
		private Map<Core, SymbolType> firstSet;
		
		// Constructor
		public ParseOrExit(Map<Core, SymbolType> firstSet) {
			this.firstSet = firstSet;
		}

		@Override
		public Instruction getInstruction() {
			return ParseOrExit;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {

			/* Check if the current token is present in any of the
			 * first sets for any of the symbols that we can parse. */
			if(this.firstSet.containsKey(scanner.currentToken())) {
				SymbolType type = this.firstSet.get(scanner.currentToken());
				
				/* Decl, Stmt, and DeclSeq do not have terminal symbols 
				 * in between their first appearance and the next potential 
				 * parse so we do not have any terminal child to consume in 
				 * those cases. */
				if(!symbol.getType().equals(Decl) && !symbol.getType().equals(Stmt) &&
						!symbol.getType().equals(DeclSeq)) {
					
					// Add terminal child to the parse tree
					symbol.addTerminalChild(scanner);
					
					// Advance to next token
					scanner.nextToken();
				}
				
				// Add symbol to the parse tree, and parse it
				symbol.addNonTerminalChild(SymbolFactory.createSymbol(type));
				symbol.getNonTerminalChildren().get(symbol.getNonTerminalChildren().size() - 1).parse(scanner);
			} else {
				
				// The current token isn't present in any of the first sets.
				ErrorHandler.handleTokenNotInFirstSet(scanner.currentToken(), this.firstSet.keySet(), symbol.getNameOrValue());
			}
		}
	}
	
	/* This parse instruction parses a
	 * non-terminal symbol of a given type if the current
	 * token of the scanner can be found
	 * within any of the "first sets" of
	 * any of the symbols, but does NOT
	 * exit the program if the current
	 * token doesn't match any of the
	 * tokens in any of the "first sets". */
	public class ParseIfStartTokenValid implements ParseInstruction{
		private Map<Core, SymbolType> firstSet;
		
		// Constructor
		public ParseIfStartTokenValid(Map<Core, SymbolType> firstSet) {
			this.firstSet = firstSet;
		}

		@Override
		public Instruction getInstruction() {
			return ParseIfStartTokenValid;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {

			/* Check if the current token is present in any of the
			 * first sets for any of the symbols that we can parse. */
			if(this.firstSet.containsKey(scanner.currentToken())) {
				
				/* Decl and Stmt do not have terminal symbols in between
				 * their first occurance and the next potential parse so
				 * we do not have any terminal child to consume in those
				 * cases. */
				if(!symbol.getType().equals(Decl) && !symbol.getType().equals(Stmt) &&
						!symbol.getType().equals(DeclSeq) && !symbol.getType().equals(StmtSeq)) {
					
					// Add terminal child to the parse tree
					symbol.addTerminalChild(scanner);
					
					// Advance to next token
					scanner.nextToken();
				}
				
				/* Jump back to the first instruction 
				 * (which is an unconditional parse). */
				symbol.getInstructionManager().setRepeatInstructions(true);
			} else {
				
				/* We do not want to restart the instruction 
				 * set because if we did not parse a symbol, 
				 * then we do not need to run this method
				 * again and look for another symbol to
				 * parse. */
				symbol.getInstructionManager().setRepeatInstructions(false);
			}
		}
	}
	
	/* This parse instruction consumes the
	 * leading terminal symbols that lie
	 * before a non-terminal symbol. */
	public class ConsumeLeadingTerminals implements ParseInstruction{
		private Map<Core, SymbolType> firstSet;
		
		public ConsumeLeadingTerminals(Map<Core, SymbolType> firstSet) {
			this.firstSet = firstSet;
		}

		@Override
		public Instruction getInstruction() {
			return ConsumeLeadingTerminals;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			SymbolType type = this.firstSet.get(scanner.currentToken());
			
			/* If the first symbol isn't terminal
			 * then there are no leading terminals
			 * to consume and we can jump straight
			 * into parsing. */
			while(type == Terminal) {
				
				// Add terminal child to the parse tree
				symbol.addTerminalChild(scanner);
				
				// Advance to next token
				scanner.nextToken();
				
				// Update current symbol type
				type = this.firstSet.get(scanner.currentToken());
			}
		}
	}
	
	/* This parse instruction triggers a
	 * non-terminal symbol's parse method
	 * to stop deleting parse instructions
	 * once all instructions from an invalid
	 * possible instruction set have been
	 * deleted. */
	public class StartNewPossibleInstructionSet implements ParseInstruction{
		
		// Constructor
		public StartNewPossibleInstructionSet() {}

		@Override
		public Instruction getInstruction() {
			return StartNewPossibleInstructionSet;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			
			/* At the start of a new possible parse 
			 * instruction set, we haven't determined 
			 * whether it is invalid or not, so
			 * invalidInstructionSet needs to be reset. */
			symbol.getInstructionManager().setInstructionsInvalid(false);
		}
	}
	
	/* This instruction set checks if the end
	 * of the parse instruction set has been
	 * reached. */
	public class CheckIfEndReached implements ParseInstruction{
		private Core validToken;
		private boolean restartExecution;
		
		// Constructor
		public CheckIfEndReached(Core validToken, boolean restartExecution) {
			this.validToken = validToken;
			this.restartExecution = restartExecution;
		}

		@Override
		public Instruction getInstruction() {
			return CheckIfEndReached;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			
			// Check if token is valid
			if(scanner.currentToken().equals(this.validToken)) {
				
				// Add terminal child to the parse tree
				symbol.addTerminalChild(scanner);
				
				// Advance to the next token
				if(!scanner.currentToken().equals(EOS)) {
					scanner.nextToken();
				}
				
				/* Trigger the restart of the execution of 
				 * the instruction set if necessary. */
				if(this.restartExecution) {
					// Restart the execution of the instruction set
					symbol.getInstructionManager().setRepeatInstructions(true);
				} else {
					// Stop parsing if otherwise
					symbol.getInstructionManager().setDoneParsing(true);
				}
			}
		}
	}
	
	/* This parse instruction checks if an
	 * assignment was made with ID variables
	 * of the required type. */
	public class ValidateAssignment implements ParseInstruction{

		// Constructor
		public ValidateAssignment() {}

		@Override
		public Instruction getInstruction() {
			return ValidateAssignment;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			SemanticChecker.verifyAssignment(symbol.getTerminalChildren());
		}
	}
	
	/* This parse instruction checks if a
	 * given identifier is of a given 
	 * variable type. */
	public class ValidateVarType implements ParseInstruction{
		private Core requiredVarType;
		
		// Constructor
		public ValidateVarType(Core requiredVarType) {
			this.requiredVarType = requiredVarType;
		}

		@Override
		public Instruction getInstruction() {
			return ValidateVarType;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			SemanticChecker.verifyVarType(symbol.getTerminalChildren().get(0), this.requiredVarType);
		}
	}
	
	/* This parse instruction consumes
	 * trailing consecutive terminal
	 * symbols. This is traditionally
	 * used to consume closing square
	 * braces. */
	public class ConsumeTrailingTerminals implements ParseInstruction{
		private ValidateTokenOrExit validateToken;
		private String openTerminal;

		// Constructor
		public ConsumeTrailingTerminals(Core closingTerminal, String openTerminal) {
			this.openTerminal = openTerminal;
			this.validateToken = new ValidateTokenOrExit(closingTerminal, false);
		}

		@Override
		public Instruction getInstruction() {
			return ConsumeTrailingTerminals;
		}

		@Override
		public void execute(CoreScanner scanner, Symbol symbol) {
			
			// Count the amount of children that are the open terminal symbol
			int numTrailingTerminals = symbol.getTerminalChildCount(this.openTerminal);
			
			// Consume each trailing closed terminal symbol
			for(int i = 0; i < numTrailingTerminals; i++) {
				this.validateToken.execute(scanner, symbol);
			}
		}
	}
}
