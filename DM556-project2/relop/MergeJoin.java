package relop;


import java.util.IllegalFormatException;

public class MergeJoin extends Iterator {


    /**
     * The underlying left iterator.
     */
    protected Iterator left;

    /**
     * The underlying right iterator.
     */
    protected Iterator right;

    /**
     * left col.
     */
    protected Integer lcol;

    /**
     * right col.
     */
    protected Integer rcol;

    /**
     * Current tuple from left iterator.
     */
    protected Tuple tuple;

    /**
     * Current tuple from left iterator.
     */
    protected Tuple outer;


    /**
     * Next tuple to return.
     */
    protected Tuple next;


    public MergeJoin(Iterator left, Iterator right, Integer lcol, Integer rcol) {
        this.left = left;
        this.right = right;
        this.lcol = lcol;
        this.rcol = rcol;
        schema = Schema.join(left.schema, right.schema);

    }


    @Override
    public void explain(int depth) {

        indent(depth);
        System.out.print("Projection : ");
        for (int i = 0; i < this.schema.names.length - 1; i++) {
            System.out.println("{" + this.schema.names[i] + "}");
        }
        System.out.println("{" + this.schema.names[this.schema.names.length - 1] + "}");
        this.left.explain(depth + 1);
        this.right.explain(depth + 1);
//
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
    public boolean isOpen() {
        return (left != null);
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
        while (true) {
            if (outer == null) {
                if (left.hasNext()) {
                    outer = left.getNext();
                } else {
                    return false;
                }
            }
            while (this.right.hasNext()) {
                Tuple rightTuple = right.getNext();
                next = Tuple.join(outer, rightTuple, schema);
                if (outer.getField(lcol) == rightTuple.getField(rcol)) {
                    return true;
                }
            }


            outer = null;
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
