import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class findImage_copy {
	// kirby 5
	private static final int LIMIT_DIST = 10;
	static drwaImg drawimg = new drwaImg();
	static int width = 640; // default image width and height
	static int height = 480;
	BufferedImage scalImg;
	static int[][] uH = new int[width][height];
	static int[][] vH = new int[width][height];

	static int[] objectU = new int[256];
	static int[] objectV = new int[256];
	static int[] objectY = new int[256];
	static int[] objectH = new int[361];
	static int[] objectS = new int[101];
	static int[] objecthV = new int[101];
	static int[] imgU = new int[256];
	static int[] imgV = new int[256];
	static int[] imgY = new int[256];
	static int[] imgH = new int[361];
	static int[] imgS = new int[101];
	static int[] imgHV = new int[101];

	static int RObject[] = new int[256];
	static int GObject[] = new int[256];
	static int BObject[] = new int[256];
	static double RGBObject[][] = new double[3][256];
	static int RImg[] = new int[256];
	static int GImg[] = new int[256];
	static int BImg[] = new int[256];
	static double RGBImg[][] = new double[3][256];

	static boolean points[][] = new boolean[width][height];
	static List<List<Point>> groups;
	static List<Point> groupXY;
	static List<Point[]> tmp;

	private static void readImageRGB(int width, int height, String imgPath, BufferedImage img, int[] frequencyU,
			int[] frequencyV, int[] frequencyY, int[] frequencyH, int[] frequencyS, int[] frequencyHV) {

		try {
			int frameLength = width * height * 3;
			// int newFrameLength = width * newHeight * 3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {

					byte r = bytes[ind];
					byte g = bytes[ind + height * width];
					byte b = bytes[ind + height * width * 2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

					int R = r & 0xFF;
					int G = g & 0xFF;
					int B = b & 0xFF;
					if (G > R + 100 && G > B + 100) {
						ind++;
						// System.out.println("continue");
						continue;
					}

					float rf = R / 255f;
					float gf = G / 255f;
					float bf = B / 255f;

					float maxf = Math.max(rf, Math.max(gf, bf));
					float minf = Math.min(rf, Math.min(gf, bf));
					float delta = maxf - minf;
					float s, h = 0;
					float v = maxf;
					if (delta == 0) {
						h = 0;
					} else if (maxf == rf) {
						h = (60 * ((gf - bf) / delta) + 360) % 360;
					} else if (maxf == gf) {
						h = (60 * ((bf - rf) / delta) + 120) % 360;
					} else {
						h = (60 * ((rf - gf) / delta) + 240) % 360;
					}

					if (maxf == 0) {
						s = 0;
					} else {
						s = delta / maxf;
					}

					frequencyH[(int) h]++;
					frequencyS[(int) (s * 100)]++;
					frequencyHV[(int) (v * 100)]++;
					double Y = 0.299 * R + 0.587 * G + 0.114 * B;
					double U = -0.14713 * R - 0.28886 * G + 0.436 * B + 128;
					double V = 0.615 * R - 0.51499 * G - 0.10001 * B + 128;

					Y = Math.min(Math.max(Y, 0), 255);
					U = Math.min(Math.max(U, 0), 255);
					V = Math.min(Math.max(V, 0), 255);

					if (U < 0 || U > 255 || V < 0 || V > 255) {
						System.out.println("Unexpected UV values: U=" + U + ", V=" + V);
					}

					img.setRGB(x, y, pix);
					frequencyV[(int) V]++;
					frequencyU[(int) U]++;
					ind++;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<List<Point>> mergePixels(int num) {
		List<List<Point>> islands = new ArrayList<>();
		boolean[][] visited = new boolean[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (points[x][y] && !visited[x][y]) {
					List<Point> island = floodFillIterative(x, y, visited);
					if (island.size() > num) {
						islands.add(island);
					}
				}
			}
		}

		return islands;
	}

	private static List<Point> floodFillIterative(int startX, int startY, boolean[][] visited) {
		List<Point> island = new ArrayList<>();
		Stack<Point> stack = new Stack<>();
		stack.push(new Point(startX, startY));

		while (!stack.isEmpty()) {
			Point p = stack.pop();
			int x = p.x;
			int y = p.y;

			if (x < 0 || x >= width || y < 0 || y >= height || visited[x][y] || !points[x][y]) {
				continue;
			}

			visited[x][y] = true;
			island.add(p);

			// Check points within the LIMIT_DIST
			for (int dx = -LIMIT_DIST; dx <= LIMIT_DIST; dx++) {
				for (int dy = -LIMIT_DIST; dy <= LIMIT_DIST; dy++) {
					if (dx * dx + dy * dy <= LIMIT_DIST * LIMIT_DIST) {
						stack.push(new Point(x + dx, y + dy));
					}
				}
			}
		}

		return island;
	}

	private static boolean[][] findSimilarRegion2(BufferedImage img, double minU, double maxU, double minV, double maxV, double minY, double maxY,
	int maxH, int minH, int minS, int maxS, int minHV, int maxHV){
		//RGBObject;
		//points;
		int count = 0;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				int rgb = img.getRGB(i, j);
				int R = (rgb >> 16) & 0xFF;
			    int G = (rgb >> 8) & 0xFF;
			    int B = (rgb) & 0xFF;

				float rf = R / 255f;
				float gf = G / 255f;
				float bf = B / 255f;
					
				float maxf = Math.max(rf, Math.max(gf, bf));
				float minf = Math.min(rf, Math.min(gf, bf));
				float delta = maxf - minf;
				float s,h = 0;
				float v = maxf;
				if (delta == 0) {
					h = 0;
				} else if (maxf == rf) {
					h = (60 * ((gf - bf) / delta) + 360) % 360;
				} else if (maxf == gf) {
					h = (60 * ((bf - rf) / delta) + 120) % 360;
				} else {
					h = (60 * ((rf - gf) / delta) + 240) % 360;
				}

				if (maxf == 0) {
					s = 0;
				} else {
					s = delta / maxf;
				}


				double Y = 0.299 * R + 0.587 * G + 0.114 * B;
				double U = -0.14713 * R - 0.28886 * G + 0.436 * B + 128;
				double V = 0.615 * R - 0.51499 * G - 0.10001 * B + 128;

				if ((findPeakH(objectH) >= 0 && findPeakH(objectH) <= 30) || (findPeakH(objectH) >= 330 && findPeakH(objectH) <= 360)) {
					if(V >= minV && V <= Math.max(maxV, 255)){
						count++; //!!!
						points[i][j] = true;
					}
				}
				else if((findPeakH(objectH) >= 210 && findPeakH(objectH) <= 240)){
					if(U >= minU && U <= Math.max(maxU , 255)){
						points[i][j] = true;
					}
				}
				else if((findPeakH(objectH) > 30 && findPeakH(objectH) < 60 )){
					if((h >= minH - 10 && h <= maxH + 10) && (s*100 >= minS && s*100 <= maxS + 15)){
						points[i][j] = true;
					}
				}
			}
		}
		// System.out.println("count: "+ count);
		return points;
	}
		
	public static int findTopPeaks(int[] histogram) {
		int highestPeakIndex = -1; // Initializing with an invalid index
		int highestPeakHeight = Integer.MIN_VALUE;

		// Find the highest peak
		for (int i = 1; i < histogram.length - 1; i++) {
			if (histogram[i] > histogram[i - 1] && histogram[i] > histogram[i + 1]) {
				if (histogram[i] > highestPeakHeight) {
					highestPeakHeight = histogram[i];
					highestPeakIndex = i;
				}
			}
		}

		return highestPeakIndex;
	}

	private static int[] findAreaY(int frequencyY[], int threshold) {
		int[] area = new int[2];
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		int max_index = -1;
		int min_index = -1;
		for (int i = 0; i < frequencyY.length; i++) {
			if (frequencyY[i] > threshold) {
				if (frequencyY[i] > max) {
					max_index = i;
					max = frequencyY[i];
				} else if (frequencyY[i] < min) {
					min_index = i;
					min = frequencyY[i];
				}
			}
		}
		area[0] = Math.min(max_index, min_index);
		area[1] = Math.max(max_index, min_index);
		return area;
	}

	private static int findPeakH(int frequencyH[]) {
		int max = 0;
		int index = 0;
		for (int i = 0; i < frequencyH.length; i++) {
			if (frequencyH[i] >= max) {
				index = i;
				max = frequencyH[i];
			}
		}
		return index;
	}

	private static void saveToCSV(int frequencyU[], int frequencyV[], String path) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
			// Write headers
			for (int i = 0; i < 256; i++) {
				writer.println(i + "," + frequencyU[i] + "," + frequencyV[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void saveHToCSV(int frequencyH[], String path) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
			// Write headers
			writer.println("Value,H");
			for (int i = 0; i <= 360; i++) {
				writer.println(i + "," + frequencyH[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int[][] foundUV(List<Point> group, BufferedImage img) {
		int[][] uv = new int[2][256];

		for (Point p : group) {

			int x = (int) p.getX();
			int y = (int) p.getY();
			// System.out.println("("+x+", "+y+")");
			int rgb = img.getRGB(x, y);
			// System.out.println(rgb);
			int R = (rgb >> 16) & 0xFF;
			int G = (rgb >> 8) & 0xFF;
			int B = rgb & 0xFF;

			// if (R == 0 && G == 255 && B == 0) {
			// 	continue;
			// }

			double U = -0.14713 * R - 0.28886 * G + 0.436 * B + 128;
			double V = 0.615 * R - 0.51499 * G - 0.10001 * B + 128;

			// Y = Math.min(Math.max(Y, 0), 255);
			U = Math.min(Math.max(U, 0), 255);
			V = Math.min(Math.max(V, 0), 255);

			uv[0][(int) U]++;
			uv[1][(int) V]++;
		}
		return uv;
	}

	private static double bhattacharyyaDistance(double[] hist1, double[] hist2) {
		double sum = 0;
		for (int i = 0; i < hist1.length; i++) {
			sum += Math.sqrt(hist1[i] * hist2[i]);
		}
		return Math.sqrt(1 - sum);
	}

	private static double[] normalize(int[] hist) {
		double totalNum = 0;
		for (int i = 0; i < hist.length; i++) {
			totalNum += hist[i];
			// if(totalNum >= Integer.MAX_VALUE){
			// 	System.out.println("Report!");
			// }
		}

		double normalizedHist[] = new double[256];
		for (int i = 0; i < hist.length; i++) {
			normalizedHist[i] = (double) hist[i] / totalNum;
		}

		return normalizedHist;
	}
	
	private static List<List<Point>> confirmObjects(List<List<Point>> groups, BufferedImage img){
		int[][] uv = new int[2][256];
		List<List<Point>> results = new ArrayList<>();
		double minDis = Integer.MAX_VALUE;
		List<Integer> indexs = new ArrayList<>(); 
		for(int i = 0; i < groups.size(); i++){
			BufferedImage tmpImage = img;
			uv = foundUV(groups.get(i), img);
			double[] u = new double[256];
			u = normalize(uv[0]);
			double[] v = new double[256];
			v = normalize(uv[1]);
			double[] objectNormalU = normalize(objectU);
			double[] objectNormalV = normalize(objectV);
			double distance1 = bhattacharyyaDistance(objectNormalU, u);
			double distance2 = bhattacharyyaDistance(objectNormalV, v);
			double distance = (distance1 + distance2) / 2;
			double roundedValue = Math.round(distance * 100.0) / 100.0;
			if(roundedValue < minDis && roundedValue < 0.8){
				minDis = roundedValue;
				indexs = new ArrayList<>();
				indexs.add(i);

			}
			else if(roundedValue == minDis){
				minDis = roundedValue;
				indexs.add(i);
			}
		}
		for(int i : indexs){
			results.add(groups.get(i));
		}
		return results;
	}

	private static List<Point[]> createSquare(List<List<Point>> groups) {

		List<Point[]> output = new ArrayList<>();
		for (List<Point> group : groups) {
			int leftX = Integer.MAX_VALUE;
			int rightX = Integer.MIN_VALUE;
			int top = Integer.MIN_VALUE;
			int bottom = Integer.MAX_VALUE;
			for (Point p : group) {
				leftX = Math.min(leftX, p.x);
				rightX = Math.max(rightX, p.x);
				top = Math.max(top, p.y);
				bottom = Math.min(bottom, p.y);
			}
			Point[] p = new Point[4];
			p[0] = new Point();
			p[0].setLocation(leftX, top);
			p[1] = new Point();
			p[1].setLocation(rightX, top);
			p[2] = new Point();
			p[2].setLocation(leftX, bottom);
			p[3] = new Point();
			p[3].setLocation(rightX, bottom);
			output.add(p);
		}
		return output;
	}

	public static void main(String[] args) {

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		String imagePath = args[0];
		readImageRGB(width, height, imagePath, img, imgU, imgV, imgY, imgH, imgS, imgHV);
		List<Point[]> totalRes = new ArrayList<>();
		List<String> strs = new ArrayList<>();
		for (int i = 1; i < args.length; i++) {
			String objectPath = args[i];
			BufferedImage imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			BufferedImage imageObject = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			String fileName = objectPath.substring(objectPath.lastIndexOf("\\") + 1);
			readImageRGB(width, height, objectPath, imageObject, objectU, objectV, objectY, objectH, objectS, objecthV);
			saveToCSV(objectU, objectV, fileName + ".csv");
			RGBObject = drawimg.calculateRGB(imageObject);
			saveHToCSV(objectH, fileName + "H.csv");

			fileName = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
			readImageRGB(width, height, imagePath, imgOne, imgU, imgV, imgY, imgH, imgS, imgHV);

			saveToCSV(imgU, imgV, fileName + ".csv");
			RGBImg = drawimg.calculateRGB(imgOne);

			int[] areaY = findAreaY(objectY, 100);
			double minU = findTopPeaks(objectU) - 20;
			double maxU = findTopPeaks(objectU) + 20;
			double minV = findTopPeaks(objectV) - 20;
			double maxV = findTopPeaks(objectV) + 20;
			double minY = areaY[0];
			double maxY = areaY[1];
			int minH = Math.max(findTopPeaks(objectH) - 10, 0);
			int maxH = findTopPeaks(objectH) + 20;
			int minS = findTopPeaks(objectS) - 5;
			int maxS = findTopPeaks(objectS) + 5;
			int minHV = findTopPeaks(objecthV) - 5;
			int maxHV = findTopPeaks(objecthV) + 5;

			// System.out.println("maxU: " + maxU);
			// double minUV = (areaU[0] << 1) | areaV[0];
			// points = findSimilarRegion(imgOne, minUV, maxUV);
			points = findSimilarRegion2(imgOne, minU, maxU, minV, maxV, minY, maxY, maxH, minH, minS, maxS, minHV,
					maxHV);
			groups = mergePixels(50);
			// drawimg.draw(fileName, imgOne, groups);

			List<List<Point>> newRes = new ArrayList<>();
			newRes = confirmObjects(groups, imgOne);
			List<Point[]> squares = new ArrayList<>();
			squares = createSquare(newRes);
			totalRes.addAll(squares);
			fileName = objectPath.substring(objectPath.lastIndexOf("\\") + 1);
			for(int j = 0; j < totalRes.size(); j++){
				strs.add(fileName);		
			}	
		}

		drawimg.draw3(img, totalRes,strs);
	}
}
