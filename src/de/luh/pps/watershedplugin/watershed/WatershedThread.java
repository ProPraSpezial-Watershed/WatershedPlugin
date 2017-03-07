package de.luh.pps.watershedplugin.watershed;

import main.Segment;
import misc.grid.BitCube;
import threads.SegmentingThread;

public abstract class WatershedThread extends SegmentingThread {
	
	private WatershedRange range;

	public WatershedThread(Segment seg, boolean monitor,WatershedRange range){
		super(seg,monitor);
		this.range=range;
	}
	
	public WatershedRange getRange(){
		return range;
	}
	
	public abstract int countSegments();
	
	public abstract BitCube getSegment(int index);
}
