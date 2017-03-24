package de.luh.pps.watershedplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.luh.pps.watershedplugin.watershed.ImmersionThread;
import de.luh.pps.watershedplugin.watershed.WatershedThread;
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
import settings.JDoubleOptionTFSlider;
import threads.SegmentingThread;
import yplugins.YModule;

public class WatershedModule extends GMPanel implements YModule, YObserver {

	// The "Generate" Start button
	private JButton Start;
	// The next button
	private JButton next;
	// The previous button
	private JButton previous;
	// Textfield for min Value
	private JFormattedTextField min;
	// Textfield for maxvalue
	private JFormattedTextField max;
	// Slider for min max slection
	private JRangeSlider slider;
	// Histopanel
	private HistoPanel hp;
	// Selected High and Toplevel from User
	private int maxLevel;
	private int minLevel;
	// Slider for dynamic value within the algorithm
	private JSlider jslider;
	private JFormattedTextField value;
	private double dynamicValue;
	// Need actually
	// TODO: TRY TO LOAD DATA WITHOUT USER TASK ...
	private boolean first = true;
	
	private WatershedThread my_thread;
	
	private int[] segments;
	
	private int currentSegment=0;

	public WatershedModule() {

		// Ini Generate Button
		this.Start = new JButton("Generate");
		// Set Action Listener when the button was clicked
		this.Start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Segment tmp_seg = MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME);
				my_thread = new ImmersionThread(tmp_seg, true,minLevel,maxLevel,1,dynamicValue /* <- Put dynamic here*/);
				segments=null;
				currentSegment=0;
				my_thread.start();
			}
		});
		// next Generate Button
		this.next = new JButton("Next Segment");
		this.next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(segments!=null){
					currentSegment++;
					if(currentSegment>=segments.length)
						currentSegment=0;
					
					BitCube segmentData=my_thread.getSegment(segments[currentSegment]);
					MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME).set_bc(segmentData);
				}
			}
		});
		// prev Generate Button
		this.previous = new JButton("Prev Segment");
		this.previous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(segments!=null){
					currentSegment--;
					if(currentSegment<0)
						currentSegment=segments.length-1;

					BitCube segmentData=my_thread.getSegment(segments[currentSegment]);
					MasterControl.get_is().get_segment(ToolSegGen.TMP_SEG_NAME).set_bc(segmentData);
				}
			}
		});

		// Create a Load button for data not nice but i search for a better
		// solution
		JButton load = new JButton("Load HP");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int i = 1;
				if (first) {
					// don't know why but it need to run 3 time for the first
					// run, so that the user does not push it 3 times we do it
					// for him
					i = 3;
					first = false;
				}
				for (int j = 0; j < i; j++) {
					// load stack to set data
					ImageStack is = MasterControl.get_is();
					if (is.get_state() == 2) {
						minLevel = is.get_vch().get_nonzero_min();
						maxLevel = is.get_vch().get_nonzero_max();
						slider.setMinimum(minLevel);
						slider.setMaximum(maxLevel);
						slider.setRange(minLevel, maxLevel);
						min.setText(is.get_raw_value(minLevel) + "");
						max.setText(is.get_raw_value(maxLevel) + "");
					}

				}

			}
		});

		ImageStack is = MasterControl.get_is();
		is.addObserver(this, "Test Module Listener");
		
		// Create min Textfield and add a change listener for input
		this.min = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.min.setColumns(8);
		this.min.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get new Value, set Slider and histopanel highlight new
				int minTemp = Integer.parseInt(min.getText());
				minLevel = minTemp;
				slider.setRange(minLevel, maxLevel);
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});
		
		// Create max Textfield and add a change listener for input
		this.max = new JFormattedTextField(NumberFormat.getIntegerInstance());
		this.max.setColumns(8);
		this.max.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get new Value, set Slider and histopanel highlight new
				int maxTemp = Integer.parseInt(max.getText());
				maxLevel = maxTemp;
				slider.setRange(minLevel, maxLevel);
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});

		// Create the histopanel
		this.hp = new HistoPanel(is.get_vch(), "HPTest", false, false);
		JPanel hpPanel = new JPanel();
		hpPanel.setBorder(new EmptyBorder(0, 16, 0, 16));
	 
		hpPanel.setLayout(new BorderLayout());
		hpPanel.add(this.hp, "Center");
		
		// Create slider and add change listener
		this.slider = new JRangeSlider(0, 1000, 100, 300, 1);
		this.slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// get new Value, histopanel highlight and textfields new
				minLevel = slider.getLowValue();
				maxLevel = slider.getHighValue();

				min.setText(minLevel + "");
				max.setText(maxLevel + "");
				hp.highlight_interval(minLevel, maxLevel, new Color(255, 0, 0, 80));
			}
		});
		// Create Slider with value between 0 and 10,000 (or 0-1) with 0.15 as ini value
		this.jslider = new JSlider(JSlider.HORIZONTAL,0,10000,1500);
		 // Create change listener to update textfield
		this.jslider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e){
				//Get Real number we wont 
				dynamicValue = jslider.getValue()/10000.0;
				value.setText(dynamicValue+"");
			}
		});
		// Create textfield to see the slider value
		this.value =  new JFormattedTextField(NumberFormat.INTEGER_FIELD);
		this.value.setColumns(8);
		dynamicValue = this.jslider.getValue()/10000.0;
		this.value.setText(dynamicValue+"");
		
		
		
		
	 
		// Add components
		add("generate", this.Start);
		add("min", this.min);
		add("max", this.max);
		add("hp", hpPanel);
		add("slider", this.slider);
		add("load", load);
		add("next", this.next);
		add("prev", this.previous);
		add("jslider", this.jslider);
		add("value", this.value);
		
		// Set layout
		set_layout(
				  "   <table width='100%' height='100%' cellpadding='0' border='0'>									"
				+ " 		<tr height='97%'> 																		"
				+ "   		<td fill='both'>::hp::</td>																"
				+ "       </tr>																						"
				+ "  		<tr height='6%'> 																		"
				+ "   		<td fill='horizontal'>::slider::</td>													"
				+ "       </tr>																						"
				+ "   	<tr height='1%'>  																			"
				+ "   		<td fill='horizontal'> 																	"
				+ "  	    		<table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>      "
				+ "   					<tr>																		"
				+ "       		 			<td width='1%' anchor='west'>::min::</td>   							"
				+ "        	     			<td width='1%' anchor='west'>Min</td>   								"
				+ "							<td>::load::</td>           							    			"  
				+ "       		 			<td width='1%' anchor='east'>Max</td>                                 	"
				+ "       		 			<td width='1%' anchor='east'>::max::</td>  							   	"
				+ "   					</tr> 																		"
				+ "   				</table>																	    "
				+ "  			</td> 																				"
				+ "  		</tr>																					"
				+ "		<tr height='1%'> 																	    	"
				+ "       	 <td fill='horizontal'> 																"
				+ "     			<table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>      "
				+ "     				 <tr>         														        "	
				+ "							<td colspan='3' fill='both'>::jslider::</td>							"
				+ "							<td anchor='west' >::value::</td>	 							    	"          							
				+ "							<td anchor='west'>::generate::</td>										"        					
				+ "					    </tr> 																	    "
				+ "				</table> 																			"
				+ "			</td>																					"
				+ "		</tr>																						"
				+ "		<tr height='1%'> 																	    	"
				+ "       	 <td fill='horizontal'> 																"
				+ "     		<table width='100%' cellpadding='0' cellspacing='0' margin='0' border='0'>     	    "
				+ "     			<tr>         														            "		 
				+ "       		 		<td anchor='east'>::next::</td>												"
				+ "						<td anchor='west'>::prev::</td>												"        					
				+ "					</tr> 																	  	    "
				+ "				</table> 																			"
				+ "			</td>																					"
				+ "		</tr>																						"					
				+ "	</table>																						"
				);

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
		System.out.println("SampleSegModule::update received message from " + sender.getClass() + ": "
				+ Message.get_message_string(m._type));

		if (sender.getClass() == ImageStack.class) {
			if (m._type == ImageStack.M_LOADING_END) {
				System.out.println("TestModule received M_LOADING_END message");
			}
			
			if (m._type == ImageStack.M_SEG_END) {
				segments=my_thread.getSortedList();
			}
		}
	}

}
