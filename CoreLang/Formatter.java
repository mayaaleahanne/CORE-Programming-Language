package CoreLang;

import static CoreLang.Symbol.SymbolType.*;
import java.util.Iterator;
import java.util.Stack;
import CoreLang.Symbol.SymbolType;

// This class formats the output of the printed parse tree
public final class Formatter{
	private static boolean leadingOpenParen = false;
	private static Stack<String> indents = new Stack<>();
	
	// Private class to prevent instantiation
	private Formatter() {}
	
	// Prints a leading space or indent when necessary
	public static void printLeadingSpaceOrIndent(Symbol symbol, String child) {
		SymbolType type = symbol.getType();
		
		// Check if we need to print leading indent(s)
		if((child.equals("object") && !type.equals(Assign)) || child.equals("integer") || 
				child.equals("for") || child.equals("print") || child.equals("read") || 
				child.equals("if") || child.equals("else") || (child.equals("end") && !type.equals(Procedure)) || 
				(type.equals(Assign) && symbol.getTerminalChildren().indexOf(child) == 0)) {
			
			// Make sure stack isn't empty
			if(!indents.isEmpty()) {
				
				// Else will not have be indented with the rest of the block
				if(child.equals("else")) {
					indents.pop();
				}
				
				Iterator<String> iterator = indents.iterator();
				
				// Print leading indent(s)
				while(iterator.hasNext()) {
					System.out.print(iterator.next());
				}
				
				// Indent the statements in the else block
				if(child.equals("else")) {
					indents.push("\t");
				}
			}
		}
		
		// Check if we need to prints a leading space
		else if(((CoreCollections.getSymbolsMap().containsValue(child.charAt(0)) || Character.isDigit(child.charAt(0)) || 
				(type.equals(Loop) && !child.equals(symbol.getTerminalChildren().get(2))) || type.equals(DeclInteger) || 
				type.equals(DeclObj) || (!type.equals(Factor) && !leadingOpenParen)|| child.equals("then") ||
				(type.equals(Procedure) && !child.equals("begin") && !child.equals("end")) || type.equals(Cond)) && 
				!type.equals(Print) && !type.equals(Read)) && !child.equals("procedure") && !child.equals(";") && 
				!child.equals(")") && !child.equals("]") && !child.contains("\'")) {
			System.out.print(" ");
			
			// Check if we need to reset leadingOpenParen
			if(leadingOpenParen && !child.equals("(")) {
				leadingOpenParen = false;
			}
		}
	}
	
	// Prints newlines in the parse tree output when necessary
	public static void printNewline(Symbol symbol, String child) {
		SymbolType type = symbol.getType();
		
		// Check if we need to print a newline
		if(child.equals("is") || (child.equals("begin") && symbol.getType().equals(Call)) || child.equals("then") || child.equals("else") ||
				child.equals("do") || child.equals("end") || (child.equals(";") && !type.equals(Loop))) {
			System.out.println();
		}
	}
	
	
	// Manages the amount of indents that appear in the printed parse tree
	public static void manageIndents(Symbol symbol, String child) {
		
		// Make sure the symbol has terminal children
		if(symbol.getTerminalChildren().size() > 0) {
			
			/* Add an indent every time we enter 
			 * a new scope in the program. */
			if(child.equals("is") || child.equals("begin") || child.equals("do") || child.equals("then")) {
				
				/* Clear the indents because the DeclSeq and StmtSeq scopes
				 * should have the same indentation unless the program enters
				 * another StmtSeq scope within the first StmtSeq scope. */
				if(child.equals("begin") && !symbol.getType().equals(Call)) {
					indents.clear();
				}
				indents.push("\t");
			}
			
			/* Remove an indent every time we
			 * leave a scope of the program. */
			else if(child.equals("end")) {
				indents.pop();
			}
		}
	}
	
	/* Tells the Formatter is a leading open parenthesis or square 
	 * brace has been found. */
	public static void setLeadingOpenParen(boolean hasLeadingOpenParen) {
		leadingOpenParen = hasLeadingOpenParen;
	}
}
