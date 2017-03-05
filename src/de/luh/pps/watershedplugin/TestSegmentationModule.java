package de.luh.pps.watershedplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

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
		JButton jb_do_it = new JButton("Do it!");
		jb_do_it.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				Segment tmp_seg = MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME);
				SampleSegThread my_thread = new SampleSegThread(tmp_seg, true);
				my_thread.start();
			}
		});

		add("do_it", jb_do_it);
		
		set_layout(""+
			"<table>"+
			  "<tr>"+
			    "<td>A simple Button:</td>"+
			    "<td>::do_it::</td>"+
			  "</tr>"+
			"</table>");				

		ImageStack is = MasterControl.get_is();
		is.addObserver(this, "Test Module Listener");
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