package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;
import parser.AST_Delete;
import relop.*;

import java.util.ArrayList;

/**
 * Execution plan for deleting tuples.
 */
class Delete implements Plan {
  protected String filename;
  protected Schema schema;
  protected Predicate[][] preds;

  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if table doesn't exist or predicates are invalid
   */
  public Delete(AST_Delete tree) throws QueryException {
    this.filename = tree.getFileName();
    this.schema = QueryCheck.tableExists(this.filename);
    this.preds = tree.getPredicates();
    QueryCheck.predicates(this.schema, this.preds);
//    f.a(this.schema, this.preds);
  } // public Delete(AST_Delete tree) throws QueryException

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
    HeapFile file = new HeapFile(this.filename);
    FileScan fileScan = new FileScan(this.schema, file);
    Object selection = fileScan;

    for(int i = 0; i < this.preds.length; ++i) {
      selection = new Selection((Iterator)selection, this.preds[i]);
    }

    ArrayList var13 = new ArrayList();
    IndexDesc[] indexes = Minibase.SystemCatalog.getIndexes(this.filename);

    while(((Iterator)selection).hasNext()) {
      Tuple next = ((Iterator)selection).getNext();
      RID lastRID = fileScan.getLastRID();
      var13.add(lastRID);

      for(int j = 0; j < indexes.length; ++j) {
        IndexDesc index = indexes[j];
        SearchKey searchKey = new SearchKey(next.getField(index.indexName));
        (new HashIndex(index.indexName)).deleteEntry(searchKey, lastRID);
      }
    }

    ((Iterator)selection).close();
    java.util.Iterator deleteIterator = var13.iterator();

    while(deleteIterator.hasNext()) {
      RID var14 = (RID)deleteIterator.next();
      file.deleteRecord(var14);
    }

    System.out.println(var13.size() + " rows affected.");

    // print the output message


  } // public void execute()

} // class Delete implements Plan
