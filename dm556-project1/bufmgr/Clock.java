package bufmgr;

import global.Minibase;

public class Clock extends Replacer{


	protected Clock(BufMgr bufmgr) {
		super(bufmgr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void newPage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void freePage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pinPage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpinPage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int pickVictim() {
        // Finds the first element in the frametab array, where pin count is equal to zero and returns it.
		for (int i = 0; i < Minibase.BufferManager.frametab.length; i++) {
            if(Minibase.BufferManager.frametab[i].pincnt == 0) {
                return i;
			}
		}
		// If no pages has zero pins, then it returns -1.
        return -1;
    }
}
