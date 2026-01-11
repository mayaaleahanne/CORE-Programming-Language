package CoreLang;

/* This class represents the garbage collector 
 * for the Core programming language. */
public final class GarbageCollector{
	private static int numReachableObjs = 0;
	
	// Updates the garbage collector's number of reachable objects
	public static void update(boolean increaseCount) {
		
		// Check if we are adding or subtracting from the reachable object count
		if(increaseCount) {
			System.out.println("gc:" + ++numReachableObjs);
		} 
		else {
			System.out.println("gc:" + --numReachableObjs);
		}
	}
}
