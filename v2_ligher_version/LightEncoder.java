import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;
import java.util.Scanner;

public class LightEncoder {

    public static void main(String[] Args){

        Scanner inputScanner = new Scanner(System.in);

        System.out.println("\n\nEnter the image filename:");
        String userFileStr = inputScanner.nextLine();

        File imageFile = new File(userFileStr);
        BufferedImage image = getImageData(imageFile);

        System.out.println("\n\nEnter the payload filename:");
        String payloadFileStr = inputScanner.nextLine();

        File payloadFile = new File(payloadFileStr);     

        String message = getpayloadData(payloadFile);
        String payload = messageToPayloadWithHeader(message);

        BufferedImage encodedImage = insertPayload(payload, image);

        try{
            ImageIO.write(encodedImage, "png", imageFile);
        } catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Message Successfully Encoded");

        inputScanner.close();
    }

    private static BufferedImage getImageData(File imageFile){
        BufferedImage payloadImg = null;    
        try{
        payloadImg = ImageIO.read(imageFile);
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }

    private static String getpayloadData(File imageFile){
        StringBuilder payloadBuilder = new StringBuilder();
        try{
            Scanner reader = new Scanner(imageFile);
            while(reader.hasNextLine()){
                payloadBuilder.append(reader.nextLine()+"\n");
            }
            reader.close();
        } catch(FileNotFoundException e){
            System.out.println("file does not exist");
        }     
        return payloadBuilder.toString();
    }

    private static String messageToPayloadWithHeader(String message){
        char[] chs = message.toCharArray();
        StringBuilder binaryMessage = new StringBuilder();

        for(char ch : chs){
            binaryMessage.append(
                String.format("%8s", Integer.toBinaryString(ch))
                .replaceAll(" ","0")  
                );
        }
        String noHeaderPayload = binaryMessage.toString();
        String payloadSizeBinaryString = Integer.toBinaryString(noHeaderPayload.length());       
        String header = String.format("%" + 25 + "s", payloadSizeBinaryString).replace(' ', '0');

        return (header + noHeaderPayload);
    }
    
    private static String replaceLastChar(String text, char newLastChar){
        String removeLastChar = text.substring(0,text.length()-1);
        return (removeLastChar+ newLastChar);
    }

    private static BufferedImage insertPayload(String payload, BufferedImage image){

        int payloadPointer = 0;
        int bitPackageCount = (int)Math.ceil(payload.length()/3);

        int xPixelPointer = 0;
        int yPixelPointer = 0;
        
        //The payload insertion is done in 3 bit packages for the RGB values,
        // there is then the special case at the end for the last package that might not contain 3 bits of data
        for(int packageIterator=0; packageIterator<=bitPackageCount; packageIterator++){

            //Checks if the x pixel falls past the image size
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            //Get the colour data for the current pixel
            Color originalPixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            //Initialise the new colour data to the old colour data
            int newRedInt = originalPixelColour.getRed();
            int newGreenInt = originalPixelColour.getGreen();
            int newBlueInt = originalPixelColour.getBlue();        

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
            
            Color newColour = new Color(newRedInt, newGreenInt, newBlueInt);

            image.setRGB(xPixelPointer, yPixelPointer, newColour.getRGB());

            xPixelPointer++;
        }
    return image;
    }
}