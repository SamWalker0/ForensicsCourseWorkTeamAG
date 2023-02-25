import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;

public class Decoder{

    
    public static void main(String[] Args){

        BufferedImage image = readImage("final.png");
        String decodedMessage = decodeImageToBits(image);
        System.out.println(decodedMessage);
    }


    //Given a bit string returns a String of the decoded message
    private static String bytesToString(String bytesMessage){


        String[] characterByte = bytesMessage.split("(?<=\\G.{8})");
        StringBuilder decodedMessage = new StringBuilder();

        for (String by : characterByte){   

            int tempInt = Integer.parseInt(by, 2);
            char c = (char)tempInt;
            decodedMessage.append(c);

        }

        return decodedMessage.toString();
    }


    private static BufferedImage readImage(String imageName){

        BufferedImage payloadImg = null;
        
        try{
        payloadImg = ImageIO.read( new File(imageName));
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }  

    private static int findHeaderSize(BufferedImage image){
        int heighestPayloadPossible = image.getWidth()*image.getHeight()*3;
        int headerBitSize = (Integer.toBinaryString(heighestPayloadPossible)).length();
        return headerBitSize;
    }

    private static String decodeImageToBits(BufferedImage image){

        int headerSize = findHeaderSize(image);

        int bitPointer = 0;

        int headerPackages = (int)Math.ceil(headerSize/3);

        int xPixelPointer = 0;
        int yPixelPointer = 0;

        StringBuilder headerBuilder = new StringBuilder();
        StringBuilder payloadBuilder = new StringBuilder();

        for(int headerPackageIterator=0;headerPackageIterator<=headerPackages;headerPackageIterator++){
           
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }
            
            Color pixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            if (  ((headerSize-1) - bitPointer) >= 0 ){
                headerBuilder = appendLSB(headerBuilder, pixelColour, "red");
            } else{
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "red");                  
            }
            bitPointer++;

            if (((headerSize-1) - bitPointer) >= 0){
                headerBuilder = appendLSB(headerBuilder, pixelColour, "green");
            } else{
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "green");                  
            }
            bitPointer++;

            if (((headerSize-1) - bitPointer) >= 0){
                headerBuilder = appendLSB(headerBuilder, pixelColour, "blue");
            } else{
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "blue");                  
            }
            bitPointer++;
            

            xPixelPointer++;
        }

        String headerBits = headerBuilder.toString();
        int payloadSize = Integer.parseInt(headerBits,2);

        int payloadPackages = (int)Math.ceil(payloadSize/3);

        for(int payloadPackageIterator = 0; payloadPackageIterator < payloadPackages; payloadPackageIterator++){
            
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            Color pixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            if( ((headerSize+payloadSize)-1) - bitPointer >=0 ){
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "red");
            } if( ((headerSize+payloadSize)-1) - bitPointer >=0 ){
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "green");
            } if( ((headerSize+payloadSize)-1) - bitPointer >=0 ){
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "blue");
            }

            xPixelPointer++;

        }

        String decodedMessage = bytesToString(payloadBuilder.toString());
        
        return decodedMessage;
    }

    

    private static StringBuilder appendLSB (StringBuilder text, Color pixelColour, String colourIdentifier){

        String binaryStr = "";
        switch(colourIdentifier){
            case("red"):
                binaryStr =  Integer.toBinaryString(pixelColour.getRed());
                break;
            case("green"):
                binaryStr =  Integer.toBinaryString(pixelColour.getGreen());
                break;            
            case("blue"):
                binaryStr =  Integer.toBinaryString(pixelColour.getBlue());
                break;
        }
        return text.append(binaryStr.charAt(binaryStr.length() - 1));
    }

}