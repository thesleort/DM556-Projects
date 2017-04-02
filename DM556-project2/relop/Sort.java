package relop;

import global.*;

public class Sort extends Iterator implements GlobalConst {
	protected Iterator iterator;
	protected int sortField;
	protected int sortMemSize;
	protected int bufSize;
  /**
   * Constructs a sort operator. 
   * @param sortMemSize the size the memory used for internal sorting. For simplicity, you can assume it is in the unit of tuples.
   * @param bufSize the total buffer size for the merging phase in the unit of page.
   * TODO
   */
	public Sort(Iterator iter, int sortfield, int sortMemSize, int bufSize) {
	    this.iterator = iter;
	    this.sortField = sortfield;
	    this.sortMemSize = sortMemSize;
	    this.bufSize = bufSize;
		throw new UnsupportedOperationException("Not implemented");
	}
	

	@Override
	public void explain(int depth) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void restart() {
	    this.iterator.restart();
	}

	@Override
	public boolean isOpen() {
	    return this.iterator.isOpen();
	}

	@Override
	public void close() {
	    this.iterator.close();
	}

	@Override
	public boolean hasNext() {
		return this.iterator.hasNext();
	}

	@Override
	public Tuple getNext() {
		throw new UnsupportedOperationException("Not implemented");
	}

}
