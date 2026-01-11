package CoreLang;

/* This class creates the tokens for the
 * CoreScanner as well as the string
 * representations of the value of the 
 * tokens. */
public class Tokenizer {
	private FileReaderHelper reader;
	
	// Constructor
	Tokenizer(FileReaderHelper reader){
		this.reader = reader;
	}
	
	/* Builds and returns a CoreToken. 
	 * Will return null token if unable
	 * to read from the file supplied. */
	public CoreToken getCoreToken() {
		CoreToken token = new CoreToken();
		TokenClassifier classifier = new TokenClassifier(token, this.reader);
		
		// Read the first character of the token
		int currCharAsInt = this.reader.getFirstChar();
		
		// Make preliminary token classification based on first character
		classifier.classifyWithFirstChar(currCharAsInt);
		
		/* Check if the token is a type that can be classified with one
		 * character. If so, we can just return the token now. */
		if(token.classifyWithOneChar()) {
			return token;
		}
		
		// Read a single character at time
		while(!token.strRepBuilt()) {
			
			/* Continue token classification if token hasn't been completed.
			 * We only want to read more characters if we haven't completed
			 * the token. */
			currCharAsInt = this.reader.getNextChar();
			classifier.classifyWithNextTokenChar(currCharAsInt);
			
			// Only add to the string representation if it hasn't been fully built.
			if(!token.strRepBuilt()) {
				token.getStrRep().append((char)currCharAsInt);
			} else {
				/* Checks for all of the cases in which we need to "peak ahead" at a character
				 * within the next token's string in order to detect the end of the current token. 
				 * If whitespace is what denotes the end of the current token, we don't need to
				 * read it again because it is not a part of the next token's string representation. */
				if(token.peakAtNextChar() && !Character.isWhitespace(currCharAsInt) &&
						currCharAsInt != -1) {
					
					/* Unread currCharAsInt because it is the start of the next token and 
					 * we need to be able to read it again when we begin classifying the 
					 * next token. */
					this.reader.unreadChar(currCharAsInt);
				}
				
				/* Conduct final classification (check if constant is valid and 
				 * differentiate between a keyword and an identifier if
				 * necessary).*/
				classifier.classifyWithStrRep();
			}
		}
	return token;
	}
}
