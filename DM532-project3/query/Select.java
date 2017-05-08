package query;

import heap.HeapFile;
import parser.AST_Select;
import relop.*;

/**
 * Execution plan for selecting tuples.
 */
class Select implements Plan {
  protected String[] tables;
  protected Schema[] newschema;
  protected String[] columns;
  protected Predicate[][] preads;
  public boolean j;
  protected Iterator k;
  protected Schema schema;
  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if validation fails
   */
  public Select(AST_Select tree) throws QueryException {

    this.j = tree.isExplain;
    this.Selecter(tree);
    this.selectjoin();

  } // public Select(AST_Select tree) throws QueryException

  protected void Selecter(AST_Select tree) throws QueryException {
    this.schema = new Schema(0);
    this.preads = tree.getPredicates();
    this.tables = tree.getTables();
    this.columns = tree.getColumns();


    String[] columns = this.columns;
    this.newschema = new Schema[this.tables.length];

    for(int i = 0; i < this.tables.length; ++i) {
      this.newschema[i] = QueryCheck.tableExists(this.tables[i]);
      this.schema = Schema.join(this.schema, this.newschema[i]);
    }


    for(int j = 0; j < columns.length; ++j) {
      String columnName = columns[j];
      QueryCheck.columnExists(this.schema, columnName);
    }


    f.a(this.schema, this.preads);
  }

  protected void selectjoin() {
    this.k = new FileScan(this.newschema[0], new HeapFile(this.tables[0]));

    int var1;
    for(var1 = 1; var1 < this.tables.length; ++var1) {
      this.k = new SimpleJoin(this.k, new FileScan(this.newschema[var1], new HeapFile(this.tables[var1])), new Predicate[0]);
    }

    for(var1 = 0; var1 < this.preads.length; ++var1) {
      this.k = new Selection(this.k, this.preads[var1]);
    }

    if(this.columns.length > 0) {
      Integer[] var3 = new Integer[this.columns.length];

      for(int j = 0; j < this.columns.length; ++j) {
        var3[j] = Integer.valueOf(this.schema.fieldNumber(this.columns[j]));
      }

      this.k = new Projection(this.k, var3);
    }

  }

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
    if(this.j) {
      this.k.explain(0);
    } else {
      int var1 = this.k.execute();
      System.out.println("\n" + var1 + " rows affected.");
    }
  } // public void execute()

} // class Select implements Plan
