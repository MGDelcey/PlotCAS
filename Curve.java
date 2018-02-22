package plotCAS;

import java.awt.Color;
import java.io.File;

/* Type is the origin of the curve
 1 - MOLCAS log file
 2 - List of transitions
 3 - XY file
 */
public class Curve {
	private String curvename;
	private File curvefile;
	private String curveinfo;
	private int curvetype;
	private float xoffset;
	private float yscale;
	private Color ccolor;
	private Broadening broad;
	private Molcasfile molcas;
	private int nativeunit;
	/* Default */
	public Curve(){ 
		  curvename="";
		  curvefile=new File("","");
		  curveinfo="";
		  curvetype=0;
		  xoffset=0;
		  yscale=1;
		  ccolor=Color.black;
		  broad=Window.curDefault.get_dbroad();
		  molcas=new Molcasfile();
		  nativeunit=Curveplot.getunit();
	}
	/* With parameters */
	public Curve(String pName, int ptype, File pFile, String pInfo, int punit){ 
		  curvename=pName;
		  curvetype=ptype;
		  curvefile=pFile;
		  curveinfo=pInfo;
		  xoffset=0;
		  yscale=1;
		  ccolor=Color.black;
		  broad=Window.curDefault.get_dbroad();
		  nativeunit=punit;
		  if (ptype==1)
		  {
			  molcas=new Molcasfile(pFile);
		  }
		  else
		  {
			  molcas=new Molcasfile();
		  }
	}
	
	public String getname() {
		return curvename;
	}
	public Broadening getbroad() {
		return broad;
	}
	public String getinfo() {
		return curveinfo;
	}
	public File getfile() {
		return curvefile;
	}
	public int gettype() {
		return curvetype;
	}
	public float getxoffset() {
		return xoffset;
	}
	public float getyscale() {
		return yscale;
	}
	public Color getcolor() {
		return ccolor;
	}
	public Molcasfile getmolcas() {
		return molcas;
	}
	public int getunit() {
		return nativeunit;
	}
	public void setname(String pname) {
		curvename=pname;
	}
	public void setinfo(String pinfo) {
		curveinfo=pinfo;
	}
	public void setfile(File pfile) {
		curvefile=pfile;
	}
	public void settype(int ptype) {
		curvetype=ptype;
	}
	public void setxoffset(float poffset) {
		xoffset=poffset;
	}
	public void setyscale(float pscale) {
		yscale=pscale;
	}
	public void setcolor(Color pcolor) {
		ccolor=pcolor;
	}
	public void setbroad(Broadening pbroad) {
		broad=pbroad;
	}
	public void setmolcas(Molcasfile pfile) {
		molcas=pfile;
	}
}
