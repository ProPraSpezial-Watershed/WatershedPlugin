package de.luh.pps.watershedplugin.watershed;

import java.util.Arrays;

import main.ImageStack;
import main.MasterControl;
import main.Segment;
import misc.grid.BitCube;
import misc.grid.RegularGrid3i;
import misc.grid.VoxelCube;
import misc.grid.gradients.GradientCachingMethod;
import misc.grid.gradients.GradientFunction;
import misc.grid.gradients.GradientFunctionFactory;
import misc.grid.gradients.GradientFunctionType;

public class ImmersionThread extends WatershedThread{
	
	private static final short WATERSHED=(short) SKIZ_SEGMENT,
								UNPROCESSED=0,
								MASK=-2,
								EDGE=-3,
								FICTICOUS=-4;
	
	private int numVoxels;
	
	private short[] imageStack;
	
	private int[] sortedData;
	
	private int[] rangeOffsets;
	
	private int[] segmentData;
	
	private int[] sortedSegments;
	
	private short[] distanceData;
	
	private RegularGrid3i regGrid;
	
	private int dimX,dimY,dimZ;

	private double relativeDynamic;
	
	private int numSegments=0;
	
	private int[] neighbours;
	
	public ImmersionThread(Segment seg, boolean monitor){
		this(seg, monitor, new WatershedQuantizer(),0.15);
	}

	public ImmersionThread(Segment seg, boolean monitor,WatershedQuantizer range) {
		this(seg,monitor,range,0.15);
	}
	
	public ImmersionThread(Segment seg,boolean monitor,int min,int max,int interval,double relativeDynamic){
		this(seg,monitor,new WatershedQuantizer(min,max,interval),relativeDynamic);
	}
	
	public ImmersionThread(Segment seg, boolean monitor,WatershedQuantizer range,double relativeDynamic){
		super(seg, monitor,range);
		neighbours=new int[3*3*3];
		this.relativeDynamic=relativeDynamic/100;
		
		ImageStack is = MasterControl.get_is();
		regGrid=is.get_voxel_cube();

		numVoxels=regGrid.get_number_of_voxels();
		dimX=regGrid.get_dim_x();
		dimY=regGrid.get_dim_y();
		dimZ=regGrid.get_dim_z();
	}
	
	private void preTransform(){
		WatershedQuantizer quantizer=getQuantizer();
		VoxelCube vc=new VoxelCube(dimX,dimY,dimZ);
		for(int i=0;i<numVoxels;i++)
			vc.set(i, quantizer.transformValue(regGrid.get(i)));

		imageStack=new short[numVoxels];
		GradientFunction gradient=GradientFunctionFactory.create_gradient_function(vc,GradientFunctionType.CENTRAL_DIFFERENCES,
																					GradientCachingMethod.ON_THE_FLY);
		gradient.set_normalized(false);
		javax.vecmath.Vector3f vec=new javax.vecmath.Vector3f();
		int range=getQuantizer().getRange();
		for(int z=0;z<dimZ;z++){
			for(int y=0;y<dimY;y++){
				for(int x=0;x<dimX;x++){
					if(x==0 || x==dimX-1 || y==0 || y==dimY-1 || z==0 || z==dimZ-1){
						imageStack[x+y*dimX+z*dimX*dimY]=(short) (range-1);
						continue;
					}
					gradient.gradient_at(x,y,z,vec);
					double vecLength=vec.length();
					if(vecLength>=relativeDynamic*range){
						imageStack[x+y*dimX+z*dimX*dimY]=(short) vecLength;
					}else
						imageStack[x+y*dimX+z*dimX*dimY]=0;
				}
			}
		}
	}
	
	/**
	 * Initialize the transform.
	 */
	private void init(){
		sortedData=new int[numVoxels];
		segmentData=new int[numVoxels];
		distanceData=new short[numVoxels];
	}
	
	/**
	 * Do the presorting. This is a counting sort.
	 */
	private void sort(){
		rangeOffsets=new int[getQuantizer().getRange()];
		int[] frequencies=new int[getQuantizer().getRange()];
		for(int i=0;i<numVoxels;i++){
			frequencies[imageStack[i]]++;
		}
		int sum=0;
		for(int i=0;i<rangeOffsets.length;i++){
			rangeOffsets[i]=sum;
			sum+=frequencies[i];
		}
		
		int[] rangeIndices=new int[rangeOffsets.length];
		int value=0;
		for(int i=0;i<numVoxels;i++){
			value=imageStack[i];
			sortedData[rangeOffsets[value]+rangeIndices[value]]=i;
			rangeIndices[value]++;
		}
		
		/*boolean test=true;
		int prev=imageStack[sortedData[0]];
		for(int i=1;i<length;i++){
			value=imageStack[sortedData[i]];
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
	
	/**
	 * Reads the beighbours of a pixel and stores them in a global array.
	 * Pixels that are out of bounds are labeles with EDGE.
	 * @param index
	 */
	private void readNeighbourIndices(int index){
		int x=index % dimX % dimY;
		int y=(index/dimX) % dimY;
		int z=index/dimX/dimY;
		for(int i=0;i<neighbours.length;i++)
			neighbours[i]=EDGE;
		
		for(int i=-1;i<=1;i++){
			if(x+i<0 || x+i>=dimX)
				continue;
			for(int j=-1;j<=1;j++){
				if(y+j<0 || y+j>=dimY)
					continue;
				for(int k=-1;k<=1;k++){
					if(z+k<0 || z+k>=dimZ)
						continue;
					if(i==0 && j==0 && k==0)
						continue;
					neighbours[(k+1)*9+ (j+1)*3+ (i+1)]=(z+k)*dimY*dimX+(y+j)*dimX+(x+i);
				}
			}
		}
	}
	
	public void free(){
		imageStack=null;
		sortedData=null;
		segmentData=null;
		distanceData=null;
		sortedSegments=null;
		rangeOffsets=null;
		regGrid=null;
		System.gc();
	}

	@Override
	public void my_run() {
		WatershedQuantizer range=getQuantizer();
		long progress=0;
		try{
			set_progress_label("Pretransforming...");
			preTransform();
		}catch(OutOfMemoryError err){
			err.printStackTrace();
			set_progress_label("Error :(");
			free();
			return;
		}
		
		try{
			set_progress_label("Initializing...");
			init();
		}catch(OutOfMemoryError err){
			err.printStackTrace();
			set_progress_label("Error :(");
			free();
			return;
		}
		
		try{
			set_progress_label("Presorting...");
			sort();
		}catch(OutOfMemoryError err){
			err.printStackTrace();
			set_progress_label("Error :(");
			free();
			return;
		}
		regGrid=null;
		
		System.gc();

		set_progress_attributes("Immersing...",0,3*(numVoxels>>10),0);
		//Run for each level
		for(int i=0;i<range.getRange();i++){
			
			//Calculate offset and start of the data for this level.
			int length,start;
			if(i==rangeOffsets.length-1)	//Is this the last iteration?
				length=numVoxels-rangeOffsets[i];
			else
				length=rangeOffsets[i+1]-rangeOffsets[i];
			start=rangeOffsets[i];
			
			ImmersionQueue queue=new ImmersionQueue();
			
			//Masking
			for(int j=start;j<start+length;j++){
				if((progress & 1023)==0)
					set_progress_val((int) (progress>>10));
				
				segmentData[sortedData[j]]=MASK;
				
				readNeighbourIndices(sortedData[j]);
				
				for(int k=0;k<neighbours.length;k++){
					//Only use neighbours that are on the same level or lower
					if(neighbours[k]==EDGE)
						continue;
					
					//Check wether the current pixel will be affected by the next step (IZ)
					if(segmentData[neighbours[k]]==WATERSHED || segmentData[neighbours[k]]>0){
						//Prepare the pixel
						distanceData[sortedData[j]]=1;
						queue.enqueue(sortedData[j]);
						break;
					}
				}
				progress++;
			}
			//Compute IZ
			int distance=1;
			//This is a boundary. All pixels between two FICTICIOUS markers have the same distance from
			//a minimum.
			queue.enqueue(FICTICOUS);
			while(true){
				int pixel=queue.dequeue();
				if(pixel==FICTICOUS){	//Are we at the boundary?
					if(queue.isEmpty())
						break;	//we are done for this level
					else{
						//More stuff after the boundary, increment distance.
						distance++;
						queue.enqueue(FICTICOUS);
						pixel=queue.dequeue();
					}
				}
				
				readNeighbourIndices(pixel);
				for(int j=0;j<neighbours.length;j++){
					if(neighbours[j]==EDGE)
						continue;
					
					//These neighbours are already processed...
					if(distanceData[neighbours[j]]<distance
							&& (segmentData[neighbours[j]]>0 || segmentData[neighbours[j]]==WATERSHED)){
						
						//Is there a labeled neighbour?
						if(segmentData[neighbours[j]]>0){
							if(segmentData[pixel]==MASK || segmentData[pixel]==UNPROCESSED || segmentData[pixel]==WATERSHED)
								segmentData[pixel]=segmentData[neighbours[j]];	//Extends the catchment basin into this pixel.
							else if(segmentData[pixel]!=segmentData[neighbours[j]])
								segmentData[pixel]=WATERSHED;	//Two pixels with the same distance from a minimum are a WATERSHED pixel by definition
						}else if(segmentData[pixel]==MASK)	//If there is no labeled neighbour, this pixel is part of the SKIZ
							segmentData[pixel]=WATERSHED;
					}else if((segmentData[neighbours[j]]==MASK || segmentData[pixel]==UNPROCESSED) && distanceData[neighbours[j]]==0){
						//... and these neighbours will be in the next iteration
						//Prepare pixel just like before.
						distanceData[neighbours[j]]=(short) (distance+1);
						queue.enqueue(neighbours[j]);
					}
				}
			}
			progress+=length;
			set_progress_val((int) (progress>>10));
			
			//Check for new minima
			for(int j=start;j<start+length;j++){
				
				if((progress & 1023)==0)
					set_progress_val((int) (progress>>10));
				distanceData[sortedData[j]]=0;
				if(segmentData[sortedData[j]]==MASK){	//New minimum found!
					numSegments++;	//Give it a name
					segmentData[sortedData[j]]=numSegments;
					
					//Floodfill minimum
					queue.enqueue(sortedData[j]);
					while(!queue.isEmpty()){
						int pixel=queue.dequeue();
						readNeighbourIndices(pixel);
						
						for(int k=0;k<neighbours.length;k++){
							//Only floodfill on our current level
							if(neighbours[k]==EDGE)
								continue;
							
							if(segmentData[neighbours[k]]==MASK){
								segmentData[neighbours[k]]=numSegments;
								queue.enqueue(neighbours[k]);
							}
						}
					}
					//System.out.println(numSegments);
				}
				progress++;
			}
		}
		
		//Free memory. These array are LARGE!
		imageStack=null;
		sortedData=null;
		distanceData=null;
		System.gc();
		
		//Generate sorted array
		set_progress_label("Generating Metadata...");
		
		long[] sortingData=new long[numSegments];
		for(int i=0;i<sortingData.length;i++)
			sortingData[i]=i;
		for(int i=0;i<segmentData.length;i++){
			if(segmentData[i]==WATERSHED)
				continue;
			else if(segmentData[i]>=1)
				sortingData[segmentData[i]-1]+=0x10000;	//Store value as [segment Size][segment Index] in a long.
		}
		Arrays.sort(sortingData);
		sortedSegments=new int[numSegments];
		//Reverse order so the larger segments are in the front. Larger segments are more interesting.
		for(int i=0;i<numSegments;i++){
			sortedSegments[i]=(int) (sortingData[numSegments-1-i] & 0xffff);
		}
	}

	@Override
	public int countSegments() {
		return numSegments;
	}

	@Override
	public BitCube getSegment(int index) {
		if(index!=SKIZ_SEGMENT)
			index++;
		BitCube bitCube=new BitCube(_seg.get_bc(),false);
		/*if(index==0){
			for(int i=0;i<imageStack.length;i++){
				if(regGrid.get(i)!=imageStack[i])
			}
			return bitCube;
		}*/
		for(int i=0;i<segmentData.length;i++){
			if(segmentData[i]==index)
				bitCube.set(i,1);
			else
				bitCube.set(i,0);
		}
		return bitCube;
	}

	@Override
	public BitCube getSegmentAt(int x, int y, int z) {
		int index=x+y*dimX+z*dimX*dimY;
		return getSegment(segmentData[index]-1);
	}

	@Override
	public BitCube getSegmentAt(int index) {
		return getSegment(segmentData[index]-1);
	}

	@Override
	public int[] getSortedList() {
		return sortedSegments;
	}

}
