package plotCAS;

public class Broadening {
	private boolean isline;
	private float gaussw,lorentzw,Elorsplit,lorentzw2;
	private int broadtype;// 0 = gaussian, 1=gauss+lor, 2 = gauss+2xlor
	public Broadening()
	{
		broadtype=0;
		isline=false;
		//Dummies, should be changed later:
		gaussw=0.1f;
		//Not used by default:
		lorentzw=0.1f;
		Elorsplit=0;
		lorentzw2=0.1f;
	}
	public Broadening(String pString)
	{
		broadtype=Integer.parseInt(pString.split(" ")[0]);
		gaussw=Float.parseFloat(pString.split(" ")[1]);
		lorentzw=Float.parseFloat(pString.split(" ")[2]);
		Elorsplit=Float.parseFloat(pString.split(" ")[3]);
		lorentzw2=Float.parseFloat(pString.split(" ")[4]);
		isline=Boolean.parseBoolean(pString.split(" ")[5]);
	}
	/*   Gaussian broadening */
	public void set_Broadening(float pgauss)
	{
		broadtype=0;
		gaussw=pgauss;
		//Not used:
		lorentzw=0.1f;
		Elorsplit=0;
		lorentzw2=0.1f;
	}
	/*   Gaussian + lorentzian broadening */
	public void set_Broadening(float pgauss, float plorentz)
	{
		broadtype=1;
		gaussw=pgauss;
		lorentzw=plorentz;
		//Not used:
		Elorsplit=0;
		lorentzw2=0.1f;
	}
	/*   Gaussian + dual lorentzian broadening */
	public void set_Broadening(float pgauss, float plorentz, float psplit, float plorentz2)
	{
		broadtype=2;
		gaussw=pgauss;
		lorentzw=plorentz;
		Elorsplit=psplit;
		lorentzw2=plorentz2;
	}
	public void setline(boolean is)
	{
		isline=is;
	}
	public boolean isline()
	{
		return isline;
	}
	public boolean islorentz()
	{
		return (broadtype>=1);
	}
	public boolean isduallorentz()
	{
		return (broadtype>=2);
	}
	public float getgaussw()
	{
		return gaussw;
	}
	public float getlorsplit()
	{
		return Elorsplit;
	}
	public float getlorw1()
	{
		return lorentzw;
	}
	public float getlorw2()
	{
		return lorentzw2;
	}
	public int getbroadtype()
	{
		return broadtype;
	}
	public String filestring()
	{
		String answer;
		answer=broadtype+" "+gaussw+" "+lorentzw+" "+Elorsplit+" "+lorentzw2+" "+isline;
		return answer;
	}
}
