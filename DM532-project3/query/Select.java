package query;

import heap.HeapFile;
import parser.AST_Select;
import relop.*;

import java.util.ArrayList;

/**
 * Execution plan for selecting tuples.
 */
class Select implements Plan {
    protected String[] tables;
    protected Schema[] newSchema;
    protected String[] columns;
    protected Predicate[][] preds;
    public boolean isExplain;
    protected Iterator iterator;
    protected ArrayList<Iterator> iteratorlist;
    protected Schema schema;

    /**
     * Optimizes the plan, given the parsed query.
     *
     * @throws QueryException if validation fails
     */
    public Select(AST_Select tree) throws QueryException {

        this.isExplain = tree.isExplain;
        this.parser(tree);
        this.optimizer();

    } // public Select(AST_Select tree) throws QueryException

    protected void parser(AST_Select tree) throws QueryException {
        this.schema = new Schema(0);
        this.preds = tree.getPredicates();
        this.tables = tree.getTables();
        this.columns = tree.getColumns();


        String[] columns = this.columns;
        this.newSchema = new Schema[this.tables.length];

        for (int i = 0; i < this.tables.length; ++i) {
            this.newSchema[i] = QueryCheck.tableExists(this.tables[i]);
            this.schema = Schema.join(this.schema, this.newSchema[i]);
        }


        for (int j = 0; j < columns.length; ++j) {
            String columnName = columns[j];
            QueryCheck.columnExists(this.schema, columnName);
        }


        QueryCheck.predicates(this.schema, this.preds);
    }

    protected void optimizer1() {
        this.iterator = new FileScan(this.newSchema[0], new HeapFile(this.tables[0]));

        int i;
        for (i = 1; i < this.tables.length; ++i) {
            System.out.println("i is " + i);
            this.iterator = new SimpleJoin(this.iterator, new FileScan(this.newSchema[i], new HeapFile(this.tables[i])), new Predicate[0]);
        }

        for (i = 0; i < this.preds.length; ++i) {
            System.out.println("i is " + i);
            this.iterator = new Selection(this.iterator, this.preds[i]);
        }

        if (this.columns.length > 0) {
            Integer[] intlist = new Integer[this.columns.length];

            for (int j = 0; j < this.columns.length; ++j) {
                intlist[j] = Integer.valueOf(this.schema.fieldNumber(this.columns[j]));
            }

            this.iterator = new Projection(this.iterator, intlist);
        }

    }

    protected void optimizer() {

        //TODO make list of interators.
        this.iteratorlist = new ArrayList<Iterator>();
        for (int j = 0; j < this.preds.length; ++j) {
            this.iteratorlist.add(new Selection(new FileScan(this.newSchema[0], new HeapFile(this.tables[0])), this.preds[j]));
        }
        int i;
        for (i = 1; i < this.tables.length; ++i) {
            for (int j = 0; j < this.preds.length; ++j) {
                System.out.println("j is " + j + " i is " + i);
                this.iteratorlist.add(new Selection(new FileScan(this.newSchema[i], new HeapFile(this.tables[i])), this.preds[j]));
            }
        }
        this.iterator = this.iteratorlist.get(0);
        //TODO Join lists of interators.
        for (i = 1; i < this.tables.length; ++i) {
            this.iterator = new SimpleJoin(this.iterator, this.iteratorlist.get(i), new Predicate[0]);

        }


        if (this.columns.length > 0) {
            Integer[] intlist = new Integer[this.columns.length];

            for (int j = 0; j < this.columns.length; ++j) {
                intlist[j] = Integer.valueOf(this.schema.fieldNumber(this.columns[j]));
            }

            this.iterator = new Projection(this.iterator, intlist);
        }

    }

    /**
     * Executes the plan and prints applicable output.
     */
    public void execute() {
        if (isExplain) {
            this.iterator.explain(0);
        } else {
            int i = this.iterator.execute();
            System.out.println("\n" + i + " rows affected.");
        }
    } // public void execute()

} // class Select implements Plan
