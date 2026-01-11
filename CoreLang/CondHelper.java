package CoreLang;

import static CoreLang.Symbol.SymbolType.*;

// This is class of helper methods that help execute a COND symbol
public class CondHelper{
	private int currChildIndex;
	private boolean orTheResult;
	private Symbol symbol;
	
	// Constructor
	public CondHelper(Symbol symbol) {
		this.currChildIndex = 0;
		this.symbol = symbol;
	}
	
	// Check if the result of the execution of a cmpr symbol needs to be negated
	public final boolean leadingNegationPresent() {
		boolean negationPresent = false;
		int numNegations = 0;
		
		// Search for trailing consecutive negations
		while(this.currChildIndex < this.symbol.getChildren().size()) {
			Symbol currChild = this.symbol.getChildren().get(this.currChildIndex);
			
			// Check for the presence of a terminal symbol
			if(currChild.getType().equals(Terminal)) {
				
				// Check if that terminal symbol is a negation
				switch(currChild.getNameOrValue()){
					case "not":
						numNegations++;
						break;
					case "or":
						this.orTheResult = true;
						break;
					case "and":
						this.orTheResult = false;
						break;
					default:
						break;
				}
			} else {
				break;
			}
			this.currChildIndex++;
		}
		
		// Check if a leading negation exists
		if(numNegations % 2 != 0) {
			negationPresent = true;
		}
		return negationPresent;
	}
	
	// Returns the value of orTheResult
	public boolean getOrTheResult() {
		return this.orTheResult;
	}
	
	/* Returns the current child index that's been 
	 * reached during the execution process. */
	public int getCurrChildIndex() {
		return this.currChildIndex;
	}
	
	// Sets the value of currChildIndex
	public void setCurrChildIndex(int currChildIndex) {
		this.currChildIndex = currChildIndex;
	}
}
