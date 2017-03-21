package de.luh.pps.watershedplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import gui.HistoPanel;
import gui.JRangeSlider;
import jgridmaker.GMPanel;
import main.ImageStack;
import main.MasterControl;
import main.Segment;
import main.VoxelCubeHistogram;
import main.tools.ToolSegGen;
import misc.grid.BitCube;
import misc.grid.RegularGrid3i;
import misc.messages.Message;
import misc.messages.YObservable;
import misc.messages.YObserver;
import threads.SegmentingThread;
import yplugins.YModule;

public class TestSegmentationModule extends GMPanel implements YModule, YObserver {
	
	//The "Generate" Start button
	private JButton Start;
	//Textfield for min Value
	private JFormattedTextField min;
	//Textfield for maxvalue
	private JFormattedTextField max;
	//Slider for min max slection
	private JRangeSlider slider;
	//Histopanel
	private HistoPanel hp;
	//Selected High and Toplevel from User
	private int maxLevel;
	private int minLevel;
	//Need actually 
	//TODO: TRY TO LOAD DATA WITHOUT USER TASK ... 
	private boolean first=true;
	
	//OLD CLASS FROM GIVEN SAMPLE 
	private class SampleSegThread extends SegmentingThread {
		public SampleSegThread(Segment seg, boolean monitor) {
			super(seg, monitor);
		}

		@Override
		public void my_run() {
			// first, get the volume data
			ImageStack is = MasterControl.get_is();
			
			// this is where the volume data is stored
			RegularGrid3i vc = (RegularGrid3i)(is.get_voxel_cube());			
			int dim_z = vc.get_dim_z();
			int dim_y = vc.get_dim_y();
			int dim_x = vc.get_dim_x();
						
			// _seg is a reference to the temporary segment ( selection)			
			BitCube bc = _seg.get_bc();

			// clear the segment
			bc.clear();
			
			// this is for longer lasting methods that want to report progress on the progress bar 
			set_progress_attributes("TestSegmentation", 0, dim_z, 0);

			// now perform the segmentation
			int x_percent = (int)(dim_x*.2);
			int y_percent = (int)(dim_y*.2);
			int z_percent = (int)(dim_z*.2);
			
			// the voxel cube histogram keeps track of the occurence of all values in the voxel cube
			VoxelCubeHistogram vch = is.get_vch();
			int my_min = (int)(vch.get_nonzero_max() * 0.8);
			
			// silly test segmentation method, for learning purpose only
			for (int z=z_percent; z<dim_z-z_percent; z++) {
				for (int y=y_percent; y<dim_y-y_percent; y++) {
					for (int x=x_percent; x<dim_x-x_percent; x++) {
						if (Math.sin(x/5)>0 && vc.get(x,y,z)>=my_min) {
							bc.setXYZ(x, y, z, true);
						}
					}							
				}
				inc_progress_value(1);
				
				// this will inform all observers (e.g. viewports) that the segment has changed and shows the current progress
				_seg.new_data(true);
			}
			
			System.out.println("Did something ...");
		}
		
	}
	
	public TestSegmentationModule() {
		
		//Ini Generate Button 
		this.Start = new JButton("Generate");
		//Set Action Listener when the button was clicked
		this.Start.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				//Actually start the old sample				
				Segment tmp_seg = MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME);
				SampleSegThread my_thread = new SampleSegThread(tmp_seg, true);
				my_thread.start();
			}
		});
		
		//Create a Load button for data not nice but i search for a better solution
		JButton load = new JButton("Load HP");
		 load.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				int i = 1;
				if(first){
					//don't know why but it need to run 3 time for the first run, so that the user does not push it 3 times we do it for him
					i=3;
					first = false;
				}
				for(int j = 0;j<i;j++){
					//load stack to set data
					ImageStack is = MasterControl.get_is();
					if(is.get_state()==2){
						minLevel =is.get_vch().get_nonzero_min();
						maxLevel =is.get_vch().get_nonzero_max();
						slider.setMinimum(minLevel);
						slider.setMaximum(maxLevel);
						slider.setRange(minLevel , maxLevel );					
						min.setText(is.get_raw_value(minLevel)+"");
						max.setText(is.get_raw_value(maxLevel)+"");
					}
				 
				}
				
				
			}
		});	
	 				
		 
		ImageStack is = MasterControl.get_is();		
		is.addObserver(this, "Test Module Listener");
		//Create min Textfield and add a change listener for input 
		this.min = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.min.setColumns(8);
		this.min.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get new Value, set Slider and histopanel highlight new 
				int minTemp = Integer.parseInt(min.getText());
				minLevel=minTemp;
				slider.setRange(minLevel,maxLevel);
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});
		//Create max Textfield and add a change listener for input 
		this.max = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.max.setColumns(8);
		this.max.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get new Value, set Slider and histopanel highlight new 
				int maxTemp = Integer.parseInt(max.getText());
				maxLevel=maxTemp;
				slider.setRange(minLevel,maxLevel);
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});
		
		//Create the histopanel
		this.hp=new HistoPanel(is.get_vch(),"HPTest",false,false);
		JPanel hpPanel = new JPanel();
		hpPanel.setBorder(new EmptyBorder(0,16,0,16));;
		hpPanel.setLayout(new BorderLayout());
		hpPanel.add(this.hp, "Center");
		//Create slider and add change listener 
		this.slider = new JRangeSlider(0,1000,100,300,1);
		this.slider.addChangeListener(new ChangeListener(){
			 public void stateChanged(ChangeEvent e)
		      {
				//get new Value, histopanel highlight and textfields new 
		       minLevel=slider.getLowValue();
		        maxLevel=slider.getHighValue();
		        
		        min.setText(minLevel+"");
				max.setText(maxLevel+"");
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
		      }
		});
		//Add components
		add("generate", this.Start);	
		add("min",this.min);
		add("max",this.max);
		add("hp",hpPanel);
		add("slider",this.slider);
		add("load",load);
		//Set layout
		set_layout(
			      "<table width='100%' height='100%' cellpadding='0' border='0'>  <tr height='97%'> "
			      + "   <td fill='both'>::hp::</td>  </tr><tr height='6%'> "
			      + "   <td fill='horizontal'>::slider::</td>  </tr><tr height='1%'>  "
			      + "  <td fill='horizontal'> "
			      + "    <table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>     "
			      + "  <tr>         <td width='1%' anchor='west'>::min::</td>   "
			      + "      <td width='1%' anchor='west'>Min</td>    "
			      + "    <td width='100%' anchor='center'></td>           "
			      + "   <td width='1%' anchor='east'>Max</td>    "
			      + "     <td width='1%' anchor='east'>::max::</td>   "
			      + "    </tr>     </table>   </td>  </tr><tr height='1%'>  "
			      + "  <td fill='horizontal'>      <table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>  "
			      + "      <tr>            "
			      + "        <td width='98%' >            <table cellpadding='0' cellspacing='0' margin='0'>   "
			      + "           <tr>                <td>::generate::</td>  <td>::load::</td>                "
			      + "             </tr>            </table>          </td> "
			      + "        "
			      + "        </tr>      </table>    </td>  </tr></table>");
			    
		
		
		
	 
		
	}
	
	@Override
	public String get_module_name() {
		return "PPS16 Seg";
	}

	@Override
	public String get_module_short_descr() {
		return "A small sample Module to learn YPlugin programming";
	}

	@Override
	public JPanel get_module_interface() {
		return this;
	}

	@Override
	public void update(YObservable sender, Message m) {
		System.out.println("SampleSegModule::update received message from "+sender.getClass()+": "+Message.get_message_string(m._type));
		
		if (sender.getClass()==ImageStack.class) {
			if (m._type == ImageStack.M_LOADING_END) {
				System.out.println("TestModule received M_LOADING_END message");
			}
		}
	}

}
