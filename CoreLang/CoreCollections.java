package CoreLang;

import static CoreLang.Core.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/* This class allows us to access 
 * certain data structures that help us
 * classify the token or determine the
 * token's attributes. 
 * This means we don't have to declare
 * these structures all in one class
 * and have them only be accessible to
 * that single class. */
public final class CoreCollections{
	private static final HashSet<Character> keywordStartChars = new HashSet<>(
			Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'i', 'n', 'o', 'p', 'r', 't'));
	
	private static final Map<Core, Character> symbols = Map.ofEntries(Map.entry(LPAREN, '('),
			Map.entry(RPAREN, ')'), 
			Map.entry(LSQUARE, '['),
			Map.entry(RSQUARE, ']'), 
			Map.entry(LCURL, '{'),
			Map.entry(RCURL, '}'), 
			Map.entry(PERIOD, '.'),
			Map.entry(COLON, ':'), 
			Map.entry(SEMICOLON, ';'),
			Map.entry(COMMA, ','), 
			Map.entry(LESS, '<'),
			Map.entry(DIVIDE, '/'), 
			Map.entry(ADD, '+'),
			Map.entry(MULTIPLY, '*'),
			Map.entry(SUBTRACT, '-'),
			Map.entry(ASSIGN, '='));
	
	private static final Map<Core, String> keywords = Map.ofEntries(Map.entry(AND, "and"), 
			Map.entry(BEGIN, "begin"), 
			Map.entry(CASE, "case"), 
			Map.entry(DO, "do"),
			Map.entry(ELSE, "else"), 
			Map.entry(END, "end"), 
			Map.entry(FOR, "for"), 
			Map.entry(IF, "if"), 
			Map.entry(IN, "in"),
			Map.entry(INTEGER, "integer"), 
			Map.entry(IS, "is"), 
			Map.entry(NEW, "new"), 
			Map.entry(NOT, "not"), 
			Map.entry(OBJECT, "object"), 
			Map.entry(OR, "or"), 
			Map.entry(PRINT, "print"), 
			Map.entry(PROCEDURE, "procedure"), 
			Map.entry(READ, "read"), 
			Map.entry(RETURN, "return"), 
			Map.entry(THEN, "then"));
	
	// Private constructor to prevent instantiation
	private CoreCollections() {}
	
	/* Returns a Map of all of the keywords and 
	 * their corresponding tokens */
	public static Map<Core, String> getKeywordsMap() {
		return keywords;
	}
	
	/* Returns a Map of all of the symbols (excluding EQUAL)
	 * and their corresponding tokens */
	public static Map<Core, Character> getSymbolsMap(){
		return symbols;
	}
	
	/* Returns a HashSet of all of the characters that a
	 * keyword can start with. */
	public static HashSet<Character> getKeywordStartCharsSet() {
		return keywordStartChars;
	}
}
