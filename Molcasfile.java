package plotCAS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Molcasfile {
	private static int nfiles;
	private File inputfile; // MOLCAS log file
	private File intfile; // Intermediate "summary" file
	public int nrSFstates=0;
	private int nrSOCstates=0;
	private boolean isRASSI=false;
	private boolean isRASSCF=false;
	private boolean isCASPT2=false;
	private boolean isMSCASPT2=false;
	private boolean isdipole=true;
	private boolean isquadrupole=true;
	private boolean isveloc=true;
	private int nsym=1;
	private String[] symlabel=null;
	public String[] orblabel=null;
	private int[] nact=null;
	public int ntact=0;
	public int nspin;
	public int spinlist[];
	private int maxRASSCF=0;
	
	public Molcasfile() {
	}
	
	public Molcasfile(File WorkDir,File pFile){
		inputfile=pFile;
		nfiles++;
		String filename="molcas"+String.valueOf(nfiles);
		intfile=new File(WorkDir,filename);
		browselog();
	}
	
    /* ******************************** */
    /* *******      Browse     ******** */
    /* ******************************** */
	public void browselog()
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputfile));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(intfile), "utf-8"));
			String text;
			int index;
			double tmp;
			boolean isfirst=true;
			int nroot=0;
			int nPT2root=0;
			int nSCFroot=0;
			while ((text = reader.readLine()) != null) {
				if (text.contains("--- Start Module: gateway")) {break;}
			}
			while ((text = reader.readLine()) != null) {	
				/* ****** */
				/* RASSCF */
				/* ****** */
				
				if (text.contains("&RASSCF")||text.contains("MOLCAS executing module RASSCF"))
				{
					nroot=0;
					writer.write("*RASSCF\n");
					isRASSCF=true;
					boolean isfirstCASSCF=true;
					while ((text = reader.readLine()) != null) {
						if (text.contains("Stop Module:")) {break;}
						if (text.contains("Symmetry species")&&isfirst)
						{
							if (text.contains("8"))
							{
								nsym=8;
							}
							else if (text.contains("4"))
							{
								nsym=4;
							}
							else if (text.contains("2"))
							{
								nsym=2;
							}
							symlabel=new String[nsym];
							nact=new int[nsym];
							text = reader.readLine();
							symlabel=text.trim().split(" +");
						}
						if (text.contains("Active orbitals")&&isfirst)
						{
							isfirst=false;
							ntact=0;
							for (int i = 0; i < nsym; i++)
							{
								nact[i]=Integer.parseInt(text.trim().split(" +")[i+2]);
								ntact=ntact+nact[i];
							}
						}
						if (text.contains("Number of root(s) required")&&isfirstCASSCF)
						{
							nroot=Integer.parseInt(text.trim().split(" +")[4]);
							maxRASSCF = Math.max(maxRASSCF, nroot);
							writer.write(String.valueOf(nroot)+"\n");
							isfirstCASSCF=false;
						}
						if (text.contains("Natural orbitals and occupation numbers for root"))
						{
							for (int i = 0; i < nsym; i++)
							{
								if (nact[i]>0)
								{
									text = reader.readLine();
									if (text.indexOf("sym")<=0) {text = reader.readLine();}
									writer.write(text.substring(text.indexOf("sym")+6));
									if (nact[i]>10)
									{
										int ipos=text.indexOf("sym")+6;
										text = reader.readLine();
										writer.write(text.substring(ipos));
									}
								}
							}
							writer.write("\n");
						}
					}
				}
				/* ****** */
				/* CASPT2 */
				/* ****** */
				
				if (text.contains("&CASPT2")||text.contains("MOLCAS executing module CASPT2"))
				{
					writer.write("*CASPT2\n");
					isCASPT2=true;
					while ((text = reader.readLine()) != null) {
						if (text.contains("Stop Module:")) {break;}
						if (text.contains("Number of root(s) available"))
						{
							nSCFroot=Integer.parseInt(text.trim().split(" +")[4]);
							if (nSCFroot==1)
							{
								writer.write("1  1\n");
								writer.write("1\n");
							}
							else
							{
								while(!text.contains("Number of CI roots used"))
								{
									text = reader.readLine();
								}
								nPT2root=Integer.parseInt(text.trim().split(" +")[5]);
								writer.write(String.valueOf(nSCFroot)+"  "+String.valueOf(nPT2root)+"\n");
								text = reader.readLine();
								writer.write(text.substring(text.indexOf("These are:")+10).trim()+" ");
								int i=1;
								while (nPT2root>i*10)
								{
									text = reader.readLine();
									writer.write(text.trim()+" ");
									i++;
								}
								writer.write("\n");
							}
						}
						if (text.contains("Multi-State CASPT2"))
						{
							isMSCASPT2=true;
						}
						if (text.contains("Eigenvectors:"))
						{
							int i=0;
							while (nPT2root>i*5)
							{
								for (int j = 0; j < nPT2root; j++)
								{
									text = reader.readLine();
									writer.write(text.trim()+"\n");
								}
								i++;
								text = reader.readLine();
								writer.write("\n");
							}
							
						}
					}
				}
						
				/* ****** */
				/* RASSI  */
				/* ****** */
				if (text.contains("&RASSI")||text.contains("MOLCAS executing module RASSI"))
				{
					writer.write("*RASSI\n");
					isRASSI=true;
					boolean skip=false;
					int njob=0;
					ArrayList<Integer> spin = new ArrayList<Integer>();
					while ((text = reader.readLine()) != null) {
						if (text.contains("Specific data for"))
						{
							while ((text = reader.readLine()) != null) {
								if (text.contains("SPIN MULTIPLICITY:")) {break;}
							}
							spin.add(Integer.parseInt(text.trim().split(" +")[2]));
							njob++;
							
						}
						if (text.contains("Stop Module:")) {break;}
						if (text.contains("Nr of states:"))
						{
							index = text.indexOf("Nr of states:")+13;
							nrSFstates=Integer.parseInt(text.substring(index).trim());
							
							Collections.sort(spin);
							nspin=1;
							for (int i = 1; i < njob; i++)
							{
								if (spin.get(i-1)!=spin.get(i)) {nspin++;}
							}
							spinlist= new int[nspin];
							int ispin=0;
							spinlist[0]=spin.get(0);
							for (int i = 1; i < njob; i++)
							{
								if (spin.get(i-1)!=spin.get(i))
								{
									ispin++;
									spinlist[ispin]=spin.get(i);
								}
							}
							int[] statespin=new int[nrSFstates];
							writer.write("*SF\n");
							int istate=0;
							for (int i = 0; i < nrSFstates/20+1; i++)
							{
								text = reader.readLine();
								text = reader.readLine();
								text = reader.readLine();
								for (int j = 0; j < Math.min(20,nrSFstates-20*i); j++)
								{
									statespin[istate]=spin.get(Integer.parseInt(text.trim().split(" +")[j+1])-1);
									istate++;
								}
								text = reader.readLine();
							}
							while ((text = reader.readLine()) != null) {
								if (text.contains("::")) {break;}
							}
							for (int i = 0; i < nrSFstates; i++)
							{
								index = text.indexOf("energy:")+7;
								tmp=Double.parseDouble(text.substring(index).trim());
								writer.write(String.valueOf(tmp)+" "+statespin[i]+"\n");
								text = reader.readLine();
							}
						}
						if (text.contains("SO State  Total energy"))
						{
							int l1=0;
							text = reader.readLine();
							while (text.contains("---"))
							{
								text = reader.readLine();
							}
							for (int i=0; i<nrSOCstates; i++)
							{
								if (i==0)
								{
									
									if (text.substring(0,5).equals("    1")) {l1=1;}
								}
								for (int j=0; j<5; j++)
								{
									int imax=text.length();
									if (24+2*l1+(16+l1)*j+16<=imax)
									{
										tmp=Float.parseFloat(text.substring(24+2*l1+(16+l1)*j+9,24+2*l1+(16+l1)*j+16).trim()); // Has to use fixed length because sometimes number collapses
										if (tmp!=0)
										{
											writer.write(String.valueOf(Integer.parseInt(text.substring(24+l1+(16+l1)*j,24+2*l1+(16+l1)*j+4).trim()))+" "+String.valueOf(tmp)+" ");
										}
									}
								}
								writer.write("\n");
								text = reader.readLine();
							}
							
						}
						if (text.contains("Total energies including SO-coupling:"))
						{
							nrSOCstates=0;
							text = reader.readLine();
							while (text.startsWith("::"))
							{
								index = text.indexOf("energy:")+7;
								tmp=Double.parseDouble(text.substring(index).trim());
								writer.write(String.valueOf(tmp)+"\n");
								nrSOCstates++;
								text = reader.readLine();
							}
						}
						if (text.contains("Spin-orbit section"))
						{
							writer.write("*SOC\n");
							skip=false;
						}
						if (text.contains("Dipole transition strengths"))
						{
							isdipole=true;
							writer.write("*Dipole\n");
						}
						if (text.contains("Velocity transition strengths"))
						{
							isveloc=true;
							isdipole=true; /* Is it only dipole? */
							writer.write("*Velocity\n");
						}
						if (text.contains("Quadrupole transition strengths")||text.contains("Total transition strengths for the second-order expansion"))
						{
							isquadrupole=true;
							writer.write("*Quadrupole\n");
						}
						if (text.contains("Problematic transitions have been found"))
						{
							skip=true;
						}
						if (text.contains("From   To")||text.contains("To  From")||text.contains("From  To"))
						{
							text = reader.readLine();
							if (text.contains("Total A"))
							{
								text = reader.readLine();
							}
							while (text.contains("-------"))
							{
								text = reader.readLine();
							}
							if (!skip)
							{
								while (!text.contains("--"))
								{
									writer.write(text+"\n");
									text = reader.readLine();
								}
								writer.write("********\n");
							}
							else
							{
								skip=false;
							}
						}
					}
				}
			}
			writer.write("**end");
			writer.close();
			reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), "Failed to read Molcas file", "Error",JOptionPane.ERROR_MESSAGE);
		}
	}
    /* ******************************** */
    /* *******  to transition  ******** */
    /* ******************************** */
	public void totransition(int iunit, boolean dSF, boolean dSOC, boolean ddip, boolean dveloc,boolean dquad, boolean dboltz, float dtemp, File output, int fromGS, int toGS)
	{
		double Escale=1;
		String text;
		String stop="*SF";
		double[] energies=null;
		double[] boltz=null;
		int nrofstates=0;
		int i1,i2;
		double tmp;
		int ntrans=0;
		if (dSF) {stop="*SF"; energies=new double[nrSFstates];nrofstates=nrSFstates;}
		if (dSOC) {stop="*SOC";energies=new double[nrSOCstates];nrofstates=nrSOCstates;}
		Escale=Curveplot.unitfactor(iunit);
		double kT=0.000086173325*dtemp/27.211399;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(intfile));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output), "utf-8"));
			boolean first=false;
			while ((text = reader.readLine()) != null) {
				if (text.contains(stop))
				{
					for (int i = 0; i < nrofstates; i++)
					{
						text = reader.readLine();
						energies[i]=Double.parseDouble(text.trim().split(" +")[0]);
					}
					first=true;
					/* Boltzman */
					boltz=new double[toGS-fromGS+1];
					double norm=0;
					for (int i = 0; i <= toGS-fromGS; i++)
					{
						if (dboltz)
						{
							boltz[i]=Math.exp(-(energies[i+fromGS-1]-energies[fromGS-1])/kT);
						}
						else
						{
							boltz[i]=1;
						}
						norm=norm+boltz[i];
					}
					for (int i = 0; i <= toGS-fromGS; i++)
					{
						boltz[i]=boltz[i]/norm;
					}
				}
				if (text.contains("*SOC")) {break;}
				if (first&&((text.contains("*Velocity")&&ddip&&dveloc)||(text.contains("*Dipole")&&ddip&&!dveloc)||(text.contains("*Quadrupole")&&dquad)))
				//if ((text.contains("*Velocity")&&ddip&&dveloc)||(text.contains("*Dipole")&&ddip&&!dveloc)||(text.contains("*Quadrupole")&&dquad))
				{
					//first=false;
					text = reader.readLine();
					while (!text.startsWith("*"))
					{
						i1=Integer.parseInt(text.trim().split(" +")[0]);
						if (i1>=fromGS&&i1<=toGS)
						{
							i2=Integer.parseInt(text.trim().split(" +")[1]);
							tmp=Double.parseDouble(text.trim().split(" +")[2])*boltz[i1-fromGS];
							writer.write(String.valueOf(Escale*(energies[i2-1]-energies[i1-1]))+" "+String.valueOf(tmp)+" "+String.valueOf(i2)+" "+String.valueOf(i1)+"\n");
							ntrans++;
						}
						else
						{
							i2=Integer.parseInt(text.trim().split(" +")[1]);
							if (i2>=fromGS&&i2<=toGS)
							{
								tmp=Double.parseDouble(text.trim().split(" +")[2])*boltz[i2-fromGS];
								writer.write(String.valueOf(Escale*(energies[i1-1]-energies[i2-1]))+" "+String.valueOf(tmp)+" "+String.valueOf(i1)+" "+String.valueOf(i2)+"\n");
								ntrans++;
							}
						}
						text = reader.readLine();
					}
				}
			}
			writer.write("#"+String.valueOf(ntrans)+"\n");
			writer.close();
			reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), "Failed to create spectrum from Molcas file", "Error",JOptionPane.ERROR_MESSAGE);
		}
	}
    /* ******************************** */
    /* *******     Analysis    ******** */
    /* ******************************** */
	public float[][] spinocc()
	{
		float[][] natorb=new float[nrSFstates][nspin];
		String text;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(intfile));
			while ((text = reader.readLine()) != null) {
				/* Read RASSCF NatOrb */
				if (text.contains("*SF"))
				{
					
				}
			}
			reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), "Failed to analyse Molcas file", "Error",JOptionPane.ERROR_MESSAGE);
			}
		return natorb;
	}
	public float[][] orbocc()
	{
		String text;
		float[][] natorb=new float[nrSFstates][ntact]; //assumes sum of CAS = nr SF states
		float[][] tmporb=new float[maxRASSCF][ntact];
		int[] index=new int[maxRASSCF];
		float[][] caspt2=new float[maxRASSCF][5];
		int iroot=0;
		int jroot=0;int jroot2=0;
		/* Create orbital labels */
		int icounter=1;
		int jsym=0;
		orblabel=new String[ntact];
        for (int j=0; j<ntact; j++)
        {
            while (icounter>nact[jsym])
            {
                    icounter=1;
                    jsym++;
            }
            orblabel[j]=String.valueOf(icounter)+symlabel[jsym];
            icounter++;
        }
		try {
			BufferedReader reader = new BufferedReader(new FileReader(intfile));
			while ((text = reader.readLine()) != null) {
				/* Read RASSCF NatOrb */
				if (text.contains("*RASSCF"))
				{
					text = reader.readLine();
					int nroot=Integer.parseInt(text);
					for (int i=0; i<nroot; i++)
					{
						text = reader.readLine();
						for (int j=0; j<ntact; j++)
						{
							tmporb[i][j]=Float.parseFloat(text.trim().split(" +")[j]);
						}
					}
					for (int i=0; i<nroot; i++)
					{
						for (int j=0; j<ntact; j++)
						{
							natorb[iroot][j]=tmporb[i][j];
						}
						iroot++;
					}
				}
				/* Deal with MS-CASPT2  */
				if (text.contains("*CASPT2"))
				{
					if (isMSCASPT2) 
					{
						text = reader.readLine();
						int nSCFroot=Integer.parseInt(text.trim().split(" +")[0]);
						int nroot=Integer.parseInt(text.trim().split(" +")[1]);
						text = reader.readLine();
						for (int i=0; i<nroot-1; i++)
						{
							index[i]=Integer.parseInt(text.trim().split(" +")[i]);
						}
						int ileft=nroot;
						if (nSCFroot>1)
						{
							while (ileft>0)
							{
								// Read up to 5 eigenstates
								for (int i=0; i<nroot; i++)
								{
									text = reader.readLine();
									for (int j=0; j<Math.min(ileft,5); j++)
									{
										caspt2[i][j]=Float.parseFloat(text.trim().split(" +")[j]);
									}
								}
								// Rotate the orbitals accordingly
								for (int i=0; i<Math.min(ileft,5); i++)
								{
									for (int j=0; j<ntact; j++)
									{
										tmporb[i+nroot-ileft][j]=0;
										for (int k=0; k<nSCFroot; k++)
										{
											tmporb[i+nroot-ileft][j]+=natorb[jroot+k][j] * caspt2[k][i]*caspt2[k][i];
										}
									}
								}
								ileft-=5;
								text = reader.readLine();
								
							}
						}
						else
						{
							caspt2[0][0]=(float) 1.0;
						}
						/* Copy into natorb again */
						for (int i=0; i<nroot; i++)
						{
							for (int j=0; j<ntact; j++)
							{
								natorb[jroot2][j]=tmporb[i][j];
							}
							jroot2++;
						}
						jroot+=nSCFroot;
						/* Check that this is smaller or equal to iroot */
					}
				}

			}
			reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), "Failed to analyse Molcas file", "Error",JOptionPane.ERROR_MESSAGE);
		}
		return natorb;
	}
    /* ******************************** */
    /* *******     Scatter     ******** */
    /* ******************************** */
	public void toscatter()
	{
		/*int iunit=Curveplot.getunit();
		double Escale=1;
		String text;
		String stop="*SF";
		double[] energies=null;
		double[] boltz=null;
		int nrofstates=0;
		int i1,i2;
		double tmp;
		int ntrans=0;
		if (isSF) {stop="*SF"; energies=new double[nrSFstates];nrofstates=nrSFstates;}
		if (isSOC) {stop="*SOC";energies=new double[nrSOCstates];nrofstates=nrSOCstates;}
		Escale=Curveplot.unitfactor(iunit);
		double kT=0.000086173325*Boltztemp/27.211399;
		File output=new File (PlotCAS.WorkDir,"tmp");*//* Temporary just to prevent fail */
	/*	try {
			BufferedReader reader = new BufferedReader(new FileReader(intfile));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output), "utf-8"));
			boolean first=false;
			while ((text = reader.readLine()) != null) {
				if (text.contains(stop))
				{
					for (int i = 0; i < nrofstates; i++)
					{
						text = reader.readLine();
						energies[i]=Double.parseDouble(text.trim());
					}
					first=true;*/
					/* Boltzman */
					/*boltz=new double[toGS-fromGS+1];
					double norm=0;
					for (int i = 0; i <= toGS-fromGS; i++)
					{
						if (isboltz)
						{
							boltz[i]=Math.exp(-(energies[i+fromGS-1]-energies[fromGS-1])/kT);
						}
						else
						{
							boltz[i]=1;
						}
						norm=norm+boltz[i];
					}
					for (int i = 0; i <= toGS-fromGS; i++)
					{
						boltz[i]=boltz[i]/norm;
					}
				}
				if (text.contains("*SOC")) {break;}
				if (first&&((text.contains("*Velocity")&&isdipole&&isveloc)||(text.contains("*Dipole")&&isdipole&&!isveloc)||(text.contains("*Quadrupole")&&isquadrupole)))
				//if ((text.contains("*Velocity")&&ddip&&dveloc)||(text.contains("*Dipole")&&ddip&&!dveloc)||(text.contains("*Quadrupole")&&dquad))
				{
					//first=false;
					text = reader.readLine();
					while (!text.startsWith("*"))
					{
						i1=Integer.parseInt(text.trim().split(" +")[0]);
						if (i1>=fromGS&&i1<=toGS)
						{
							i2=Integer.parseInt(text.trim().split(" +")[1]);
							tmp=Double.parseDouble(text.trim().split(" +")[2])*boltz[i1-fromGS];
							writer.write(String.valueOf(Escale*(energies[i2-1]-energies[i1-1]))+" "+String.valueOf(tmp)+"\n");
							ntrans++;
						}
						else
						{
							i2=Integer.parseInt(text.trim().split(" +")[1]);
							if (i2>=fromGS&&i2<=toGS)
							{
								tmp=Double.parseDouble(text.trim().split(" +")[2])*boltz[i2-fromGS];
								writer.write(String.valueOf(Escale*(energies[i1-1]-energies[i2-1]))+" "+String.valueOf(tmp)+"\n");
								ntrans++;
							}
						}
						text = reader.readLine();
					}
				}
			}
			writer.write("#"+String.valueOf(ntrans)+"\n");
			writer.close();
			reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), "Failed to create spectrum from Molcas file", "Error",JOptionPane.ERROR_MESSAGE);
		}*/
	}
    /* ******************************** */
    /* *******  Gets and sets  ******** */
    /* ******************************** */
	public int whichWF()
	{
		int wf=0;
		if (isMSCASPT2)
		{
			wf=3;
		}
		else if (isCASPT2)
		{
			wf=2;
		}
		else if (isRASSCF)
		{
			wf=1;
		}
		return wf;
	}
	public boolean isRASSI()
	{
		return isRASSI;
	}
	public boolean isdipole()
	{
		return isdipole;
	}
	public boolean isveloc()
	{
		return isveloc;
	}
	public boolean isquadrupole()
	{
		return isquadrupole;
	}
	public int getSFstates()
	{
		return nrSFstates;
	}
	public int getSOCstates()
	{
		return nrSOCstates;
	}
}
