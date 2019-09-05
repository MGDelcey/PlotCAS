package plotCAS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

public class Scatterplot extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JPanel plotscreen = new JPanel();
    private JPanel optionscreen = new JPanel();
    private JPanel container = new JPanel();
    Plotgraph plot;
    private float[][] i1i2plane;
    private float[][] scatterplane;
    private float lorentzx,lorentzx2,lorentzxE12,lorentzy,gauss;
    private float xresol,yresol;
    private JTextField lxfield,lxfield2,lxfield3, lyfield, gfield, extractfield;
    private int xres, yres,lorsplit;
    private float e1i,e1t;
    private JComboBox<String> extractsel;
    private Window fen;
    private Curve curve;
    private boolean isdual,isbefore;
    
	public Scatterplot(Window dfen,Curve dcurve,float de1i,float e2i,float de1t,float e2t, int dxres,int dyres, boolean dbefore, int unit)
	{
		//JOptionPane.showMessageDialog(new JFrame(), "Experimental feature", "Warning",JOptionPane.WARNING_MESSAGE);
		
		xres=dxres;
		yres=dyres;
		e1i=de1i;
		e1t=de1t;
		fen=dfen;
		curve=dcurve;
		isbefore=dbefore;
		/* ********************************* */
		/* ******* Create the window ******* */
		/* ********************************* */
		this.setTitle("Scattering plot");
		this.setSize(800, 500);
	    this.setLocationRelativeTo(null);
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    
	    
	    container.setLayout(new BorderLayout());
	    optionscreen.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.black));
	    optionscreen.setLayout(new BoxLayout(optionscreen, BoxLayout.PAGE_AXIS));
	    JScrollPane optionpanel = new JScrollPane(optionscreen);
	    
	    plotscreen.setLayout(new BorderLayout());
	    
	    
	    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,plotscreen, optionpanel);
	    splitPane.setResizeWeight(0.7);
	    container.add(splitPane);
	    
	    this.setContentPane(container);	    
	    this.setVisible(true);
	    
	    plot= new Plotgraph(xres,yres,e1i,e2i,e1t,e2t,unit);
	    plotscreen.add(plot,BorderLayout.CENTER);
	    
		/* ************************************ */
		/* ******* Compute the spectrum ******* */
		/* ************************************ */
	    
		Transition trans=curve.transition;
	    i1i2plane=trans.scatterplane(e1i,e2i,e1t,e2t,xres,yres,unit,isbefore); // Product intensity
	    
	    // only works for single Lorentzian broadening
	    lorentzx=curve.getbroad().getlorw1();
	    lorentzx2=curve.getbroad().getlorw2();
	    lorentzxE12=curve.getbroad().getlorsplit();
	    isdual=curve.getbroad().isduallorentz();
	    lorentzy=0;// as a guess, no broadening
	    gauss=curve.getbroad().getgaussw();
	    if (lorentzx==0) { lorentzx=(float) 0.1;}
	    
	    xresol=xres/(e2i-e1i);
	    yresol=yres/(e2t-e1t);
	    
	    lorsplit=xres;
	    if (isdual) {lorsplit=Math.min((int) ((lorentzxE12-e1i)*xresol),xres);}
	    
	    scatterplane=Broad2D(i1i2plane,xres,yres,e1i,e1t);
	   
	    /* Print it */
	    plot.plotlist.add(scatterplane);
	    plot.nplot++;
	    plot.repaint();
	    
		/* ******************************** */
		/* *******     Options      ******* */
		/* ******************************** */
	    /* Broadenings  */

	    optionscreen.add(new JLabel("Broadenings"));
	    JPanel l1 = new JPanel();
		l1.add(new JLabel("Incident Lorentzian (HWHM):"));
		lxfield  = new JTextField(String.valueOf(lorentzx));
		l1.add(lxfield);
		optionscreen.add(l1);
		
		if (isdual)
		{
			JPanel l1_2 = new JPanel();
			l1_2.add(new JLabel("Second Lorentzian (HWHM):"));
			lxfield2  = new JTextField(String.valueOf(lorentzx2));
			l1_2.add(lxfield2);
			optionscreen.add(l1_2);
		
			JPanel l1_3 = new JPanel();
			l1_3.add(new JLabel("Lorentzian split energy:"));
			lxfield3  = new JTextField(String.valueOf(lorentzxE12));
			l1_3.add(lxfield3);
			optionscreen.add(l1_3);
		}
		
	    JPanel l2 = new JPanel();
		l2.add(new JLabel("Transfer Lorentzian (HWHM):"));
		lyfield  = new JTextField(String.valueOf(lorentzy));
		l2.add(lyfield);
		optionscreen.add(l2);
		
	    JPanel l3 = new JPanel();
		l3.add(new JLabel("Gaussian (HWHM):"));
		gfield  = new JTextField(String.valueOf(gauss));
		l3.add(gfield);
		optionscreen.add(l3);
		
		JButton redrawbutton=new JButton("Redraw");
		redrawbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				lorentzx=Float.parseFloat(lxfield.getText());
				if (isdual)
				{
					lorentzx2=Float.parseFloat(lxfield2.getText());
					lorentzxE12=Float.parseFloat(lxfield3.getText());
					lorsplit=Math.min((int) ((lorentzxE12-e1i)*xresol),xres);
				}
				lorentzy=Float.parseFloat(lyfield.getText());
				gauss=Float.parseFloat(gfield.getText());
				scatterplane=Broad2D(i1i2plane,xres,yres,e1i,e1t);
				plot.plotlist.set(0,scatterplane);
				plot.repaint();
			}
		});
		optionscreen.add(redrawbutton);

		/* Extract 2D curves  */
		optionscreen.add(new JLabel("Extract graph"));
		
		JPanel l4 = new JPanel();
		extractsel=new JComboBox<String>();
		extractsel.addItem("Horizontal cut");
		extractsel.addItem("Vertical cut");
		extractsel.addItem("Vertical Integration");
		l4.add(extractsel);
		optionscreen.add(l4);
		
		JPanel l5 = new JPanel();
		l5.add(new JLabel("Cut energy:"));
		extractfield  = new JTextField("0.00");
		l5.add(extractfield);
		optionscreen.add(l5);
		
		/* Extract cuts */
		JButton extractbutton=new JButton("Extract");
		extractbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int imod = extractsel.getSelectedIndex();
				float ecut=lorentzx=Float.parseFloat(extractfield.getText());
				int icut;
				int n=1;
				float [] result=null;
				float [] eaxis=null;
				String namecurve=curve.getname();
				switch (imod)
				{
				case 0:
					icut=(int) ((ecut-e1t)*yresol);
					result=new float[xres];
					eaxis=new float[xres];
					n=xres;
					for (int i = 0; i < xres; i++) {
						result[i]=scatterplane[i][icut];
						eaxis[i]=i/xresol+e1i;
					}
					namecurve=namecurve+" ycut = "+String.valueOf(ecut);
					break;
				case 1:
					icut=(int) ((ecut-e1i)*xresol);
					result=new float[yres];
					eaxis=new float[yres];
					n=yres;
					for (int i = 0; i < yres; i++) {
						result[i]=scatterplane[icut][i];
						eaxis[i]=i/yresol+e1t;
					}
					namecurve=namecurve+" xcut = "+String.valueOf(ecut);
					break;
				case 2:
					result=new float[xres];
					eaxis=new float[xres];
					float tmp;
					n=xres;
					for (int i = 0; i < xres; i++) {
						tmp=0;
						for (int j = 0; j < yres; j++) {
							tmp+=scatterplane[i][j];
						}
						result[i]=tmp/yresol;
						eaxis[i]=i/xresol+e1i;
					}
					namecurve=namecurve+" fluo. yield";
					break;
				}
				// Write to file
				Transition trans=new Transition(fen);
				File output=trans.getfile();
				try {
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"));
					for(int i = 0; i <n; i++) {writer.write(String.valueOf(eaxis[i])+" "+String.valueOf(result[i])+"\n");}
					writer.close();
	        			fen.addcurve(namecurve,3,trans,"",plot.getunit());
				}
				catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(new JFrame(), "Failed to create extracted curve", "Error",JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		optionscreen.add(extractbutton);
		
		/* Export curves */
		JButton exportbutton=new JButton("Export xyz plane");
		exportbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			  try {
				String filename = JOptionPane.showInputDialog("Name this file");
				JFileChooser fcPick = new JFileChooser();
				fcPick.setSelectedFile(new File(filename));
				int sf = fcPick.showSaveDialog(null);
		        if(sf == JFileChooser.APPROVE_OPTION){
		    	  		BufferedWriter writer = new BufferedWriter(new FileWriter(fcPick.getSelectedFile()));
		    	  		float e1,e2;
		    	  		for (int i=0; i<xres; i++) {
		    	  			e1=i/xresol+e1i;;
		    	  			for (int j=0; j<yres; j++) {
		    	  				e2=j/yresol+e1t;;
		    	  				writer.write(String.format("%f", e1)+"   "+String.format("%f", e2)+"   "+String.format("%6.3e",scatterplane[i][j])+"\n");
		    	  			}
		    	  		}
			    	  	writer.close();
		    	  	}
			  }
			  catch (IOException e) {
				  e.printStackTrace();
				  JOptionPane.showMessageDialog(new JFrame(), "Failed to create xyz file", "Error",JOptionPane.ERROR_MESSAGE);
				  }
		    	  	
			}
		});
		optionscreen.add(exportbutton);
		
		optionscreen.revalidate();
		optionscreen.repaint();
	    
	}
	
	public float[][] Broad2D(float[][] i1i2plane, int xres, int yres, float e1i, float e1t)
	{
		float [][] result=new float[xres][yres];
	    float[] tmpvec=new float[Math.max(xres,yres)];
	    float [] B,B2;
	    int span,span2;
	    int nfinal=i1i2plane[0].length;
	    
	    /* x-axis Lorentzian-ish broadening */
	    float tmp=0;
	    for (int i = 0; i < xres; i++) {for (int j = 0; j < yres; j++) {result[i][j]=0;}}
	    
	    int istart=0;
	    int iend=1;
	    if (isbefore) { istart=4; iend=4;}
	    
	    for (int imod = istart; imod <= iend; imod++) // Real and imaginary parts
	    {
		    B=broadvec(lorentzx,imod,xresol);
		    B2=broadvec(lorentzx2,imod,xresol);
		    span=B.length;
		    span2=B2.length;
		    float sign=1;
		    if (imod==0) sign=-1;
	    		for (int i2 = 0; i2 < nfinal; i2++)
	    		{
	    			int i=(int) ((i1i2plane[xres][i2]-e1t)*yresol);
	    			if (i<0 || i>=yres) { continue;}
	    			for (int j = 0; j < xres; j++) {tmpvec[j]=0;}
	    			for (int j = 0; j < lorsplit; j++)
	    			{
	    				tmp=i1i2plane[j][i2];
	    				for (int k = Math.max(0,j-span+1); k < j; k++)
	    				{
	    					tmpvec[k]+=sign*tmp*B[j-k];
	    				}
	    				for (int k = j; k < Math.min(j+span, xres); k++)
	    				{
	    					tmpvec[k]+=tmp*B[k-j];
	    				}
	    			}
	    			for (int j = lorsplit; j < xres; j++)
	    			{
	    				tmp=i1i2plane[j][i2];
	    				for (int k = Math.max(0,j-span2+1); k < j; k++)
	    				{
	    					tmpvec[k]+=sign*tmp*B2[j-k];
	    				}
	    				for (int k = j; k < Math.min(j+span2, xres); k++)
	    				{
	    					tmpvec[k]+=tmp*B2[k-j];
	    				}
	    			}
	    			if (isbefore) {
	    				for (int j = 0; j < xres; j++)
		    			{
		    				result[j][i]+=tmpvec[j];
		    			}
	    			}
	    			else
	    			{
	    				for (int j = 0; j < xres; j++)
		    			{
		    				result[j][i]+=tmpvec[j]*tmpvec[j];
		    			}
	    			}
	    			
	    		}
		}
	    
		/* x-axis Gaussian broadening */
	    B=broadvec(gauss,3,xresol);
	    span=B.length;
		for (int i = 0; i < yres; i++)
		{
			for (int j = 0; j < xres; j++) {tmpvec[j]=0;}
			for (int j = 0; j < xres; j++)
			{
				tmp=result[j][i];
				for (int k = Math.max(0,j-span+1); k < j; k++)
				{
					tmpvec[k]+=tmp*B[j-k];
				}
				for (int k = j; k <= Math.min(j+span-1, xres-1); k++)
				{
					tmpvec[k]+=tmp*B[k-j];
				}
			}
			for (int j = 0; j < xres; j++)
			{
				result[j][i]=tmpvec[j];
			}
		}
		
		
		/* y-axis Lorentzian broadening */
	    B=broadvec(lorentzy,2,yresol);
	    span=B.length;
	    
		for (int i = 0; i < xres; i++)
		{
			float wk=i/xresol+e1i;
			for (int j = 0; j < yres; j++) {tmpvec[j]=0;}
			for (int j = 0; j < yres; j++)
			{
				tmp=result[i][j];
				for (int k = Math.max(0,j-span+1); k < j; k++)
				{
					tmpvec[k]+=tmp*B[j-k];
				}
				for (int k = j; k <= Math.min(j+span-1, yres-1); k++)
				{
					tmpvec[k]+=tmp*B[k-j];
				}
			}
			for (int j = 0; j < yres; j++)
			{
				float wkp=j/yresol+e1t;
				wkp=wk-wkp;
				result[i][j]=Math.abs(wkp/wk)*tmpvec[j];
			}
		}
		
		/* y-axis Gaussian broadening */
	    B=broadvec(gauss,3,yresol);
	    span=B.length;
	    
		for (int i = 0; i < xres; i++)
		{
			for (int j = 0; j < yres; j++) {tmpvec[j]=0;}
			for (int j = 0; j < yres; j++)
			{
				tmp=result[i][j];
				for (int k = Math.max(0,j-span+1); k < j; k++)
				{
					tmpvec[k]+=tmp*B[j-k];
				}
				for (int k = j; k <= Math.min(j+span-1, yres-1); k++)
				{
					tmpvec[k]+=tmp*B[k-j];
				}
			}
			for (int j = 0; j < yres; j++)
			{
				result[i][j]=tmpvec[j];
			}
		}
		
		
		/* Weird code for diagonal broadening */
		/* xy-axis Gaussian broadening */
		/*int res1=yres, res2=xres;
		if (xresol>yresol)
		{
			res1=xres;
			res2=yres;
			System.out.print("Bottom right first ");
			System.out.print(res1);
			System.out.print(" ");
			System.out.print(res2);
			System.out.print("\n");
		}
		else
		{
			System.out.print("Upper left first");
			System.out.print(res1);
			System.out.print(" ");
			System.out.print(res2);
			System.out.print("\n");
		}
		float xyresol=(float) (Math.max(xresol, yresol)*Math.sqrt(2));
	   
	    float slope=Math.min(xresol, yresol)/Math.max(xresol, yresol);
	    int [] shift=new int[res1];
	    for (int i2 = 0; i2 < res1; i2++) {shift[i2]=(int) (i2*slope);}
	    
	    B=broadvec(gauss,3,xyresol);
	    span=B.length;*/
	    /* Upper-left (or lower-right) triangle */
		/*for (int i = 0; i < res1; i++)
		{
			if (i>0) { if (shift[i]==shift[i-1]) { continue;}}
			for (int i2 = i; i2 < res1; i2++) {tmpvec[i2]=0;}
			for (int i2 = i; i2 < res1; i2++)
			{
				int j=shift[i2]-shift[i];
				if (j>=res2) { break;}
				if (xresol>yresol) { tmp=result[i2][j];}
				else { tmp=result[j][i2]; }
				
				for (int k = Math.max(i,i2-span+1); k < i2; k++)
				{
					tmpvec[k]+=tmp*B[i2-k];
				}
				for (int k = i2; k <= Math.min(i2+span-1, res1-1); k++)
				{
					tmpvec[k]+=tmp*B[k-i2];
				}
			}
			for (int i2 = i; i2 < res1; i2++)
			{
				int j=shift[i2]-shift[i];
				if (j>=res2) { break;}
				if (xresol>yresol) { result[i2][j]=tmpvec[i2]; }
				else {result[j][i2]=tmpvec[i2];  }
			}
		}*/
		 /* Lower-right (or upper-left) triangle */
		/*for (int j2 = 1; j2 < res2; j2++)
		{
			for (int i = 0; i < res1; i++) {tmpvec[i]=0;}
			for (int i = 0; i < res1; i++)
			{
				int j=j2+shift[i];
				if (j>=res2) { break;}
				if (xresol>yresol) { tmp=result[i][j]; }
				else { tmp=result[j][i]; }
				
				for (int k = Math.max(0,i-span+1); k < i; k++)
				{
					tmpvec[k]+=tmp*B[i-k];
				}
				for (int k = i; k <= Math.min(i+span-1, res1-1); k++)
				{
					tmpvec[k]+=tmp*B[k-i];
				}
			}
			for (int i = 0; i < res1; i++)
			{
				int j=j2+shift[i];
				if (j>=res2) { break;}
				if (xresol>yresol) { result[i][j]=tmpvec[i]; }
				else {result[j][i]=tmpvec[i];  }	
			}
		}*/
	    //result=i1i2plane; //For testing purposes
		
		// Normalize
		float max=0;
		for(int i = 0; i < xres; i++)
		{
			for(int j = 0; j < yres; j++)
			{
				max=Math.max(result[i][j],max);
			}
		}
		for(int i = 0; i < xres; i++)
		{
			for(int j = 0; j < yres; j++)
			{
				result[i][j]=result[i][j]/max;
			}
		}
		//
		return result;
	}
	/* Broadening Vector */
	public float[] broadvec(float broadening, int mode,float resol)
	{
		float [] result;
		double tol=0.01;
		int nx;
		float span=1;
		float fact=1;
		//float broadening=dbroadening;
		if (mode==3) { broadening=(float) (broadening/Math.sqrt(2.0*Math.log(2)));}
		float b2=broadening*broadening;
		float de,de2;
		if (broadening<=0)
		{
			result=new float[1];
			result[0]=resol;
			return result;
		}
		switch (mode)
		{
			case 0:
			case 1:
				span=(float) Math.sqrt((1-tol)/tol)*broadening;
				break;
			case 2:
				fact=(float) (1/(Math.PI));
				span=(float) Math.sqrt((1-tol)/tol)*broadening;
				break;
			case 3:
				fact=(float) (1/(Math.sqrt(2*Math.PI)*broadening))/resol; // /resol is the integrator for convolution
				span=(float) Math.sqrt(-Math.log(tol)*2*b2);
			case 4:
				span=(float) Math.sqrt((1-tol)/tol)*broadening;
				break;	
		}
		nx=(int) (span*resol)+1;
		result=new float[nx];
		for (int i = 0; i < nx; i++)
		{
			de=(i/resol);
			de2=de*de;
			switch (mode)
			{
			case 0:
				result[i]=de/(b2+de2);
				break;
			case 1:
			case 2:
				result[i]=fact*broadening/(b2+de2);
				break;
			case 3:
				result[i]=fact*(float)Math.exp(-de2/(2*b2));
				break;
			case 4:
				result[i]=1/(b2+de2);
				break;
			}
		}
		return result;
	}
}
