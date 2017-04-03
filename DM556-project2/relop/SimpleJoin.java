package relop;

/**
 * The simplest of all join algorithms: nested loops (see textbook, 3rd edition,
 * section 14.4.1, page 454).
 */
public class SimpleJoin extends Iterator {

  /** The underlying left iterator. */
  protected Iterator left;

  /** The underlying right iterator. */
  protected Iterator right;

  /** Predicates to evaluate. */
  protected Predicate[] preds;

  /** Current tuple from left iterator. */
  protected Tuple outer;

  /** Next tuple to return. */
  protected Tuple next;

  // --------------------------------------------------------------------------

  /**
   * Constructs a join, given the left and right iterators and join predicates
   * (relative to the combined schema).
   */
  public SimpleJoin(Iterator left, Iterator right, Predicate... preds) {
    schema = Schema.join(left.schema, right.schema);
    this.left = left;
    this.right = right;
    this.preds = preds;
    outer = null;
    next = null;
  }

  /**
   * Gives a one-line explaination of the iterator, repeats the call on any
   * child iterators, and increases the indent depth along the way.
   */
  public void explain(int depth) {
    indent(depth);
    System.out.print("SimpleJoin : ");
    if (preds.length > 0) {
      for (int i = 0; i < preds.length - 1; i++) {
        System.out.print(preds[i].toString() + " OR ");
      }
      System.out.println(preds[preds.length - 1]);
    } else {
      System.out.println("(cross)");
    }
    left.explain(depth + 1);
      right.explain(depth + 1);
  }

  /**
   * Restarts the iterator, i.e. as if it were just constructed.
   */
  public void restart() {
    left.restart();
    right.restart();
    outer = null;
    next = null;
  }

  /**
   * Returns true if the iterator is open; false otherwise.
   */
  public boolean isOpen() {
    return (left != null);
  }

  /**
   * Closes the iterator, releasing any resources (i.e. pinned pages).
   */
  public void close() {
    if (left != null) {
      left.close();
      right.close();
      left = null;
      right = null;
    }
  }

  /**
   * Returns true if there are more tuples, false otherwise.
   */
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

        } // else

      } // if

      // in general, continue looping through the right
      while (right.hasNext()) {

        // try to pass the next tuple
        next = Tuple.join(outer, right.getNext(), schema);
        if (preds.length == 0) {
          return true;
        }
        for (int i = 0; i < preds.length; i++) {

          // if it passes, return found
          if (preds[i].evaluate(next)) {
            return true;
          }

        } // for

      } // while

      // end of a round; restart the inner loop
      outer = null;
      right.restart();

    } // while (true)

  } // public boolean hasNext()

  /**
   * Gets the next tuple in the iteration.
   * 
   * @throws IllegalStateException if no more tuples
   */
  public Tuple getNext() {

    // validate the next tuple
    if (next == null) {
      throw new IllegalStateException("no more tuples");
    }

    // return (and forget) the tuple
    Tuple tuple = next;
    next = null;
    return tuple;

  } // public Tuple getNext()

} // public class SimpleJoin extends Iterator
