package CoreLang;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

/* A class of helper methods that read
 * characters from a file. 
 * I wrote this so I wouldn't have
 * any try-catch statements in the
 * Tokenizer class. */
public class FileReaderHelper{
	private PushbackReader reader;
	private final String fileName;
	
	// Constructor
	public FileReaderHelper(String fileName){
		this.fileName = fileName;
		
		// Initialize PushbackReader
		try {
			this.reader = new PushbackReader(new FileReader(fileName));
		} catch(FileNotFoundException e) {
			ErrorHandler.handleError("ERROR: File " + fileName + " not found.", e);
		}
	}
	
	/* Reads and returns the first non-whitespace character in
	 * the file as an integer.
	 * Returns -1 if EOS is reached before encountering any
	 * non-whitespace characters.
	 * Returns -2 if an error occurs while reading the file. */
	public int getFirstChar() {
		
		// Read the first character of the token
		int firstCharAsInt = -2;
		try {
			firstCharAsInt = this.reader.read();
			
			/* Make sure the first character isn't ever 
			 * whitespace.
			 * We do not want to pass a whitespace character 
			 * into the first character classifier. */
			while(firstCharAsInt != -1 && Character.isWhitespace((char)firstCharAsInt)) {
				firstCharAsInt = this.reader.read();
			}
		} catch (IOException e) {
			ErrorHandler.handleError("ERROR: Problem reading from " + this.fileName + ".", e);
		}
		
		return firstCharAsInt;
	}
	
	/* Returns the next character.
	 * Returns -1 if EOS has been
	 * reached.
	 * Returns -2 if a problem
	 * occurs while reading the file. */
	public int getNextChar() {
		int nextChar = -2;
		try {
			nextChar = reader.read();
		} catch (IOException e) {
			ErrorHandler.handleError("ERROR: Problem reading from " + this.fileName + ".", e);
		}
		return nextChar;
	}
	
	// Unreads a character
	public void unreadChar(int currCharAsInt) {
		try {
			reader.unread(currCharAsInt);
		} catch (IOException e) {
			ErrorHandler.handleError("ERROR: Problem unreading a character from " + this.fileName + ".", e);
		}
	}
	
	// Closes the file
	public void closeFile() {
		try {
			
			// Make sure we don't try to close a null reader
			if(!this.reader.equals(null))
    		reader.close();
		} catch(IOException e) {
			ErrorHandler.handleError("ERROR: Couldn't close file " + fileName + ".", e);
		}
	}
}
