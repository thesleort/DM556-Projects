package relop;


import java.util.IllegalFormatException;

public class MergeJoin extends Iterator {


	/** The underlying left iterator. */
	protected Iterator left;

	/** The underlying right iterator. */
	protected Iterator right;

	/** left col. */
	protected Integer lcol;

	/** right col. */
	protected Integer rcol;

	/** Current tuple from left iterator. */
	protected Tuple tuple;

	/** Current tuple from left iterator. */
	protected Tuple outer;


	/** Next tuple to return. */
	protected Tuple next;


	public MergeJoin(Iterator left, Iterator right, Integer lcol, Integer rcol){
		schema = Schema.join(left.schema, right.schema);
		this.left = left;
		this.right =  right;
		this.lcol = lcol;
		this.rcol = rcol;
		System.out.println("lcol = "+ lcol);
		System.out.println("rcol = "+ rcol);
		outer = null;
		next = null;
		
	}
	

	@Override
	public void explain(int depth) {
//
//		indent(depth);
//		System.out.print("MergeJoin : ");
//		if (.length > 0) {
//			for (int i = 0; i < preds.length - 1; i++) {
//				System.out.print(preds[i].toString() + " OR ");
//			}
//			System.out.println(preds[preds.length - 1]);
//		} else {
//			System.out.println("(cross)");
//		}
//		left.explain(depth + 1);
//		right.explain(depth + 1);

		// TODO Auto-generated method stub
//		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void restart() {
		left.restart();
		right.restart();
		outer = null;
		next = null;
	}

	@Override
	public boolean isOpen() { return (left != null);
	}

	@Override
	public void close() {
		if (left != null) {
			left.close();
			right.close();
			left = null;
			right = null;
		}
	}

	@Override
	public boolean hasNext() {
//		System.out.println("lcol = "+ lcol);
//		System.out.println("rcol = "+ rcol);
	while(true) {
		if (outer == null) {
//            System.out.println("outer = true");
            if(left.hasNext()) {
				outer = left.getNext();
				lcol++;
			}else{
				return false;

			}
		}
		if (this.right.hasNext()) {

			this.next = this.right.getNext();
			rcol++;
//			System.out.println("joining "+outer.toString()+" with "+ right.toString());
			this.next = Tuple.join(outer, right.getNext(), schema);
			if(next.getField())
			return true;
			}


		outer = null;
		lcol = 0;
		right.restart();
		}

//		throw new IllegalStateException("debugging crash");

	}

	@Override
	public Tuple getNext() {

		// validate the next tuple
		if (next == null) {
			throw new IllegalStateException("no more tuples");
		}
		// return (and forget) the tuple
		Tuple tuple = next;
		next = null;
		return tuple;
	}

}
