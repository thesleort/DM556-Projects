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
    String[] var2 = tree.getColumns();
    this.fldnos = QueryCheck.updateFields(this.schema, var2);
    this.values = tree.getValues();
    QueryCheck.updateValues(this.schema, this.fldnos, this.values);
    this.preds = tree.getPredicates();
    QueryCheck.predicates(this.schema, this.preds);

  } // public Update(AST_Update tree) throws QueryException

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
    HeapFile var1 = new HeapFile(this.filename);
    FileScan var2 = new FileScan(this.schema, var1);
    Object var3 = var2;

    for(int var4 = 0; var4 < this.preds.length; ++var4) {
      var3 = new Selection((Iterator)var3, this.preds[var4]);
    }

    IndexDesc[] var13 = Minibase.SystemCatalog.getIndexes(this.filename, this.schema, this.fldnos);
    int var5 = 0;
    if(((Iterator)var3).hasNext()) {
      Tuple var6 = ((Iterator)var3).getNext();
      RID var7 = var2.getLastRID();
//      IndexDesc[] var11 = var13;
      int var9 = 0;

      IndexDesc var8;
      int var10;
      SearchKey var12;
      for(var10 = var13.length; var9 < var10; ++var9) {
        var8 = var13[var9];
        var12 = new SearchKey(var6.getField(var8.columnName));
        (new HashIndex(var8.indexName)).deleteEntry(var12, var7);
      }

      for(int var14 = 0; var14 < this.fldnos.length; ++var14) {
        var6.setField(this.fldnos[var14], this.values[var14]);
      }

      var1.updateRecord(var7, var6.getData());
      ++var5;
//      var11 = var13;
      var9 = 0;

      for(var10 = var13.length; var9 < var10; ++var9) {
        var8 = var13[var9];
        var12 = new SearchKey(var6.getField(var8.columnName));
        (new HashIndex(var8.indexName)).insertEntry(var12, var7);
      }
    }

    ((Iterator)var3).close();
    System.out.println(var5 + " rows affected.");
    // print the output message
    System.out.println("0 rows affected. (Not implemented)");

  } // public void execute()

} // class Update implements Plan
