package CoreLang;

import static CoreLang.Core.*;
import java.util.Map;

/* This class stores the string representation
 * of the value of the token as well as its 
 * token itself. 
 * It also contains methods that tell us
 * more information about the token as well
 * as helper methods for the TokenClassifier
 * class and the Tokenizer class. */
public class CoreToken{
	private Core token;
	private StringBuilder strRep;
	private boolean tokenClassified;
	private boolean strRepBuilt;
	private final int FLOOR = 0; 
	private final int CEILING = 8191;
	private final Character singleQuote = '\'';
	private final Character equalSign = '=';
	
	// Constructs a CoreToken
	public CoreToken(){
		
		// Initialize members
		this.token = null;
		this.strRep = new StringBuilder();
		this.tokenClassified = false;
		this.strRepBuilt = false;
	}
	
	// Returns a token of type Core
	public Core getToken() {
		return this.token;
	}
	
	/* Returns the StringBuilder object
	 * that holds strRep. */
	public StringBuilder getStrRep() {
		return this.strRep;
	}
	
	/* Tells whether the token has been 
	 * classified and that classification
	 * is a "final", not temporary 
	 * classification.
	 * Temporary classifications are 
	 * the approach we take with
	 * potential EQUAL tokens,
	 * actual ASSIGN tokens,
	 * ID tokens that can't be 
	 * classified as an ID with the
	 * first character, and 
	 * potential keyword tokens. 
	 * NOTE: a final classification
	 * means that we have identified
	 * what the token SHOULD be. If
	 * we find that the token is invalid
	 * for its token type down the line,
	 * we will change it to an ERROR token.*/
	public boolean tokenClassified() {
		return this.tokenClassified;
	}
	
	/* Tells whether the string representation
	 * of the value of the token has been built. */
	public boolean strRepBuilt() {
		return this.strRepBuilt;
	}
	
	// Sets a token of type Core.
	public void setToken(Core token) {
		this.token = token;
	}
	
	// Sets the value of tokenClassified.
	public void SetTokenClassified(boolean tokenClassified) {
		this.tokenClassified = tokenClassified;
	}
	
	// Sets the value of strRepBuilt
	public void setStrRepBuilt(boolean strRepBuilt) {
		this.strRepBuilt = strRepBuilt;
	}
	
	/* Tells whether a token of type core
	 * is equal to this token. 
	 * This method makes it so that we don't
	 * have to directly access the token of
	 * type Core every time we want to compare 
	 * it to another token. */
	public boolean equals(Core compare) {
		return this.token.equals(compare);
	}
	
	// Tells if token is a symbol token
	public boolean isSymbol() {
		return (CoreCollections.getSymbolsMap().containsKey(this.token) || 
			this.token.equals(EQUAL)); 
	}
	
	// Tells if token is a keyword token
	public boolean isKeyword() {
		return CoreCollections.getKeywordsMap().containsKey(this.token);
	}
	
	/* Tells whether we can classify this token using only one character
	 * NOTE: technically ASSIGN is classified with one character. However,
	 * it is the value of the character after it that tells us whether
	 * the token is ASSIGN or EQUAL, so we cannot classify it with
	 * only one character. 
	 * STRING and CONST tokens also cannot be classified with only one
	 * character because we need to look at more characters to check
	 * their validity. 
	 * There are cases where ID tokens can be classified with only one
	 * character, but since this isn't always the case, they aren't
	 * considered to be tokens that can be classified with one
	 * character. */
	public boolean classifyWithOneChar() {
		return (this.token.equals(EOS) || this.token.equals(ERROR) ||
			(CoreCollections.getSymbolsMap().containsKey(this.token) && 
			!this.token.equals(ASSIGN)));
	}
	
	// This is where the helper methods begin
	
	/* Helper method for the Tokenizer class.
	 * Tells if one needs to peak at the first
	 * character within the next token to detect
	 * the end of this token. */
	public boolean peakAtNextChar() {
		boolean peakAtNextChar = false;
		
		/* Check for conditions where the next
		 * character would need to be looked at
		 * to detect the end of this token. */
		if(this.token.equals(ID) || 
				CoreCollections.getKeywordsMap().containsKey(this.token) || 
			this.token.equals(ASSIGN) || this.token.equals(CONST)) {
			peakAtNextChar = true;
		}
		return peakAtNextChar;
	}
	
	/* Helper method for the TokenClassifier class.
	 * class.
	 * Determines whether a string is a keyword. */
	public boolean isKeyword(String str) {
		return CoreCollections.getKeywordsMap().containsValue(str);
	}
	
	/* Is a helper method for the TokenClassifier
	 * class.
	 * Determines whether the first character 
	 * is a valid symbol. 
	 * NOTE: we DO NOT use this to check for
	 * the EQUAL symbol because if the character
	 * is an equal sign, our approach is to default
	 * to the ASSIGN token and then peak ahead at 
	 * the next character to see if another equal
	 * sign is present and we can convert the 
	 * token from ASSIGN to EQUAL. */
	public boolean isSymbol(char firstChar) {
		return CoreCollections.getSymbolsMap().containsValue(firstChar);
	}
	
	/* Helper method for the TokenClassifier class.
	 * Tells whether we've reached the end of this token 
	 * NOTE: we use ID tokens as the default for keyword
	 * tokens; we convert ID tokens into keyword tokens
	 * once we have the final string representation
	 * of the value of the token and we've verified that
	 * it's equal to one of the keywords.*/
	public boolean endOfTokenReached(int charAsInt) {
		boolean nextTokenReached = false;
		
		// Check conditions for reaching next token
		if(this.token.equals(CONST) && !Character.isDigit(charAsInt) ||
				this.token.equals(STRING) && 
				(singleQuote.equals((char)charAsInt) || charAsInt == -1) ||
				this.token.equals(ID) && !Character.isLetterOrDigit(charAsInt) ||
				CoreCollections.getSymbolsMap().containsKey(this.token) || 
				this.token.equals(EOS) || this.token.equals(ERROR)) {
			nextTokenReached = true;
		}
		return nextTokenReached;
	}
	
	/* Helper method for the TokenClassifier class.
	 * Gets token that corresponds to the given symbol, 
	 * NOT including the EQUAL token. 
	 * Returns a null token if currChar isn't a value 
	 * within the symbol Map. */
	public Core getSymbolToken(char currChar) {
		Core token = null;
		Map<Core, Character> symbols = CoreCollections.getSymbolsMap();
		
		/* Verify that currChar is a value in the 
		 * symbol map. */
		if(symbols.containsValue(currChar)) {
			
			/* Find the key. */
			for(Map.Entry<Core, Character> entry : symbols.entrySet()) {
				
				/* Return token if it corresponds to 
				 * the given symbol. */
				if(entry.getValue().equals(currChar)) {
					token = entry.getKey();
					break;
				}
			}
		}
		return token;
	}
	
	/* Helper method for the TokenClassifier class.
	 * Gets token that corresponds to the given keyword.
	 * Returns a null token if the string isn't a value
	 * within the keywords Map. */
	public Core getKeywordToken(String str) {
		Core token  = null;
		Map<Core, String> keywords = CoreCollections.getKeywordsMap();
		
		/* Verify that the string is a value in the 
		 * keywords map. */
		if(keywords.containsValue(str)) {
			
			/* Find the key. */
			for(Map.Entry<Core, String> entry : keywords.entrySet()) {
				
				/* Return token if it corresponds to the 
				 * given symbol. */
				if(entry.getValue().equals(str)) {
					token = entry.getKey();
					break;
				}
			}
		}
		return token;
	}
	
	/* Helper method for the TokenClassifier class.
	 * Checks whether the given starting character 
	 * is any of the letters that any of the keywords 
	 * start with (is case sensitive). */
	public boolean canBeKeyword(char startChar) {
		return CoreCollections.getKeywordStartCharsSet().contains(startChar);
	}
	
	// Tells whether a given string representation of a CONST is valid
	public boolean isValidConstant(String str) {
		final int MAXSTRLEN = 5;
		boolean isValid = false;
		
		/* Check for leading zeroes or too big of a value. */
		if(!(str.charAt(0) == '0' && str.length() > 1) && str.length() < MAXSTRLEN) {
			
			/* Check if value falls within required range. 
			 * The reason these checks are broken up is that we
			 * don't want to pass an integer into the parseInt 
			 * method that is above the upper bound of what is 
			 * allowed for an integer value and get an error or 
			 * pass a string with a leading 0 into the parseInt 
			 * method and get a valid integer when it should 
			 * be invalid.
			 * The parseInt method removes the leading zeroes we
			 * want to check for to detect an invalid CONST token. */
			if(Integer.parseInt(str) >= FLOOR && Integer.parseInt(str) <= CEILING) {
				isValid = true;
			}
		}
		return isValid;
	}
	
	/* Helper method for the TokenClassifier class.
	 * Handles when an assign token is present.
	 * This method either maintains the ASSIGN token
	 * or converts it into an EQUAL token depending
	 * on the value of character that comes after it. */
	public void assignTokenHandler(char nextChar) {
		this.tokenClassified = true;
		this.strRepBuilt = true;
		
		// Check whether the token is an EQUAL token
		if(equalSign.equals(nextChar)) {
			this.strRep.append(nextChar);
			this.token = EQUAL;
		}
	}
}
