package relop;

/**
 * The selection operator specifies which tuples to retain under a condition; in
 * Minibase, this condition is simply a set of independent predicates logically
 * connected by OR operators.
 */
public class Selection extends Iterator {

  protected Iterator iterator;
  protected Predicate[] predicates;
  protected Tuple tuple;
  /**
   * Constructs a selection, given the underlying iterator and predicates.
   */
  public Selection(Iterator iter, Predicate... preds) {
    this.iterator = iter;
    this.predicates = preds;
    this.schema = iter.schema;
    this.tuple = null;
  }

  /**
   * Gives a one-line explaination of the iterator, repeats the call on any
   * child iterators, and increases the indent depth along the way.
   */
  public void explain(int depth) {
    System.out.print("Selection : ");

    for(int i = 0; i < this.predicates.length - 1; ++i) {
      System.out.print(this.predicates[i].toString() + " OR ");
    }

    System.out.println(this.predicates[this.predicates.length - 1]);
    this.iterator.explain(depth + 1);
  }

  /**
   * Restarts the iterator, i.e. as if it were just constructed.
   */
  public void restart() {
    this.iterator.restart();
    this.tuple = null;
  }

  /**
   * Returns true if the iterator is open; false otherwise.
   */
  public boolean isOpen() {
    return this.iterator != null;
  }

  /**
   * Closes the iterator, releasing any resources (i.e. pinned pages).
   */
  public void close() {
    if(this.iterator != null) {
      this.iterator.close();
      this.iterator = null;
    }
  }

  /**
   * Returns true if there are more tuples, false otherwise.
   */
  public boolean hasNext() {
    while(this.iterator.hasNext()) {
      this.tuple = this.iterator.getNext();
      for(int i = 0; i < this.predicates.length; ++i) {
        if(this.predicates[i].evaluate(this.tuple)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the next tuple in the iteration.
   * 
   * @throws IllegalStateException if no more tuples
   */
  public Tuple getNext() {
    if(this.tuple == null) {
      throw new IllegalStateException("no more tuples");
    } else {
      Tuple tuple = this.tuple;
      this.tuple = null;
      return tuple;
    }
  }

} // public class Selection extends Iterator
