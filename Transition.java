package plotCAS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
	public int ntrans;
	
	/* List input */
	public Transition(String text)
	{
		transfile();
		put_to_file(transitionfile,text);
	}
	public Transition()
	{
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
		transfile();
		input.totransition(dSF, dSOC, ddip, dveloc, dquad, dboltz, dtemp, transitionfile,i1,i2);
	}
	
    /* ******************************** */
    /* *******     Analysis    ******** */
    /* ******************************** */
	public void analysis(String parentname,int mode)
	{
		/* Make list of SOC states for screening */
		int[] SOClist=new int[1];
		int jtrans=0;
/*		if (isSOC)
		{
			SOClist = new int[ntrans+toGS-fromGS+1]; // max possible number of states
			for (int itrans = 0; itrans < toGS-fromGS+1; itrans++)
			{
				SOClist[itrans]=fromGS+itrans;
			}
			try {
				BufferedReader reader = new BufferedReader(new FileReader(transitionfile));
				String text;
				int i1;
				for (int itrans = 0; itrans < ntrans; itrans++)
				{
					text = reader.readLine();
					i1=Integer.parseInt(text.trim().split(" +")[2]);
					SOClist[itrans+toGS-fromGS+1]=i1;
				}
				reader.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Internal I/O error", "Error",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			java.util.Arrays.sort( SOClist );*/
			/* Remove doublons */
			/*for (int itrans = 1; itrans < ntrans+toGS-fromGS+1; itrans++)
			{
				
				if (SOClist[itrans]!=SOClist[jtrans])
				{
					jtrans++;
					if (itrans!=jtrans) {SOClist[jtrans]=SOClist[itrans];}
				}
			}
		}
		else
		{*/
			/* Get natural orbitals */
			int ntact;
			if (mode==0) {ntact=molcas.ntact;}
			else {ntact=molcas.nspin;}
			int nrSFstates=molcas.nrSFstates;
			float[][] natorb=new float[nrSFstates][ntact];
			Transition[] trans=new Transition[ntact];
			File[] inpfile=new File[ntact];
			BufferedWriter[] writer = new BufferedWriter[ntact];
			if (mode==0) {natorb=molcas.orbocc();}
			else {natorb=molcas.spinocc();}
			/* Plot differences */
			try {
				BufferedReader reader = new BufferedReader(new FileReader(transitionfile));
				for (int iorb =0; iorb < ntact; iorb++)
				{
					trans[iorb]=new Transition();
					Window.ncurve++;
					inpfile[iorb]=trans[iorb].getfile();
					writer[iorb]= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inpfile[iorb]), "utf-8"));
				}
				Window.ncurve-=ntact;
				String text;
				int i1,i2;
				float e1,e2,intens;
				for (int itrans = 0; itrans < ntrans; itrans++)
				{
					text = reader.readLine();
					e1=Float.parseFloat(text.trim().split(" +")[0]);
					e2=Float.parseFloat(text.trim().split(" +")[1]);
					i2=Integer.parseInt(text.trim().split(" +")[2])-1;
					i1=Integer.parseInt(text.trim().split(" +")[3])-1;
					for (int iorb =0; iorb < ntact; iorb++)
					{
						intens=e2*(natorb[i2][iorb]-natorb[i1][iorb]);
						writer[iorb].write(String.valueOf(e1)+" "+String.valueOf(intens)+"\n");
					}
				}
				reader.close();
                for (int j=0; j<ntact; j++)
                {
                        writer[j].write("#"+String.valueOf(ntrans));
                        writer[j].close();
                        String curvename=parentname+" "+molcas.orblabel[j];
                        PlotCAS.fen.addcurve(curvename,1,trans[j],"",Curveplot.getunit());
                }

			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Internal I/O error", "Error",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
	//}
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
	private void transfile()
	{
		String filename="curve"+String.valueOf(Window.ncurve+1)+".xy";
		transitionfile=new File (PlotCAS.WorkDir,filename);
	}

}
