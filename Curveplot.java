package plotCAS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Curveplot {
	
	public Curveplot(Window fen,int i){
		int resolution=fen.plot.resolution;
		int unit=fen.plot.getunit();
		float agap=fen.plot.getda();
		float astart=fen.plot.geta1();
		float estart=fen.plot.getx1();
		float egap=fen.plot.getxde();
		if (i>fen.plot.nplot)
		{
			fen.plot.plotlist.add(new float[resolution]);
			fen.plot.nplot++;
		}
		if (i==0)
		{
			for(int epos = 0; epos < resolution; epos++)
			{
				fen.plot.plotlist.get(i)[epos]=(float)epos/(float)resolution;
			}
		}
		else
		{
			float unitfactor=(float)(unitfactor(unit)/unitfactor(fen.curve.get(i-1).getunit()));
			agap=0;//hopefully will be changed
			float xoffset=fen.curve.get(i-1).getxoffset();
			float yscale=fen.curve.get(i-1).getyscale();
			String text;
			boolean firstline=true;
			int ntrans=0;
			float translist[][] = null;
			switch (fen.curve.get(i-1).gettype())
			{
			case 1:
				firstline=false;
				String text2="0";
				try {
					BufferedReader reader = new BufferedReader(new FileReader(fen.curve.get(i-1).transition.getfile()));
					while ((text = reader.readLine()) != null) {
						text2=text;
					}
					reader.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph", "Error",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph", "Error",JOptionPane.ERROR_MESSAGE);
				}
				ntrans=Integer.parseInt(text2.substring(1));
				fen.curve.get(i-1).transition.ntrans=ntrans;
				translist=new float[2][ntrans];
			case 2:
				int itrans;
				try {
					BufferedReader reader = new BufferedReader(new FileReader(fen.curve.get(i-1).transition.getfile()));
					float a1,e1;
					itrans=0;
					/* Read translist*/
					while ((text = reader.readLine()) != null) {
						if (!text.startsWith("#"))
						{
							if (firstline)
							{
								ntrans=Integer.parseInt(text.trim().split(" ")[0]);
								fen.curve.get(i-1).transition.ntrans=ntrans;
								translist=new float[2][ntrans];
								firstline=false;
							}
							else
							{
								if (itrans<ntrans)
								{
									e1=Float.parseFloat(text.trim().split(" +")[0])*unitfactor+xoffset;
									a1=Float.parseFloat(text.trim().split(" +")[1])*yscale;
									translist[0][itrans]=e1;
									translist[1][itrans]=a1;
									itrans=itrans+1;
								}
							}
						}
					}
					reader.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph", "Error",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph", "Error",JOptionPane.ERROR_MESSAGE);
				}
				/* Create curve*/
				float ex1,ex2;
				double tmp;
				double fact;
				double width;
				double twoW2;
				if (fen.curve.get(i-1).getbroad().islorentz())
				{
					/* Lorentzian */
					width=fen.curve.get(i-1).getbroad().getlorw1();
					float lorsplit=fen.curve.get(i-1).getbroad().getlorsplit();
					double width2=fen.curve.get(i-1).getbroad().getlorw2();
					fact=1/(Math.PI);
					twoW2=Math.pow(width,2);
					double twoW2bis=Math.pow(width2,2);
					float[] lorplot=new float[resolution];
					for (int epos = 0; epos < resolution; epos++)
					{
						ex1=estart+egap*(float)epos/(float)resolution;
						tmp=0;
						for (itrans = 0; itrans < ntrans; itrans++)
						{
							ex2=translist[0][itrans];
							if ((fen.curve.get(i-1).getbroad().isduallorentz())&&(ex2>lorsplit))
							{
								tmp=tmp+translist[1][itrans]*fact*width2/(Math.pow(ex2-ex1,2)+twoW2bis);
							}
							else
							{
								tmp=tmp+translist[1][itrans]*fact*width/(Math.pow(ex2-ex1,2)+twoW2);
							}
						}
						lorplot[epos]=(float)tmp;
					}
					/* + Gaussian */
					width=fen.curve.get(i-1).getbroad().getgaussw()/Math.sqrt(2.0*Math.log(2));
					if (width!=0)
					{
						fact=1/(Math.sqrt(2*Math.PI)*width);
						twoW2=1/(2*Math.pow(width,2));
						for (int epos = 0; epos < resolution; epos++)
						{
							ex1=estart+egap*(float)epos/(float)resolution;
							tmp=0;
							for (int epos2 = 0; epos2 < resolution; epos2++)
							{
								ex2=estart+egap*(float)epos2/(float)resolution;
								tmp=tmp+lorplot[epos2]*fact*Math.exp(-Math.pow(ex2-ex1,2)*twoW2)*egap/resolution;
							}
							fen.plot.plotlist.get(i)[epos]=(float)tmp;
							agap=Math.max(fen.plot.plotlist.get(i)[epos], agap);
							astart=Math.min(fen.plot.plotlist.get(i)[epos], astart);
						}
					}
					else
					{
						for (int epos = 0; epos < resolution; epos++)
						{
							fen.plot.plotlist.get(i)[epos]=lorplot[epos];
							agap=Math.max(fen.plot.plotlist.get(i)[epos], agap);
							astart=Math.min(fen.plot.plotlist.get(i)[epos], astart);
						}
					}
				}
				/* Gaussian only */
				else
				{
					width=fen.curve.get(i-1).getbroad().getgaussw()/Math.sqrt(2.0*Math.log(2));
					if (width!=0)
					{
						fact=1/(Math.sqrt(2*Math.PI)*width);
						twoW2=1/(2*Math.pow(width,2));
						for (int epos = 0; epos < resolution; epos++)
						{
							ex1=estart+egap*(float)epos/(float)resolution;
							tmp=0;
							for (itrans = 0; itrans < ntrans; itrans++)
							{
								ex2=translist[0][itrans];
								tmp=tmp+translist[1][itrans]*fact*Math.exp(-Math.pow(ex2-ex1,2)*twoW2);
							}
							fen.plot.plotlist.get(i)[epos]=(float)tmp;
							agap=Math.max(fen.plot.plotlist.get(i)[epos], agap);
							astart=Math.min(fen.plot.plotlist.get(i)[epos], astart);
						}
					}
					else
					{
						for (int epos = 0; epos < resolution; epos++)
						{
							fen.plot.plotlist.get(i)[epos]=0; // Temporary
						}
					}
				}
				break;
			case 3:
				try {
					BufferedReader reader = new BufferedReader(new FileReader(fen.curve.get(i-1).transition.getfile()));
					float e1=estart;
					float a1=0;
					float e2=0;
					float a2=0;
					float ex;
					int epos=0;
					float[] lorplot=new float[resolution];
					while ((text = reader.readLine()) != null) {
						if (!text.startsWith("#"))
						{
							// This assumes ordered energy list
							e2=Float.parseFloat(text.trim().split(" +")[0])*unitfactor+xoffset;
							a2=Float.parseFloat(text.trim().split(" +")[1])*yscale;
							ex=estart+egap*(float)epos/(float)resolution;
							while((e2>ex)&&(epos<resolution))
							{
								//fen.plot.plotlist.get(i)[epos]=a1+(a2-a1)/(e2-e1)*(ex-e1);
								lorplot[epos]=a1+(a2-a1)/(e2-e1)*(ex-e1);
								//agap=Math.max(fen.plot.plotlist.get(i)[epos], agap);
								//astart=Math.min(fen.plot.plotlist.get(i)[epos], astart);
							 	epos=epos+1;
							 	ex=estart+egap*(float)epos/(float)resolution;
							}
							e1=e2;
							a1=a2;
						}
					}
					e2=estart+egap;
					a2=0;
				 	ex=estart+egap*(float)epos/(float)resolution;
				 	while(epos<resolution)
				 	{
				 		//fen.plot.plotlist.get(i)[epos]=a1+(a2-a1)/(e2-e1)*(ex-e1);
				 		lorplot[epos]=a1+(a2-a1)/(e2-e1)*(ex-e1);
				 		epos=epos+1;
				 		ex=estart+egap*(float)epos/(float)resolution;
				 	}
				 	reader.close();
				 	//Smoothing, this is a copy of the Gaussian convolution above
					width=fen.curve.get(i-1).getbroad().getgaussw()/Math.sqrt(2.0*Math.log(2));
					if (width!=0)
					{
						fact=1/(Math.sqrt(2*Math.PI)*width);
						twoW2=1/(2*Math.pow(width,2));
						for (epos = 0; epos < resolution; epos++)
						{
							ex1=estart+egap*(float)epos/(float)resolution;
							tmp=0;
							for (int epos2 = 0; epos2 < resolution; epos2++)
							{
								ex2=estart+egap*(float)epos2/(float)resolution;
								tmp=tmp+lorplot[epos2]*fact*Math.exp(-Math.pow(ex2-ex1,2)*twoW2)*egap/resolution;
							}
							fen.plot.plotlist.get(i)[epos]=(float)tmp;
							agap=Math.max(fen.plot.plotlist.get(i)[epos], agap);
							astart=Math.min(fen.plot.plotlist.get(i)[epos], astart);
						}
					}
					else
					{
						for (epos = 0; epos < resolution; epos++)
						{
							fen.plot.plotlist.get(i)[epos]=lorplot[epos];
							agap=Math.max(fen.plot.plotlist.get(i)[epos], agap);
							astart=Math.min(fen.plot.plotlist.get(i)[epos], astart);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph", "Error",JOptionPane.ERROR_MESSAGE);
				}
				catch (ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph, please check your input", "Error",JOptionPane.ERROR_MESSAGE);
				}
				catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(new JFrame(), "Failed to plot graph", "Error",JOptionPane.ERROR_MESSAGE);
				}
				break;
			}
			agap=agap-astart;
			fen.plot.mina=Math.min(astart, fen.plot.mina);
			fen.plot.maxagap=Math.max(agap, fen.plot.maxagap);
			fen.curve.get(i-1).mina=astart;
			fen.curve.get(i-1).maxagap=agap;
		}
	}
	/* *********************************** */
	/* *******    Get and set's    ******* */
	/* *********************************** */
	
	public static float getintens(Plotgraph plot,int i,float e1,float e2){
		float estart=plot.getx1();
		float egap=plot.getxde();
		int resolution=plot.resolution;
		int istart=(int) ((e1-estart)/egap*(float)resolution);
		istart=Math.max(istart,0);
		istart=Math.min(istart,resolution);
		int iend=(int) ((e2-estart)/egap*(float)resolution);
		iend=Math.max(iend,0);
		iend=Math.min(iend,resolution);
		float norm=0;
		float resol=egap/(float) resolution;
		for(int epos = istart; epos < iend; epos++)
		{
			norm+=plot.plotlist.get(i+1)[epos]*resol;
		}
		return norm;
	}
	/* *********************************** */
	/* *******       Utils         ******* */
	/* *********************************** */
	public static double unitfactor(int dunit)
	{
		double unitfactor=1;
		switch (dunit)
		{
			case 1:
				unitfactor=27.211399;
				break;
			case 2:
				unitfactor=627.50961;
				break;
			case 3:
				unitfactor=2625.5;
				break;
			case 4:
				unitfactor=219474.63;
				break;
				// Not supported yet (need E-dependent broadening)
			/*case 5:
				Escale=1241.25/27.211399;
				break;
				*/
		}
		return unitfactor;
	}
}
