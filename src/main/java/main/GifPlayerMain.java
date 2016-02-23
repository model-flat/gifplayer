package main;

import java.awt.Color;
import java.awt.Point;

import javax.swing.JWindow;

public class GifPlayerMain {
	public static boolean debug = false;
	public static boolean onTop = true;
	public static String imageSource = "";
	public static Point loc = null;
	public static boolean centredLoc = false;
	public static int rotationAngle = 0;
	public static double scale = 1.0;
	public static boolean refx = false;
	public static boolean refy = false;
	public static boolean displayHelp = false;

	public static void main(String[] args) {
		parseArgs(args);
		
		if (displayHelp) {
			System.out.println(HelpContent.text + HelpContent.version);
			return;
		}
		
    	JWindow frame = new JWindow();

    	frame.setBackground(new Color(0, 0, 0, 0));
   		GifPlayerPane ip = new GifPlayerPane(frame, imageSource, debug);
		frame.setContentPane(ip);
    	frame.setAlwaysOnTop(onTop);
    	frame.pack();
    	if (loc == null) frame.setLocationRelativeTo(null);
    	else {
    		if (centredLoc) 
    			frame.setLocation(loc.x - ip.gif.width/2, loc.y - ip.gif.height/2);
    		else
    			frame.setLocation(loc.x, loc.y);
    	}
    	frame.setVisible(true);
	}
	
	private static void parseArgs(String[] args) {
		if (args.length == 0) {
            displayHelp = true;
            return;
        }

		for (int i = 0; i < args.length - 1; i++) {
			switch (args[i]) {
                case "-?":
                case "--help":
                case "-h":
                    displayHelp = true;
                    return;
                case "-ref": {
                    switch (args[++i]) {
                        case "x": {
                            refy = true;
                            break;
                        }
                        case "y": {
                            refx = true;
                            break;
                        }
                    }
                    continue;
                }
                case "-debug": {
                    debug = true;
                    continue;
                }
                case "-nontop": {
                    onTop = false;
                    continue;
                }
                case "-cloc": {
                    centredLoc = true;
                    continue;
                }
                case "-loc": {
                    String[] sa = args[++i].split(":");
                    loc = new Point(Integer.valueOf(sa[0]), Integer.valueOf(sa[1]));
                    continue;
                }
                case "-rot": {
                    rotationAngle = Integer.valueOf(args[++i]);
                    continue;
                }
                case "-scale": {
                    scale = Double.valueOf(args[++i]);
                }
            }
		}
		imageSource = args[args.length - 1];
	}

}
