package relop;

/**
 * The projection operator extracts columns from a relation; unlike in
 * relational algebra, this operator does NOT eliminate duplicate tuples.
 */
public class Projection extends Iterator {
  protected Iterator iterator; //do
  protected Integer[] integers;//if
  /**
   * Constructs a projection, given the underlying iterator and field numbers.
   */
  public Projection(Iterator iter, Integer... fields) {
    this.schema = new Schema(fields.length);

    for(int var3 = 0; var3 < fields.length; ++var3) {
      this.schema.initField(var3, iter.schema, fields[var3].intValue());
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

    for(int var2 = 0; var2 < this.integers.length - 1; ++var2) {
      System.out.print("{" + this.integers[var2] + "}, ");
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
    Tuple var1 = this.iterator.getNext();
    Tuple var2 = new Tuple(this.schema);

    for(int var3 = 0; var3 < this.integers.length; ++var3) {
      var2.setField(var3, var1.getField(this.integers[var3].intValue()));
    }

    return var2;
  }

} // public class Projection extends Iterator
