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
		// TODO Auto-generated method stub

		int victim = 0;
		if(Minibase.BufferManager.frametab == null) {
            System.out.println("frametab null");
        }
		for (int i = 0; i < Minibase.BufferManager.frametab.length; i++) {
			if(Minibase.BufferManager.frametab[i].pincnt > victim) {
				victim = i;
			}
		}
		if(victim > 0) {
			return victim;
		} else {
			return -1;
		}
	}
}
