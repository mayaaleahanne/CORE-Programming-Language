package CoreLang;

import static CoreLang.Core.*;

// This class classifies the tokens.
public class TokenClassifier{
	private final Character equalSign = '=';
	private final Character singleQuote = '\'';
	private CoreToken coreToken;
	private FileReaderHelper reader;
	
	// Constructor
	TokenClassifier(CoreToken token, FileReaderHelper reader){
		this.coreToken = token;
		this.reader = reader;
	}
	
	/* Classifies a token by using the
	 * first character in its string
	 * representation. */
	public void classifyWithFirstChar(int firstCharAsInt) {
		
		// Check if EOS has been reached
		if(firstCharAsInt == -1) {
			this.coreToken.setToken(EOS);
			this.coreToken.SetTokenClassified(true);
			this.coreToken.setStrRepBuilt(true);
			this.reader.closeFile();
		}
		
		// Check if token is an "ID" or a CONST token
		else if(Character.isLetterOrDigit(firstCharAsInt)) {
			
			// Assign the token to CONST or "ID"
			if(Character.isDigit(firstCharAsInt)) {
				this.coreToken.setToken(CONST);
				this.coreToken.SetTokenClassified(true);
			}
			
			/* Both ID and keywords tokens start with a letter
			 * so the approach we'll take is to treat them both
			 * TEMPORARILY as ID tokens since both start with a 
			 * letter and then convert ID tokens to keyword tokens
			 * if their string representations are found to be
			 * equal to any of the keywords once the string
			 * representation of the token has been fully
			 * constructed. */
			else {
				this.coreToken.setToken(ID);
				
				/* When doing first character examination, it is easier to determine if a token 
				 * isn't a keyword rather than verify that it is a keyword; this is because
				 * we need the full string to do that. Thus, we will see if we can verify that the
				 * token is an ID instead by seeing if its first character violates any of the rules
				 * of keywords, like if the ID doesn't start with a lower case letter that any of the 
				 * keywords start with. */
				if(!this.coreToken.canBeKeyword((char)firstCharAsInt)) {
					this.coreToken.SetTokenClassified(true);
				}
			}
		}
		
		/* We can only automatically classify a symbol token if its first character isn't 
		 * an equal sign because, in that case, we don't have to check whether the next
		 * character is also an equal sign or not (and whether the token is thus an EQUAL
		 * token rather than an ASSIGN token). 
		 * Thus, we will use ASSIGN as the default initial token for ASSIGN and EQUAL tokens 
		 * and convert the token to an EQUAL token if a second equals sign is found. */
		else if(this.coreToken.isSymbol((char)firstCharAsInt)) {
			
			/* Check if currChar is an equals sign. */
			if(!equalSign.equals((char)firstCharAsInt)) {
				Core symbolToken = this.coreToken.getSymbolToken((char)firstCharAsInt);
				this.coreToken.setToken(symbolToken);
				this.coreToken.SetTokenClassified(true);
				this.coreToken.setStrRepBuilt(true);
			} else {
				this.coreToken.setToken(ASSIGN);
			}
		}
		
		// Check if a token is a STRING
		else if(singleQuote.equals((char)firstCharAsInt)) {
			this.coreToken.setToken(STRING);
			this.coreToken.SetTokenClassified(true);
		} else {
			// Close file and handle error
			this.reader.closeFile();
			this.coreToken.setToken(ERROR);
			ErrorHandler.handleError("ERROR: \'" + (char)firstCharAsInt + "\' is not a valid symbol token.");
		}
		
		// Do NOT add single quote to currTokenStr
		if(!this.coreToken.equals(STRING) && !this.coreToken.equals(EOS)) {
			this.coreToken.getStrRep().append((char)firstCharAsInt);
		}
	}
	
	/* Classifies a token by using
	 * a character in the next token. */
	public void classifyWithNextTokenChar(int nextTokenChar) {
		
		// Make sure the end of the token has been reached
		if(this.coreToken.endOfTokenReached(nextTokenChar)) {
			this.coreToken.setStrRepBuilt(true);
			
			/* Check if a STRING token is missing a closing
			 * single quote (that makes it invalid). */
			if(this.coreToken.equals(STRING) && nextTokenChar == -1) {
				
				// Close file and handle error
				this.reader.closeFile();
				this.coreToken.setToken(ERROR);
				ErrorHandler.handleError("ERROR: \'" + coreToken.getStrRep().toString() + 
						"\' is an invalid STRING (missing closing single quote).");
			}
			
			/* Check if the token we initially classified as an 
			 * ASSIGN token is actually an EQUAL token. */
			else if(this.coreToken.equals(ASSIGN)){
				this.coreToken.assignTokenHandler((char)nextTokenChar);
			}
		}
	}
	
	/* Classifies a token by using its
	 * string representation. */
	public void classifyWithStrRep() {
		
		/* Check whether token type has already been determined. */
		if(!this.coreToken.tokenClassified()) {
			
			/* Convert ID token to keyword token 
			 * if it is a keyword token. */
			if(this.coreToken.isKeyword(this.coreToken.getStrRep().toString())) {
				Core token = coreToken.getKeywordToken(this.coreToken.getStrRep().toString());
				this.coreToken.setToken(token);
				this.coreToken.SetTokenClassified(true);
			}
		}
		
		/* Change CONST token to ERROR token if necessary. */
		else if(this.coreToken.equals(CONST)) {
			
			/* Check if token is a valid CONST token. */
			if(!this.coreToken.isValidConstant(this.coreToken.getStrRep().toString())) {
				
				// Close file and handle error
				this.reader.closeFile();
				this.coreToken.setToken(ERROR);
				
				// Determine which rule the CONST token violates
				if(this.coreToken.getStrRep().charAt(0) == '0' && this.coreToken.getStrRep().length() > 1) {
					ErrorHandler.handleError("ERROR: " + coreToken.getStrRep().toString() + " is an Invalid CONST. "
							+ "CONST cannot have leading zeroes.");
				} else {
					ErrorHandler.handleError("ERROR: " + coreToken.getStrRep().toString() + " is an Invalid CONST. "
							+ "CONST is too big.");
				}
			}
		}
	}
}
