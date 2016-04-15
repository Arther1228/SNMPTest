package me.gacl.snmp;

import java.util.ArrayList;

public class ApTerminalMac {
	private String apmac ;
	private ArrayList<String> terminalmac;    //terminalmac  List 

	public ApTerminalMac(){}

	public ApTerminalMac(String apmac, ArrayList<String> terminalmac) {
		super();
		this.apmac = apmac;
		this.terminalmac = terminalmac;
	}
	public String getApmac() {
		return apmac;
	}
	public void setApmac(String apmac) {
		this.apmac = apmac;
	}
	public ArrayList<String> getTerminalmac() {
		return terminalmac;
	}
	public void setTerminalmac(ArrayList<String> terminalmac) {
		this.terminalmac = terminalmac;
	}

}