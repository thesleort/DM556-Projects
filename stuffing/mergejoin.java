package relop;

public class MergeJoin extends Iterator {
    /** The underlying left iterator. */
    protected Iterator left;

    /** The underlying right iterator. */
    protected Iterator right;

    /** Current tuple from left iterator. */
    protected Tuple outer;

    protected Tuple next;

    protected Integer lcol;
    protected Integer rcol;

    public MergeJoin(Iterator left, Iterator right, Integer lcol, Integer rcol) {
        this.left = left;
        this.right = right;
        this.lcol = lcol;
        this.rcol = rcol;
        schema = Schema.join(left.schema, right.schema);
    }

    public void explain(int depth) {
        indent(depth);
        System.out.println("Projection : ");
        for (int i = 0; i < this.schema.names.length - 1; i++) {
            System.out.println("{" + this.schema.names[i] + "}");
        }
        System.out.println("{" + this.schema.names[this.schema.names.length - 1] + "}");
        this.left.explain(depth + 1);
        this.right.explain(depth+1);
    }

    public void restart() {
        left.restart();
        right.restart();
        outer = null;
        next = null;
    }

    public boolean isOpen() {
        return (left != null);
    }

    public void close() {
        if (left != null) {
            left.close();
            right.close();
            left = null;
            right = null;
        }
    }

    public boolean hasNext() {
        while (true) {
            // handle boundary cases
            if (outer == null) {
                if (left.hasNext()) {
                    // get the next (or first) outer tuple
                    outer = left.getNext();
                } else {
                    // no more tuples from either iterator
                    return false;
                }
            }
            while (right.hasNext()) {
                // try to pass the next tuple
                Tuple rightTuple = right.getNext();
                next = Tuple.join(outer, rightTuple, schema);
                if (outer.getField(lcol) == rightTuple.getField(rcol)) {
                    // if it passes, return found
                    return true;
                }
            }//while
            // end of a round; restart the inner loop
            outer = null;
            right.restart();
        }
    }

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