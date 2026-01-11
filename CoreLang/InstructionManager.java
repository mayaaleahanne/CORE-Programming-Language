package CoreLang;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import static CoreLang.ParseInstruction.Instruction.*;

// This class manages the instruction set of a symbol
public class InstructionManager{
	private Symbol symbol;
	private boolean doneParsing;
	private boolean instructionsInvalid;
	private boolean repeatInstructions;
	private Queue<ParseInstruction> parseInstructs;
	
	// Constructor
	public InstructionManager(Symbol symbol) {
		this.symbol = symbol;
		this.parseInstructs = new LinkedList<>();
		this.doneParsing = false;
		this.instructionsInvalid = false;
		this.repeatInstructions = false;
	}
	
	// Get the value of doneParsing
	public boolean getDoneParsing() {
		return this.doneParsing;
	}
	
	// Set the value of doneParsing
	public void setDoneParsing(boolean doneParsing) {
		this.doneParsing = true;
	}
	
	// Get the value of instructionsInvalid
	public boolean getInstructionsInvalid() {
		return this.instructionsInvalid;
	}
	
	// Set the value of instructionsInvalid
	public void setInstructionsInvalid(boolean instructionsInvalid) {
		this.instructionsInvalid = instructionsInvalid;
	}
	
	// Get the value of restartInstructions
	public boolean getRepeatInstructions() {
		return this.repeatInstructions;
	}
	
	// Set the value of restartInstructions
	public void setRepeatInstructions(boolean repeatInstructions) {
		this.repeatInstructions = repeatInstructions;
	}
	
	// Registers a parse instruction
	public void registerParseInstruct(ParseInstruction instruction) {
		this.parseInstructs.add(instruction);
	}
	
	/* Executes a symbol's parse instruction set; this 
	 * is the general methodology for parsing each symbol. */
	public void executeParseInstructions(CoreScanner scanner) {
		Iterator<ParseInstruction> iterator = this.parseInstructs.iterator();
		
		// Gives the semantic error checker the current symbol's type
		SemanticChecker.update(symbol);
	
		// Parse the instruction set
		while(iterator.hasNext() && !this.doneParsing) {
			
			// Get current instruction
			ParseInstruction currInstruct = iterator.next();
			
			/* Check if we need to remove remaining parse 
			 * operations of a possible parse instruction set, 
			 * that was found to be invalid, from parseSeq 
			 * without first executing them. 
			 * At the start of a new possible parse instruction 
			 * set, we want to reset the value of parseSeqInvalid
			 * so that we don't remove the rest of the parse
			 * operations without investigating and/or 
			 * evaluating subsequent possible parse sequences. */
			if(currInstruct.getInstruction().equals(StartNewPossibleInstructionSet) || 
					!this.instructionsInvalid) {
				currInstruct.execute(scanner, this.symbol);
				
				// Check if we want to repeat the instruction set
				if(this.repeatInstructions) {
					iterator = this.parseInstructs.iterator();
					this.repeatInstructions = false;
				}
			} else {
				iterator.remove();
			}
		}
	}
}
