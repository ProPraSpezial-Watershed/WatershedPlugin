package de.luh.pps.watershedplugin.watershed;

import main.ImageStack;
import main.MasterControl;
import main.Segment;
import misc.grid.BitCube;
import misc.grid.RegularGrid3i;
import threads.SegmentingThread;

public class ImmersionThread extends WatershedThread{
	
	private int[] sortedData;
	
	private int[] rangeOffsets;
	
	private int dimX,dimY,dimZ;
	
	private RegularGrid3i regGrid;
	
	public ImmersionThread(Segment seg, boolean monitor){
		this(seg, monitor, new WatershedRange());
	}

	public ImmersionThread(Segment seg, boolean monitor,WatershedRange range) {
		super(seg, monitor,range);
	}
	
	private void init(){
		ImageStack imageStack = MasterControl.get_is();
		regGrid = (RegularGrid3i)(imageStack.get_voxel_cube());
		dimX=regGrid.get_dim_x();
		dimY=regGrid.get_dim_y();
		dimZ=regGrid.get_dim_z();
		
		int[] histo=imageStack.get_vch().get_histo();
		rangeOffsets=new int[histo.length];
		int sum=0;
		for(int i=0;i<rangeOffsets.length;i++){
			rangeOffsets[i]=sum;
			sum+=histo[i];
		}
	}
	
	private void sort(){
		int length=regGrid.get_number_of_voxels();
		sortedData=new int[length];
		
		int[] rangeIndices=new int[rangeOffsets.length];
		int value=0;
		WatershedRange range=getRange();
		for(int i=0;i<length;i++){
			value=range.transformValue(regGrid.get(i));
			sortedData[rangeOffsets[value]+rangeIndices[value]]=i;
			rangeIndices[value]++;
		}
		/*
		boolean test=true;
		int prev=range.transformValue(regGrid.get(sortedData[0]));
		for(int i=1;i<length;i++){
			value=range.transformValue(regGrid.get(sortedData[i]));
			test&=value>=prev;
			if(!test){
				System.out.println("Error at "+i+":"+prev+","+value);
				break;
			}
			prev=value;
		}
		System.out.println(test);
		*/
	}

	@Override
	public void my_run() {
		init();
		
		sort();
		
		end_thread();
	}

	@Override
	public int countSegments() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BitCube getSegment(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
