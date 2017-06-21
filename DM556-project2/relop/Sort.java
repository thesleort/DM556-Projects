package relop;

import global.*;
import heap.HeapFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Sort extends Iterator implements GlobalConst {
    protected Iterator iterator;
    protected HeapFile file;
    protected FileScan scan;

    /**
     * Constructs a sort operator.
     *
     * @param sortMemSize the size the memory used for internal sorting. For simplicity, you can assume it is in the unit of tuples.
     * @param bufSize     the total buffer size for the merging phase in the unit of page.
     */
    public Sort(Iterator iter, int sortfield, int sortMemSize, int bufSize) {

        this.iterator = iter;
        schema = iter.schema;
        HeapFile[] records = new HeapFile[bufSize];
        Tuple[] internal = new Tuple[sortMemSize];
        Object[] all = new Object[sortMemSize];
        HashMap<Object, Tuple> hashmap = new HashMap<Object, Tuple>();

        int pos = 0;
        // read data into sorting area
        while (iter.hasNext()) {
            // Load the records into the internal memory
            for (int i = 0; i < sortMemSize; i++) {
                if (iter.hasNext()) {
                    internal[i] = iter.getNext();
                    all[i] = internal[i].getField(0);
                    hashmap.put(all[i], internal[i]);
                }
            } // for

            ArrayList<Object> queue = new ArrayList<Object>();

            for (Object object : all) {
                if (object != null) {
                    queue.add(object);
                }
            }

            all = queue.toArray();
//            Sort the tuples
            java.util.Arrays.sort(all);

            records[pos] = new HeapFile(null);
            for (Object object : all) {
                      records[pos].insertRecord(hashmap.get(object).data);
    }
    pos++;
}

        file = sorter(records, bufSize, iter, sortfield)[0];
        scan = new FileScan(iter.schema, file);
    }

    private HeapFile[] sorter(HeapFile[] records, int bufSize, Iterator iter, int sortfield) {
        int heapCount = getHeaps(records);
        if (heapCount == 1) {
            return records;
        }
        if (heapCount >= bufSize) {
            heapCount = bufSize - 1;
        }
        FileScan[] scan = new FileScan[heapCount];

        // Create a new filescan on every record in the current record array
        for (int i = 0; i < heapCount; i++) {
            scan[i] = new FileScan(iter.schema, records[i]);
        }
        HeapFile file = new HeapFile(null);
        Tuple[] tuples = new Tuple[heapCount];
        int compared = 0;

        // Load the tuples from the filescanner
        for (int i = 0; i < tuples.length; i++) {
            tuples[i] = scan[i].getNext();
        }

        while (compared != heapCount) {
            Object[] smallest = {null, null};
            int smallestPos = 0;
            int current = 0;
            for (Tuple tuple : tuples) {
                Object next = tuple.getField(sortfield);
                if (smallest[0] == null) {
                    smallest[0] = next;
                    smallest[1] = next;

                } else { // compare
                    smallest[1] = next;
                    java.util.Arrays.sort(smallest);

                    if (smallest[0] == next) {
                        smallestPos = current;
                    }
                }
                current++;
            }
            file.insertRecord(tuples[smallestPos].data);
            if (scan[smallestPos].hasNext()) {
                tuples[smallestPos] = scan[smallestPos].getNext();
            } else {
                tuples[smallestPos].setField(sortfield, Integer.MAX_VALUE);
                compared++;
            }
        }
        records[heapCount - 1] = file;
        HeapFile[] rest = Arrays.copyOfRange(records, heapCount - 1, records.length);
        return sorter(rest, bufSize, iter, sortfield);
    }

    private int getHeaps(HeapFile[] records) {
        int result = 0;
        while (records[result] != null) {
            result++;
        }
        return result;
    }

    @Override
    public void explain(int depth) {
        FileScan fs = new FileScan(iterator.schema, file);
        fs.explain(depth);
    }

    @Override
    public void restart() {
        scan.restart();
    }

    @Override
    public boolean isOpen() {
        return scan.isOpen();
    }

    @Override
    public void close() {
        if (scan != null) {
            scan.close();
            scan = null;
        }
    }

    @Override
    public boolean hasNext() {
        return scan.hasNext();
    }

    @Override
    public Tuple getNext() {
        return scan.getNext();
//		throw new UnsupportedOperationException("Not implemented");
    }

}
