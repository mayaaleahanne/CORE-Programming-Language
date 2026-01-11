package CoreLang;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import static CoreLang.Core.*;
import static CoreLang.Symbol.*;
import static CoreLang.ParseInstruction.*;
import static CoreLang.Symbol.SymbolType.*;

/* This class returns an object from the
 * Symbol interface or the parts 
 * we use to initialize it within its
 * constructor. 
 * NOTE: We do not create a class for
 * symbols of type Terminal or any
 * parse instruction set because a
 * Terminal symbol is just a string rather
 * than a Symbol object. */
public final class SymbolFactory{
	
	// Private constructor to prevent instantiation
	private SymbolFactory() {}
	
	/* Returns a non-terminal symbol of the requested 
	 * type.
	 * Exits the program if the requested
	 * type doesn't exist and prints an error
	 * message. */
	public static Symbol createSymbol(SymbolType type) {
		Symbol symbol = null;
		
		// Creates each non-terminal symbol type
		switch(type) {
			case Procedure:
				symbol = new Procedure(type);
				break;
			case DeclSeq:
				symbol = new DeclSeq(type);
				break;
			case StmtSeq:
				symbol = new StmtSeq(type);
				break;
			case Decl:
				symbol = new Decl(type);
				break;
			case DeclInteger:
				symbol = new DeclInteger(type);
				break;
			case DeclObj:
				symbol = new DeclObj(type);
				break;
			case Function:
				symbol = new Function(type);
				break;
			case Parameters:
				symbol = new Parameters(type);
				break;
			case Stmt:
				symbol = new Stmt(type);
				break;
			case Call:
				symbol = new Call(type);
				break;
			case Assign:
				symbol = new Assign(type);
				break;
			case Print:
				symbol = new Print(type);
				break;
			case Read:
				symbol = new Read(type);
				break;
			case If:
				symbol = new If(type);
				break;
			case Loop:
				symbol = new Loop(type);
				break;
			case Cond:
				symbol = new Cond(type);
				break;
			case Cmpr:
				symbol = new Cmpr(type);
				break;
			case Expr:
				symbol = new Expr(type);
				break;
			case Term:
				symbol = new Term(type);
				break;
			case Factor:
				symbol = new Factor(type);
				break;
			default:
				ErrorHandler.handleError("ERROR: Cannot build Symbol because its type is invalid.");
				break;
		}
		
		return symbol;
	}
	
	// Creates and returns a terminal symbol
	public static Symbol createSymbol(String value) {
		return new Terminal(value);
	}
	
	
	// Registers the parse instruction set for a given non-terminal symbol
	public static void createInstructionSet(Symbol symbol){
		Map<Core, SymbolType> firstSet;
		Queue<Core> tokens = null;
		
		// Creates each instruction set based on the symbol's type
		switch(symbol.getType()) {
			case Procedure:
				tokens = new LinkedList<>(Arrays.asList(PROCEDURE, ID, IS));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new ParseIfTokenInvalid(BEGIN, DeclSeq));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(BEGIN, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(StmtSeq));
				tokens = new LinkedList<>(Arrays.asList(END, EOS));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, true));
				break;
			case DeclSeq:
				firstSet = Map.ofEntries(Map.entry(INTEGER, Decl), Map.entry(OBJECT, Decl), Map.entry(PROCEDURE, Function));
				symbol.getInstructionManager().registerParseInstruct(new ParseOrExit(firstSet));
				symbol.getInstructionManager().registerParseInstruct(new ParseIfStartTokenValid(firstSet));
				break;
			case StmtSeq:
				firstSet = Map.ofEntries(Map.entry(ID, Stmt), 
						Map.entry(IF, Stmt), 
						Map.entry(FOR, Stmt),
						Map.entry(PRINT, Stmt), 
						Map.entry(READ, Stmt), 
						Map.entry(INTEGER, Stmt), 
						Map.entry(OBJECT, Stmt),
						Map.entry(BEGIN, Stmt));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Stmt));
				symbol.getInstructionManager().registerParseInstruct(new ParseIfStartTokenValid(firstSet));
				break;
			case Decl:
				firstSet = Map.ofEntries(Map.entry(INTEGER, DeclInteger), Map.entry(OBJECT, DeclObj));
				symbol.getInstructionManager().registerParseInstruct(new ParseOrExit(firstSet));
				break;
			case DeclInteger:
				tokens = new LinkedList<>(Arrays.asList(INTEGER, ID, SEMICOLON));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, true));
				break;
			case DeclObj:
				tokens = new LinkedList<>(Arrays.asList(OBJECT, ID, SEMICOLON));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, true));
				break;
			case Function:
				tokens = new LinkedList<>(Arrays.asList(PROCEDURE, ID, LPAREN, OBJECT));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Parameters));
				tokens = new LinkedList<>(Arrays.asList(RPAREN, IS));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(StmtSeq));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(END, true));
				break;
			case Parameters:
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(ID, false));
				symbol.getInstructionManager().registerParseInstruct(new CheckIfEndReached(COMMA, true));
				break;
			case Stmt:
				firstSet = Map.ofEntries(Map.entry(ID, Assign), 
						Map.entry(IF, If), 
						Map.entry(FOR, Loop),
						Map.entry(PRINT, Print), 
						Map.entry(READ, Read), 
						Map.entry(INTEGER, Decl), 
						Map.entry(OBJECT, Decl),
						Map.entry(BEGIN, Call));
				symbol.getInstructionManager().registerParseInstruct(new ParseOrExit(firstSet));
				break;
			case Call:
				tokens = new LinkedList<>(Arrays.asList(BEGIN, ID, LPAREN));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Parameters));
				tokens = new LinkedList<>(Arrays.asList(RPAREN, SEMICOLON));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, true));
				break;
			case Assign:
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(ID, false));
				symbol.getInstructionManager().registerParseInstruct(new DetectInvalidInstructionSet(COLON));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(ID, false));
				symbol.getInstructionManager().registerParseInstruct(new ValidateAssignment());
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(SEMICOLON, true));
				symbol.getInstructionManager().registerParseInstruct(new StartNewPossibleInstructionSet());
				symbol.getInstructionManager().registerParseInstruct(new DetectInvalidInstructionSet(LSQUARE));
				tokens = new LinkedList<>(Arrays.asList(STRING, RSQUARE, ASSIGN));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				symbol.getInstructionManager().registerParseInstruct(new ValidateAssignment());
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(SEMICOLON, true));
				symbol.getInstructionManager().registerParseInstruct(new StartNewPossibleInstructionSet());
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(ASSIGN, false));
				symbol.getInstructionManager().registerParseInstruct(new DetectInvalidInstructionSet(NEW));
				tokens = new LinkedList<>(Arrays.asList(OBJECT, LPAREN, STRING, COMMA));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(RPAREN, false));
				symbol.getInstructionManager().registerParseInstruct(new ValidateAssignment());
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(SEMICOLON, true));
				symbol.getInstructionManager().registerParseInstruct(new StartNewPossibleInstructionSet());
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(SEMICOLON, true));
				break;
			case Print:
				tokens = new LinkedList<>(Arrays.asList(PRINT, LPAREN));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				tokens = new LinkedList<>(Arrays.asList(RPAREN, SEMICOLON));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, true));
				break;
			case Read:
				tokens = new LinkedList<>(Arrays.asList(READ, LPAREN, ID, RPAREN, SEMICOLON));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, true));
				break;
			case If:
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(IF, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Cond));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(THEN, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(StmtSeq));
				symbol.getInstructionManager().registerParseInstruct(new CheckIfEndReached(END, false));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(ELSE, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(StmtSeq));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(END, true));
				break;
			case Loop:
				tokens = new LinkedList<>(Arrays.asList(FOR, LPAREN, ID, ASSIGN));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(SEMICOLON, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Cond));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(SEMICOLON, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				tokens = new LinkedList<>(Arrays.asList(RPAREN, DO));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokensOrExit(tokens, false));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(StmtSeq));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(END, true));
				break;
			case Cond:
				firstSet = Map.ofEntries(Map.entry(OR, Cmpr), Map.entry(AND, Cmpr), 
						Map.entry(NOT,Terminal), Map.entry(LSQUARE, Terminal));
				symbol.getInstructionManager().registerParseInstruct(new ConsumeLeadingTerminals(firstSet));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Cmpr));
				symbol.getInstructionManager().registerParseInstruct(new ParseIfStartTokenValid(firstSet));
				symbol.getInstructionManager().registerParseInstruct(new ConsumeTrailingTerminals(RSQUARE, "["));
				break;
			case Cmpr:
				firstSet = Map.ofEntries(Map.entry(EQUAL, Expr), Map.entry(LESS, Expr));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				symbol.getInstructionManager().registerParseInstruct(new ParseOrExit(firstSet));
				break;
			case Expr:
				firstSet = Map.ofEntries(Map.entry(ADD, Term), Map.entry(SUBTRACT, Term));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Term));
				symbol.getInstructionManager().registerParseInstruct(new ParseIfStartTokenValid(firstSet));
				break;
			case Term:
				firstSet = Map.ofEntries(Map.entry(MULTIPLY, Factor), Map.entry(DIVIDE, Factor));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Factor));
				symbol.getInstructionManager().registerParseInstruct(new ParseIfStartTokenValid(firstSet));
				break;
			case Factor:
				symbol.getInstructionManager().registerParseInstruct(new CheckIfEndReached(CONST, false));
				symbol.getInstructionManager().registerParseInstruct(new DetectInvalidInstructionSet(LPAREN));
				symbol.getInstructionManager().registerParseInstruct(new UnconditionalParse(Expr));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(RPAREN, true));
				symbol.getInstructionManager().registerParseInstruct(new StartNewPossibleInstructionSet());
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(ID, false));
				symbol.getInstructionManager().registerParseInstruct(new DetectInvalidInstructionSet(LSQUARE));
				symbol.getInstructionManager().registerParseInstruct(new ValidateVarType(OBJECT));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(STRING, false));
				symbol.getInstructionManager().registerParseInstruct(new ValidateTokenOrExit(RSQUARE, true));
				break;
			default:
				ErrorHandler.handleError("ERROR: Cannot build Symbol's parse instruction set "
						+ "because its type is invalid.");
				break;
		}
	}
}
