package plotCAS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Transition {
	/* Molcas specific */
	public int fromGS=1;
	public int toGS=1;
	public float Boltztemp=0;
	public boolean isdipole=false;
	public boolean isquadrupole=false;
	public boolean isveloc=false;
	public boolean isboltz=false;
	public boolean isSF=false;
	public boolean isSOC=false;
	private Molcasfile molcas; 
	/* General */
	private File transitionfile;
	private boolean islist=false;
	
	/* List input */
	public Transition(String text)
	{
		islist=false;
		transfile();
		put_to_file(transitionfile,text);
	}
	public Transition()
	{
		islist=false;
		transfile();
	}
	/* MOLCAS input */
	public Transition(Molcasfile input,boolean dSF, boolean dSOC, boolean ddip, boolean dveloc,boolean dquad, boolean dboltz, float dtemp,int i1,int i2)
	{
		molcas=input;
		isSF=dSF;
		isSOC=dSOC;
		isdipole=ddip;
		isveloc=dveloc;
		isquadrupole=dquad;
		Boltztemp=dtemp;
		isboltz=dboltz;
		fromGS=i1;
		toGS=i2;
		islist=true;
		transfile();
		input.totransition(dSF, dSOC, ddip, dveloc, dquad, dboltz, dtemp, transitionfile,i1,i2);
	}
	private void transfile()
	{
		String filename="curve"+String.valueOf(Window.ncurve+1)+".xy";
		transitionfile=new File (PlotCAS.WorkDir,filename);
	}
	
	/* Gets, sets and utils */
	
	public Molcasfile getmolcas()
	{
		return molcas;
	}
	public File getfile() {
		return transitionfile;
	}
	
	public void put_to_file(File pfile,String ptext){
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(pfile), "utf-8"));
			writer.write(ptext);
			writer.close();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(new JFrame(), "Failed to save this file", "Error",JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

}
