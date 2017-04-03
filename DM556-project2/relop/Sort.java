package relop;

import global.*;

public class Sort extends Iterator implements GlobalConst {
	protected Iterator iterator;
	protected int sortField;
	protected int sortMemSize;
	protected int bufSize;

	/** The underlying left iterator. */
	protected Iterator left;

	/** The underlying right iterator. */
	protected Iterator right;

	/** Current tuple from left iterator. */
	protected Tuple outer;

	/** Next tuple to return. */
	protected Tuple next;

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
//		throw new UnsupportedOperationException("Not implemented");

	}
	

	@Override
	public void explain(int depth) {
		throw new UnsupportedOperationException("Not implemented");

//		indent(depth);
//		System.out.print("SimpleJoin : ");
//		if (depth > 0) {
//			for (int i = 0; i < depth - 1; i++) {
//				System.out.print(preds[i].toString() + " OR ");
//			}
//			System.out.println(preds[preds.length - 1]);
//		} else {
//			System.out.println("(cross)");
//		}
//		left.explain(depth + 1);
//		right.explain(depth + 1);

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
		while(true) {
			if (next == null) {
				if(iterator.hasNext()) {
					next = iterator.getNext();
					return true;
				}else{
					return false;

				}
			}
			next = null;
			iterator.restart();
		}
	}

	@Override
	public Tuple getNext() {
		if (next == null) {
			throw new IllegalStateException("no more tuples");
		}
		// return (and forget) the tuple
		Tuple tuple = next;
		next = null;
		return tuple;
//		throw new UnsupportedOperationException("Not implemented");
	}

}
