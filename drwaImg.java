import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.*;

public class drwaImg {
    int width = 640;
    int height = 480;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
    public void draw(String imgPath, BufferedImage img, List<List<Point>> points) {
        for (List<Point> groups : points) {
            Random random = new Random();
            int randomNumber = random.nextInt(4); 
            for(Point p : groups){
                  
                if (randomNumber == 0) {
                    img.setRGB(p.x, p.y, Color.GREEN.getRGB());
                } else if (randomNumber == 1) {
                    img.setRGB(p.x, p.y, Color.RED.getRGB());
                } else if (randomNumber == 2) {
                    img.setRGB(p.x, p.y, Color.BLUE.getRGB());
                } else {
                    img.setRGB(p.x, p.y, Color.GRAY.getRGB());
                }
            }
        }

        // 2. Save the modified image to the same path
        // ImageIO.write(img, "jpg", new File(imgPath));

        // 3. Display the image
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public void draw2(BufferedImage img, List<Point[]> points) {
        for (Point[] g : points) {
            Point topLeft = g[0];
            Point topRight = g[1];
            Point bottomLeft = g[2];
            Point bottomRight = g[3];
            Random random = new Random();
            int randomNumber = random.nextInt(4);
            for(int x = topLeft.x; x < bottomRight.x; x++){
                for(int y = topLeft.y; y < bottomRight.y; y++){
                    if(randomNumber == 0){
                        img.setRGB(x, y, Color.GREEN.getRGB());
                    }
                    else if(randomNumber == 1){
                        img.setRGB(x, y, Color.RED.getRGB());
                    }
                    else if(randomNumber == 2){
                        img.setRGB(x, y, Color.BLUE.getRGB());
                    }
                    else{
                        img.setRGB(x, y, Color.GRAY.getRGB());
                    }            
                }
            }           
        }

        //2. Display the image
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public void draw3(BufferedImage img, List<Point[]> points, List<String> strs) {
        Graphics2D g = img.createGraphics();
        int num = 0;
        for (Point[] point : points) {
            int x1 = point[0].x;
            int x2 = point[1].x;
            int y1 = point[2].y;
            int y2 = point[0].y;
            String str = strs.get(num);
            // System.out.println("y1 = "+y1+"y2 = "+y2);
            for (int x = x1; x <= x2; x++) {
                // System.out.println("drawed");
                for (int dy = 0; dy < 3; dy++) {
                    img.setRGB(x, y1 + dy, Color.GREEN.getRGB());
                }
        
                // 绘制从y2开始的粗线
                for (int dy = 0; dy < 3; dy++) {
                    img.setRGB(x, y2 - dy, Color.GREEN.getRGB());
                }
            }
    
            // Draw left and right lines
            for (int y = y1; y <= y2; y++) {
                // System.out.println("vertical line drawed.");
                for (int dx = 0; dx < 3; dx++) {
                    img.setRGB(x1 + dx, y, Color.GREEN.getRGB());
                }
        
                // 绘制从y2开始的粗线
                for (int dx = 0; dx < 3; dx++) {
                    img.setRGB(x2 - dx, y, Color.GREEN.getRGB());
                }
            }
            double dy = y2 * 0.02;
            double dx = x1 * 0.03;
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString(str, (int)(x1 + dx), (int)(y2 - dy));
            num++;
        }

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public double[][] calculateRGB(BufferedImage img){
        int bufWidth = img.getWidth();
        int bufHeight = img.getHeight();        
        
        int[][] indexRGB = new int[3][256];
        int validPixelCount = 0;

        for (int y = 0; y < bufHeight; y++) {
				for (int x = 0; x < bufWidth; x++) {
                    int rgb = img.getRGB(x, y);
                    
			        int R = (rgb >> 16) & 0xFF;
			        int G = (rgb >> 8) & 0xFF;
			        int B = (rgb) & 0xFF;
 
                    if(R == 0 && G == 255 && B == 0){
                        continue;
                    }
                    if(R == 0 && G == 0 && B == 0){
                        continue;
                    }
                    if(R == 255 && G == 255 && B == 255){
                        continue;
                    }
                    // System.out.println("r: "+R+", g: "+G+",b: "+B);
                    indexRGB[0][R]++;
                    indexRGB[1][G]++;
                    indexRGB[2][B]++;
                    validPixelCount++;
				}
			}
        // return indexRGB;
        double[][] proportionRGB = new double[3][256];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 256; j++) {
                proportionRGB[i][j] = (double) indexRGB[i][j] / validPixelCount;
            
            }
        }
        return proportionRGB;
    }
    
}
