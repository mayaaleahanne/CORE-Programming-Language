package CoreLang;

import static CoreLang.Core.*;
import java.util.Map;
import java.util.TreeMap;

/* This interface contains all
 * of the methods that classes
 * that represent variables
 * in the Core language will
 * implement. */
public interface CoreVar{
	
	// Updates the reference of the variable (for object variables)
	public void updateVar(String key, int value, boolean replaceMap);
	
	// Updates the value of the variable (for integer variables)
	public void updateVar(int value);
	
	// Returns the variable's type
	public Core getVarType();
	
	// Returns the variable's identifier
	public String getIdentifier();
	
	// Returns the value of the variable (for integer variables)
	public int getValue();
	
	// Checks if a key exists (for object variables)
	public boolean keyExists(String key);
	
	// Returns the value of the variable (for object variables)
	public int getValue(String key);
	
	// Returns the default key of an object
	public String getDefaultKey();
	
	// Returns the reference of an object variable
	public Map<String, Integer> getReference();
	
	// Make an object point to the same reference as another object
	public void alias(CoreVar var);
	
	// Checks if an object has a null reference value
	public boolean refIsNull();
	
	// Updates the reference count of an object
	public void updateRefCount(boolean increase);
	
	// Returns the number of to an object
	public int getRefCount();
	
	// Classes that implement the CoreVar interface
	
	public class IntegerVar implements CoreVar{
		private int value;
		private final String identifier;
		
		// Constructor
		public IntegerVar(String identifier) {
			this.value = 0;
			this.identifier = identifier;
		}

		// N/A for an integer variable update
		@Override
		public void updateVar(String key, int value, boolean replaceMap) {
			return;
		}
		
		@Override
		public void updateVar(int value) {
			this.value = value;
		}

		@Override
		public String getIdentifier() {
			return this.identifier;
		}
		
		@Override
		public int getValue() {
			return this.value;
		}
		
		@Override
		public Core getVarType() {
			return INTEGER;
		}
		
		// N/A for an integer variable
		@Override
		public int getValue(String key) {
			return this.value;
		}
		
		// N/A for an integer variable 
		@Override
		public void alias(CoreVar var) {
			return;
		}
		
		// N/A for an integer variable
		@Override
		public String getDefaultKey() {
			return "";
		}
		
		// N/A for an integer variable
		@Override
		public boolean keyExists(String key) {
			return false;
		}
		
		// N/A for an integer variable
		@Override
		public boolean refIsNull() {
			return true;
		}
		
		// N/A for an integer variable
		@Override
		public Map<String, Integer> getReference(){
			return null;
		}
		
		// N/A for an integer variable
		@Override
		public void updateRefCount(boolean increase) {
			return;
		}
		
		// N/A for an integer variable
		@Override
		public int getRefCount() {
			return 0;
		}
	}
	
	public class ObjectVar implements CoreVar{
		private Map<String, Integer> reference;
		private final String identifier;
		private StringBuilder defaultKey = new StringBuilder();
		private int refCount;
		
		// Constructor
		public ObjectVar(String identifier) {
			this.reference = null;
			this.identifier = identifier;
			this.refCount = 0;
		}

		@Override
		public void updateVar(String key, int value, boolean replaceMap) {
			
			// Conduct assignment or initialize the object
			if(replaceMap) {
				
				// Check if map already exists
				if(this.reference != null) {
					
					// Report unreachable object to garbage collector
					if(--this.refCount == 0) {
						GarbageCollector.update(false);
					}
				}
				
				// Initialize the object and the default key
				this.reference = new TreeMap<>();
				this.defaultKey.append(key);
				this.reference.put(key, value);
				this.refCount++;
				
				// Update the number of reachable objects in the program
				GarbageCollector.update(true);
			} 
			else if(this.reference.containsKey(key)){
				
				// Update value associated with the key
				this.reference.replace(key, value);
			} else {
				
				// Add a new key value pair
				this.reference.put(key, value);
			}
		}
		
		// Not applicable to an object variable update
		@Override
		public void updateVar(int value) {
			return;
		}

		@Override
		public String getIdentifier() {
			return this.identifier;
		}
		
		// N/A for an object variable
		@Override
		public int getValue() {
			return -1;
		}
		
		public int getValue(String key) {
			return this.reference.get(key);
		}
		
		@Override
		public String getDefaultKey() {
			return this.defaultKey.toString();
		}
		
		@Override
		public boolean keyExists(String key) {
			return this.reference.containsKey(key);
		}
		
		@Override
		public boolean refIsNull() {
			return this.reference == null;
		}
		
		@Override
		public Core getVarType() {
			return OBJECT;
		}
		
		@Override
		public Map<String, Integer> getReference(){
			return this.reference;
		}
		
		@Override
		public void updateRefCount(boolean increase) {
			
			// Increase or decrease the reference count of the object
			if(increase) {
				++this.refCount;
			} else {
				--this.refCount;
				
				// Check if we need to update the garbage collector
				if(this.refCount == 0) {
					GarbageCollector.update(false);
				}
			}
		}
		
		@Override
		public int getRefCount() {
			return this.refCount;
		}
		
		@Override
		public void alias(CoreVar var) {
			
			// Change reference and the default key for the given variable
			this.defaultKey.replace(0, this.defaultKey.length(), var.getDefaultKey());
			this.reference = var.getReference();
			
			/* Update reference count and check if object is unreachable.
			 * NOTE: We only want to decrement the reference count if
			 * there was a reference to begin with. If there were no references
			 * then the count should stay the same value, which is 0. */
			if(this.refCount > 0) {
				--this.refCount;
				if(this.refCount == 0) {
					GarbageCollector.update(false);
				}
			}
		}
	}
}
