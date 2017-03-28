package de.luh.pps.watershedplugin.watershed;

public class HMinTransform {
	
	private final int EDGE=-1;
	
	private int dynamic,range;
	
	private short[] original;
	
	private short[] elevated;
	
	private int[] sortedData,rangeOffsets;
	
	private int dimX,dimY,dimZ;
	
	private int[] neighbours;
	
	public HMinTransform(int dynamic,int range,int dimX,int dimY,int dimZ){
		if(dynamic<=0)
			this.dynamic=1;
		else
			this.dynamic=dynamic;
		this.range=range;
		this.dimX=dimX;
		this.dimY=dimY;
		this.dimZ=dimZ;
		neighbours=new int[27];
	}
	
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
	
	private void sort(){
		rangeOffsets=new int[range];
		int[] frequencies=new int[range];
		for(int i=0;i<elevated.length;i++){
			frequencies[elevated[i]]++;
		}
		int sum=0;
		for(int i=0;i<rangeOffsets.length;i++){
			rangeOffsets[i]=sum;
			sum+=frequencies[i];
		}
		
		int[] rangeIndices=new int[rangeOffsets.length];
		int value=0;
		for(int i=0;i<elevated.length;i++){
			value=elevated[i];
			sortedData[rangeOffsets[value]+rangeIndices[value]]=i;
			rangeIndices[value]++;
		}
		
		/*boolean test=true;
		int prev=elevated[sortedData[0]];
		for(int i=1;i<elevated.length;i++){
			value=elevated[sortedData[i]];
			test&=value>=prev;
			if(!test){
				System.out.println("Error at "+i+":"+prev+","+value);
				break;
			}
			prev=value;
		}
		System.out.println(test);*/
		
	}
	
	public short[] apply(short[] source){
		original=source;
		elevated=new short[source.length];
		sortedData=new int[source.length];
		byte[] enqueued=new byte[source.length];
		
		for(int i=0;i<elevated.length;i++){
			elevated[i]=(short) (original[i]+dynamic);
			if(elevated[i]>=range){
				elevated[i]=(short) (range-1);
			}
		}
		
		sort();
		
		for(int i=range-1;i>=dynamic;i--){
			//Calculate offset and start of the data for this level.
			int length,start;
			if(i==range-1)	//Is this the last iteration?
				length=elevated.length-rangeOffsets[i];
			else
				length=rangeOffsets[i+1]-rangeOffsets[i];
			start=rangeOffsets[i];
			ImmersionQueue queue=new ImmersionQueue(length);
			
			for(int j=start;j<start+length;j++){
				queue.enqueue(sortedData[j]);
			}

			while(!queue.isEmpty()){
				int pixel=queue.dequeue();
				enqueued[pixel]=0;
				short reference=elevated[pixel];
				readNeighbourIndices(pixel);
				
				//if(pixel==2654336)
				//	System.out.println("!");
				for(int j=0;j<neighbours.length;j++){
					if(neighbours[j]==EDGE)
						continue;
					if(elevated[neighbours[j]]<=reference)
						continue;
					if(elevated[neighbours[j]]<=original[neighbours[j]])
						continue;
					
					elevated[neighbours[j]]=(short) Math.max(reference,original[neighbours[j]]);
					if(enqueued[neighbours[j]]==0){
						queue.enqueue(neighbours[j]);
						enqueued[neighbours[j]]=1;
					}
				}
			}
		}
		
		return elevated;
	}
}
