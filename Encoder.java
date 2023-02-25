import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;

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

    //Adds the header to the payload, header is a binary int of how large the payload is
    //This header is defined to be as large as the largest possible payload
    private static String addHeader(String noHeaderPayload, BufferedImage photo){
        
        int maximumPayloadSize = photo.getWidth()*photo.getHeight()*3;
        int headerSize = (Integer.toBinaryString(maximumPayloadSize)).length();
        
        String payloadSizeBinaryString = Integer.toBinaryString(noHeaderPayload.length());
        String header = String.format("%" + headerSize + "s", payloadSizeBinaryString).replace(' ', '0');

        return (header + noHeaderPayload);
    }


    //Basic Read image method, could change in future
    private static BufferedImage readImage(String imageName){

        BufferedImage payloadImg = null;
        
        try{
        payloadImg = ImageIO.read( new File(imageName));
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }  

    //Quick function mainly to clean up the insert payload function
    private static String replaceLastChar(String text, char newLastChar){
        String removeLastChar = text.substring(0,text.length()-1);
        return (removeLastChar+ newLastChar);
    }


    //Payload inserted into the image
    private static BufferedImage insertPayload(String payload, BufferedImage photo){

        int payloadPointer = 0;
        int bitPackageCount = (int)Math.ceil(payload.length()/3);

        int xPixelPointer = 0;
        int yPixelPointer = 0;
        

        //The payload insertion is done in 3 bit packages for the RGB values,
        // there is then the special case at the end for the last package that might not contain 3 bits of data
        for(int packageIterator=0; packageIterator<=bitPackageCount; packageIterator++){

            //Checks if the x pixel falls past the photo size
            if(xPixelPointer>=photo.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            //Get the colour data for the current pixel
            Color originalPixelColour = new Color(photo.getRGB(xPixelPointer, yPixelPointer));
            int originalRedInt = originalPixelColour.getRed();
            int originalGreenInt = originalPixelColour.getGreen();
            int originalBlueInt = originalPixelColour.getBlue();

            //Initialise the new colour data to the old colour data
            int newRedInt = originalRedInt;
            int newGreenInt = originalGreenInt;
            int newBlueInt = originalBlueInt;           

            //If statements are used to stop writing once payload is written
            if(((payload.length()-1)-payloadPointer)>=0){

                String redBinaryRep = Integer.toBinaryString(originalPixelColour.getRed());
                    
                redBinaryRep = replaceLastChar(redBinaryRep,payload.charAt(payloadPointer));
                newRedInt =  Integer.parseInt(redBinaryRep, 2);
                payloadPointer++;
            }
            if(((payload.length()-1)-payloadPointer)>=0){

                String greenBinaryRep = Integer.toBinaryString(originalPixelColour.getGreen());
                    
                greenBinaryRep = replaceLastChar(greenBinaryRep,payload.charAt(payloadPointer));
                newGreenInt =  Integer.parseInt(greenBinaryRep, 2);
                payloadPointer++;
            }
            if(((payload.length()-1)-payloadPointer)>=0){

                String blueBinaryRep = Integer.toBinaryString(originalPixelColour.getBlue());
                    
                blueBinaryRep = replaceLastChar(blueBinaryRep,payload.charAt(payloadPointer));
                newBlueInt =  Integer.parseInt(blueBinaryRep, 2);
                payloadPointer++;
            }
            
            Color newColour = new Color(newRedInt, newBlueInt, newGreenInt);

            photo.setRGB(xPixelPointer, yPixelPointer, newColour.getRGB());

            xPixelPointer++;
        }
    return photo;
    }

} 