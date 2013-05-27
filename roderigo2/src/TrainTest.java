import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import neuralnetwork.NeuralNetwork;

public class TrainTest {
	static NeuralNetwork neuralNetwork;
	static final int imageSz = 400;
	static final int resolution = 10; //cell size
	static int numHidden = 20;
	static int maxEpochs = 10000;
	static double maxError = 0.001;
	static double learningRate = 0.1;
	static double learningRateDecay = 0;
	static double momentum = 0;
	static int epoch = 0;
	
	static double dataSetIn[][] = {{}};
	static double dataSetOut[][] = {{}};
	
	static final Win win = new Win();
	
	static int f2i(double f) {return (int)(imageSz * (f + 0.1) / 1.2);}
	static double i2f(double i) {return 1.2 * i / (double)imageSz - 0.1;}
	
	static BufferedImage makePredicitonImage(double din[][], double dout[][]) {
		double t;
		BufferedImage i = new BufferedImage(imageSz, imageSz, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = i.createGraphics();
		
		// draw prediciton surface (as grayscale image)
		for(int x = 0; x < imageSz; x += resolution) {
			for(int y = 0; y < imageSz; y += resolution) {
				t = predict(i2f(x), i2f(y));
				t = Math.max(0, Math.min(1, t));
				g.setColor(Color.getHSBColor(0, 0, (float)t));
				g.fillRect(x, y, resolution, resolution);
			}
		}
		
		final int rectSz = 9;
		final int inset = 2;
		
		// draw dataset on top of it
		// hilighting samples part of the current minibatch
		for(int j = 0; j < dataSetIn.length; j++) {
			double in[] = dataSetIn[j];
			double out[] = dataSetOut[j];
			int x = f2i(in[0]) - 3, y = f2i(in[1]) - 3;
			
			g.setColor(Color.red);
			for(int xi = 0; xi < din.length; xi++)
				if(Math.abs(in[0] - din[xi][0]) < 0.01 && Math.abs(in[1] - din[xi][1]) < 0.01) {
					g.setColor(Color.green);
					break;
				}
			g.fillRect(x, y, rectSz, rectSz);
			
			t = out[0];
			g.setColor(Color.getHSBColor(0, 0, (float)t));
			g.fillRect(x + inset, y + inset, rectSz - 2 * inset, rectSz - 2 * inset);
			
		}
		String s = String.format("learningRate = %.5f", learningRate);
		if(epoch > 0) {
			s = String.format("EPOCH %d    %s", epoch, s);
			g.setColor(Color.black);
			g.drawString(s, 6, imageSz - 4);
			g.setColor(Color.yellow);
			g.drawString(s, 5, imageSz - 5);
		}
		return i;
	}
	
	static void train() {
		neuralNetwork.initializeRandomWeights(1);
		epoch = 0;
		
		do {
			learningRate = 0.001 * win.slLearningRate.getValue();
			momentum = 0.001 * win.slMomentum.getValue();
			int subsetSize = Math.max(1, Integer.valueOf(win.spMiniBatchSize.getValue().toString()));
			win.spMiniBatchSize.setValue(subsetSize);
			
			if(dataset.size() > 0) {
				double in[][] = new double[subsetSize][2], out[][] = new double[subsetSize][1];
				for(int i = 0; i < subsetSize; i++) {
					int src = (int)(Math.random() * dataSetIn.length);
					for(int j = 0; j < in[i].length; j++)
						in[i][j] = dataSetIn[src][j];
					for(int j = 0; j < out[i].length; j++)
						out[i][j] = dataSetOut[src][j];
				}
			
				neuralNetwork.epoch(in, out, learningRate, momentum);
				
				win.setImage(makePredicitonImage(in, out));
				
				//learningRate = Math.max(0, learningRate * (1 - learningRateDecay));
				epoch++;
			}
			try {Thread.sleep(1);} catch(InterruptedException e) {}
		//} while(epoch < maxEpochs && neuralNetwork.getMeanSquareError(dataSetIn, dataSetOut) > maxError);
		} while(true);
	}
	
	static double predict(double x, double y) {
		neuralNetwork.setInput(new double[]{x, y});
		neuralNetwork.feedForward();
		return neuralNetwork.getOutput()[0];
	}
	
	public static void main(String[] args) throws IOException {
		numHidden = Integer.parseInt(System.getProperty("numHidden", "" + numHidden));
		maxEpochs = Integer.parseInt(System.getProperty("maxEpochs", "" + maxEpochs));
		maxError = Double.parseDouble(System.getProperty("maxError", "" + maxError));
		learningRate = Double.parseDouble(System.getProperty("learningRate", "" + learningRate));
		learningRateDecay = Double.parseDouble(System.getProperty("learningRateDecay", "" + learningRateDecay));
		momentum = Double.parseDouble(System.getProperty("momentum", "" + momentum));
		
		neuralNetwork = new NeuralNetwork(2, numHidden, 1);
		train();
	}
	
	private static Map<Point, Boolean> dataset = new HashMap<>();
	
	private static void regenDataSet() {
		dataSetIn = new double[dataset.size()][2];
		dataSetOut = new double[dataset.size()][1];
		int i = 0;
		for(Map.Entry<Point, Boolean> e : dataset.entrySet()) {
			dataSetIn[i][0] = i2f(e.getKey().getX());
			dataSetIn[i][1] = i2f(e.getKey().getY());
			dataSetOut[i][0] = e.getValue() ? 1 : 0;
			i++;
		}
	}
	
	static class Win extends JFrame {
		private static final long serialVersionUID = 1L;
		
		private BufferedImage im;
		
		final Cnv cnv;
		final JSlider slLearningRate;
		final JSlider slMomentum;
		final JSpinner spMiniBatchSize;
		
		private Point selected = null;
		
		private class Cnv extends JPanel {
			private static final long serialVersionUID = 1L;
			private final MouseAdapter mouseAdapter = new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Point h = getClosestPoint(e.getPoint());
					if(h != null && dist(e.getPoint(), h) < 8)
						dataset.remove(h);
					else
						dataset.put(new Point(e.getPoint()), e.getButton() == MouseEvent.BUTTON1);
					regenDataSet();
				}
				
				public void mouseDragged(MouseEvent e) {
					if(selected == null) return;
					boolean b = dataset.get(selected);
					dataset.remove(selected);
					dataset.put(selected = new Point(e.getPoint()), b);
					regenDataSet();
				}
				
				public void mousePressed(MouseEvent e) {
					selected = getClosestPoint(e.getPoint());
				}
				
				public void mouseReleased(MouseEvent e) {
					selected = null;
				}
			};
			
			private Point getClosestPoint(final Point p) {
				Point c = null;
				double d = Double.POSITIVE_INFINITY;
				for(Map.Entry<Point, Boolean> e1 : dataset.entrySet()) {
					double d1 = dist(p, e1.getKey());
					if(d1 < d) {
						c = e1.getKey();
						d = d1;
					}
				}
				return c;
			}
			
			private double dist(Point p1, Point p2) {
				if(p1 == null) throw new RuntimeException("p1 == null");
				if(p2 == null) throw new RuntimeException("p2 == null");
				return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
			}
			
			public Cnv() {
				setPreferredSize(new Dimension(imageSz, imageSz));
				addMouseListener(mouseAdapter);
				addMouseMotionListener(mouseAdapter);
			}
			
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(im, 0, 0, null);
			}
		}
		
		public Win() {
			super("");
			Box b = Box.createVerticalBox();
			addControl(b, "Learning rate: ", slLearningRate = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500));
			addControl(b, "Momentum: ", slMomentum = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0));
			addControl(b, "Mini-batch size: ", spMiniBatchSize = new JSpinner());
			b.add(cnv = new Cnv());
			add(b);
			pack();
			setVisible(true);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}
		
		public void addControl(Container container, String label, JComponent control) {
			Box b = Box.createHorizontalBox();
			b.add(new JLabel(label));
			b.add(control);
			container.add(b);
		}
		
		public void setImage(BufferedImage im) {
			this.im = im;
			if(im != null && cnv != null) cnv.repaint();
		}
	}
}
