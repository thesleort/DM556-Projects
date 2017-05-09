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
  public boolean isExplain;
  protected Iterator interator;
  protected Schema schema;
  /**
   * Optimizes the plan, given the parsed query.
   * 
   * @throws QueryException if validation fails
   */
  public Select(AST_Select tree) throws QueryException {

    this.isExplain = tree.isExplain;
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


    QueryCheck.predicates(this.schema, this.preads);
  }

  protected void selectjoin() {
    this.interator = new FileScan(this.newschema[0], new HeapFile(this.tables[0]));

    int i;
    for(i = 1; i < this.tables.length; ++i) {
      this.interator = new SimpleJoin(this.interator, new FileScan(this.newschema[i], new HeapFile(this.tables[i])), new Predicate[0]);
    }

    for(i = 0; i < this.preads.length; ++i) {
      this.interator = new Selection(this.interator, this.preads[i]);
    }

    if(this.columns.length > 0) {
      Integer[] intlist = new Integer[this.columns.length];

      for(int j = 0; j < this.columns.length; ++j) {
        intlist[j] = Integer.valueOf(this.schema.fieldNumber(this.columns[j]));
      }

      this.interator = new Projection(this.interator, intlist);
    }

  }

  /**
   * Executes the plan and prints applicable output.
   */
  public void execute() {
    if(isExplain) {
      this.interator.explain(0);
    } else {
      int i = this.interator.execute();
      System.out.println("\n" + i + " rows affected.");
    }
  } // public void execute()

} // class Select implements Plan
