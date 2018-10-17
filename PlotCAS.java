package plotCAS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.JOptionPane;

public class PlotCAS {
	//public Window fen;
	public static String currentversion="0.2.006";
	public static int nwindow=0;
	
	public static void main(String[] args) {
		/* Self-update if necessary */
		URL version;
		try {
			version = new URL("https://raw.githubusercontent.com/MGDelcey/PlotCAS/master/version");
			HttpURLConnection huc = (HttpURLConnection) version.openConnection();
		    huc.setConnectTimeout(1000);
		    huc.setReadTimeout(1000);
		    //huc.setRequestMethod("GET");
	//		try {
				BufferedReader versionfile = new BufferedReader(new InputStreamReader(version.openStream()));
				String versionid;
				versionid = versionfile.readLine();
				if (!versionid.equals(currentversion))
				{
					JOptionPane.showMessageDialog(null, "A new version of PlotCAS is available. The program will exit and update itself.");
					try {
			            URL website = new URL("https://raw.githubusercontent.com/MGDelcey/PlotCAS/master/PlotCAS.jar");
			            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir")+"/PlotCAS.jar");
			            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			            fos.close();
			            rbc.close();
			            System.exit(0);
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
	/*			}
			} catch (IOException e) {
				e.printStackTrace();*/
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/* Go */
		new Window();
	}
}
