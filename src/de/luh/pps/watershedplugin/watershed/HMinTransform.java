package de.luh.pps.watershedplugin.watershed;

public class HMinTransform {
	
	private final int EDGE=-1;
	
	private int dynamic;
	
	private short[] original;
	
	private short[] current,next,buffer1,buffer2;
	
	private int dimX,dimY,dimZ;
	
	private int[] neighbours;
	
	private int erosionChanges=-1;
	
	public HMinTransform(int dynamic,int dimX,int dimY,int dimZ){
		if(dynamic<=0)
			this.dynamic=1;
		else
			this.dynamic=dynamic;
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
	
	private void swapBuffers(){
		if(current==buffer1){
			current=buffer2;
			next=buffer1;
		}else{
			current=buffer1;
			next=buffer2;
		}
	}
	
	private void erode(){
		erosionChanges=0;
		for(int i=0;i<current.length;i++){
			if(current[i]==original[i])
				continue;
			readNeighbourIndices(i);
			
			short min=Short.MAX_VALUE;
			for(int j=0;j<neighbours.length;j++){
				if(neighbours[j]==EDGE)
					continue;
				
				if(current[neighbours[j]]<min)
					min=current[neighbours[j]];
			}
			if(min>=original[i] && min!=next[i]){
				next[i]=min;
				erosionChanges++;
			}
		}
		swapBuffers();
	}
	
	public short[] apply(short[] source){
		original=source;
		buffer1=new short[source.length];
		buffer2=new short[source.length];
		current=buffer1;
		next=buffer2;
		
		for(int i=0;i<source.length;i++){
			current[i]=(short) (source[i]+dynamic);
		}
		
		while(erosionChanges!=0){
			erode();
			System.out.println(erosionChanges);
		}
		
		next=null;
		buffer1=null;
		buffer2=null;
		return current;
	}
}
