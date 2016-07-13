package qrcode;  
  
import java.awt.image.BufferedImage;  
import java.io.File;  
import java.io.IOException;  
import java.nio.file.FileSystems;  
import java.nio.file.Path;  
import java.util.HashMap;
import java.util.List;
import java.util.Map;  
  
import javax.imageio.ImageIO;  
  
import org.junit.Test;  
  
import com.google.zxing.BarcodeFormat;  
import com.google.zxing.Binarizer;  
import com.google.zxing.BinaryBitmap;  
import com.google.zxing.DecodeHintType;  
import com.google.zxing.EncodeHintType;  
import com.google.zxing.LuminanceSource;  
import com.google.zxing.MultiFormatReader;  
import com.google.zxing.MultiFormatWriter;  
import com.google.zxing.NotFoundException;  
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.WriterException;  
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;  
import com.google.zxing.client.j2se.MatrixToImageWriter;  
import com.google.zxing.common.BitMatrix;  
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;  
  
public class QRCodeTest {  
  
    /** 
     * 生成图像 
     *  
     * @throws WriterException 
     * @throws IOException 
     */  
    @Test  
    public void testEncode() throws WriterException, IOException {  
        String filePath = "D://";  
        String fileName = "zxing.png";  
        int width = 200; // 图像宽度  
        int height = 200; // 图像高度  
        String format = "png";// 图像类型  
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();  
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
//        hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);
//        hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_RECTANGLE);
//        hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_NONE); //DATA_MATRIX 类型时使用
        BitMatrix bitMatrix = new MultiFormatWriter().encode("This is a test",  
                BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵  
        Path path = FileSystems.getDefault().getPath(filePath, fileName);  
        MatrixToImageWriter.writeToPath(bitMatrix, format, path);// 输出图像  
        System.out.println("输出成功.");  
    }  
  
    /** 
     * 解析图像 
     */  
    @Test  
    public void testDecode() {  
//        String filePath = "D://zxing.png";
        String filePath = "D://fxxk.png";  
        BufferedImage image;  
        try {  
            image = ImageIO.read(new File(filePath));  
            LuminanceSource source = new BufferedImageLuminanceSource(image);  
            Binarizer binarizer = new HybridBinarizer(source);  
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);  
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();  
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");  
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);// 对图像进行解码  
            String content = result.getText();  
            System.out.println(result.getBarcodeFormat());
            Map<ResultMetadataType, Object> map = result.getResultMetadata();
            for(ResultMetadataType key : map.keySet()){
            	System.out.println(key+":"+map.get(key)+" type:"+map.get(key).getClass().getName());
            }
            for(ResultPoint rp : result.getResultPoints()){
            	System.out.println(rp);
            }
            System.out.println(content);
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (NotFoundException e) {  
            e.printStackTrace();  
        }  
    }  
} 