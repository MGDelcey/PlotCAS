package plotCAS;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Default{
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private File ddir;
	private Broadening dbroad;
	private int resolution,iunit;
	private float E1,dE,A1,dA;
	public Default()
	{
		if (prefs.get("Default", null)!=null)
		{
			readdefaults("Default");
		}
		else
		{
			dbroad=new Broadening();
		}
	}
	public void add_default(String pname)
	{
		String defaultvalue;
		defaultvalue=ddir+"\n"+dbroad.filestring()+"\n"+resolution+" "+iunit+" "+E1+" "+dE+" "+A1+" "+dA;
		prefs.put(pname,defaultvalue);
	}
	public void readdefaults(String pname)
	{
		if (prefs.get(pname, null)!=null)
		{	
			String value=prefs.get(pname, "");
			if (pname.equals("Default")) {ddir=new File(value.split("\n")[0]);}
			dbroad=new Broadening(value.split("\n")[1]);
			readgraphopts(value.split("\n")[2]);
		}
	}
    /* ******************************** */
    /* *******  Gets and sets  ******** */
    /* ******************************** */
	public void set_dfile(File aFile)
	{
		ddir=aFile;
		add_default("Default");
	}
	public File get_dfile()
	{
		return ddir;
	}
	public Broadening get_dbroad()
	{
		return dbroad;
	}
	public void set_dbroad(Broadening abroad)
	{
		dbroad=abroad;
		add_default("Default");
	}
	public void set_graphopts(int dresolution, int dunit, float x1, float dx, float b1, float db)
	{
		resolution=dresolution;
		iunit=dunit;
		E1=x1;
		dE=dx;
		A1=b1;
		dA=db;
	}
	public String[] get_preflist()
	{ 
		String[] list;
		try{
			list = prefs.keys();
			}
		catch (BackingStoreException ex) {
            System.out.println(ex);
            list=new String[0];
        }
		return list;
	}
	public boolean labelexists(String plabel)
	{ 
		return (prefs.get(plabel, null)!=null);
	}
	public void readgraphopts(String pString)
	{ 
		resolution=Integer.parseInt(pString.split(" ")[0]);
		iunit=Integer.parseInt(pString.split(" ")[1]);
		Curveplot.setunit(iunit);
		E1=Float.parseFloat(pString.split(" ")[2]);
		dE=Float.parseFloat(pString.split(" ")[3]);
		Plotgraph.set_xscale(E1,dE);
		Curveplot.setx(E1,dE);
		A1=Float.parseFloat(pString.split(" ")[4]);
		dA=Float.parseFloat(pString.split(" ")[5]);
		Curveplot.seta(A1,dA);
		Plotgraph.set_ascale(A1,dA);
	}
}
