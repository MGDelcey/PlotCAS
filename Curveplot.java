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
		int emax=0;
		if (i>fen.plot.nplot)
		{
			fen.plot.plotlist.add(new float[resolution][1]);
			fen.plot.nplot++;
		}
		if (i==0)
		{
			for(int epos = 0; epos < resolution; epos++)
			{
				fen.plot.plotlist.get(i)[epos][0]=(float)epos/(float)resolution;
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
					if (egap/(float)resolution>width)
					{
						  Thread t = new Thread(new Runnable(){
						        public void run(){
									JOptionPane.showMessageDialog(new JFrame(),
										    "Graph resolution may be too low for the broadening used.\n"
										    + " increase the number of curve points.",
										    "Warning",
										    JOptionPane.WARNING_MESSAGE);
						        }
						    });
						  t.start();
					}
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
							fen.plot.plotlist.get(i)[epos][0]=(float)tmp;
							if (fen.plot.plotlist.get(i)[epos][0]> agap)
							{
								agap=fen.plot.plotlist.get(i)[epos][0];
								emax=epos;
							}
							astart=Math.min(fen.plot.plotlist.get(i)[epos][0], astart);
						}
					}
					else
					{
						for (int epos = 0; epos < resolution; epos++)
						{
							fen.plot.plotlist.get(i)[epos][0]=lorplot[epos];
							if (fen.plot.plotlist.get(i)[epos][0]> agap)
							{
								agap=fen.plot.plotlist.get(i)[epos][0];
								emax=epos;
							}
							astart=Math.min(fen.plot.plotlist.get(i)[epos][0], astart);
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
							fen.plot.plotlist.get(i)[epos][0]=(float)tmp;
							if (fen.plot.plotlist.get(i)[epos][0]> agap)
							{
								agap=fen.plot.plotlist.get(i)[epos][0];
								emax=epos;
							}
							astart=Math.min(fen.plot.plotlist.get(i)[epos][0], astart);
						}
					}
					else
					{
						for (int epos = 0; epos < resolution; epos++)
						{
							fen.plot.plotlist.get(i)[epos][0]=0; // Temporary
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
							fen.plot.plotlist.get(i)[epos][0]=(float)tmp;
							if (fen.plot.plotlist.get(i)[epos][0]> agap)
							{
								agap=fen.plot.plotlist.get(i)[epos][0];
								emax=epos;
							}
							astart=Math.min(fen.plot.plotlist.get(i)[epos][0], astart);
						}
					}
					else
					{
						for (epos = 0; epos < resolution; epos++)
						{
							fen.plot.plotlist.get(i)[epos][0]=lorplot[epos];
							if (fen.plot.plotlist.get(i)[epos][0]> agap)
							{
								agap=fen.plot.plotlist.get(i)[epos][0];
								emax=epos;
							}
							astart=Math.min(fen.plot.plotlist.get(i)[epos][0], astart);
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
			fen.curve.get(i-1).emax=estart+emax*egap/(float) resolution;
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
			norm+=plot.plotlist.get(i+1)[epos][0]*resol;
		}
		return norm;
	}
	public static float halfintensity(Plotgraph plot,int i,float e1,float e2){
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
		float position=estart;
		for(int epos = istart; epos < iend; epos++)
		{
			norm+=plot.plotlist.get(i+1)[epos][0]*resol;
		}
		float totala=norm;
		norm=0;
		for(int epos = istart; epos < iend; epos++)
		{
			norm+=plot.plotlist.get(i+1)[epos][0]*resol;
			if (norm>(0.5*totala))
			{
				position=estart+epos*resol;
				break;
			}
		}
		return position;
	}
	public static String similarity(Plotgraph plot,int i1,int i2,int imethod,float width, int align){
		int resolution=plot.resolution;
		//float estart=plot.getx1();
		float egap=plot.getxde();
		String result="";
		//double resol=(double) egap/(double) resolution;
		double norm=0, norm1=0, norm2=0;
		int epos2;
		int shift=0; int center=0; int gap;
		double [][] matrix = new double [3][3]; 
		double [] array = new double [3];
		gap=resolution/10;
		for (int iter = 0; iter <= 10*align ; iter++ )
		{
			for (int ishift = -align; ishift <= align ; ishift++ )
			{
				shift=center+ishift*gap;
				matrix[ishift+1][2]=1;
				matrix[ishift+1][1]=shift;
				matrix[ishift+1][0]=shift*shift;
				switch (imethod)
				{
					case 0: // Euclidian similarity
						norm=0;
						for (int epos = 0; epos < resolution; epos++)
						{
							epos2=Math.max(Math.min(epos-shift,resolution-1),0);
							norm+=Math.pow((double) plot.plotlist.get(i1+1)[epos][0]-plot.plotlist.get(i2+1)[epos2][0],2);
						}
						norm=Math.sqrt(norm/(double) resolution)*(double) egap;
						array[ishift+1]=-norm;
						result="Euclidian distance between the two curves : "+String.valueOf(norm);
						break;
					case 1: // Cosine similarity
						double product=0;
						norm1=0; norm2=0;
						for (int epos = 0; epos < resolution; epos++)
						{
							epos2=Math.max(Math.min(epos-shift,resolution-1),0);
							product+=plot.plotlist.get(i1+1)[epos][0]*plot.plotlist.get(i2+1)[epos2][0];
							norm1+=plot.plotlist.get(i1+1)[epos][0]*plot.plotlist.get(i1+1)[epos][0];
							norm2+=plot.plotlist.get(i2+1)[epos2][0]*plot.plotlist.get(i2+1)[epos2][0];
						}

						product=product/Math.sqrt(norm1*norm2);
						array[ishift+1]=product;
						result="Cosine similarity between the two curves : "+String.valueOf(product);
						break;
					case 2: // Gaussian-weighted correlation function
						float egap2=egap/(float)resolution;
						float width2=(float) (width/Math.sqrt(2.0*Math.log(2)));
						float w2=width2*width2;
						double tol=0.01;
						float span=(float) Math.sqrt(-Math.log(tol)*2*w2);
						int ispan=(int) (span/egap2);
						norm=0;
						norm1=0; norm2=0;
						for (int wpos = -ispan; wpos <= ispan; wpos++)
						{
							double tmp=0, tmp1=0, tmp2=0;
							float de2=wpos*egap2;
							de2=de2*de2;
							float fact=1;
							if (w2>0.0001)
							{
								fact=(float) Math.exp(-de2/(2*w2));
							}	// If width is 0, span is 0, so only one point of weight 1.
					
							for (int epos = Math.max(-wpos, 0); epos < Math.min(resolution-wpos, resolution); epos++)
							{
								epos2=Math.max(Math.min(epos-shift,Math.min(resolution-wpos, resolution)-1),Math.max(-wpos, 0));
								tmp+= plot.plotlist.get(i1+1)[epos][0]*plot.plotlist.get(i2+1)[epos2+wpos][0];
								tmp1+=plot.plotlist.get(i1+1)[epos][0]*plot.plotlist.get(i1+1)[epos+wpos][0];
								tmp2+=plot.plotlist.get(i2+1)[epos2][0]*plot.plotlist.get(i2+1)[epos2+wpos][0];
							}
							norm=norm+tmp*fact;
							norm1=norm1+tmp1*fact;
							norm2=norm2+tmp2*fact;
						}
						norm=norm/Math.sqrt(norm1*norm2);
						array[ishift+1]=norm;
						result="Gaussian-weighted correlation function (HWHM ="+String.valueOf(width)+")\n between the two curves : "+String.valueOf(norm);
						//result="Correlation function between the two curves : "+String.valueOf(norm);
						break;
				}
			}/* shift loop */
			if (align>0)
			{
				double[] x=lsolve(matrix,array);
				if (x[0]<0)
				{
					double tmp=-x[1]/(2*x[0])-center;
					if (Math.abs(tmp)<gap)
					{
						center+=(int) Math.round(tmp);
						gap=gap/3;
					}
					else
					{
						center+=((int) Math.signum(tmp))*gap;
					}
				}
				else
				{
					double gradient=(array[2]-array[0]);
					center+=((int) Math.signum(gradient))*gap;
				}
				float resol=egap/(float) resolution;
				float position=center*resol;
				result=String.valueOf(position);
				if (gap<1) 
				{
					break;
				}
			}
			
		}/* Iter loop */
		return result;
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
	public static double[] lsolve(double[][] A, double[] b) {
		int n = b.length;
	    double EPSILON = 1e-10;
	    for (int p = 0; p < n; p++) {
	    		// find pivot row and swap
	    		int max = p;
	        for (int i = p + 1; i < n; i++) {
	        		if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
	        			max = i;
	             }
	        }
	        double[] temp = A[p]; A[p] = A[max]; A[max] = temp;
	        double   t    = b[p]; b[p] = b[max]; b[max] = t;
	        // singular or nearly singular
	        if (Math.abs(A[p][p]) <= EPSILON) {
	        		throw new ArithmeticException("Matrix is singular or nearly singular");
	         }

	         // pivot within A and b
	         for (int i = p + 1; i < n; i++) {
	        	 	double alpha = A[i][p] / A[p][p];
	            b[i] -= alpha * b[p];
	            for (int j = p; j < n; j++) {
	            		A[i][j] -= alpha * A[p][j];
	            }
	        }
	    }
	    // back substitution
	    double[] x = new double[n];
	    for (int i = n - 1; i >= 0; i--) {
	    		double sum = 0.0;
	        for (int j = i + 1; j < n; j++) {
	        		sum += A[i][j] * x[j];
	        }
	        x[i] = (b[i] - sum) / A[i][i];
	    }
	    return x;
	}
}
