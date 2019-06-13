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
	public float mina=0;
	public float maxagap=0;
	private Window fen;
	private int ncurve;
	private boolean is2D;
	
	public int resolution=300;
	private int yresolution=1;
	public int nplot=-1;
	public ArrayList<float[][]> plotlist = new ArrayList<float[][]>();
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
		update(g,d,false,1);
		//update(fen,g,d,false,1);
	}
	public Plotgraph(Window tmp)
	{
		fen=tmp;
		ncurve=fen.ncurve;
		is2D=true;
	}
	public Plotgraph(int xres,int yres, float e1i, float e2i,float e1t, float e2t, int dunit)
	{
		is2D=false;
		ncurve=1;
		resolution=xres;
		yresolution=yres;
		xgrad1=e1i;
		xscale=e2i-e1i;
		ygrad1=e1t;
		yscale=e2t-e1t;
		unit=dunit;
	}
	//public void update(Window tmp,Graphics g3, Dimension d,boolean isprint, int lwidth)
	public void update(Graphics g3, Dimension d,boolean isprint, int lwidth)
	{
		Graphics2D g = (Graphics2D) g3;
		int x1,y1;
		float lx,ly;
		x1=d.width/10;  // let 10% margin
		y1=d.height/10;
		lx=d.width/10*8;
		ly=d.height/10*8;
		
		g.setStroke(new BasicStroke(lwidth));
		
		/*         2D PLOT : print curves      */
		if (is2D) ncurve=fen.ncurve;
		if (is2D)
		{
			int[] curve0 = new int[resolution];
			int[] curvei = new int[resolution];
			for (int j=0; j<resolution; j++) {
				curve0[j] = (int) (plotlist.get(0)[j][0] * lx)+x1;
			}
			for(int i = 0; i < ncurve; i++)
			{
				g.setColor(fen.curve.get(i).getcolor());
				if (fen.whichcurve.get(i).isSelected())
				{
					for (int j=0; j<resolution; j++) {
						curvei[j] = y1+(int)(ly*(yscale+ygrad1)/yscale)-(int) (plotlist.get(i+1)[j][0] * ly/yscale);
						}
					g.drawPolyline(curve0,curvei,resolution);
				}
			}
		}
		/*         3D PLOT : print color map      */
		else
		{
			if (nplot>=0)
			{
				// Find max
				float max=0;
				for(int i = 0; i < resolution; i++)
				{
					for(int j = 0; j < yresolution; j++)
					{
						max=Math.max(plotlist.get(0)[i][j],max);
					}
				}
				int sqx,sqy,sqh,sqw;
				sqh=(int) ly/yresolution+1;
				sqw=(int) lx/resolution+1;
				
				for(int i = 0; i < resolution; i++)
				{
					for(int j = 0; j < yresolution; j++)
					{
						g.setColor(colorgen((double) plotlist.get(0)[i][j]/max));
						sqx=(int) (i*lx/resolution)+x1;
						sqy=(int) ((yresolution-j-1)*ly/yresolution)+y1;
						g.fillRect (sqx, sqy, sqw, sqh);
					}
				}
			}
			
		}
		
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
		if (is2D)
		{
			label="Energy ("+labelunit()+")";
		}
		else
		{
			label="Incident energy ("+labelunit()+")";
		}
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
		if (is2D && isprint)
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
	public static Color colorseries(int i)
	{
		double base;
		if (i==0)
		{
			return Color.getHSBColor(0,0,0);
		}
		else
		{
			base=((i-1)*1.61803398875) % 1; //Golden ratio lack of periodicity should be ideal
			return colorgen2(base);
		}
	}
	/* Homemade supposedly clever formula to fit a typical heat map scale */
	public static Color colorgen(double base)
	{
		//base=0.65-0.65*(0.8*base+0.2*Math.sqrt(base));
		base=0.65-0.65*base;
		double H,S,B;
		H=base;
		S=0.75+0.2*Math.cos((1-base+2*Math.pow(1-base,3))*2/3*Math.PI);
		B=1.0/(1+Math.exp(20*(base-0.65)));
		return Color.getHSBColor((float)H,(float)S,(float)B);
	}
	/* Homemade supposedly clever formula to fit Munsell's colors palette */
	public static Color colorgen2(double base)
	{
		double H,S,B;
		H=base+Math.sin(3*base*2*Math.PI)/24-1/3;
		S=0.7+0.3*Math.cos((base+2*Math.pow(base,3))*2/3*Math.PI);
		B=0.8+0.16*Math.cos((10*base-10*Math.pow(base,2)+6*Math.pow(base,3))/6*4*Math.PI);
		return Color.getHSBColor((float)H,(float)S,(float)B);
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
