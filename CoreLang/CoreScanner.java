package CoreLang;

import static CoreLang.Core.*;

// This class implements the scanner for the Core language
public class CoreScanner {
	private Tokenizer tokenizer;
	private CoreToken coreToken;
	private Core currToken;
	
    // Initialize the scanner
    public CoreScanner(String filename) {

    	// Initialize the Tokenizer to build and return each token.
    	this.tokenizer = new Tokenizer(new FileReaderHelper(filename));
    	
    	// Build first token and store it inside of currToken
    	this.coreToken = tokenizer.getCoreToken();
    	this.currToken = this.coreToken.getToken();
    }

    /* Advance to the next token. 
     * Print error message if the 
     * current token is the EOS token and
     * exit the program. */
    public void nextToken() {
    	
    	// Handle case when one tries to advance past the EOS token
    	if(this.coreToken.equals(EOS)) {
    		ErrorHandler.handleError("ERROR: No next token. Already reached EOS.");
    	}
    	
    	// Build the next token and store it inside of currToken
    	this.coreToken = tokenizer.getCoreToken();
    	this.currToken = this.coreToken.getToken();
    }

    // Return the current token
    public Core currentToken() {
    	return this.currToken;
    }

	/* Return an ID token's string value.
	 * Prints an error message and exits the
	 * program if the current token is not
	 * an ID token. */
    public String getId() {

    	// Check if the token is a STRING token
    	if(!this.coreToken.equals(ID)) {
    		ErrorHandler.handleError("ERROR: Cannot use an ID token method on a " + 
    				this.currToken.toString() + " token.");
    	}
		String str = this.coreToken.getStrRep().toString();
		
    	return str;
    }

	/* Returns a CONST token's value 
	 * Prints an error message and exits the
	 * program if the current token is not
	 * a CONST token. */
    public int getConst() {

    	// Check if the token is a CONST token
    	if(!this.coreToken.equals(CONST)) {
    		ErrorHandler.handleError("ERROR: Cannot use a CONST token method on a " + 
    				this.currToken.toString() + " token.");
    	}
		int val = Integer.parseInt(coreToken.getStrRep().toString());
		
    	return val;
    }
	
	/* Return a STRING token's string value.
	 * Prints an error message and exits the
	 * program if the current token is not
	 * an STRING token. */
    public String getString() {

    	// Check if the token is a STRING token
    	if(!this.coreToken.equals(STRING)) {
    		ErrorHandler.handleError("ERROR: Cannot use a STRING token method on a " + 
    				this.currToken.toString() + " token.");
    	}
		String str = this.coreToken.getStrRep().toString();
		
    	return str;
    }
    
    /* Returns a string representation of a symbol
     * token.
     * Prints an error message and exits the program
     * if the current token is not a symbol token. */
    public String getSymbol() {
    	
    	// Check if the token is a symbol token
    	if(!this.coreToken.isSymbol()) {
    		ErrorHandler.handleError("ERROR: Cannot use a symbol token method on a " + 
    				this.currToken.toString() + " token.");
    	}
    	String str = this.coreToken.getStrRep().toString();
    	return str;
    }
}
