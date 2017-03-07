package de.luh.pps.watershedplugin;

import javax.swing.JPanel;

import main.MasterControl;
import yplugins.YModule;

public class WatershedModule implements YModule{
	
	private TemporaryGUI gui;
	
	public WatershedModule(){
		gui=new TemporaryGUI();
		MasterControl.get_is().addObserver(gui,"Need to know when segmentation is done.");
	}

	@Override
	public JPanel get_module_interface() {
		return new TemporaryGUI().getPanel();
	}

	@Override
	public String get_module_name() {
		return "Watershed";
	}

	@Override
	public String get_module_short_descr() {
		return "Applies the Watershed tranformation";
	}
}
