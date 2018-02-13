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
        this.optimizer2();

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


    //doesn't push down, (naive).
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


    //pushes down if possible.
    protected void optimizer2() {
        this.iterator = new FileScan(this.newSchema[0], new HeapFile(this.tables[0]));
        for (int i = 1; i < this.tables.length; i++) {
            this.iterator = new SimpleJoin(this.iterator, new FileScan(this.newSchema[i], new HeapFile(this.tables[i])), new Predicate[0]);
            ArrayList<Predicate[]> PredicatesToBeRemoved = new ArrayList<>();
            for (Predicate[] pred : this.preds) {
                Predicate[][] Checkingpred = new Predicate[1][pred.length];
                Checkingpred[0] = pred;
                for (int j = 0; j < Checkingpred[0].length - 1; j++) {

                    if (Checkingpred[0][j].validate(this.iterator.getSchema())) {
                        try {
                            QueryCheck.predicates(this.iterator.getSchema(), Checkingpred);
                            this.iterator = new Selection(this.iterator, pred);

                            PredicatesToBeRemoved.add(Checkingpred[0]);
                        } catch (QueryException e) {}
                    }
                }
            }
            Predicate[][] newPredicates = new Predicate[this.preds.length - PredicatesToBeRemoved.size()][this.preds[0].length];

            for (int j = 0; j < this.preds.length; j++) {

                if (!PredicatesToBeRemoved.contains(this.preds[j])) {

                    if (j < newPredicates.length) {
                        newPredicates[j] = this.preds[j];
                    }
                }
            }
            this.preds = newPredicates;
        }
        for (int i = 0; i < this.preds.length; ++i) {
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


    //forces select to be pushed down, and therefor crashes.
    protected void optimizer3() {

        //TODO make list of interators.
        this.iteratorlist = new ArrayList<Iterator>();
        this.iterator = new FileScan(this.newSchema[0], new HeapFile(this.tables[0]));

        for (int j = 0; j < this.preds.length; ++j) {
            this.iterator = new Selection(this.iterator, this.preds[j]);
        }
        this.iteratorlist.add(this.iterator);

        Iterator tempiterator;
        for (int i = 1; i < this.tables.length; ++i) {
            tempiterator = new FileScan(this.newSchema[i], new HeapFile(this.tables[i]));
            for (int j = 0; j < this.preds.length; ++j) {
                tempiterator = new Selection(tempiterator, this.preds[j]);
            }
            this.iteratorlist.add(tempiterator);
        }
        //TODO Join lists of interators.
        if (this.tables.length != 0) {
            System.out.println("getting first arg");
            this.iterator = this.iteratorlist.get(0);
        }
        for (int i = 1; i < this.tables.length; ++i) {
            System.out.println("joining the two");
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
