package bufmgr;

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
		for (int i = 0; i < frametab.length; i++) {
			if(frametab[i].pincnt > victim) {
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
