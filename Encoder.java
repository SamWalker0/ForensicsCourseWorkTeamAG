import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Encoder {

    public static void main(String[] Args){


    }

    //Takes a string and returns the ascii bit representation for each character 
    private static String stringToBytes(String message){
        
        char[] chs = message.toCharArray();
        StringBuilder binaryMessage = new StringBuilder();

        for(char ch : chs){

            binaryMessage.append(
                String.format("%8s", Integer.toBinaryString(ch))
                .replaceAll(" ","0")  
                );

        }

        return binaryMessage.toString();
    }


    //Basic Read image method, could change in future
    private static BufferedImage readImage(){

        BufferedImage payloadImg = null;
        
        try{
        payloadImg = ImageIO.read( new File("test.png"));
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }  



} 