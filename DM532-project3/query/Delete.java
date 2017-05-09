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

    ArrayList deletelist = new ArrayList();
    IndexDesc[] indexes = Minibase.SystemCatalog.getIndexes(this.filename);

    while(((Iterator)selection).hasNext()) {
      Tuple next = ((Iterator)selection).getNext();
      deletelist.add(fileScan.getLastRID());

      for(int j = 0; j < indexes.length; ++j) {
        IndexDesc index = indexes[j];
        SearchKey searchKey = new SearchKey(next.getField(index.indexName));
        (new HashIndex(index.indexName)).deleteEntry(searchKey, fileScan.getLastRID());
      }
    }

    ((Iterator)selection).close();
    java.util.Iterator deleteIterator = deletelist.iterator();

    while(deleteIterator.hasNext()) {
      file.deleteRecord((RID)deleteIterator.next());
    }

    System.out.println(deletelist.size() + " rows affected.");

    // print the output message


  } // public void execute()

} // class Delete implements Plan
