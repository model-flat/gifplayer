package main;

import utils.GifDecoder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;

public class GifPlayerPane extends JPanel {
	private static final long serialVersionUID = -7819840475433972214L;
	public Thread operator;
	public GifDecoder gif;
	public JWindow parentWindow;
	private BufferedImage nextImage;
	
	public boolean debugMode;
	private boolean old = false;
	
	public GifPlayerPane(JWindow parentWindow, String gifSource, boolean debug) {
		this.parentWindow = parentWindow; // 
		this.debugMode = debug;
		this.gif = new GifDecoder();
		this.gif.read(gifSource);

		if (debugMode) {
			JButton button = new JButton("Close");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			add(button);
			setBorder(new CompoundBorder(new LineBorder(Color.RED), new EmptyBorder(0, 0, 250, 0)));
		}

		this.addMouseListener(new MouseInputListener() {
			private Point initialLoc;
			private Point initialMouseLoc;

			@Override
			public void mouseClicked(MouseEvent arg0) {	
				if ((arg0.getModifiers() & 
						(MouseEvent.CTRL_MASK + MouseEvent.ALT_MASK)) == 
						(MouseEvent.CTRL_MASK + MouseEvent.ALT_MASK)) {
					System.exit(0);
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) { }

			@Override
			public void mouseExited(MouseEvent arg0) { }

			@Override
			public void mousePressed(MouseEvent arg0) {
				initialLoc = ((GifPlayerPane)arg0.getSource()).parentWindow.getLocation();
				initialMouseLoc = arg0.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				((GifPlayerPane)arg0.getSource()).parentWindow.setLocation(
						initialLoc.x - initialMouseLoc.x + arg0.getX(), 
						initialLoc.y - initialMouseLoc.y + arg0.getY());
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {	}

			@Override
			public void mouseMoved(MouseEvent arg0) { }

		});
		
		this.setDoubleBuffered(true);

		setOpaque(false);
		setLayout(new GridBagLayout());

		transformGIF(gif, GifPlayerMain.rotationAngle, GifPlayerMain.scale, GifPlayerMain.refx, GifPlayerMain.refy);
		
		nextImage = gif.getImage();
		operator = new Thread(new Operator(this));
		operator.start(); // start looping
	}
  
    public static void transformGIF(GifDecoder gif, int de_angle, double scale, boolean reflectX, boolean reflectY) {
    	if (!(reflectX || reflectY || de_angle > 0 || scale != 1.0)) return;
    	de_angle %= 360;
    	
    	double angle = Math.toRadians(de_angle);
		int w = (int)Math.round(
				Math.abs(Math.sin(angle) * gif.height * scale) + 
				Math.abs(Math.cos(angle) * gif.width * scale)
				);
		int h = (int)Math.round(
				Math.abs(Math.cos(angle) * gif.height * scale) + 
				Math.abs(Math.sin(angle) * gif.width * scale)
				);
        int transparency = gif.getImage().getColorModel().getTransparency();

        AffineTransform rx = new AffineTransform();
        AffineTransform ry = new AffineTransform();
        if (reflectX) {
        	rx.scale(-1, 1);
        	rx.translate(-w, 0);
        }
        if (reflectY) {	
        	ry.scale(1, -1);
        	ry.translate(0, -h);
        }
        
        int locx = 0, locy = 0; // rotation shift
        if (de_angle > 0) {
        	double x = 0, y = 0;
        	
        	switch (de_angle / 90) {
        	case 0:
        		x = gif.height * Math.sin(angle);
        		break;
        	case 1:
        		x = gif.height * Math.sin(angle) - gif.width * Math.cos(angle);
        		y = gif.height * Math.cos(angle);
        		break;
        	case 2:
        		x = -gif.width * Math.cos(angle);
        		y = gif.width * Math.sin(angle) + gif.height * Math.cos(angle);
        		break;
        	case 3:
        		y = gif.width * Math.sin(angle);
        	}
        	
        	locx = (int)Math.round(x * Math.cos(angle) - y * Math.sin(angle));
        	locy = -(int)Math.round(x * Math.sin(angle) + y * Math.cos(angle));
        }
        
    	for (int i = 0; i < gif.frames.size(); i++) {
    		GifDecoder.GifFrame frame = gif.frames.get(i);
    		BufferedImage rotatedImage = new BufferedImage(w, h, transparency);
            Graphics2D g2d = rotatedImage.createGraphics();
            if (reflectX) g2d.transform(rx);
            if (reflectY) g2d.transform(ry);
            if (scale != 1.0) g2d.scale(scale, scale);
            if (de_angle > 0) g2d.rotate(angle);
            g2d.drawImage(frame.image, locx, locy, null);
    		gif.frames.set(i, new GifDecoder.GifFrame(rotatedImage, frame.delay));
    		g2d.dispose();
    	}
    	gif.height = h;
    	gif.width = w;
    }
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(nextImage.getWidth(), nextImage.getHeight());
	}
	
	public synchronized void setNewImage(BufferedImage bim) {
		nextImage = bim;
		old = false;
	}

	public synchronized BufferedImage getNextImage() {
		old = true;
		return nextImage;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d;
		if (!old) {
			g2d = (Graphics2D)g.create();
			g2d.drawImage(getNextImage(), 0, 0, this);
			g2d.dispose();
		}
	}

	public class Operator implements Runnable {
		public JPanel f;
		public boolean enough = false;
		private int delayFix = 0;

		public Operator(JPanel f) {
			this.f = f;
		}

		public synchronized void setDelayFix(int n) {
			delayFix = n;
		}

		@Override
		public void run() {
			int repeatCount = gif.getLoopCount();
			boolean infinite = repeatCount == 0;
			while (infinite || repeatCount > 0) {
				for (int i = 0; i < gif.getFrameCount(); i++) {
					try {
						Thread.sleep(gif.getDelay(i) - delayFix);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
					if (enough) return;
					nextImage = gif.getFrame(i);
					old = false;
					f.repaint();
				}
				repeatCount--;
			}
		}
	}
}
