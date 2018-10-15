package plotCAS;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class Scatterplot extends JFrame {
	
    private JPanel plotscreen = new JPanel();
    private JPanel optionscreen = new JPanel();
    private JPanel container = new JPanel();
    Plotgraph plot;
    private float[][] i1i2plane;
    private float[][] scatterplane;
    private float lorentz1;
    private float xresol,yresol;
    
	public Scatterplot(Curve curve,float e1i,float e2i,float e1t,float e2t, int xres,int yres, int unit)
	{
		/*    Create the window   */
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
	    
	    /*   Compute the spectrum   */
	    
		Transition trans=curve.transition;
	    i1i2plane=trans.scatterplane(e1i,e2i,e1t,e2t,xres,yres,unit); // Product intensity
	    
	    // only works for single Lorentzian broadening
	    lorentz1=curve.getbroad().getlorw1();
	    if (lorentz1==0) { lorentz1=(float) 0.1;}
	    
	    xresol=xres/(e2i-e1i);
	    yresol=yres/(e2t-e1t);
	    
	    scatterplane=Broad2D(i1i2plane,xres,yres,e1i,e1t);
	   
	    /* Print it */
	    plot.plotlist.add(scatterplane);
	    plot.nplot++;
	    plot.repaint();
	}
	
	public float[][] Broad2D(float[][] i1i2plane, int xres, int yres, float e1i, float e1t)
	{
		float [][] result=new float[xres][yres];
	    float[][] tmpplane=new float[xres][yres];
	    float [] B;
	    int span;
	    
	    /* x-axis broadening */
	    float tmp=0;
	    for (int i = 0; i < xres; i++) {for (int j = 0; j < yres; j++) {result[i][j]=0;}}
	    
	    for (int imod = 0; imod <= 1; imod++) // Real and imaginary parts
	    {
		    B=broadvec(lorentz1,imod);
		    span=B.length;
	    		for (int i = 0; i < xres; i++) {for (int j = 0; j < yres; j++) {tmpplane[i][j]=0;}}
	    		for (int i = 0; i < yres; i++)
	    		{
	    			for (int j = 0; j < xres; j++)
	    			{
	    				tmp=i1i2plane[j][i];
	    				for (int k = Math.max(0,j-span+1); k < j; k++)
	    				{
	    					tmpplane[k][i]+=tmp*B[j-k];
	    				}
	    				for (int k = j; k < Math.min(j+span-1, xres); k++)
	    				{
	    					tmpplane[k][i]+=tmp*B[k-j];
	    				}
	    			}
	    			for (int j = 0; j < xres; j++)
	    			{
	    				result[j][i]+=tmpplane[j][i]*tmpplane[j][i];
	    			}
	    		}
		}
		
		
		/* y-axis Lorentzian broadening */
	    for (int i = 0; i < xres; i++) {for (int j = 0; j < yres; j++) {tmpplane[i][j]=0;}}
	    B=broadvec(lorentz1,1); // as a guess, same broadening
	    span=B.length;
	    
		for (int i = 0; i < xres; i++)
		{
			float wk=i/xresol+e1i;
			for (int j = 0; j < yres; j++)
			{
				tmp=result[i][j];
				for (int k = Math.max(0,j-span+1); k < j; k++)
				{
					tmpplane[i][k]+=tmp*B[j-k];
				}
				for (int k = j; k < Math.min(j+span-1, yres); k++)
				{
					tmpplane[i][k]+=tmp*B[k-j];
				}
			}
			for (int j = 0; j < yres; j++)
			{
				float wkp=j/yresol+e1t;
				wkp=wk-wkp;
				result[i][j]=wkp/wk*tmpplane[i][j]*tmpplane[i][j];
			}
		}
	    //result=i1i2plane; For testing purposes
		return result;
	}
	/* Broadening Vector */
	public float[] broadvec(float broadening, int mode)
	{
		float [] result;
		double tol=0.1;
		int nx;
		float span=1;
		float fact=1;
		switch (mode)
		{
			case 0:
			case 1:
				span=(float) Math.sqrt(1-tol)*broadening;
				break;
			case 2:
				fact=(float) (1/(Math.PI));
				span=(float) Math.sqrt(1-tol)*broadening;
				break;
		}
		nx=(int) (span*xresol)+1;
		result=new float[nx];
		
		float b2=broadening*broadening;
		float de,de2;
		for (int i = 0; i < nx; i++)
		{
			de=(i/span);
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
			}
		}
		return result;
	}
}
