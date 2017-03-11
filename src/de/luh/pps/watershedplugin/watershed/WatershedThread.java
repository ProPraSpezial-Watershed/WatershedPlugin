package de.luh.pps.watershedplugin.watershed;

import main.Segment;
import misc.grid.BitCube;
import threads.SegmentingThread;

/**
 * Base class for a Watershed implementation.
 * The thread must have finished before calling any mathod of this class (except for getQuantizer()).
 * @author Jonas Rinke
 */
public abstract class WatershedThread extends SegmentingThread {
	
	/**
	 * Used as an index for the getSegment(int) method, to return the SKIZ (Skeleton of influenz zones).
	 */
	public static final int SKIZ_SEGMENT=-1;
	
	private WatershedQuantizer range;

	public WatershedThread(Segment seg, boolean monitor,WatershedQuantizer range){
		super(seg,monitor);
		this.range=range;
	}
	
	public WatershedQuantizer getQuantizer(){
		return range;
	}
	
	/**
	 * Returns the total number of segments.
	 */
	public abstract int countSegments();
	
	/**
	 * Returns the segment with the given index.
	 */
	public abstract BitCube getSegment(int index);
	
	/**
	 * Returns the segment at the given position where position is a (x,y,z) vector.
	 */
	public abstract BitCube getSegmentAt(int x,int y,int z);
	
	/**
	 * Returns the segment at the given postion where postion is an index into the underlying regular Grid.
	 */
	public abstract BitCube getSegmentAt(int index);
	
	/**
	 * Returns an array that contains the segment indices ordered by the size of the corresponding segments.
	 * Indices of larger segments are at the begining and smaller segments are at the end.
	 */
	public abstract int[] getSortedList();
}
