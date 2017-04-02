package relop;

/**
 * The projection operator extracts columns from a relation; unlike in
 * relational algebra, this operator does NOT eliminate duplicate tuples.
 */
public class Projection extends Iterator {
  protected Iterator iterator;
  protected Integer[] integers;
  /**
   * Constructs a projection, given the underlying iterator and field numbers.
   */
  public Projection(Iterator iter, Integer... fields) {
    this.schema = new Schema(fields.length);

    for(int i = 0; i < fields.length; ++i) {
      this.schema.initField(i, iter.schema, fields[i].intValue());
    }

    this.iterator = iter;
    this.integers = fields;
  }

  /**
   * Gives a one-line explaination of the iterator, repeats the call on any
   * child iterators, and increases the indent depth along the way.
   */
  public void explain(int depth) {
    //this.schema(depth);
    System.out.print("Projection : ");

    for(int i = 0; i < this.integers.length - 1; ++i) {
      System.out.print("{" + this.integers[i] + "}, ");
    }

    System.out.println("{" + this.integers[this.integers.length - 1] + "}");
    this.iterator.explain(depth + 1);
  }

  /**
   * Restarts the iterator, i.e. as if it were just constructed.
   */
  public void restart() {
    this.iterator.restart();
  }

  /**
   * Returns true if the iterator is open; false otherwise.
   */
  public boolean isOpen() {
    return this.iterator.isOpen();
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
    return this.iterator.hasNext();
  }

  /**
   * Gets the next tuple in the iteration.
   * 
   * @throws IllegalStateException if no more tuples
   */
  public Tuple getNext() {
    Tuple nextTuple = this.iterator.getNext();
    Tuple newTuple = new Tuple(this.schema);

    for(int i = 0; i < this.integers.length; ++i) {
      newTuple.setField(i, nextTuple.getField(this.integers[i].intValue()));
    }

    return newTuple;
  }

} // public class Projection extends Iterator
