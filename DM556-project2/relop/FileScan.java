package relop;

import global.RID;
import heap.HeapFile;
import heap.HeapScan;

/**
 * Wrapper for heap file scan, the most basic access method. This "iterator"
 * version takes schema into consideration and generates real tuples.
 */
public class FileScan extends Iterator {

  /** The heap file to scan. */
  protected HeapFile file;

  /** The underlying heap scan. */
  protected HeapScan scan;

  /** Identifies returned tuples. */
  protected RID rid;

  // --------------------------------------------------------------------------

  /**
   * Constructs a file scan, given the schema and heap file.
   */
  public FileScan(Schema schema, HeapFile file) {
    this.schema = schema;
    this.file = file;
    scan = file.openScan();
    rid = new RID();
  }

  /**
   * Gives a one-line explaination of the iterator, repeats the call on any
   * child iterators, and increases the indent depth along the way.
   */
  public void explain(int depth) {
    indent(depth);
    System.out.println("FileScan : " + file.toString());
  }

  /**
   * Restarts the iterator, i.e. as if it were just constructed.
   */
  public void restart() {
    scan.close();
    scan = file.openScan();
  }

  /**
   * Returns true if the iterator is open; false otherwise.
   */
  public boolean isOpen() {
    return (scan != null);
  }

  /**
   * Closes the iterator, releasing any resources (i.e. pinned pages).
   */
  public void close() {
    if (scan != null) {
      scan.close();
      scan = null;
    }
  }

  /**
   * Returns true if there are more tuples, false otherwise.
   */
  public boolean hasNext() {
    return scan.hasNext();
  }

  /**
   * Gets the next tuple in the iteration.
   * 
   * @throws IllegalStateException if no more tuples
   */
  public Tuple getNext() {

    // convert the record into a tuple
    return new Tuple(schema, scan.getNext(rid));

  } // public Tuple getNext()

  /**
   * Gets the RID of the last tuple returned.
   */
  public RID getLastRID() {
    return new RID(rid);
  }

} // public class FileScan extends Iterator
