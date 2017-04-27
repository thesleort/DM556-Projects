package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;
import parser.AST_Insert;
import relop.Schema;
import relop.Tuple;

/**
 * Execution plan for inserting tuples.
 */
class Insert implements Plan {

  /** Name of the table to insert into. */
  protected String fileName;

  /** Schema of the table to insert into. */
  protected Schema schema;

  /** Actual values to insert into the table. */
  protected Object[] values;

  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if table doesn't exists or values are invalid
   */
  public Insert(AST_Insert tree) throws QueryException {

    // make sure the table exists
    fileName = tree.getFileName();
    schema = QueryCheck.tableExists(fileName);

    // get and validate the values to insert
    values = tree.getValues();
    QueryCheck.insertValues(schema, values);

  } // public Insert(AST_Insert tree) throws QueryException

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {

    // create the tuple to insert
    HeapFile file = new HeapFile(fileName);
    Tuple tuple = new Tuple(schema, values);

    // insert the tuple and maintain any indexes
    RID rid = tuple.insertIntoFile(file);
    IndexDesc[] inds = Minibase.SystemCatalog.getIndexes(fileName);
    for (IndexDesc ind : inds) {

      // get the search key from the new tuple
      SearchKey key = new SearchKey(tuple.getField(ind.columnName));

      // insert the entry into the index file
      new HashIndex(ind.indexName).insertEntry(key, rid);

    } // for

    // print the output message
    System.out.println("1 row affected.");

  } // public void execute()

} // class Insert implements Plan
