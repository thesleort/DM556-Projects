package query;

import global.Minibase;
import global.RID;
import global.SearchKey;
import heap.HeapFile;
import index.HashIndex;
import parser.AST_Update;
import relop.*;

/**
 * Execution plan for updating tuples.
 */
class Update implements Plan {
  protected String filename; //else
  protected Schema schema; //case
  protected int[] fldnos;    //byte
  protected Object[] values;  //try
  protected Predicate[][] preds; //char

  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if invalid column names, values, or pedicates
   */
  public Update(AST_Update tree) throws QueryException {
    this.filename = tree.getFileName();
    this.schema = QueryCheck.tableExists(this.filename);
    String[] columns = tree.getColumns();
    this.fldnos = QueryCheck.updateFields(this.schema, columns);
    this.values = tree.getValues();
    QueryCheck.updateValues(this.schema, this.fldnos, this.values);
    this.preds = tree.getPredicates();
    QueryCheck.predicates(this.schema, this.preds);

  } // public Update(AST_Update tree) throws QueryException

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
    HeapFile heap = new HeapFile(this.filename);
    FileScan fileScan = new FileScan(this.schema, heap);
    Object obj = fileScan;

    for(int i = 0; i < this.preds.length; ++i) {
      obj = new Selection((Iterator)obj, this.preds[i]);
    }

    IndexDesc[] indexDescriptors = Minibase.SystemCatalog.getIndexes(this.filename, this.schema, this.fldnos);
    int i = 0;
    if(((Iterator)obj).hasNext()) {
      Tuple tuple = ((Iterator)obj).getNext();
      RID rid = fileScan.getLastRID();
//      IndexDesc[] indexDecs2 = indexDescriptors;
      int j = 0;

      IndexDesc indexDesc;
      int l;
      SearchKey key;
      for(l = indexDescriptors.length; j < l; ++j) {
        indexDesc = indexDescriptors[j];
        key = new SearchKey(tuple.getField(indexDesc.columnName));
        (new HashIndex(indexDesc.indexName)).deleteEntry(key, rid);
      }

      for(int k = 0; k < this.fldnos.length; ++k) {
        tuple.setField(this.fldnos[k], this.values[k]);
      }

      heap.updateRecord(rid, tuple.getData());
      ++i;
//      indexDecs2 = indexDescriptors;
      j = 0;

      for(l = indexDescriptors.length; j < l; ++j) {
        indexDesc = indexDescriptors[j];
        key = new SearchKey(tuple.getField(indexDesc.columnName));
        (new HashIndex(indexDesc.indexName)).insertEntry(key, rid);
      }
    }

    ((Iterator)obj).close();
    System.out.println(i + " rows affected.");
    // print the output message
    System.out.println("0 rows affected. (Not implemented)");

  } // public void execute()

} // class Update implements Plan
