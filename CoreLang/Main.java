package CoreLang;

import static CoreLang.Symbol.SymbolType.*;

class Main {
	public static void main(String[] args) {
		
		// Initialize the scanner with the input file
		CoreScanner scanner = new CoreScanner(args[0]);
		
		// Initialize the file reader
		CoreScanner reader = new CoreScanner(args[1]);

		// Initialize and build parse tree
		Symbol parseTreeRoot = SymbolFactory.createSymbol(Procedure);
		parseTreeRoot.parse(scanner);
		
		// Check for semantic errors
		SemanticChecker.checkForErrors();
		
		// Initialize the call stack
		CallStack.addFrame(new Frame());
		
		// Execute the parse tree
		parseTreeRoot.execute(reader);
		
		// Break down the call stack
		CallStack.removeFrame();
	}
}
