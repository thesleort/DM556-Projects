package tests;

import java.util.Random;

import heap.HeapFile;
import index.HashIndex;
import global.AttrOperator;
import global.AttrType;
import global.RID;
import global.SearchKey;
import relop.*;

public class BROTest extends TestDriver {
	  /** The display name of the test suite. */
	  private static final String TEST_NAME = "basic relational operator tests";

	 private static final int SORTPGNUM = 20; 
	  

	  /** Drivers table schema. */
	  private static Schema s_drivers;

	  /** table schema for the sorting test */
	  private static Schema numbers;

	  /** Rides table schema. */
	  private static Schema s_rides;
	  
	  /**
	   * Test application entry point; runs all tests.
	   */
	  public static void main(String argv[]) {

	    // create a clean Minibase instance
	    BROTest rot = new BROTest();
	    rot.create_minibase();

	    // initialize schema for the "Drivers" table
	    s_drivers = new Schema(5);
	    s_drivers.initField(0, AttrType.INTEGER, 4, "DriverId");
	    s_drivers.initField(1, AttrType.STRING, 20, "FirstName");
	    s_drivers.initField(2, AttrType.STRING, 20, "LastName");
	    s_drivers.initField(3, AttrType.FLOAT, 4, "Age");
	    s_drivers.initField(4, AttrType.INTEGER, 4, "NumSeats");
	    
		//initialize the schema for the Numbers
		numbers = new Schema(1);
		numbers.initField(0, AttrType.INTEGER, 4, "Id");
	
		// initialize schema for the "Rides" table
	    s_rides = new Schema(4);
	    s_rides.initField(0, AttrType.INTEGER, 4, "DriverId");
	    s_rides.initField(1, AttrType.INTEGER, 4, "GroupId");
	    s_rides.initField(2, AttrType.STRING, 10, "FromDate");
	    s_rides.initField(3, AttrType.STRING, 10, "ToDate");	 
	    
	    // run all the test cases
	    System.out.println("\n" + "Running " + TEST_NAME + "...");
	    boolean status = PASS;
	    status &= rot.test1();
	    status &= rot.test2();
	    status &= rot.test3();

	    // display the final results
	    System.out.println();
	    if (status != PASS) {
	      System.out.println("Error(s) encountered during " + TEST_NAME + ".");
	    } else {
	      System.out.println("All " + TEST_NAME
	          + " completed; verify output for correctness.");
	    }

	  } // public static void main (String argv[])

	  /**
	   * 
	   */
	  protected boolean test1() {
	    try {

	      System.out.println("\nTest 1: Primative relational operators");
	      initCounts();
	      saveCounts(null);

	      // create and populate a temporary Drivers file and index
	      Tuple tuple = new Tuple(s_drivers);
	      HeapFile file = new HeapFile(null);
	      HashIndex index = new HashIndex(null);
	      for (int i = 1; i <= 10; i++) {

	        // create the tuple
	        tuple.setIntFld(0, i);
	        tuple.setStringFld(1, "f" + i);
	        tuple.setStringFld(2, "l" + i);
	        Float age = (float) (i * 7.7);
	        tuple.setFloatFld(3, age);
	        tuple.setIntFld(4, i + 100);

	        // insert the tuple in the file and index
	        RID rid = file.insertRecord(tuple.getData());
	        index.insertEntry(new SearchKey(age), rid);

	      } // for
	      saveCounts("insert");

	      // test selection operator
	      saveCounts(null);
	      System.out.println("\n  ~> test selection (Age > 65 OR Age < 15)...\n");
	      Predicate[] preds = new Predicate[] {
	          new Predicate(AttrOperator.GT, AttrType.FIELDNO, 3, AttrType.FLOAT,
	              65F),
	          new Predicate(AttrOperator.LT, AttrType.FIELDNO, 3, AttrType.FLOAT,
	              15F) };
	      FileScan scan = new FileScan(s_drivers, file);
	      Selection sel = new Selection(scan, preds);
	      sel.execute();
	      saveCounts("select");

	      // test projection operator
	      saveCounts(null);
	      System.out.println("\n  ~> test projection (columns 3 and 1)...\n");
	      scan = new FileScan(s_drivers, file);
	      Projection pro = new Projection(scan, 3, 1);
	      pro.execute();
	      saveCounts("project");

	      // test simple pipelining
	      saveCounts(null);
	      System.out.println("\n  ~> selection and projection (pipelined)...\n");
	      scan = new FileScan(s_drivers, file);
	      sel = new Selection(scan, preds);
	      pro = new Projection(sel, 3, 1);
	      pro.execute();
	      saveCounts("both");


	      // destroy temp files before doing final counts
	      pro = null;
	      sel = null;
	      scan = null;
	      index = null;
	      file = null;
	      System.gc();
	      saveCounts("join");

	      // that's all folks!
	      System.out.print("\n\nTest 1 completed without exception.");
	      return PASS;

	    } catch (Exception exc) {

	      exc.printStackTrace(System.out);
	      System.out.print("\n\nTest 1 terminated because of exception.");
	      return FAIL;

	    } finally {
	      printSummary(6);
	      System.out.println();
	    }
	  } // protected boolean test1()

	  protected boolean test2() {
			try {
				System.out.println("\nTest 2: Sorting Test");
				//create and populate a temporary numbersfile
				Tuple tuple = new Tuple(numbers);
				HeapFile file = new HeapFile(null);
				int no_to_sort = 1000;
				Random generator = new Random();
				for (int i = 0; i < no_to_sort; i++) {
					//create tuple
					int random = generator.nextInt(no_to_sort*10);
					
					tuple.setIntFld(0, random);
					RID rid = file.insertRecord(tuple.getData());
				}
				System.out.println("\n...Inserted");
				
				//test sorting
				System.out.println("\n ~> sort numbers");
				FileScan scan = new FileScan(numbers,file);
				Sort srun = new Sort(scan,0,100,SORTPGNUM);
				boolean passed = true;
				int prev = Integer.MIN_VALUE;
				
				System.out.println("\n cheking the result.");
				int outputs = 0;
				while(srun.hasNext()){
					outputs++;
					Tuple temp = srun.getNext();
					if(temp.getIntFld(0) >= prev){
						//its good :)
						prev = temp.getIntFld(0);
//						System.out.println(temp.getIntFld(0));
						
					} else {
						// its bad :(
//						System.out.println("its bad :(");
						passed = false;
						break;
					}
				}
				scan = null;
				srun = null;
				file = null;
				System.gc();
	
				if(passed && outputs == no_to_sort){
					System.out.println("\n\n Test 2 completed without error.");
					return PASS;
				} else {
					System.out.println("\n Test 2 completed with errors. !!!!");
					return FAIL;
				}
			
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
				System.out.println("\n FAIL!!!!");
				return FAIL;
			}
			
	}
	  /**
	   * SELECT * FROM Drivers D INNER JOIN Rides R ON (D.DriverId = R.DriverId);
	   */
	  protected boolean test3() {
	    try {

	      System.out.println("\nTest 3: MergeJoin operator\n");
	      initCounts();

	      // create and populate the drivers table
	      saveCounts(null);
	      HeapFile drivers = new HeapFile(null);
	      Tuple tuple = new Tuple(s_drivers);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(1, "Ahmed", "Elmagarmid", 25F, 5);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(2, "Walid", "Aref", 27F, 13);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(3, "Christopher", "Clifton", 18F, 4);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(4, "Sunil", "Prabhakar", 22F, 7);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(5, "Elisa", "Bertino", 26F, 5);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(6, "Susanne", "Hambrusch", 23F, 3);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(7, "David", "Eberts", 24F, 8);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(8, "Arif", "Ghafoor", 20F, 5);
	      tuple.insertIntoFile(drivers);
	      tuple.setAllFields(9, "Jeff", "Vitter", 19F, 10);
	      tuple.insertIntoFile(drivers);
	      saveCounts("driver2");

	      // create and populate the rides table
	      saveCounts(null);
	      HeapFile rides = new HeapFile(null);
	      tuple = new Tuple(s_rides);
	      tuple.setAllFields(1, 2, "2/12/2006", "2/14/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(1, 3, "2/15/2006", "2/16/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(2, 6, "2/17/2006", "2/20/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(2, 7, "2/18/2006", "2/23/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(3, 5, "2/10/2006", "2/13/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(3, 4, "2/18/2006", "2/19/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(3, 2, "2/24/2006", "2/26/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(4, 1, "2/19/2006", "2/19/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(5, 7, "2/14/2006", "2/18/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(6, 6, "2/25/2006", "2/26/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(8, 5, "2/20/2006", "2/22/2006");
	      tuple.insertIntoFile(rides);
	      tuple.setAllFields(9, 1, "2/15/2006", "2/15/2006");
	      tuple.insertIntoFile(rides);
	      saveCounts("rides2");

	      // test merge join operator
	      saveCounts(null);
	      MergeJoin join = new MergeJoin(new FileScan(s_drivers, drivers),
	          new FileScan(s_rides, rides), 0, 0);
	      join.execute();

	      // destroy temp files before doing final counts
	      join = null;
	      rides = null;
	      drivers = null;
	      System.gc();
	      saveCounts("m_join");

	      // that's all folks!
	      System.out.print("\n\nTest 3 completed without exception.");
	      return PASS;

	    } catch (Exception exc) {

	      exc.printStackTrace(System.out);
	      System.out.print("\n\nTest 3 terminated because of exception.");
	      return FAIL;

	    } finally {
	      printSummary(3);
	      System.out.println();
	    }
	  } // protected boolean test3()

	  
}
