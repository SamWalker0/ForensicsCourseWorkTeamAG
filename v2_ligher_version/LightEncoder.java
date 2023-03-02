import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;

public class LightEncoder {

    public static void main(String[] Args){

        Scanner inputScanner = new Scanner(System.in);

        System.out.println("\nEnter the image filename:");
        String userFileStr = inputScanner.nextLine();

        File imageFile = new File(userFileStr);
        BufferedImage image = getImageData(imageFile);

        System.out.println("\nEnter the payload filename:");
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

        System.out.println("\nMessage Successfully Encoded");

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

    private static String getpayloadData(File payloadFile){

        StringBuilder payloadBuilder = new StringBuilder();

        //now using buffered reader since scanners were having issues
        BufferedReader br = null;
        try {
           br = new BufferedReader(new FileReader(payloadFile));
           String available;
           while((available = br.readLine()) != null) {
                payloadBuilder.append(available+"\n");            
           }
        } catch (FileNotFoundException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } finally {
           if (br != null) {
              try {
                 br.close();
              } catch (IOException e) {
                 e.printStackTrace();
              }
           }
        }

        //removes non ascii characters
        String payloadtoString = payloadBuilder.toString();
        String onlyAsciiPayload = payloadtoString.replaceAll("[^\\x00-\\x7F]", "");
        return onlyAsciiPayload;

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
        System.out.println(payloadSizeBinaryString);      
        String header = String.format("%" + 25 + "s", payloadSizeBinaryString).replace(' ', '0');
        System.out.println(header);
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
        
        for(int packageIterator=0; packageIterator<=bitPackageCount; packageIterator++){

            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            Color originalPixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            int newRedInt = originalPixelColour.getRed();
            int newGreenInt = originalPixelColour.getGreen();
            int newBlueInt = originalPixelColour.getBlue();        

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