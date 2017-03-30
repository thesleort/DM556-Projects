package relop;

import global.*;

public class Sort extends Iterator implements GlobalConst {
			
  /**
   * Constructs a sort operator. 
   * @param sortMemSize the size the memory used for internal sorting. For simplicity, you can assume it is in the unit of tuples.
   * @param bufSize the total buffer size for the merging phase in the unit of page.   
   */
	public Sort(Iterator iter, int sortfield, int sortMemSize, int bufSize) {
	    throw new UnsupportedOperationException("Not implemented");
	}
	

	@Override
	public void explain(int depth) {
	    throw new UnsupportedOperationException("Not implemented");
		
	}

	@Override
	public void restart() {
	    throw new UnsupportedOperationException("Not implemented");
		
	}

	@Override
	public boolean isOpen() {
	    throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public void close() {
	    throw new UnsupportedOperationException("Not implemented");
		
	}

	@Override
	public boolean hasNext() {
	    throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public Tuple getNext() {
	    throw new UnsupportedOperationException("Not implemented");

	}

}
