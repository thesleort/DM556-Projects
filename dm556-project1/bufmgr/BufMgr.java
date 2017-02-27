package bufmgr;

import java.util.HashMap;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

/**
 * <h3>Minibase Buffer Manager</h3> The buffer manager reads disk pages into a
 * main memory page as needed. The collection of main memory pages (called
 * frames) used by the buffer manager for this purpose is called the buffer
 * pool. This is just an array of Page objects. The buffer manager is used by
 * access methods, heap files, and relational operators to read, write,
 * allocate, and de-allocate pages.
 */
@SuppressWarnings("unused")
public class BufMgr implements GlobalConst {
	
	/** Actual pool of pages (can be viewed as an array of byte arrays). */
	protected Page[] bufpool;

	private boolean debugvalue = true;

	/** Array of descriptors, each containing the pin count, dirty status, etc. */
	protected FrameDesc[] frametab;

	/** Maps current page numbers to frames; used for efficient lookups. */
	protected HashMap<Integer, FrameDesc> pagemap;

	/** The replacement policy to use. */
	protected Replacer replacer;
	
	/**
	 * Constructs a buffer manager with the given settings.
	 * 
	 * @param numbufs: number of pages in the buffer pool
	 */

	public BufMgr(int numbufs) {
	    // initialize the buffer pool and frame table
	    bufpool = new Page[numbufs];
	    frametab = new FrameDesc[numbufs];
	    for (int i = 0; i < numbufs; i++) {
	      bufpool[i] = new Page();
	      frametab[i] = new FrameDesc(i);
	    }
	    
	    // initialize the specialized page map and replacer
	    pagemap = new HashMap<Integer, FrameDesc>(numbufs);
	    replacer = new Clock(this);
	}

	/**
	 * Allocates a set of new pages, and pins the first one in an appropriate
	 * frame in the buffer pool.
	 * 
	 * @param firstpg
	 *            holds the contents of the first page 
	 * @param run_size
	 *            number of new pages to allocate
	 * @return page id of the first new page
	 * @throws IllegalArgumentException
	 *             if PIN_MEMCPY and the page is pinned
	 * @throws IllegalStateException
	 *             if all pages are pinned (i.e. pool exceeded)
	 */
	public PageId newPage(Page firstpg, int run_size) {
		// allocate the run
		PageId firstid = Minibase.DiskManager.allocate_page(run_size);
		
		// try to pin the first page
		try {pinPage(firstid, firstpg, PIN_MEMCPY);} 
		catch (RuntimeException exc) {
		      // roll back because pin failed
		      for (int i = 0; i < run_size; i++) {
		        firstid.pid += 1;
		        Minibase.DiskManager.deallocate_page(firstid);
		      }
		      // re-throw the exception
		      throw exc;
		}
		// notify the replacer and return the first new page id
		replacer.newPage(pagemap.get(firstid.pid));
		return firstid;
	}

	/**
	 * Deallocates a single page from disk, freeing it from the pool if needed.
	 * Call Minibase.DiskManager.deallocate_page(pageno) to deallocate the page before return.
	 * 
	 * @param pageno
	 *            identifies the page to remove
	 * @throws IllegalArgumentException
	 *             if the page is pinned
	 */
	public void freePage(PageId pageno) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Pins a disk page into the buffer pool. If the page is already pinned,
	 * this simply increments the pin count. Otherwise, this selects another
	 * page in the pool to replace, flushing the replaced page to disk if 
	 * it is dirty.
	 * 
	 * (If one needs to copy the page from the memory instead of reading from 
	 * the disk, one should set skipRead to PIN_MEMCPY. In this case, the page 
	 * shouldn't be in the buffer pool. Throw an IllegalArgumentException if so. )
	 * 
	 * 
	 * @param pageno
	 *            identifies the page to pin
	 * @param page
	 *            if skipread == PIN_MEMCPY, works as as an input param, holding the contents to be read into the buffer pool
	 *            if skipread == PIN_DISKIO, works as an output param, holding the contents of the pinned page read from the disk
	 * @param skipRead
	 *            PIN_MEMCPY(true) (copy the input page to the buffer pool); PIN_DISKIO(false) (read the page from disk)
	 * @throws IllegalArgumentException
	 *             if PIN_MEMCPY and the page is pinned
	 * @throws IllegalStateException
	 *             if all pages are pinned (i.e. pool exceeded)
	 */
	public void pinPage(PageId pageno, Page page, boolean skipRead) {

        if (debugvalue){
            System.out.println("pin function start");
        }

		//first check if the page is already pinned
		FrameDesc fdesc = pagemap.get(pageno.pid);
		if (fdesc != null) {
			if (skipRead == true && fdesc.pincnt > 0) {
				throw new IllegalArgumentException(
						"Page pinned; PIN_MEMCPY not allowed"
				);
			}

			fdesc.pincnt++;
			replacer.pinPage(fdesc);
			page.setPage(bufpool[fdesc.index]);
			return;
		} // if in pool

		// select an available frame
		int frameNo = replacer.pickVictim();
        if (debugvalue) {
            System.out.println("Framenumber is "+frameNo);
        }
		if(frameNo < 0) {

			if( fdesc.pageno.pid != INVALID_PAGEID) {
				pagemap.remove(fdesc.pageno.pid);
				if(fdesc.dirty) {
					Minibase.DiskManager.write_page(fdesc.pageno, bufpool[frameNo]);
				}
			}
		}

		//read in the page if requested, and wrap the buffer
		if(skipRead == true) {
			bufpool[frameNo].copyPage(page);
		} else {
			Minibase.DiskManager.read_page(pageno, bufpool[frameNo]);
		}
		page.setPage(bufpool[frameNo]);
        if (debugvalue) {
            System.out.println("Pageno = " + pageno.pid);
        }
		//update the frame descriptor
		if(fdesc != null) {
			fdesc.pageno.pid = pageno.pid;
			fdesc.pincnt = 1;
			fdesc.dirty = false;

			pagemap.put(pageno.pid, fdesc);
			replacer.pinPage(fdesc);
		}
        if (debugvalue){
            System.out.println("pin function end");
        }

	}

	/**
	 * Unpins a disk page from the buffer pool, decreasing its pin count.
	 * 
	 * @param pageno
	 *            identifies the page to unpin
	 * @param dirty
	 *            UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherwise
	 * @throws IllegalArgumentException
	 *             if the page is not present or not pinned
	 */
	public void unpinPage(PageId pageno, boolean dirty) throws IllegalArgumentException {

	    if (debugvalue){
            System.out.println("unpin function start");
        }
        //Checks if page is dirty.
        //first check if the page is unpinned
        FrameDesc fdesc = pagemap.get(pageno.pid);
        if (debugvalue) {
            System.out.println(pageno.getPID());
        }
        if (fdesc == null) throw new IllegalArgumentException(
                "Page not pinned;"
        );
        if (dirty){
            flushPage(pageno);
        }
        fdesc.pincnt--;
        if (debugvalue) {
            System.out.println("page state is " + pageno.getPID());
            System.out.println("Dirty state is " + dirty);
        }


        //unpin page.
        Minibase.DiskManager.deallocate_page(pageno);
        if (debugvalue){
            System.out.println("unpin function end");
        }
	    return;

	}

	/**
	 * Immediately writes a page in the buffer pool to disk, if dirty.
	 */
	public void flushPage(PageId pageno) {

		FrameDesc fdesc = pagemap.get(pageno.pid);
		if (fdesc == null)  {return;}
        if (debugvalue) {
            System.out.println("fdesc = " + fdesc.index);
        }
        if (fdesc.dirty){
			Minibase.DiskManager.write_page(pageno,Minibase.BufferManager.bufpool[pageno.getPID()]);
			fdesc.dirty = false;
		}
	}

	/**
	 * Immediately writes all dirty pages in the buffer pool to disk.
	 */
	public void flushAllPages() {
	    for (int i = 0 ; i < Minibase.BufferManager.frametab.length; i++ ){
            flushPage(Minibase.BufferManager.frametab[i].pageno);
        }
	}

	/**
	 * Gets the total number of buffer frames.
	 */
	public int getNumBuffers() {
		return Minibase.BufferManager.bufpool.length;
	}

	/**
	 * Gets the total number of unpinned buffer frames.
	 */
	public int getNumUnpinned() {
	    int j = 0;
        for (int i = 0 ; i < Minibase.BufferManager.frametab.length; i++ ){
            if (0 != Minibase.BufferManager.frametab[i].state){ j++;}
        }
        return j;
	}

} // public class BufMgr implements GlobalConst
