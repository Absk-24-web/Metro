

import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 *
 * @author karthik
 */
public class CandidateResult {
    public Mat ROI;
    public Point[] ROI_coords;
    public BufferedImage candidate;
    
    public String getROI_coords(){
        StringBuffer result = new StringBuffer("");
        
        for (Point p:ROI_coords){
            result.append("(" + Math.round(p.x * 1000)/1000.0 + "," + Math.round(p.y * 1000)/1000.0 +"), ");                    
        }
        return result.toString();
    }
}
