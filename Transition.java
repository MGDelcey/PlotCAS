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
	private Window window;
	
	/* List input */
	public Transition(Window tmp,String text)
	{
		window=tmp;
		transfile(tmp.ncurve,tmp.WorkDir);
		put_to_file(transitionfile,text);
	}
	public Transition(Window tmp)
	{
		window=tmp;
		transfile(tmp.ncurve,tmp.WorkDir);
	}
	/* MOLCAS input */
	public Transition(Window tmp,Molcasfile input,boolean dSF, boolean dSOC, boolean ddip, boolean dveloc,boolean dquad, boolean dboltz, float dtemp,int i1,int i2)
	{
		window=tmp;
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
		transfile(tmp.ncurve,tmp.WorkDir);
		input.totransition(tmp.plot.getunit(),dSF, dSOC, ddip, dveloc, dquad, dboltz, dtemp, transitionfile,i1,i2);
	}
	
    /* ******************************** */
    /* *******     Analysis    ******** */
    /* ******************************** */
	public void analysis(String parentname,int mode)
	{
		Window tmp=window;
		/* Make list of SOC states for screening */
		//int[] SOClist=new int[1];
		//int jtrans=0;
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
		if (isSOC)
		{
			JOptionPane.showMessageDialog(new JFrame(), "Spin-orbit spectra analysis temporarily disabled", "Error",JOptionPane.ERROR_MESSAGE);
		}
		else
		{
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
					trans[iorb]=new Transition(tmp);
					tmp.ncurve++;
					inpfile[iorb]=trans[iorb].getfile();
					writer[iorb]= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inpfile[iorb]), "utf-8"));
				}
				tmp.ncurve-=ntact;
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
                        tmp.addcurve(curvename,1,trans[j],"",tmp.plot.getunit());
                }

			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Internal I/O error", "Error",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
	}
    /* ******************************** */
    /* *******   Scattering    ******** */
    /* ******************************** */
	public float[][] scatterplane(float e1i,float e2i,float e1t,float e2t, int xres,int yres,int iunit)
	{
		float[][] plane = new float[xres][yres];
		for (int i = 0; i < xres; i++) { for (int j = 0; j < yres; j++) {plane[i][j]=0;}}
		
		float Escale=(float) Curveplot.unitfactor(iunit);
		/* Store initial->intermediate transitions */
		int nstates=0;
		String stop="*SF";
		if (isSF) { nstates=molcas.getSFstates(); stop="*SF";}
		else { nstates=molcas.getSOCstates(); stop="*SOC";}
		int[] position = new int[nstates];
		for (int istate = 0; istate < nstates; istate++) { position[istate]=-1;}
		
		float[] tenergy = new float[ntrans];
		float[] intensity = new float[ntrans];
		float[] senergy = new float[nstates];
		String text;
		int i1,i2;
		float ene,intens;
		int ninter=-1;
		// Right now only work only for degenerate ground states
		try {
			BufferedReader reader = new BufferedReader(new FileReader(transitionfile));
			for (int itrans = 0; itrans < ntrans; itrans++)
			{
				text = reader.readLine();
				ene=Float.parseFloat(text.trim().split(" +")[0]);
				if (ene>e1i&&ene<e2i)
				{
					i2=Integer.parseInt(text.trim().split(" +")[2])-1;
					if (position[i2]<0)
					{
						ninter++;
						intens=Float.parseFloat(text.trim().split(" +")[1]);
						tenergy[ninter]=ene;
						intensity[ninter]=(float) Math.sqrt(3/2*intens/(ene/Escale));
						position[i2]=ninter;
					}
					else
					{
						int ipos=position[i2];
						if (Math.abs(tenergy[ipos]-ene)>(e2i-e1i)/xres)
						{
							JOptionPane.showMessageDialog(new JFrame(), "Scattering plots not implemented for non-degenerate ground states", "Error",JOptionPane.ERROR_MESSAGE);
							break;
						}
						else
						{
							intens=Float.parseFloat(text.trim().split(" +")[1]);
							intensity[ipos]+=(float) Math.sqrt(3/2*intens/(ene/Escale));
						}
					}
				}
			}
			reader.close();
			reader = new BufferedReader(new FileReader(molcas.getFile()));
			boolean passed=false;
			while ((text = reader.readLine()) != null) {
				/* Store state energies */
				if (text.contains(stop))
				{
					passed=true;
					for (int i = 0; i < nstates; i++)
					{
						text = reader.readLine();
						senergy[i]=Float.parseFloat(text.trim().split(" +")[0]);
					}
				}
				/* Read through transitions */
				if (passed&&((text.contains("*Velocity")&&isdipole&&isveloc)||(text.contains("*Dipole")&&isdipole&&!isveloc)||(text.contains("*Quadrupole")&&isquadrupole)))
				{
					int x,y;
					text = reader.readLine();
					while (!text.startsWith("*"))
					{
						i1=Integer.parseInt(text.trim().split(" +")[0])-1;
						i2=Integer.parseInt(text.trim().split(" +")[1])-1;
						if (i1==i2) {continue;}
						intens=Float.parseFloat(text.trim().split(" +")[2]);
						if (position[i1]>=0)
						{
							ene=tenergy[position[i1]]-Escale*(senergy[i1]-senergy[i2]);
							if ((ene>=e1t)&&(ene<e2t))
							{
								intens=(float) Math.sqrt(3/2*intens/Math.abs(senergy[i1]-senergy[i2]));
								intens=intens*intensity[position[i1]];
								x=(int) ((tenergy[position[i1]]-e1i)/(e2i-e1i)*(float)xres);
								y=(int) ((ene-e1t)/(e2t-e1t)*(float)yres);
								plane[x][y]+=intens;
							}
						}
						if (position[i2]>=0)
						{
							ene=tenergy[position[i2]]-Escale*(senergy[i2]-senergy[i1]);
							if ((ene>=e1t)&&(ene<e2t))
							{
								intens=(float) Math.sqrt(3/2*intens/Math.abs(senergy[i2]-senergy[i1]));
								intens=intens*intensity[position[i2]];
								x=(int) ((tenergy[position[i2]]-e1i)/(e2i-e1i)*(float)xres);
								y=(int) ((ene-e1t)/(e2t-e1t)*(float)yres);
								plane[x][y]+=intens;
							}
						}
						text = reader.readLine();
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), "Internal I/O error", "Error",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return plane;
		
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
	private void transfile(int ncurve,File WorkDir)
	{
		String filename="curve"+String.valueOf(ncurve+1)+".xy";
		transitionfile=new File (WorkDir,filename);
	}

}
