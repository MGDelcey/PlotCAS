package plotCAS;

import java.awt.Color; 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.io.BufferedReader;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Window extends JFrame {
	private static final long serialVersionUID = 1L;
    
	/*   MENU  */
	JMenu analysis, export, settings;
	JMenuItem optcurve, optplot;
    /*   SUBWINDOWS  */
    private JPanel container = new JPanel();
    private JPanel plotscreen = new JPanel();
    private JPanel optionscreen = new JPanel();
    private JLabel optiontitle = new JLabel("");
    
    /*   Various  */
	public int ncurve=0;
	public File WorkDir;
	public ArrayList<Curve> curve = new ArrayList<Curve>();
	public ArrayList<JCheckBox> whichcurve = new ArrayList<JCheckBox>();
	
	private CurveSel plotselector=new CurveSel(0);
	public Default curDefault=new Default();
	
	
	public Plotgraph plot;
	
    /* ******************************** */
    /* *******      Window     ******** */
    /* ******************************** */

	public Window(){
		/* Make work dir */
		PlotCAS.nwindow++;
		
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = String.valueOf(System.currentTimeMillis());
		WorkDir = new File(baseDir, baseName);
		System.out.println(WorkDir);
		WorkDir.mkdir();
		
		/* Open window */
		this.setTitle("plotCAS");
		this.setSize(1000, 600);
	    this.setLocationRelativeTo(null);
	    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    WindowListener exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(null, "Are You Sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                	File folder = new File(".");
                	File fList[] = folder.listFiles();
                	for (int i = 0; i < fList.length; i++) {
                	    String pes = fList[i].getName();
                	    if (pes.endsWith(".xy")||pes.endsWith(".input")||pes.startsWith("molcas")) {
                	        fList[i].delete();
                	    }
                	}
                	Window.this.WorkDir.delete();
                	dispose();
                	PlotCAS.nwindow--;
                if (PlotCAS.nwindow==0) {System.exit(0);}
                }
            }
        };
	    this.addWindowListener(exitListener);
	    
	    plot= new Plotgraph(this);
	    
		/*   MENU  */
		JMenuBar menuBar = new JMenuBar();
	    JMenu input = new JMenu("Input");
	    JMenuItem inputmolcas = new JMenuItem("MOLCAS file");
	    JMenuItem inputtrans = new JMenuItem("List of transitions");
	    JMenuItem inputxy = new JMenuItem("XY curve");
	    input.add(inputmolcas);
	    inputmolcas.addActionListener(new Molcasinput());
	    input.add(inputtrans);
	    inputtrans.addActionListener(new Transinput());
	    input.add(inputxy);
	    inputxy.addActionListener(new XYinput());
	    menuBar.add(input);
	    
	    JMenu plotoptions = new JMenu("Plotting");
	    optcurve = new JMenuItem("Curve options");
	    optplot = new JMenuItem("General settings");
	    JMenuItem defaultmenu = new JMenuItem("Save settings");
	    JMenuItem loaddefaultmenu = new JMenuItem("Load settings");
	    optcurve.addActionListener(new PlotCurvemenu());
	    optcurve.setEnabled(false);
	    plotoptions.add(optcurve);
	    optplot.addActionListener(new PlotOptmenu());
	    optplot.setEnabled(false);
	    plotoptions.add(optplot);
	    defaultmenu.addActionListener(new defaultmenu());
	    plotoptions.add(defaultmenu);
	    loaddefaultmenu.addActionListener(new loaddefaultmenu());
	    plotoptions.add(loaddefaultmenu);
	    menuBar.add(plotoptions);
	    
	    analysis = new JMenu("Analysis");
	    analysis.setEnabled(false);
	    JMenuItem operations = new JMenuItem("Curve operations");
	    operations.addActionListener(new CurveOp());
	    analysis.add(operations);
	    JMenuItem align = new JMenuItem("Align curves");
	    align.addActionListener(new Align());
	    analysis.add(align);
	    JMenuItem intensity = new JMenuItem("Integrated intensity");
	    intensity.addActionListener(new IntIntens());
	    analysis.add(intensity);
	    JMenuItem analorb = new JMenuItem("Orbital contributions");
	    analorb.addActionListener(new Analorbmenu());
	    analysis.add(analorb);
	    JMenuItem similarity = new JMenuItem("Similarity");
	    similarity.addActionListener(new Similarity());
	    analysis.add(similarity);
	    JMenuItem scatter = new JMenuItem("Scattering");
	    scatter.addActionListener(new Scatter());
	    analysis.add(scatter);
	    menuBar.add(analysis);
	    
	    export = new JMenu("Export");
	    export.setEnabled(false);
	    JMenuItem exportxy = new JMenuItem("Curve XY");
	    exportxy.addActionListener(new ExportXY());
	    export.add(exportxy);
	    JMenuItem exportimg = new JMenuItem("Picture");
	    exportimg.addActionListener(new ExportImg());
	    export.add(exportimg);
	    JMenuItem exporttrans = new JMenuItem("List of transitions");
	    exporttrans.addActionListener(new ExportTrans());
	    export.add(exporttrans);
	    menuBar.add(export);
	    
	    JMenuItem about = new JMenuItem("About");
	    about.addActionListener(new aboutmenu());
	    menuBar.add(about);
	    
	    JMenuItem newapp = new JMenuItem("New window");
	    newapp.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent arg0) {
	            new Window();
	        }
	    });
	    menuBar.add(newapp);
	    
	    setJMenuBar(menuBar);
	    
	    /*   SUBWINDOWS  */
	    
	    JScrollPane optionpanel;
	    
	    
	    container.setLayout(new BorderLayout());
	    optionscreen.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.black));
	    optionscreen.setLayout(new BoxLayout(optionscreen, BoxLayout.PAGE_AXIS));
	    optionpanel = new JScrollPane(optionscreen);
	    
	    plotscreen.setLayout(new BorderLayout());
	    
	    
	    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
	    		plotscreen, optionpanel);
	    splitPane.setResizeWeight(0.7);
	    container.add(splitPane);
	    
	    this.setContentPane(container);	    
	    this.setVisible(true);
	}

	/* ******************************** */
	/* *******      About      ******** */
	/* ******************************** */
	
	class aboutmenu implements ActionListener {
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();		
			optiontitle.setText("About");
			optionscreen.add(optiontitle);
			
			optionscreen.add(new JLabel("PlotCAS"));
			optionscreen.add(new JLabel("A CASSCF/CASPT2 spectrum plotting program"));
			
			optionscreen.add(new JLabel("v"+PlotCAS.currentversion));
			optionscreen.add(new JLabel("2014, 4th july"));
			
			optionscreen.add(new JLabel("author : M.G. Delcey"));
			
			optionscreen.add(new JLabel("GNU Lesser General Public License v2.1"));
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	
	/* ******************************** */
	/* *******   Empty menu    ******** */
	/* ******************************** */
	
	// A good way to exit a current menu made obsolete
	
	public void emptymenu()
	{
		optionscreen.removeAll();
		optionscreen.revalidate();
	    optionscreen.repaint();
	}
	
	/* ************************************************************ */
    /* ************************************************************ */
	/* *********************                  ********************* */
    /* *********************       INPUT      ********************* */
	/* *********************                  ********************* */
	/* ************************************************************ */
	/* ************************************************************ */
	
	
	/* ******************************** */
	/* *******   MOLCAS input  ******** */
	/* ******************************** */
	
	class Molcasinput implements ActionListener {
		private File selectedFile;
		private JLabel filename = new JLabel("");
		private JRadioButton SFbutton, SOCbutton;
		private JCheckBox dipolebutton, quadrupolebutton, velocbutton,boltzbutton;
		private JTextField ground1, ground2, curvename,boltztemp;
		Molcasfile molcasinput;
		JPanel smallbox;
		public void actionPerformed(ActionEvent arg0){
			
			optionscreen.removeAll();		
			optiontitle.setText("Input from MOLCAS file");
			optionscreen.add(optiontitle);
			
			/* Open file */
			filename.setText("");
			JButton openbutton = new JButton("Select File");
		    openbutton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		        JFileChooser fileChooser = new JFileChooser();
		        fileChooser.setCurrentDirectory(curDefault.get_dfile());
		        
		        int returnValue = fileChooser.showOpenDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION) {
		          selectedFile = fileChooser.getSelectedFile();
		          filename.setText(selectedFile.getName());
		          curDefault.set_dfile(selectedFile.getParentFile());
		        }
		      }
		    });
		    JPanel l1 = new JPanel();
		    l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
		    l1.add(openbutton);
		    l1.add(filename);
		    optionscreen.add(l1);
		    
		    /* Which options should be read */
		    
		    smallbox=new JPanel();
		    smallbox.setLayout(new BoxLayout(smallbox, BoxLayout.PAGE_AXIS));
		    
		    JButton loadbutton = new JButton("Load");
		    optionscreen.add(loadbutton);
		    loadbutton.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent ae) {
			    	  molcasinput = new Molcasfile(Window.this.WorkDir,selectedFile);
			    	  
			    	  smallbox.removeAll();
		    		  int wf = molcasinput.whichWF();  
		    		  switch (wf)
		    		  {
		    		  	case 1:
		    		  		smallbox.add(new JLabel("RASSCF calculation"));
		    		  		break;
		    		  	case 2:
		    		  		smallbox.add(new JLabel("SS-CASPT2 calculation"));
		    		  		break;
		    		  	case 3:
		    		  		smallbox.add(new JLabel("MS-CASPT2 calculation"));
		    		  		break;
		    		  }
			    	  if (molcasinput.isRASSI())
			    	  {
			    		  smallbox.add(new JLabel("RASSI specifications"));
			    		  
			    		  int nrSF=molcasinput.getSFstates();
			    		  int nrSOC=molcasinput.getSOCstates();
			    		  ButtonGroup SFSOCGroup = new ButtonGroup();
			    		  JPanel l2 = new JPanel();
			    		  l2.setLayout(new BoxLayout(l2, BoxLayout.LINE_AXIS));
			    		  if (nrSF>0)
			    		  {
			    			  smallbox.add(new JLabel(String.valueOf(nrSF)+" spin-free states"));
			    			  SFbutton = new JRadioButton("Spin-Free");
			    			  SFSOCGroup.add(SFbutton);
			    			  l2.add(SFbutton);
			    		  }
			    		  if (nrSOC>0)
			    		  {
			    			  smallbox.add(new JLabel(String.valueOf(nrSOC)+" spin-orbit states"));
			    			  SOCbutton = new JRadioButton("Spin-Orbit");
			    			  SOCbutton.setSelected(true);
			    			  SFSOCGroup.add(SOCbutton);
			    			  l2.add(SOCbutton);
			    		  }
			    		  else
			    		  {
			    			  SFbutton.setSelected(true);
			    		  }
			    		  smallbox.add(l2);
			    		  
			    		  JPanel l3 = new JPanel();
			    		  l3.setLayout(new BoxLayout(l3, BoxLayout.LINE_AXIS));
			    		  if (molcasinput.isdipole())
			    		  {
			    			  dipolebutton = new JCheckBox("Dipole transitions");
			    			  dipolebutton.setSelected(true);
			    			  l3.add(dipolebutton);
			    		  }
			    		  if (molcasinput.isquadrupole())
			    		  {
			    			  quadrupolebutton = new JCheckBox("Quadrupole transitions");
			    			  quadrupolebutton.setSelected(true);
			    			  l3.add(quadrupolebutton);
			    		  }
			    		  if (molcasinput.isveloc())
			    		  {
			    			  velocbutton = new JCheckBox("Velocity representation");
			    			  velocbutton.setSelected(false);
			    			  l3.add(velocbutton);
			    		  }
			    		  smallbox.add(l3);
			    		  
			    		  JPanel l4 = new JPanel();
			    		  l4.setLayout(new BoxLayout(l4, BoxLayout.LINE_AXIS));
			    		  l4.add(new JLabel("Ground states from"));
			    		  ground1 = new JTextField("1");
			    		  l4.add(ground1);
			    		  l4.add(new JLabel("to"));
			    		  ground2 = new JTextField("1");
			    		  l4.add(ground2);
			    		  smallbox.add(l4);
			    		  
			    		  JPanel l5 = new JPanel();
			    		  l5.setLayout(new BoxLayout(l5, BoxLayout.LINE_AXIS));
			    		  boltzbutton = new JCheckBox("Boltzman distribution");
			    		  l5.add(boltzbutton);
			    		  boltztemp = new JTextField("298");
			    		  l5.add(boltztemp);
			    		  l5.add(new JLabel("K"));
			    		  smallbox.add(l5);
			    		  
			    		  
			    		  JPanel l6 = new JPanel();
			    		  l6.setLayout(new BoxLayout(l6, BoxLayout.LINE_AXIS));
			    		  JButton curvebutton = new JButton("Create curve");
			    		  l6.add(curvebutton);
			    		  curvename = new JTextField("curve name");
			    		  l6.add(curvename);
			    		  smallbox.add(l6);
			    		  
			    		  curvebutton.addActionListener(new ActionListener() {
						      public void actionPerformed(ActionEvent ae) {
						    	  boolean isSF,isSOC,isdip,isveloc,isquad;
						    	  if (molcasinput.getSFstates()<=0){isSF=false;}
						    	  else {isSF=SFbutton.isSelected();}
						    	  if (molcasinput.getSOCstates()<=0){isSOC=false;}
						    	  else {isSOC=SOCbutton.isSelected();}
						    	  if (!molcasinput.isdipole()){isdip=false;}
						    	  else {isdip=dipolebutton.isSelected();}
						    	  if (!molcasinput.isquadrupole()){isquad=false;}
						    	  else {isquad=quadrupolebutton.isSelected();}
						    	  if (!molcasinput.isveloc()){isveloc=false;}
						    	  else {isveloc=velocbutton.isSelected();}
						    	  float temp=0;
						    	  if (boltzbutton.isSelected())
						    	  {
						    		  temp=Float.parseFloat(boltztemp.getText());
						    	  }
						    	  Transition trans=new Transition(Window.this,molcasinput,isSF,isSOC, isdip,isveloc,isquad,boltzbutton.isSelected(),temp,Integer.parseInt(ground1.getText()),Integer.parseInt(ground2.getText()));
						    	  addcurve(curvename.getText(),1,trans,"",plot.getunit());
						      }
			    		  });
			    		  
			    	  }
			    	  else
			    	  {
			    		  JOptionPane.showMessageDialog(new JFrame(),
			    				    "To plot a spectrum, the input requires a RASSI section.",
			    				    "Input error",
			    				    JOptionPane.ERROR_MESSAGE);
			    	  }
			    	  optionscreen.add(smallbox);
			    	  
		    		  optionscreen.revalidate();
		    		  optionscreen.repaint();
			      }
			    });
		    
		    
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	/* ******************************** */
	/* *******    List input   ******** */
	/* ******************************** */
	
	class Transinput implements ActionListener {
		private JTextArea textArea;
		private JTextField curvename;
		private JComboBox<String> unitsel;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Input from list of transitions");
			optionscreen.add(optiontitle);
			
			/* Make text area */
			textArea = new JTextArea(
					"ntransition\n" +
	                "energy1   intensity1\n" +
	                "energy2   intensity2\n" +
	                "..."
	        ,20,20);
	        textArea.setLineWrap(true);
	        textArea.setWrapStyleWord(true);
	        JScrollPane areaScrollPane = new JScrollPane(textArea);
	        areaScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
	        optionscreen.add(areaScrollPane);
	        
  		  	JPanel l0 = new JPanel();
  		  	int iunit = plot.getunit();
  		  	l0.setLayout(new BoxLayout(l0, BoxLayout.LINE_AXIS));
  		  	l0.add(new JLabel("Native unit:"));
  		  	unitsel=new JComboBox<String>();
  		  	unitsel.addItem("hartree");
  		  	unitsel.addItem("eV");
  		  	unitsel.addItem("kcal/mol");
  		  	unitsel.addItem("kJ/mol");
  		  	unitsel.addItem("cm-1");
  		  	//unitsel.addItem("nm");
  		  	unitsel.setSelectedIndex(iunit);
  		  	l0.add(unitsel);
  		  	optionscreen.add(l0);
	        
	        /* What curve name? */
		    JPanel l1 = new JPanel();
		    l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
	        JButton curvebutton = new JButton("Create curve");
	        l1.add(curvebutton);
	        curvename = new JTextField("curve name");
	        l1.add(curvename);
	        optionscreen.add(l1);
	        		
	        /* Read and put to file */
	        curvebutton.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent ae) {
	        		Transition trans=new Transition(Window.this,textArea.getText());
	        		addcurve(curvename.getText(),2,trans,"",unitsel.getSelectedIndex());
	        	}
	        });
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}

	/* ******************************** */
	/* *******     XY input    ******** */
	/* ******************************** */
	
	class XYinput implements ActionListener {
		private JTextField curvename;
		private JTextArea textArea;
		private JComboBox<String> unitsel;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Input from XY");
			optionscreen.add(optiontitle);
			
			/* Make text area */
			textArea = new JTextArea(
	                "x1   y1\n" +
	                "x2   y2\n" +
	                "..."
	        ,20,20);
	        textArea.setLineWrap(true);
	        textArea.setWrapStyleWord(true);
	        JScrollPane areaScrollPane = new JScrollPane(textArea);
	        areaScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
	        optionscreen.add(areaScrollPane);
	        
  		  	JPanel l0 = new JPanel();
  		  	int iunit = plot.getunit();
  		  	l0.setLayout(new BoxLayout(l0, BoxLayout.LINE_AXIS));
  		  	l0.add(new JLabel("Native unit:"));
  		  	unitsel=new JComboBox<String>();
  		  	unitsel.addItem("hartree");
  		  	unitsel.addItem("eV");
  		  	unitsel.addItem("kcal/mol");
  		  	unitsel.addItem("kJ/mol");
  		  	unitsel.addItem("cm-1");
  		  	//unitsel.addItem("nm");
  		  	unitsel.setSelectedIndex(iunit);
  		  	l0.add(unitsel);
  		  	optionscreen.add(l0);
  		  	
	        /* What curve name? */
		    JPanel l1 = new JPanel();
		    l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
	        JButton curvebutton = new JButton("Create curve");
	        l1.add(curvebutton);
			curvename = new JTextField("curve name");
	        l1.add(curvename);
	        optionscreen.add(l1);
	        		
	        /* Read and put to file */
	        curvebutton.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent ae) {
	        		Transition trans=new Transition(Window.this,textArea.getText());
	        		addcurve(curvename.getText(),3,trans,"",unitsel.getSelectedIndex());
	        	}
	        });
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}

	/* ******************************** */
	/* ******* Add/Delete curve ******* */
	/* ******************************** */
	
	public void addcurve(String pname,int ptype,Transition trans, String pinfo, int nativeunit)
	{
		this.ncurve++;
		curve.add(new Curve(pname,ptype,trans,curDefault.get_dbroad(),pinfo,nativeunit));
		curve.get(this.ncurve-1).setcolor(Plotgraph.colorseries(this.ncurve-1));  // Get different colors
		whichcurve.add(new JCheckBox(pname));
		whichcurve.get(this.ncurve-1).setSelected(true);
		whichcurve.get(this.ncurve-1).addActionListener(new Refreshplot());
		setcurvelist();
		if (ptype==3) {
			Broadening broad=new Broadening();
			broad.set_Broadening(0);
			curve.get(this.ncurve-1).setbroad(broad);}
		if (this.ncurve==1)
		{
			init_graph();
			new Curveplot(this,0);
			new Curveplot(this,1);
			plot.set_ascale(plot.mina, plot.maxagap);
			// Enable menu
			//plotoptions.setEnabled(true);
		    optcurve.setEnabled(true);
		    optplot.setEnabled(true);
			analysis.setEnabled(true);
			export.setEnabled(true);
		}
		else
		{
			new Curveplot(this,this.ncurve);
		}
		plotscreen.add(plot,BorderLayout.CENTER);
		plot.repaint();
	}
	public void curve_delete(int icurve)
	{
		curve.remove(icurve);
		whichcurve.remove(icurve);
		plot.delete(icurve);
		this.ncurve-=1;
		
		setcurvelist();
		
		if (this.ncurve<1)
		{
		    optcurve.setEnabled(false);
		    optplot.setEnabled(false);
			analysis.setEnabled(false);
			export.setEnabled(false);
		}
		else
		{
			plot.repaint();
			plotscreen.add(plot,BorderLayout.CENTER);
		}
	}

	/* ************************************************************ */
    /* ************************************************************ */
	/* *********************                  ********************* */
    /* *********************       PLOT       ********************* */
	/* *********************                  ********************* */
	/* ************************************************************ */
	/* ************************************************************ */
	
	/* ******************************** */
	/* *******  Curve options   ******* */
	/* ******************************** */
	
	class PlotCurvemenu implements ActionListener {
		private int icurve;
		private JTextField curvename, offsetfield,yscalefield,colorfield, gausswfield, lorentzfield, lorentz2field, lorentzsplitfield;
		private JTextArea InfoArea;
		private JComboBox<String> broadsel;
		private JPanel l7;
		private JPanel l8;
		private JPanel l9;
		private JButton deletebutton;
		private JRadioButton HWHM, Gsigma;
		
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Curve specific options");
			optionscreen.add(optiontitle);
				
			icurve=plotselector.index();
			plotselector=new CurveSel(0);
			plotselector.select(icurve);
			plotselector.curvesel.addActionListener(new PlotCurvemenu()); // Recursive call
			
			JPanel l0 = new JPanel();
			l0.add(new JLabel("Options for curve:"));
			l0.add(plotselector.Box());
			
			
			/* Delete curve */
			deletebutton=new JButton("Delete");
			deletebutton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0){
					icurve=plotselector.index();
					int confirm = JOptionPane.showOptionDialog(null, "Are You Sure you want to remove this curve", "Removal Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
					if (confirm==0) {
						curve_delete(icurve);
						emptymenu();
					}
				}
			});
			l0.add(deletebutton);
			optionscreen.add(l0);
			
			JPanel l1 = new JPanel();
			l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
			l1.add(new JLabel("Name:"));
			curvename = new JTextField(curve.get(icurve).getname());
			l1.add(curvename);
			optionscreen.add(l1);
			
			optionscreen.add(new JLabel("Informations:"));
			InfoArea = new JTextArea(curve.get(icurve).getinfo(),5,10);
			optionscreen.add(InfoArea);
			
			JPanel l2 = new JPanel();
			l2.setLayout(new BoxLayout(l2, BoxLayout.LINE_AXIS));
			l2.add(new JLabel("Horizontal offset:"));
			offsetfield = new JTextField(String.valueOf(curve.get(icurve).getxoffset()));
			l2.add(offsetfield);
			optionscreen.add(l2);
			
			JPanel l3 = new JPanel();
			l3.setLayout(new BoxLayout(l3, BoxLayout.LINE_AXIS));
			l3.add(new JLabel("Vertical scaling:"));
			yscalefield = new JTextField(String.valueOf(curve.get(icurve).getyscale()));
			l3.add(yscalefield);
			optionscreen.add(l3);
			
			JPanel l4 = new JPanel();
			l4.setLayout(new BoxLayout(l4, BoxLayout.LINE_AXIS));
			l4.add(new JLabel("Color (hexadecimal):"));
			colorfield = new JTextField("#".concat(Integer.toHexString(curve.get(icurve).getcolor().getRGB()).substring(2, 8)));
			l4.add(colorfield);
			optionscreen.add(l4);
			
			/* Broadening menu */
			if (curve.get(icurve).gettype()<=2)
			{
				optionscreen.add(new JLabel("Broadening"));
				
				JPanel l5 = new JPanel();
				l5.add(new JLabel("Broadening type"));
				broadsel=new JComboBox<String>();
				broadsel.addItem("Gaussian broadening");
				broadsel.addItem("Gaussian + Lorentzian broadening");
				broadsel.addItem("Gaussian + dual Lorentzian broadening");
				l5.add(broadsel);
				optionscreen.add(l5);
				int ibroad=curve.get(icurve).getbroad().getbroadtype();
				broadsel.setSelectedIndex(ibroad);
				
				broadsel.addActionListener(new ActionListener(){
				      public void actionPerformed(ActionEvent event){				
				    	  int ibroad = broadsel.getSelectedIndex();
				    	  switch (ibroad)
				    	  {
				    	  case 0:
				    		  l7.setVisible(false);
				    		  l8.setVisible(false);
				    		  l9.setVisible(false);
				    		  break;
				    	  case 1:
				    		  l7.setVisible(true);
				    		  l8.setVisible(false);
				    		  l9.setVisible(false);
				    		  break;
				    	  case 2:
				    		  l7.setVisible(true);
				    		  l8.setVisible(true);
				    		  l9.setVisible(true);
				    		  break;
				    	  }
				      }
				});
				
				
				JPanel l6 = new JPanel();
				l6.setLayout(new BoxLayout(l6, BoxLayout.LINE_AXIS));
				l6.add(new JLabel("Gaussian width:"));
				gausswfield = new JTextField(String.valueOf(curve.get(icurve).getbroad().getgaussw()));
				l6.add(gausswfield);
				ButtonGroup GunitGroup = new ButtonGroup();
				HWHM = new JRadioButton("HWHM");
				HWHM.setSelected(true);
				GunitGroup.add(HWHM);
				l6.add(HWHM);
				Gsigma = new JRadioButton("sigma");
				GunitGroup.add(Gsigma);
				l6.add(Gsigma);
				optionscreen.add(l6);
	    		   
				
				l7 = new JPanel();
				l7.setLayout(new BoxLayout(l7, BoxLayout.LINE_AXIS));
				l7.add(new JLabel("Lorentzian width (HWHM):"));
				lorentzfield  = new JTextField(String.valueOf(curve.get(icurve).getbroad().getlorw1()));
				l7.add(lorentzfield);
				if (ibroad<1) {l7.setVisible(false);}
				optionscreen.add(l7);

				l8 = new JPanel();
				l8.setLayout(new BoxLayout(l8, BoxLayout.LINE_AXIS));
				l8.add(new JLabel("Second lorentzian width:"));
				lorentz2field = new JTextField(String.valueOf(curve.get(icurve).getbroad().getlorw2()));
				l8.add(lorentz2field);
				if (ibroad<2) {l8.setVisible(false);}
				optionscreen.add(l8);
					
				l9 = new JPanel();
				l9.setLayout(new BoxLayout(l9, BoxLayout.LINE_AXIS));
				l9.add(new JLabel("Lorentzian split energy"));
				lorentzsplitfield = new JTextField(String.valueOf(curve.get(icurve).getbroad().getlorsplit()));
				l9.add(lorentzsplitfield);
				optionscreen.add(l9);
				if (ibroad<2) {l9.setVisible(false);}
			}
			else if (curve.get(icurve).gettype()==3)
			{
				JPanel l6 = new JPanel();
				l6.setLayout(new BoxLayout(l6, BoxLayout.LINE_AXIS));
				l6.add(new JLabel("Gaussian smoothing:"));
				gausswfield = new JTextField(String.valueOf(curve.get(icurve).getbroad().getgaussw()));
				l6.add(gausswfield);
				ButtonGroup GunitGroup = new ButtonGroup();
				HWHM = new JRadioButton("HWHM");
				HWHM.setSelected(true);
				GunitGroup.add(HWHM);
				l6.add(HWHM);
				Gsigma = new JRadioButton("sigma");
				GunitGroup.add(Gsigma);
				l6.add(Gsigma);
				optionscreen.add(l6);
			};
			
			JButton setbutton=new JButton("Save");
		    
			/* Change values and replot the curve */
			setbutton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					icurve=plotselector.index();
					curve.get(icurve).setname(curvename.getText());
					curve.get(icurve).setinfo(InfoArea.getText());
					curve.get(icurve).setxoffset(Float.parseFloat(offsetfield.getText()));
					curve.get(icurve).setyscale(Float.parseFloat(yscalefield.getText()));
					curve.get(icurve).setcolor(Color.decode(colorfield.getText()));
					whichcurve.get(icurve).setText(curvename.getText());
					whichcurve.get(icurve).setForeground(curve.get(icurve).getcolor());
					Broadening broad=new Broadening();
					if (curve.get(icurve).gettype()<=2)
					{
						float gauss=Float.parseFloat(gausswfield.getText());
						if (Gsigma.isSelected())
						{
							gauss=gauss*(float)Math.sqrt(2.0*Math.log(2));
						}
						switch (broadsel.getSelectedIndex())
						{
						case 0:
							broad.set_Broadening(gauss);
							break;
						case 1:
							broad.set_Broadening(gauss,Float.parseFloat(lorentzfield.getText()));
							break;
						case 2:
							broad.set_Broadening(gauss,Float.parseFloat(lorentzfield.getText()),Float.parseFloat(lorentzsplitfield.getText()),Float.parseFloat(lorentz2field.getText()));
							break;
						}
						curve.get(icurve).setbroad(broad);
						curDefault.set_dbroad(curve.get(icurve).getbroad());
					}
					else if (curve.get(icurve).gettype()==3)
					{
						float gauss=Float.parseFloat(gausswfield.getText());
						broad.set_Broadening(gauss);
						curve.get(icurve).setbroad(broad);
					}
					new Curveplot(Window.this,icurve+1);
					plot.repaint();
				}
			});
			optionscreen.add(setbutton);

			optionscreen.revalidate();
			optionscreen.repaint();
		}
	}
	class comboitem //Complicated structure to allow curves with same name to be distinguished
	{
		String name;
		int ID;
		public comboitem(String pname, int pID){
			name=pname;
			ID=pID;
		}
		public int getID(){
			return ID;
		}
		@Override
	    public String toString() {
			return name;
		}
	}
	
	/* ******************************** */
	/* ******* Plotting options ******* */
	/* ******************************** */
	
	class PlotOptmenu implements ActionListener {
		private JTextField resolinp;
		private JTextField E1inp;
		private JTextField E2inp;
		private JTextField A1inp;
		private JTextField A2inp;
		private JComboBox<String> unitsel;
		
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("General plotting options");
			optionscreen.add(optiontitle);
			
			JPanel l1 = new JPanel();
		    l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
			l1.add(new JLabel("Number of curve points:"));
			resolinp=new JTextField(String.valueOf(plot.resolution));
			l1.add(resolinp);
			optionscreen.add(l1);
			
			float x1=plot.getx1();
			float dE=plot.getxde();
			JPanel l2 = new JPanel();
			l2.setLayout(new BoxLayout(l2, BoxLayout.LINE_AXIS));
			l2.add(new JLabel("Energy range:"));
			E1inp=new JTextField(String.valueOf(x1));
			l2.add(E1inp);
			E2inp=new JTextField(String.valueOf(x1+dE));
			l2.add(E2inp);
			optionscreen.add(l2);
			
			float A1=plot.geta1();
			float dA=plot.getda();
			JPanel l3 = new JPanel();
			l3.setLayout(new BoxLayout(l3, BoxLayout.LINE_AXIS));
			l3.add(new JLabel("Absorption range:"));
			A1inp=new JTextField(String.valueOf(A1));
			l3.add(A1inp);
			A2inp=new JTextField(String.valueOf(A1+dA));
			l3.add(A2inp);
			JButton ascalebutton = new JButton("Adjust to graph");
			ascalebutton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0){
					
					float mina=curve.get(0).mina;
					float maxagap=curve.get(0).maxagap;
					for (int i = 1; i <= Window.this.ncurve-1; i++)
					{
						mina=Math.min(curve.get(i).mina,mina);
						maxagap=Math.max(curve.get(i).maxagap,maxagap);
					}
					A1inp.setText(String.valueOf(mina));
					A2inp.setText(String.valueOf(maxagap));
				}
			});
			l3.add(ascalebutton);
			optionscreen.add(l3);
			
  		  	JPanel l4 = new JPanel();
  		  	int iunit = plot.getunit();
  		  	l4.setLayout(new BoxLayout(l4, BoxLayout.LINE_AXIS));
  		  	l4.add(new JLabel("Unit:"));
  		  	unitsel=new JComboBox<String>();
  		  	unitsel.addItem("hartree");
  		  	unitsel.addItem("eV");
  		  	unitsel.addItem("kcal/mol");
  		  	unitsel.addItem("kJ/mol");
  		  	unitsel.addItem("cm-1");
  		  	//unitsel.addItem("nm");
  		  	unitsel.setSelectedIndex(iunit);
  		  	l4.add(unitsel);
  		  	optionscreen.add(l4);
			
			JButton plotoptbutton = new JButton("Redraw");
			plotoptbutton.addActionListener(new Optredraw());
			optionscreen.add(plotoptbutton);

		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
		class Optredraw implements ActionListener{
			public void actionPerformed(ActionEvent arg0){
				plot.resolution=Integer.parseInt(resolinp.getText());
				float x1=Float.parseFloat(E1inp.getText());
				float x2=Float.parseFloat(E2inp.getText());
				float dE=x2-x1;
				plot.set_xscale(x1,dE);
				float a1=Float.parseFloat(A1inp.getText());
				float a2=Float.parseFloat(A2inp.getText());
				plot.setunit(unitsel.getSelectedIndex());
				plot.set_ascale(a1,a2-a1);
				curDefault.set_graphopts(plot.resolution,unitsel.getSelectedIndex());
				plot.reset();
				for (int i = 0; i <= Window.this.ncurve; i++)
				{
					new Curveplot(Window.this,i);
				}
				plot.repaint();
			}		
		}
	}

	/* ************************************************************ */
    /* ************************************************************ */
	/* *********************                  ********************* */
    /* *********************     ANALYSIS     ********************* */
	/* *********************                  ********************* */
	/* ************************************************************ */
	/* ************************************************************ */
	
	/* ******************************** */
	/* ******* Orbital analysis ******* */
	/* ******************************** */
	class Analorbmenu implements ActionListener {
		JButton spinbutton,orbitalbutton;
		private CurveSel selector;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Analysis of orbital contributions");
			optionscreen.add(optiontitle);
			
			JPanel l0 = new JPanel();
			selector=new CurveSel(1);
			l0.add(selector.Box());

			orbitalbutton= new JButton("Orbital analysis");
			orbitalbutton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent event){
					int icurve=selector.index();
					curve.get(icurve).transition.analysis(curve.get(icurve).getname(),0);
					/*Molcasfile molcas = curve.get(icurve).transition.getmolcas();
					molcas.analysis(curve.get(icurve));*/
				}
			});
			l0.add(orbitalbutton);
			optionscreen.add(l0);
			
			//spinbutton= new JButton("Spin analysis");
			//spinbutton.addActionListener(new ActionListener(){
			//	public void actionPerformed(ActionEvent event){
			//		int icurve=selector.index();
			//		curve.get(icurve).transition.analysis(Window.this,curve.get(icurve).getname(),1);
					/*Molcasfile molcas = curve.get(icurve).transition.getmolcas();
					molcas.analysis(curve.get(icurve));*/
			//	}
			//});
			//l0.add(spinbutton);
			optionscreen.add(l0);
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	
	/* ******************************** */
	/* ******* Curve operations ******* */
	/* ******************************** */
	class CurveOp implements ActionListener {
		private JTextField namefield;
		public void actionPerformed(ActionEvent arg0){
			final JTextField[] factors = new JTextField[Window.this.ncurve];
			optionscreen.removeAll();
			optiontitle.setText("Operations on curves");
			optionscreen.add(optiontitle);
			
			for(int i = 0; i < Window.this.ncurve; i++)
			{
				JPanel l0 = new JPanel();
				l0.add(new JLabel(curve.get(i).getname()));
				factors[i]=new JTextField("0.0");
				l0.add(factors[i]);
				optionscreen.add(l0);
			}
					
			namefield=new JTextField("Name of summed curve");
			optionscreen.add(namefield);
			
			JButton sumbutton= new JButton("Sum");
			sumbutton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent event){
					float tmp,energy;
					float[] fact=new float[Window.this.ncurve];
					int size=factors.length;
					for(int i = 0; i < size; i++)
					{
						fact[i]=Float.parseFloat(factors[i].getText());
					}
					for(int i = size; i < Window.this.ncurve; i++)
					{
						fact[i]=0;
					}
					String namecurve=namefield.getText();
	        			Transition trans=new Transition(Window.this);
	        			File output=trans.getfile();
					try {
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(output), "utf-8"));
						// Compute the new curve
						for(int epos = 0; epos < plot.resolution; epos++)
						{
							energy=plot.getx1()+plot.getxde()*plot.plotlist.get(0)[epos][0];
							tmp=0;
							for(int i = 0; i < Window.this.ncurve; i++)
							{
								tmp=tmp+plot.plotlist.get(i+1)[epos][0]*fact[i];
							}
							writer.write(String.valueOf(energy)+" "+String.valueOf(tmp)+"\n");
						}
						writer.close();
		        			addcurve(namecurve,3,trans,"",plot.getunit());
					}
					catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(new JFrame(), "Failed to create summed curve", "Error",JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			optionscreen.add(sumbutton);
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	/* ******************************** */
	/* ***** Integrated intensity ***** */
	/* ******************************** */
	class IntIntens implements ActionListener {
		private JButton integratebutton;
		private JTextField e1field, e2field;
		private CurveSel selector;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Integrated Intensity");
			optionscreen.add(optiontitle);
			
			JPanel l0 = new JPanel();
			selector=new CurveSel(0);
			l0.add(selector.Box());
			optionscreen.add(l0);
			
			float x1=plot.getx1();
			float dE=plot.getxde();
			JPanel l1 = new JPanel();
			l1.add(new JLabel("Energy range : from "));
			e1field=new JTextField(String.valueOf(x1));
			l1.add(e1field);
			l1.add(new JLabel("to "));
			e2field=new JTextField(String.valueOf(x1+dE));
			l1.add(e2field);
			optionscreen.add(l1);
		
			
			integratebutton= new JButton("Compute");
			integratebutton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent event){
					int icurve=selector.index();
					float e1=Float.parseFloat(e1field.getText());
					float e2=Float.parseFloat(e2field.getText());
					float intens=Curveplot.getintens(plot,icurve,e1,e2);
					JTextArea messagearea = new JTextArea("Integrated intensity between "+String.valueOf(e1)+" and "+String.valueOf(e2)+" : "+String.valueOf(intens));
			    	JScrollPane scrollPane = new JScrollPane(messagearea);
			    	scrollPane.setPreferredSize( new Dimension( 500, 20 ) );
			    	JOptionPane.showMessageDialog(null, scrollPane, "Intensity", JOptionPane.PLAIN_MESSAGE); 
				}
			});
			optionscreen.add(integratebutton);
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	
	/* ******************************** */
	/* ******* Curve alignment  ******* */
	/* ******************************** */
	
	class Align implements ActionListener {
		private JButton alignbutton;
		private CurveSel selector1;
		private CurveSel selector2;
		private JComboBox<String> energysel;
		private JComboBox<String> intensitysel;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Curve alignment");
			optionscreen.add(optiontitle);
			
			if (Window.this.ncurve<2)
			{
	    		  JOptionPane.showMessageDialog(new JFrame(),
	    				    "Requires at least 2 curves.",
	    				    "Input error",
	    				    JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JPanel l0 = new JPanel();
				l0.add(new JLabel("Reference curve:"));
				selector1=new CurveSel(0);
				l0.add(selector1.Box());
				optionscreen.add(l0);
				
				JPanel l1 = new JPanel();
				l1.add(new JLabel("Curve to align:"));
				selector2=new CurveSel(0);
				selector2.select(1);
				l1.add(selector2.Box());
				optionscreen.add(l1);
				
				JPanel l2 = new JPanel();
	  		  	l2.add(new JLabel("Energy alignment:"));
	  		  	energysel=new JComboBox<String>();
	  		  	energysel.addItem("None");
	  		  	energysel.addItem("Maxima");
	  		  	energysel.addItem("Half integrated intensity");
	  		  	energysel.setSelectedIndex(0);
	  		  	l2.add(energysel);
				optionscreen.add(l2);
				
				JPanel l3 = new JPanel();
	  		  	l3.add(new JLabel("Intensity alignment:"));
	  		  	intensitysel=new JComboBox<String>();
	  		  	intensitysel.addItem("None");
	  		  	intensitysel.addItem("Maxima");
	  			intensitysel.addItem("Integrated intensity");
	  			intensitysel.setSelectedIndex(0);
	  		  	l3.add(intensitysel);
				optionscreen.add(l3);
				
				
				alignbutton= new JButton("Align");
				alignbutton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent event){
						int curve1=selector1.index();
						int curve2=selector2.index();
						int iene=energysel.getSelectedIndex();
						int iintens=intensitysel.getSelectedIndex();
						float e1=plot.getx1();
						float dE=plot.getxde();
						float e2=e1+dE;
						float xshift=curve.get(curve2).getxoffset();
						switch (iene)
						{
							case 1:
								float max1=curve.get(curve1).emax;
								float max2=curve.get(curve2).emax;
								xshift=xshift+max1-max2;
								break;
							case 2:
								float grav1=Curveplot.halfintensity(plot,curve1,e1,e2);
								float grav2=Curveplot.halfintensity(plot,curve2,e1,e2);
								xshift=xshift+grav1-grav2;
								break;
						}
						curve.get(curve2).setxoffset(xshift);
						new Curveplot(Window.this,curve2+1);
						
						// Intensity alignment 
						float yscale=curve.get(curve2).getyscale();
						switch (iintens)
						{
							case 1:
								float mina1=curve.get(curve1).mina;
								float mina2=curve.get(curve2).mina;
								float maxagap1=curve.get(curve1).maxagap;
								float maxagap2=curve.get(curve2).maxagap;
								float maxa1=mina1+maxagap1;
								float maxa2=mina2+maxagap2;
								yscale=yscale*maxa1/maxa2;
								break;
							case 2:
								float intens1=Curveplot.getintens(plot,curve1,e1,e2);
								float intens2=Curveplot.getintens(plot,curve2,e1,e2);
								yscale=yscale*intens1/intens2;
								break;
						}
						curve.get(curve2).setyscale(yscale);
						
						new Curveplot(Window.this,curve2+1);
						plot.repaint();
					}
				});
				optionscreen.add(alignbutton);
			}
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
		
	}
	/* ******************************** */
	/* ******* Similarity analysis ******* */
	/* ******************************** */
	class Similarity implements ActionListener {
		JButton analysebutton;
		private CurveSel selector1;
		private CurveSel selector2;
		private JComboBox<String> methodsel;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Similarity analysis of 2 spectra");
			optionscreen.add(optiontitle);
			
			if (Window.this.ncurve<2)
			{
	    		  JOptionPane.showMessageDialog(new JFrame(),
	    				    "Requires at least 2 curves.",
	    				    "Input error",
	    				    JOptionPane.ERROR_MESSAGE);
			}
			else
			{
			
			JPanel l0 = new JPanel();
			selector1=new CurveSel(0);
			l0.add(selector1.Box());
			selector2=new CurveSel(0);
			selector2.select(1);
			l0.add(selector2.Box());
			optionscreen.add(l0);
			
			JPanel l1 = new JPanel();
  		  	l1.add(new JLabel("Method:"));
  		    methodsel=new JComboBox<String>();
  		  	methodsel.addItem("Euclidian distance");
  		  	methodsel.addItem("Cosine angle");
  		  	//methodsel.addItem("Integrated intensity");
  		  	//methodsel.addItem("Hybrid");
  		  	methodsel.setSelectedIndex(0);
  		  	l1.add(methodsel);
			optionscreen.add(l1);
			
			analysebutton=new JButton("Analyze");
			analysebutton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent event){
					int curve1=selector1.index();
					int curve2=selector2.index();
					int imethod=methodsel.getSelectedIndex();
					String result=Curveplot.similarity(plot,curve1,curve2,imethod);
					JTextArea messagearea = new JTextArea(result);
			    		JScrollPane scrollPane = new JScrollPane(messagearea);
			    		scrollPane.setPreferredSize( new Dimension( 500, 20 ) );
			    		JOptionPane.showMessageDialog(null, scrollPane, "Similarity", JOptionPane.PLAIN_MESSAGE); 
				}
			});
			optionscreen.add(analysebutton);
			}
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	/* ******************************** */
	/* ******* Scattering ******* */
	/* ******************************** */
	class Scatter implements ActionListener {
		JButton startbutton;
		private JTextField E1inc;
		private JTextField E2inc;
		private JTextField E1trans;
		private JTextField E2trans;
		private JTextField xresol;
		private JTextField yresol;
		private CurveSel selector;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("2-D scattering plots");
			optionscreen.add(optiontitle);
			
			JPanel l0 = new JPanel();
			selector=new CurveSel(1);
			l0.add(selector.Box());
			
			JPanel l1 = new JPanel();
			l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));			
			float x1=plot.getx1();
			float dE=plot.getxde();
			l1.add(new JLabel("Incident photon energy range:"));
			E1inc=new JTextField(String.valueOf(x1));
			l1.add(E1inc);
			E2inc=new JTextField(String.valueOf(x1+dE));
			l1.add(E2inc);
			optionscreen.add(l1);
			
			JPanel l2 = new JPanel();
			l2.setLayout(new BoxLayout(l2, BoxLayout.LINE_AXIS));
			l2.add(new JLabel("Energy transfer range:"));
			E1trans=new JTextField("-1");
			l2.add(E1trans);
			E2trans=new JTextField("5");
			l2.add(E2trans);
			optionscreen.add(l2);
			
			JPanel l3 = new JPanel();
			l3.setLayout(new BoxLayout(l3, BoxLayout.LINE_AXIS));
			l3.add(new JLabel("Resolution (x,y):"));
			xresol=new JTextField("300");
			l3.add(xresol);
			yresol=new JTextField("300");
			l3.add(yresol);
			optionscreen.add(l3);
			
			startbutton= new JButton("Plot");
			startbutton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent event){
					int icurve=selector.index();
					float e1i=Float.parseFloat(E1inc.getText());
					float e2i=Float.parseFloat(E2inc.getText());
					float e1t=Float.parseFloat(E1trans.getText());
					float e2t=Float.parseFloat(E2trans.getText());
					int xres=Integer.parseInt(xresol.getText());
					int yres=Integer.parseInt(yresol.getText());
					new Scatterplot(curve.get(icurve),e1i,e2i,e1t,e2t,xres,yres,plot.getunit());
				}
			});
			l0.add(startbutton);
			optionscreen.add(l0);
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	
	/* ************************************************************ */
    /* ************************************************************ */
	/* *********************                  ********************* */
    /* *********************     EXPORT       ********************* */
	/* *********************                  ********************* */
	/* ************************************************************ */
	/* ************************************************************ */
	
	/* ******************************** */
	/* *******     ExportXY     ******* */
	/* ******************************** */
	class ExportXY implements ActionListener {
		JButton exportbutton;
		private CurveSel selector;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Export curve in XY format");
			optionscreen.add(optiontitle);
		
			JPanel l0 = new JPanel();
			selector=new CurveSel(0);
			l0.add(selector.Box());
			
			exportbutton= new JButton("Export");
			exportbutton.addActionListener(new ActionListener(){
			      public void actionPerformed(ActionEvent event){
			    	  String message="";
			    	  JTextArea messagearea;
			    	  float tmp;
			    	  int icurve=selector.index();
			    	  for (int j=0; j<plot.resolution; j++) {
			    		  	tmp=plot.getx1()+plot.getxde()*plot.plotlist.get(0)[j][0];
							message=message+String.format("%f", tmp)+"   "+String.format("%6.3e",plot.plotlist.get(icurve+1)[j])+"\n";
							}
			    	  messagearea = new JTextArea(message);
			    	  JScrollPane scrollPane = new JScrollPane(messagearea);
			    	  scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
			    	  JOptionPane.showMessageDialog(null, scrollPane, "Curve "+String.valueOf(icurve+1), JOptionPane.PLAIN_MESSAGE);      
			      }
			});
			l0.add(exportbutton);
			optionscreen.add(l0);
	        
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	/* ******************************** */
	/* *******     ExportTrans  ******* */
	/* ******************************** */
	class ExportTrans implements ActionListener {
		JButton exportbutton;
		private CurveSel selector;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Export list of transitions");
			optionscreen.add(optiontitle);
		
			JPanel l0 = new JPanel();
			selector=new CurveSel(0);
			l0.add(selector.Box());
			
			exportbutton= new JButton("Export");
			exportbutton.addActionListener(new ActionListener(){
			      public void actionPerformed(ActionEvent event){
			    	  JTextArea messagearea;
			    	  int icurve=selector.index();
			    	  messagearea = new JTextArea();
			    	  try{
			    	  messagearea.read(new FileReader(curve.get(icurve).transition.getfile()),"");
			    	  }
			    	  catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(new JFrame(), "Failed to print file", "Error",JOptionPane.ERROR_MESSAGE);
						}
			    	  JScrollPane scrollPane = new JScrollPane(messagearea);
			    	  scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
			    	  JOptionPane.showMessageDialog(null, scrollPane, "Curve "+String.valueOf(icurve+1), JOptionPane.PLAIN_MESSAGE);      
			      }
			});
			l0.add(exportbutton);
			optionscreen.add(l0);
	        
			
		    optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	/* ******************************** */
	/* *******   Export Image   ******* */
	/* ******************************** */
	class ExportImg implements ActionListener {
		JButton exportbutton;
		JTextField widthfield, heightfield,lwidthfield;
		File selectedFile;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Export graph picture");
			optionscreen.add(optiontitle);
			
			JPanel l1 = new JPanel();
			l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
			l1.add(new JLabel("Width"));
			widthfield=new JTextField("1000");
			l1.add(widthfield);
			l1.add(new JLabel("Height"));
			heightfield=new JTextField("600");
			l1.add(heightfield);
			optionscreen.add(l1);
			
			JPanel l2 = new JPanel();
			l2.setLayout(new BoxLayout(l2, BoxLayout.LINE_AXIS));
			l2.add(new JLabel("Line width"));
			lwidthfield=new JTextField("1");
			l2.add(lwidthfield);
			optionscreen.add(l2);
			
			
			JButton openbutton = new JButton("Save as");
		    openbutton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		    	  JFileChooser fileChooser = new JFileChooser();
		    	  fileChooser.setCurrentDirectory(curDefault.get_dfile());
		    	  int returnValue = fileChooser.showSaveDialog(null);
		    	  if (returnValue == JFileChooser.APPROVE_OPTION) {
		    		  selectedFile = fileChooser.getSelectedFile();
		    		  curDefault.set_dfile(selectedFile.getParentFile());
		    		  int width=Integer.parseInt(widthfield.getText());
			    	  int height=Integer.parseInt(heightfield.getText());
			    	  Dimension d=new Dimension(width,height);
			    	  BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			    	  Graphics ig2 = bi.createGraphics();
			    	  plot.update(ig2,d,true,Integer.parseInt(lwidthfield.getText()));
			    	  //plot.update(Window.this,ig2,d,true,Integer.parseInt(lwidthfield.getText()));
			    	  try {
			    		  if (!selectedFile.getName().endsWith(".png"))
			    		  {
			    			  selectedFile=new File(selectedFile.getParent(),selectedFile.getName()+".png");
			    		  }
			    		  boolean confirmed=true;
			    		  if (selectedFile.exists())
			    		  {
			    			  confirmed=(JOptionPane.showOptionDialog(null, "Do you want to overwrite existing file?", "Overwrite Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==0);
			    		  }
			    		  if (confirmed)
			    		  {
			    			  ImageIO.write(bi, "PNG", selectedFile);
			    		  }
			    	  }
			    	  catch (IOException ie) {
			    	      ie.printStackTrace();
			    	      JOptionPane.showMessageDialog(new JFrame(), "Failed to export image", "Error",JOptionPane.ERROR_MESSAGE);
			    	  }
		    	  }
		      }
		    });
		    optionscreen.add(openbutton);
			
			optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	
	/* ************************************************************ */
    /* ************************************************************ */
	/* *********************                  ********************* */
    /* *********************     SETTINGS     ********************* */
	/* *********************                  ********************* */
	/* ************************************************************ */
	/* ************************************************************ */
	
	/* ******************************** */
	/* *******   Set settings   ******* */
	/* ******************************** */
	
	class defaultmenu implements ActionListener {
		private JTextField resolinp,gausswfield, lorentzfield, lorentz2field, lorentzsplitfield,preflabel;
		private JComboBox<String> unitsel,broadsel,prefsel;
		private JPanel l7,l8,l9;
		
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Save settings");
			optionscreen.add(optiontitle);	
			
			JPanel l1 = new JPanel();
		    l1.setLayout(new BoxLayout(l1, BoxLayout.LINE_AXIS));
			l1.add(new JLabel("Number of curve points:"));
			resolinp=new JTextField(String.valueOf(plot.resolution));
			l1.add(resolinp);
			optionscreen.add(l1);
			
  		  	JPanel l4 = new JPanel();
  		  	int iunit = plot.getunit();
  		  	l4.setLayout(new BoxLayout(l4, BoxLayout.LINE_AXIS));
  		  	l4.add(new JLabel("Unit:"));
  		  	unitsel=new JComboBox<String>();
  		  	unitsel.addItem("hartree");
  		  	unitsel.addItem("eV");
  		  	unitsel.addItem("kcal/mol");
  		  	unitsel.addItem("kJ/mol");
  		  	unitsel.addItem("cm-1");
  		  	//unitsel.addItem("nm");
  		  	unitsel.setSelectedIndex(iunit);
  		  	l4.add(unitsel);
  		  	optionscreen.add(l4);
  		  	
			JPanel l5 = new JPanel();
			l5.add(new JLabel("Broadening type"));
			broadsel=new JComboBox<String>();
			broadsel.addItem("Gaussian broadening");
			broadsel.addItem("Gaussian + Lorentzian broadening");
			broadsel.addItem("Gaussian + dual Lorentzian broadening");
			l5.add(broadsel);
			optionscreen.add(l5);
			int ibroad=curDefault.get_dbroad().getbroadtype();
			broadsel.setSelectedIndex(ibroad);
			
			broadsel.addActionListener(new ActionListener(){
			      public void actionPerformed(ActionEvent event){				
			    	  int ibroad = broadsel.getSelectedIndex();
			    	  switch (ibroad)
			    	  {
			    	  case 0:
			    		  l7.setVisible(false);
			    		  l8.setVisible(false);
			    		  l9.setVisible(false);
			    		  break;
			    	  case 1:
			    		  l7.setVisible(true);
			    		  l8.setVisible(false);
			    		  l9.setVisible(false);
			    		  break;
			    	  case 2:
			    		  l7.setVisible(true);
			    		  l8.setVisible(true);
			    		  l9.setVisible(true);
			    		  break;
			    	  }
			      }
			});
			
			
			JPanel l6 = new JPanel();
			l6.setLayout(new BoxLayout(l6, BoxLayout.LINE_AXIS));
			l6.add(new JLabel("Gaussian width (HWHM):"));
			gausswfield = new JTextField(String.valueOf(curDefault.get_dbroad().getgaussw()));
			l6.add(gausswfield);
			optionscreen.add(l6);
			
			l7 = new JPanel();
			l7.setLayout(new BoxLayout(l7, BoxLayout.LINE_AXIS));
			l7.add(new JLabel("Lorentzian width (HWHM):"));
			lorentzfield  = new JTextField(String.valueOf(curDefault.get_dbroad().getlorw1()));
			l7.add(lorentzfield);
			if (ibroad<1) {l7.setVisible(false);}
			optionscreen.add(l7);

			l8 = new JPanel();
			l8.setLayout(new BoxLayout(l8, BoxLayout.LINE_AXIS));
			l8.add(new JLabel("Second lorentzian width:"));
			lorentz2field = new JTextField(String.valueOf(curDefault.get_dbroad().getlorw2()));
			l8.add(lorentz2field);
			if (ibroad<2) {l8.setVisible(false);}
			optionscreen.add(l8);
				
			l9 = new JPanel();
			l9.setLayout(new BoxLayout(l9, BoxLayout.LINE_AXIS));
			l9.add(new JLabel("Lorentzian split energy"));
			lorentzsplitfield = new JTextField(String.valueOf(curDefault.get_dbroad().getlorsplit()));
			l9.add(lorentzsplitfield);
			optionscreen.add(l9);
			if (ibroad<2) {l9.setVisible(false);}
			
			JPanel l0 = new JPanel();
			prefsel=new JComboBox<String>();
			String[] preflist=curDefault.get_preflist();
			for(int i = 0; i < preflist.length; i++)
			{
				if (!preflist[i].equals("Default"))
				{
					prefsel.addItem(preflist[i]);
				}
			}
			prefsel.addItem("create new");
			prefsel.setSelectedItem("create new");
			l0.add(prefsel);
			preflabel=new JTextField("Setting name");
			preflabel.setVisible(true);
			l0.add(preflabel);
			
			prefsel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae) {
					if (prefsel.getSelectedItem().equals("create new"))
					{
						preflabel.setText("Setting name");
						preflabel.setVisible(true);
					}
					else
					{
						preflabel.setText(String.valueOf(prefsel.getSelectedItem()));
						preflabel.setVisible(false);
					}
				}
			});
			optionscreen.add(l0);
			
			
			JButton savebutton = new JButton("Save setting");
			savebutton.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent ae) {
			    	  Broadening broad=new Broadening();
			    	  switch (broadsel.getSelectedIndex())
			    	  {
			    	  case 0:
			    		  broad.set_Broadening(Float.parseFloat(gausswfield.getText()));
			    		  break;
			    	  case 1:
			    		  broad.set_Broadening(Float.parseFloat(gausswfield.getText()),Float.parseFloat(lorentzfield.getText()));
			    		  break;
			    	  case 2:
			    		  broad.set_Broadening(Float.parseFloat(gausswfield.getText()),Float.parseFloat(lorentzfield.getText()),Float.parseFloat(lorentzsplitfield.getText()),Float.parseFloat(lorentz2field.getText()));
			    		  break;
			    	  }
			    	  curDefault.set_dbroad(broad);
			    	  
			    	  curDefault.set_graphopts(Integer.parseInt(resolinp.getText()),unitsel.getSelectedIndex());

			    	  String label=preflabel.getText();
			    	  boolean confirm=true;
			    	  if (curDefault.labelexists(label))
			    	  {
			    		  confirm=(JOptionPane.showOptionDialog(null, "Do you want to overwrite existing setting?", "Overwrite Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==0);
			    	  }
			    	  if (confirm)
			    	  {
			    		  curDefault.add_default(label); 
			    	  }
			      }
			});
			optionscreen.add(savebutton);
			
			optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
	
	/* ******************************** */
	/* *******   Load settings  ******* */
	/* ******************************** */
	
	class loaddefaultmenu implements ActionListener {
		JComboBox<String> prefsel;
		public void actionPerformed(ActionEvent arg0){
			optionscreen.removeAll();
			optiontitle.setText("Load settings");
			optionscreen.add(optiontitle);
			
			JPanel l0 = new JPanel();
			prefsel=new JComboBox<String>();
			String[] preflist=curDefault.get_preflist();
			for(int i = 0; i < preflist.length; i++)
			{
				if (!preflist[i].equals("Default"))
				{
					prefsel.addItem(preflist[i]);
				}
			}
			l0.add(prefsel);
			optionscreen.add(l0);
			
			JButton loadbutton = new JButton("Load");
			optionscreen.add(loadbutton);
			loadbutton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					curDefault.readdefaults(String.valueOf(prefsel.getSelectedItem()));
					plot.reset();
					for (int i = 0; i <= Window.this.ncurve; i++)
					{
						new Curveplot(Window.this,i);
					}
					if (Window.this.ncurve>0){plot.repaint();}
				}
			});
			
			optionscreen.revalidate();
		    optionscreen.repaint();
		}
	}
			
	/* ************************************************************ */
    /* ************************************************************ */
	/* *********************                  ********************* */
    /* ********************* PLOTTING WINDOW  ********************* */
	/* *********************                  ********************* */
	/* ************************************************************ */
	/* ************************************************************ */
	
	class Refreshplot implements ActionListener {
		public void actionPerformed(ActionEvent arg0){
			plot.repaint();
		}
	}

	public void setcurvelist()
	{
		JPanel curvelist = new JPanel();
		curvelist.setLayout(new WrapLayout());
		plotscreen.removeAll();
		for(int i = 0; i < this.ncurve; i++)
		{
			whichcurve.get(i).setForeground(curve.get(i).getcolor());
			curvelist.add(whichcurve.get(i));
		}
		plotscreen.add(curvelist,BorderLayout.NORTH);
	    plotscreen.revalidate();
	    plotscreen.repaint();
	}
	 class CurveSel {
		 JComboBox<comboitem> curvesel=new JComboBox<comboitem>();
		 int[] jcurve = new int[Window.this.ncurve];
		 public CurveSel(int itype)
		 {
			 int j=0;
			 for(int i = 0; i < Window.this.ncurve; i++)
			 {
				 if (itype==0||curve.get(i).gettype()==itype)
				 {
					 jcurve[j]=i;
					 curvesel.addItem(new comboitem(curve.get(i).getname(),i));
					 j++;
				 }
			 }
		 }
		public JComboBox<comboitem> Box()
		{
			return(curvesel);
		}
		public int index()
		{
			int result=curvesel.getSelectedIndex();
			if (result<0) {result=0;}
			else { result=jcurve[result];}
			return result;
		}
		public void select(int icurve)
		{
			curvesel.setSelectedIndex(icurve);
		}
	}

	public void init_graph(){
		/*  Find min and max energies */
		float minE=0;
		float maxE=0;
		if (curve.get(0).gettype()>0) // obsolete...
		{
			try {
				BufferedReader reader = new BufferedReader(new FileReader(curve.get(0).transition.getfile()));
				String text;
				float n;
				boolean isntrans=(curve.get(0).gettype()==2);
				boolean is1=true;
				 while ((text = reader.readLine()) != null) {
					 if (!text.startsWith("#"))
					 {
						 n=Float.parseFloat(text.trim().split(" ")[0]);
						 if (isntrans)
						 {
							 isntrans=false;
						 }
						 else
						 {
							 if (is1)
							 {
								 minE=n;
								 maxE=n;
								 is1=false;
							 }
							 minE=Math.min(n,minE);
							 maxE=Math.max(n,maxE);
						 }
					 }
				 }
				 reader.close();
			} catch (IOException e) {
			    e.printStackTrace();
			    JOptionPane.showMessageDialog(new JFrame(), "Failed to initialize the graph", "Error",JOptionPane.ERROR_MESSAGE);
			}
		}
		/*    Determine optimal parameters */
		float gap=maxE-minE;
		if (curve.get(0).gettype()<3)
		{
			float broad=curDefault.get_dbroad().getgaussw();
			if (curDefault.get_dbroad().islorentz()) {broad+=curDefault.get_dbroad().getlorw1();}
			//add some space on the sides
			minE=minE-3*broad;
			gap=gap+6*broad;		
		}
		plot.set_xscale(minE,gap);
		
	}
}