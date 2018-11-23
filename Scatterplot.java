package plotCAS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
    private float lorentzx,lorentzy,gauss;
    private float xresol,yresol;
    private JTextField lxfield, lyfield, gfield, extractfield;
    private int xres, yres;
    private float e1i,e1t;
    private JComboBox<String> extractsel;
    private Window fen;
    private Curve curve;
    
	public Scatterplot(Window dfen,Curve dcurve,float de1i,float e2i,float de1t,float e2t, int dxres,int dyres, int unit)
	{
		JOptionPane.showMessageDialog(new JFrame(), "Experimental feature", "Warning",JOptionPane.WARNING_MESSAGE);
		
		xres=dxres;
		yres=dyres;
		e1i=de1i;
		e1t=de1t;
		fen=dfen;
		curve=dcurve;
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
	    i1i2plane=trans.scatterplane(e1i,e2i,e1t,e2t,xres,yres,unit); // Product intensity
	    
	    // only works for single Lorentzian broadening
	    lorentzx=curve.getbroad().getlorw1();
	    lorentzy=0;// as a guess, no broadening
	    gauss=curve.getbroad().getgaussw();
	    if (lorentzx==0) { lorentzx=(float) 0.1;}
	    
	    xresol=xres/(e2i-e1i);
	    yresol=yres/(e2t-e1t);
	    
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
		//extractsel.addItem("Integration");
		l4.add(extractsel);
		optionscreen.add(l4);
		
		JPanel l5 = new JPanel();
		l5.add(new JLabel("Cut energy:"));
		extractfield  = new JTextField("0.00");
		l5.add(extractfield);
		optionscreen.add(l5);
		
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
		
		optionscreen.revalidate();
		optionscreen.repaint();
	    
	}
	
	public float[][] Broad2D(float[][] i1i2plane, int xres, int yres, float e1i, float e1t)
	{
		float [][] result=new float[xres][yres];
	    float[] tmpvec=new float[Math.max(xres,yres)];
	    float [] B;
	    int span;
	    
	    /* x-axis broadening */
	    float tmp=0;
	    for (int i = 0; i < xres; i++) {for (int j = 0; j < yres; j++) {result[i][j]=0;}}
	    
	    for (int imod = 0; imod <= 1; imod++) // Real and imaginary parts
	    {
		    B=broadvec(lorentzx,imod);
		    span=B.length;
	    		for (int i = 0; i < yres; i++)
	    		{
	    			for (int j = 0; j < xres; j++) {tmpvec[j]=0;}
	    			for (int j = 0; j < xres; j++)
	    			{
	    				tmp=i1i2plane[j][i];
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
	    				result[j][i]+=tmpvec[j]*tmpvec[j];
	    			}
	    		}
		}
	    
		/* x-axis Gaussian broadening */
	    B=broadvec(gauss,3);
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
				result[j][i]+=tmpvec[j];
			}
		}
		
		
		/* y-axis Lorentzian broadening */
	    B=broadvec(lorentzy,1);
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
	    B=broadvec(gauss,3);
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
	    //result=i1i2plane; //For testing purposes
		return result;
	}
	/* Broadening Vector */
	public float[] broadvec(float broadening, int mode)
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
			result[0]=1;
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
				fact=(float) (1/(Math.sqrt(2*Math.PI)*broadening));
				span=(float) Math.sqrt(-Math.log(tol)*2*b2);
		}
		nx=(int) (span*xresol)+1;
		result=new float[nx];
		
		for (int i = 0; i < nx; i++)
		{
			de=(i/xresol);
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
			}
		}
		return result;
	}
}
