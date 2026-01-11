package CoreLang;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

// This class handles all possible errors
public final class ErrorHandler{
	
	// Private constructor to prevent instantiation
	private ErrorHandler() {}
	
	// Prints error message and exits the program
	public static void handleError(String msg) {
		System.out.println(msg);
		System.exit(0);
	}
	
	/* Prints an error message, gets the stack trace for an 
	 * IOException, and exits the program. */
	public static void handleError(String msg, IOException e) {
		System.out.println(msg);
		e.getStackTrace();
		System.exit(0);
	}
	
	/* Prints an error message, gets the stack trace for 
	 * a file not found exception, and exits the program. */
	public static void handleError(String msg, FileNotFoundException e) {
		System.out.println(msg);
		e.getStackTrace();
		System.exit(0);
	}
	
	/* Prints all of the semantic errors that were 
	 * found in the program. */
	public static void handleSemanticError(Queue<String> errorMsgs) {
		Iterator<String> iterator = errorMsgs.iterator();

		// Print all semantic errors that were found
		while(iterator.hasNext()) {
			System.out.println(iterator.next());
		}
		
		// Exit the program
		System.exit(0);
	}
	
	/* Handles when a token isn't equal to any of the possible tokens
	 * that it can be equal to according to the grammar. */
	public static void handleTokenNotInFirstSet(Core actualToken, Set<Core> targetTokens, String symbol) {
		
		// Print error message and exit from program
		System.out.print("ERROR parsing " + symbol + ": Token must be ");
		
		// Print all tokens that are missing
		Iterator<Core> iterator = targetTokens.iterator();
		int elementsPrinted = 0;
		while(iterator.hasNext()) {
			
			// Format output
			if(elementsPrinted != targetTokens.size() - 1) {
				System.out.print(iterator.next().toString() + ", ");
			} else {
				System.out.print("or " + iterator.next().toString() + ", but is " + actualToken.toString());
			}
			elementsPrinted++;
		}
		System.exit(0);
	}
}
