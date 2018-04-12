package plotCAS;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Plotgraph extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private float xgrad1=0;
	private float xscale=1;
	private float ygrad1=0;
	private float yscale=1;
	private Window fen;
	private int ncurve;
	
	public int resolution=300;
	public int nplot=-1;
	public ArrayList<float[]> plotlist = new ArrayList<float[]>();
	//public float maxAbs=0;
	private int unit=1;//eV

	/* *********************************** */
	/* ******* Draw the whole plot ******* */
	/* *********************************** */
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Dimension d=new Dimension();
		d=this.getSize();
		//Plotgraph(fen,g,d,false,1);
		update(fen,g,d,false,1);
	}
	public Plotgraph(Window tmp)
	{
		fen=tmp;
		ncurve=fen.ncurve;
	}
	public void update(Window tmp,Graphics g3, Dimension d,boolean isprint, int lwidth)
	{
		fen=tmp;
		ncurve=fen.ncurve;
		Graphics2D g = (Graphics2D) g3;
		int x1,y1;
		float lx,ly;
		x1=d.width/10;  // let 10% margin
		y1=d.height/10;
		lx=d.width/10*8;
		ly=d.height/10*8;
		
		
		g.setStroke(new BasicStroke(lwidth));
		
		/*   Print axis  */
		g.setColor(Color.black);
		g.drawLine(x1,y1-5,x1,y1+(int)ly);
		if (ygrad1<0)
		{
			g.drawLine(x1,y1+(int)(ly*(yscale+ygrad1)/yscale),x1+(int)lx+5,y1+(int)(ly*(yscale+ygrad1)/yscale));
		}
		else if (ygrad1==0)
		{
			g.drawLine(x1,y1+(int)ly,x1+(int)lx+5,y1+(int)ly);
		}
		
		/* Print axis legend */
		String label;
		int stringLen;
		int stringHei;
		label="Energies ("+labelunit()+")";
		stringLen = (int) (g.getFontMetrics().getStringBounds(label, g).getWidth())/2; //here half width
		stringHei = (int) (g.getFontMetrics().getStringBounds(label, g).getHeight());
		g.drawString(label,(int)(d.width/2 - stringLen),(int)(d.height-(float)y1/2));
		/*   Print x grad  */
		
		float grad=0;
		float scale=(float) Math.pow(10,(int) Math.log10(xscale));
		if (xscale<1.0f) {scale=scale/10.0f;}
		if (xscale/scale<2.01f){ scale=scale/2.0f;}
		float offset = xgrad1%scale;
		if (offset!=0)
		{
			grad=scale-offset;
		}
		int labelsize;
		while (grad/xscale<=1.001f)
		{
			labelsize=formatlabel(xgrad1+grad,scale);
			if (labelsize>0)
			{
				label=String.format("%f", xgrad1+grad).substring(0,labelsize);
			}
			else
			{
				label=String.valueOf((int)(xgrad1 + grad));
			}
			stringLen = (int) (g.getFontMetrics().getStringBounds(label, g).getWidth())/2; //here half length
			stringHei = (int) g.getFontMetrics().getStringBounds(label, g).getHeight();
			g.drawString(label,(int) (grad*lx/xscale)+x1-stringLen, y1+(int)(ly*(yscale+ygrad1)/yscale)+stringHei);
			g.drawLine((int) (grad*lx/xscale)+x1,y1+(int)(ly*(yscale+ygrad1)/yscale)-2,(int) (grad*lx/xscale)+x1,y1+(int)(ly*(yscale+ygrad1)/yscale)+2);
			grad=grad+scale;
		}
		/*   Print y grad  */
		grad=0;
		scale=(float) Math.pow(10,(int) Math.log10(yscale));
		if (yscale<1.0f) {scale=scale/10.0f;}
		if (yscale/scale<2.01f){ scale=scale/2.0f;}
		offset = ygrad1%scale;
		if (offset>0)
		{
			grad=scale-offset;
		}
		else if (offset<0)
		{
			grad=-offset;
		}
		while (grad/yscale<=1.001f)
		{
			labelsize=formatlabel(ygrad1+grad,scale);
			if (labelsize>0)
			{
				label=String.format("%f", ygrad1+grad).substring(0,labelsize);
			}
			else
			{
				label=String.valueOf((int)(ygrad1+grad));
			}
			stringLen = (int) g.getFontMetrics().getStringBounds(label, g).getWidth();
			stringHei = (int) (g.getFontMetrics().getStringBounds(label, g).getHeight()/2); //here half height
			g.drawString(label,(int) (x1-stringLen)-5, y1+(int)(ly-grad*ly/yscale)+stringHei);
			g.drawLine(x1-2,y1+(int)(ly-grad*ly/yscale),x1+2,y1+(int)(ly-grad*ly/yscale));
			grad=grad+scale;
		}
		/*   Print legend  */
		if (isprint)
		{
			String name;
			
			// Find maxlength
			int maxlength=0;
			for(int i = 0; i < ncurve; i++)
			{
				if (fen.whichcurve.get(i).isSelected())
				{
					name=fen.curve.get(i).getname();
					stringLen = (int) g.getFontMetrics().getStringBounds(label, g).getWidth();
					maxlength=Math.max(maxlength,stringLen);
				}
			}
			// Go!
			int xpos=x1+(int)lx-maxlength;
			int ypos=y1;
			int height;
			for(int i = 0; i < ncurve; i++)
			{
				if (fen.whichcurve.get(i).isSelected())
				{
					g.setColor(Color.black);
					name=fen.curve.get(i).getname();
					g.drawString(name,xpos,ypos);
					height=(int) g.getFontMetrics().getStringBounds(label, g).getHeight();
					g.setColor(fen.curve.get(i).getcolor());
					g.drawLine(xpos-(int)(2*(float)x1/10), ypos-(int)(0.3*height),xpos-(int)((float)x1/10), ypos-(int)(0.3*height));
					ypos=ypos+ (int)(1.1*height);
				}
			}
		}
		
		/*   Print curves  */
		int[] curve0 = new int[resolution];
		int[] curvei = new int[resolution];
		for (int j=0; j<resolution; j++) {
			curve0[j] = (int) (plotlist.get(0)[j] * lx)+x1;
			}
		for(int i = 0; i < ncurve; i++)
		{
			g.setColor(fen.curve.get(i).getcolor());
			if (fen.whichcurve.get(i).isSelected())
			{
				for (int j=0; j<resolution; j++) {
					curvei[j] = y1+(int)(ly*(yscale+ygrad1)/yscale)-(int) (plotlist.get(i+1)[j] * ly/yscale);
					}
				g.drawPolyline(curve0,curvei,resolution);
			}
		}
	}
	
	/* *********************************** */
	/* *******    Get and set's    ******* */
	/* *********************************** */
	
	public void set_xscale(float dx1, float dscale)
	{
		xgrad1=dx1;
		xscale=dscale;
		
	}
	public void set_ascale(float da1, float dascale)
	{
		ygrad1=da1;
		yscale=dascale;
		
	}
	public float geta1()
	{
		return ygrad1;	
	}
	public float getda()
	{
		return yscale;	
	}
	public void setunit(int dunit)
	{
		unit=dunit;
	}
	public float getx1(){
		return xgrad1;
	}
	public float getxde(){
		return xscale;
	}
	public int getunit()
	{
		return unit;
	}
	
	/* *********************************** */
	/* *******        Utils        ******* */
	/* *********************************** */
	public void delete(int i)
	{
		plotlist.remove(i+1);
		nplot-=1;
		if (nplot==0)
		{
			plotlist.remove(0);
			nplot=-1;
		}
	}
	public void reset(){
		plotlist.clear();
		nplot=-1;
	}
	public int formatlabel(float pnumber, float pscale)
	{
		String label1=String.format("%f", pnumber);
		String label2=String.format("%f", pscale);
		label1=label1.replace(",","."); // Small fix for stupid french commas
		label2=label2.replace(",",".");
		int ipos=label2.indexOf(".");
		if (ipos>1)
		{
			ipos=0;
		}
		else if (ipos==1)
		{
			if (label2.substring(0,1).equals("0"))
			{
				if (label2.substring(2,3).equals("0"))
				{
					if (label2.matches(".*[1-9].*"))
					{
						ipos=label2.split("[1-9]")[0].length();
						ipos=Math.min(ipos+1,label2.length())-1;
					}
					else
					{
						ipos=0;
					}
				}
				else
				{
					ipos=ipos+1;
				}
			}
			else
			{
				ipos=0;
			}
		}
		else if (ipos<0)
		{
			ipos=0;
		}
		if (label1.indexOf(".")>0)
		{
			ipos=ipos+label1.indexOf(".");
		}
		else
		{
			ipos=ipos+label1.length();
		}
		return ipos;
	}
	/* *********************************** */
	/* *******   Color generator   ******* */
	/* *********************************** */
	public static Color colorgen(int i)
	{
		double H,S,B,base;
		if (i==0)
		{
			return Color.getHSBColor(0,0,0);
		}
		else
		{
			if (i==1)
			{
				base=0;
			}
			else
			{
				int j=(int) (Math.log((double)(i-1))/Math.log(2))+1;
				base=(2*(i-1-(int)Math.pow(2, j-1))+1)/(Math.pow(2, j)); // Successively divides space in 2
			}
			/* Homemade supposedly clever formula to fit Munsell's colors palette */
			H=base+Math.sin(3*base*2*Math.PI)/24-1/3;
			S=0.7+0.3*Math.cos((base+2*Math.pow(base,3))*2/3*Math.PI);
			B=0.8+0.16*Math.cos((10*base-10*Math.pow(base,2)+6*Math.pow(base,3))/6*4*Math.PI);
			return Color.getHSBColor((float)H,(float)S,(float)B);
		}
	}
	public String labelunit()
	{
		String label="";
		switch (unit)
		{
			case 0:
				label="a.u.";
				break;
			case 1:
				label="eV";
				break;
			case 2:
				label="kcal/mol";
				break;
			case 3:
				label="kJ/mol";
				break;
			case 4:
				label="cm-1";
				break;
				// Not supported yet (need E-dependent broadening)
			case 5:
				label="nm";
				break;
		}
		return label;
	}
}
