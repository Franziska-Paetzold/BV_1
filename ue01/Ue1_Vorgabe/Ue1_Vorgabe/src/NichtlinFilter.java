// BV Ue1 WS2016/17 Vorgabe
//
// Copyright (C) 2015 by Klaus Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.*;

public class NichtlinFilter extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String author = "Paetzold";		// TODO: type in your name here
	private static final String initialFilename = "lena_klein.png";
	private static final File openPath = new File(".");
	private static final int borderWidth = 5;
	private static final int maxWidth = 446;
	private static final int maxHeight = maxWidth;
	private static final int maxNoise = 30;	// in per cent
	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView dstView;			// filtered image view

	private int[] origPixels = null;
	
	private JLabel statusLine = new JLabel("     "); // to print some status text
	
	private JComboBox<String> noiseType;
	private JLabel noiseLabel;
	private JSlider noiseSlider;
	private JLabel noiseAmountLabel;
	private boolean addNoise = false;
	private double noiseFraction = 0.01;	// fraction for number of pixels to be modified by noise
	
	private JComboBox<String> filterType;
	

	public NichtlinFilter() {
        super(new BorderLayout(borderWidth, borderWidth));

        setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
 
        // load the default image
        File input = new File(initialFilename);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
        
		// convert to grayscale
		makeGray(srcView);
		        
        // keep a copy of the grayscaled original image pixels
        origPixels = srcView.getPixels().clone();
       
		// create empty destination image of same size
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
        // control panel
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,borderWidth,0,0);

		// load image button
        JButton load = new JButton("Open Image");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		loadFile(openFile());
        		// convert to grayscale
        		makeGray(srcView);  
                // keep a copy of the grayscaled original image pixels
                origPixels = srcView.getPixels().clone();
        		calculate(true);
        	}        	
        });
         
        // selector for the noise method
        String[] noiseNames = {"No Noise ", "Salt & Pepper "};
        
        noiseType = new JComboBox<String>(noiseNames);
        noiseType.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		addNoise = noiseType.getSelectedIndex() > 0;
        		noiseLabel.setEnabled(addNoise);
                noiseSlider.setEnabled(addNoise);
                noiseAmountLabel.setEnabled(addNoise);
        		calculate(true);
        	}
        });
        
        // amount of noise
        noiseLabel = new JLabel("Noise:");
        noiseAmountLabel = new JLabel("" + Math.round(noiseFraction * 100.0)  + " %");
        noiseSlider = new JSlider(JSlider.HORIZONTAL, 0, maxNoise, (int) Math.round(noiseFraction * 100.0));
        noiseSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		noiseFraction = noiseSlider.getValue() / 100.0;
        		noiseAmountLabel.setText("" + Math.round(noiseFraction * 100.0) + " %");
        		calculate(true);
        	}
        });
        noiseLabel.setEnabled(addNoise);
        noiseSlider.setEnabled(addNoise);
        noiseAmountLabel.setEnabled(addNoise);

        // selector for filter
        String[] filterNames = {"No Filter", "Min Filter", "Max Filter", "Box Filter", "Median Filter"};
        filterType = new JComboBox<String>(filterNames);
        filterType.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		calculate(false);
        	}
        });
        
        controls.add(load, c);
        controls.add(noiseType, c);
        controls.add(noiseLabel, c);
        controls.add(noiseSlider, c);
        controls.add(noiseAmountLabel, c);
        controls.add(filterType, c);
        
        // images panel
        JPanel images = new JPanel(new GridLayout(1,2));
        images.add(srcView);
        images.add(dstView);
        
        // status panel
        JPanel status = new JPanel(new GridBagLayout());
        
        status.add(statusLine, c);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        
        calculate(true);
                       
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	
	private void loadFile(File file) {
		if(file != null) {
    		srcView.loadImage(file);
    		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
    		// create empty destination image of same size
    		dstView.resetToSize(srcView.getImgWidth(), srcView.getImgHeight());
    		frame.pack();
		}
		
	}
	
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Nonlinear Filters - " + author);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new NichtlinFilter();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	private void calculate(boolean createNoise) {
		long startTime = System.currentTimeMillis();
		
		if(createNoise) {
			// start with original image pixels
			srcView.setPixels(origPixels);
			// add noise
			if(addNoise) 
				makeNoise(srcView);
			// make changes visible
			srcView.applyChanges();
		}
		
		// apply filter
		filter();

		// make changes visible
		dstView.applyChanges();
		
		long time = System.currentTimeMillis() - startTime;
    	statusLine.setText("Processing Time = " + time + " ms");
	}
	
	// argb-Werte auslesen, alles als ein Integer gespeichert, aufeinanderfolgende Pixel
	public ArrayList<Integer> argbAuslesen(int pixels[], int i)
	{
		ArrayList<Integer> rgb = new ArrayList<Integer>();
		int r= (pixels[i] >> 16) & 0xff;
		rgb.add(r);
		int g= (pixels[i] >> 8) & 0xff;
		rgb.add(g);
		int b= pixels[i] & 0Xff;
		rgb.add(b);
		
		return rgb;
		
	}
	
	public int[] pixelZurueckschreiben(int pixels[], int i, int r, int g, int b)
	{
		pixels[i]= (0xff0000 << 24) | (r<<16) | (g<<8) | b;
		return pixels;
	}
	
	//Grauwerte
	
	private void makeGray(ImageView imgView) {
		int pixels[] = imgView.getPixels();
		
		// TODO: convert pixels to grayscale
		
		// loop over all pixels
		for(int i = 0; i < pixels.length; i++) 
		{
			ArrayList<Integer> rgb=argbAuslesen(pixels, i);
			int r=rgb.get(0);
			int g=rgb.get(1);
			int b=rgb.get(2);
			
			//Berechnung Grau-Wert
			//int gray = (int) (0.299*r + 0.587*g + 0.114*b);
			int gray = (r+g+b)/3;
			
			//für jede Farbkomponente speichern
			r=g=b=gray;
			
			pixelZurueckschreiben(pixels, i, r, g, b);
			
		}
	}
	
	public boolean isSecond(int zahl)
	{
		if (zahl%2==0)
		{
			return true;
		}
			return false;
	}
	
	
	
	//salt&pepper
	private void makeNoise(ImageView imgView) {
		int pixels[] = imgView.getPixels();
		
		// TODO: add noise to pixels
		
		//Anzahl der zuverändernden Pixel aus Prozentsatz bestimmen
		int pixAnz =  srcView.getImgWidth() * srcView.getImgHeight();
		int pixAnzToChange= (int) ((noiseFraction *pixAnz)+0.5) ;
		//System.out.println(pixAnz + ", " + noiseFraction + ", " + pixAnzToChange);
		
		int counter =1;
		for(int i=0; i < pixAnzToChange; i++ )
		{
			//Zufallspunkt bestimmen zw. 0 bis Breite-1 bzw. Höhe-1
			int newX = (int) (Math.random()*srcView.getImgWidth());
			int newY = (int) (Math.random()*srcView.getImgHeight());
			//pos=y*width+x
			//int pixelToChange= newY*srcView.getWidth()+newX;
			//System.out.println(srcView.getWidth() + ", " + newX + ", " + newY +", "+ pixelToChange);
			int pixelToChange=newX*newY;
			
			ArrayList<Integer> rgb=argbAuslesen(pixels, i);
			int r=rgb.get(0);
			int g=rgb.get(1);
			int b=rgb.get(2);
			
			//Hälfte schwarz, andere Hälfte weiß
			if (isSecond(counter)==true)
				{r=g=b=255;}
			else
				{r=g=b=0;}
			//System.out.println(isSecond(counter) + ", " + counter);
			counter++;
			
			pixelZurueckschreiben(pixels, pixelToChange, r, g, b);
			
		}
		
	}
	
	public int[] getPixelUmgebung(int x, int y, int[] srcView, int width, int height)
	{
		int[] pixelUmgebung = new int[9];
		int i = 0;
		//3x3 Umgebung -1, 0, 1 
		for (int yKernel = -1; yKernel <= 1; yKernel++) 
		{
			for (int xKernel = -1; xKernel <= 1; xKernel++) 
			{
				//Bildrandermittlung
				if (x == (width - 1) || x == 0 || y == (height - 1) || y == 0) 
				{
					//Wenn Rand, dann aktuellen Pixel nochmal = "konstant fortsetzen"
					pixelUmgebung[i] = srcView[x + width * y];
				} else 
				{
					//Wenn kein Rand, dann "ganz normale" Pixelumgebung 
					pixelUmgebung[i] = srcView[x + xKernel + width * (y + yKernel)];
				}

				i++;
			}
		}
		return pixelUmgebung;
		
	}
	
	private void filter() {
		int src[] = srcView.getPixels();
		int dst[] = dstView.getPixels();
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		int filterIndex = filterType.getSelectedIndex();
		
		// TODO: implement filters 
		
		//Minimumfilter, wählt den kleinsten Wert aus
		if (filterIndex==1)
		{
			//Bild durchlaufen
			for (int y=0; y<height; y++)
			{
				for (int x=0; x<width; x++)
				{
					int currPixel = y*width+x;
					int[] pixelUmgebung = getPixelUmgebung(x, y, src, width, height);
					
					//Farbwerte ermitteln und zwischensppeichern
					ArrayList<Integer> rgbKernel = new ArrayList<Integer>();
					for (int i=0; i<pixelUmgebung.length; i++)
					{
						ArrayList<Integer> rgb=argbAuslesen(pixelUmgebung, i);
						//ein Wert reicht, da im Graustufenbild r=g=b
						int gray=rgb.get(0);
						rgbKernel.add(gray);
					}
					
					//min-Filter
					int min;
					if(rgbKernel.get(0) < rgbKernel.get(1))
					{
						min=rgbKernel.get(0);
					}
					else
					{
						min=rgbKernel.get(1);
					}
					
					for (int j=2; j<9; j++)
					{
						if(rgbKernel.get(j) < min)
						{
							min = rgbKernel.get(j);
						}
					}
					
					pixelZurueckschreiben(dst, currPixel, min, min, min);
					
				}
			}
			
		}
		
		//Maximumfilter, wählt den größten Wert aus 
		if (filterIndex==2)
		{
			//Bild durchlaufen
			for (int y=0; y<height; y++)
			{
				for (int x=0; x<width; x++)
				{
					int currPixel = y*width+x;
					int[] pixelUmgebung = getPixelUmgebung(x, y, src, width, height);
					
					//Farbwerte ermitteln und zwischensppeichern
					ArrayList<Integer> rgbKernel = new ArrayList<Integer>();
					for (int i=0; i<pixelUmgebung.length; i++)
					{
						ArrayList<Integer> rgb=argbAuslesen(pixelUmgebung, i);
						//ein Wert reicht, da im Graustufenbild r=g=b
						int gray=rgb.get(0);
						rgbKernel.add(gray);
					}
					
					//max-Filter
					int max;
					if(rgbKernel.get(0) > rgbKernel.get(1))
					{
						max =rgbKernel.get(0);
					}
					else
					{
						max=rgbKernel.get(1);
					}
					
					for (int j=2; j<9; j++)
					{
						if(rgbKernel.get(j) > max)
						{
							max = rgbKernel.get(j);
						}
					}
					
					pixelZurueckschreiben(dst, currPixel, max, max, max);
					
				}
			}
			
		}
		
		//Boxfilter, errechnet einen Mittelwert
		if (filterIndex==3)
		{
			//Bild durchlaufen
			for (int y=0; y<height; y++)
			{
				for (int x=0; x<width; x++)
				{
					int currPixel = y*width+x;
					int[] pixelUmgebung = getPixelUmgebung(x, y, src, width, height);
					
					//Farbwerte ermitteln und zwischensppeichern
					ArrayList<Integer> rgbKernel = new ArrayList<Integer>();
					for (int i=0; i<pixelUmgebung.length; i++)
					{
						ArrayList<Integer> rgb=argbAuslesen(pixelUmgebung, i);
						//ein Wert reicht, da im Graustufenbild r=g=b
						int gray=rgb.get(0);
						rgbKernel.add(gray);
					}

					
					//Box-Filter /Mittelwert
					int average=0;
					for (int j=0; j<9; j++)
					{
						average= average+rgbKernel.get(j);
					}
					average= average/9;
					
					pixelZurueckschreiben(dst, currPixel, average, average, average);
					
				}
			}
		}
		
		//Medianfilter, wählt den mittigen Wert
		if (filterIndex==4)
		{
			//Bild durchlaufen
			for (int y=0; y<height; y++)
			{
				for (int x=0; x<width; x++)
				{
					int currPixel = y*width+x;
					int[] pixelUmgebung = getPixelUmgebung(x, y, src, width, height);
					
					//Farbwerte ermitteln und zwischensppeichern
					ArrayList<Integer> rgbKernel = new ArrayList<Integer>();
					for (int i=0; i<pixelUmgebung.length; i++)
					{
						ArrayList<Integer> rgb=argbAuslesen(pixelUmgebung, i);
						//ein Wert reicht, da im Graustufenbild r=g=b
						int gray=rgb.get(0);
						rgbKernel.add(gray);
					}
					
					//Medianfilter, mittelster Wert
					//1. sortieren
					for (int i=rgbKernel.size(); i>1; i--)
						{
							for(int j=0; j<i-1; j++)
							{
								if(rgbKernel.get(j)> rgbKernel.get(j+1))
								{
									//tauschen									
									rgbKernel.add(j, rgbKernel.get(j+1));
									//Redundanz, alte Stelle löschen
									rgbKernel.remove(j+2);
									}
							}
						}
					
					// 2. 4 = Mitte von 9
					int median = rgbKernel.get(4); 
					pixelZurueckschreiben(dst, currPixel, median, median, median);
				}
				}
			}
		}
	}



