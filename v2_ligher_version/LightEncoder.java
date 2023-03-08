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

// Encoder Encodes a text file and hides it's contents within a png file, both files must be located inside the same file location as where the class file is compiled
public class LightEncoder {

    public static void main(String[] Args){

        // creates scanner used for user inputs
        Scanner inputScanner = new Scanner(System.in);

        // gets the contents of the entered png file and stores this picture data into "image"
        System.out.println("\nEnter the image filename:");
        String userFileStr = inputScanner.nextLine();
        File imageFile = new File(userFileStr);
        BufferedImage image = getImageData(imageFile);

        // gets the contents of the txt file entered and stores it into "message"
        System.out.println("\nEnter the payload filename:");
        String payloadFileStr = inputScanner.nextLine();
        File payloadFile = new File(payloadFileStr);     
        String message = getpayloadData(payloadFile);

        // converts the message into a string of bits ready to be encoded into the image
        String payload = messageToPayloadWithHeader(message);

        // inserts the payload into the image and returns the new image 
        BufferedImage encodedImage = insertPayload(payload, image);

        // writes the new image into the original image file
        try{
            ImageIO.write(encodedImage, "png", imageFile);
        } catch (IOException e){
            e.printStackTrace();
        }

        // prints success message and closes the input scanner
        System.out.println("\nMessage Successfully Encoded");
        inputScanner.close();
    }

    // returns the image data from a file
    private static BufferedImage getImageData(File imageFile){
        BufferedImage payloadImg = null;    
        try{
        payloadImg = ImageIO.read(imageFile);
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }

    // gets the message text from the text file
    private static String getpayloadData(File payloadFile){

        // uses a string builder for appending the message as its read 
        StringBuilder payloadBuilder = new StringBuilder();

        // uses a bufferedReader to read the file a line at a time catching any exceptions along the way
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

        //removes non ascii characters as they cause issues decoding at the other end
        String payloadtoString = payloadBuilder.toString();
        String onlyAsciiPayload = payloadtoString.replaceAll("[^\\x00-\\x7F]", "");
        return onlyAsciiPayload;

    }

    // converts a message into a bit string with a header
    private static String messageToPayloadWithHeader(String message){
        
        // convert the string message into a character array and a string builder to append the result to
        char[] chs = message.toCharArray();
        StringBuilder binaryMessage = new StringBuilder();

        // loop through the char array converting it using toBinaryString to get the ascii representation of the character and uses format to ensure its 8 bits long
        for(char ch : chs){
            binaryMessage.append(
                String.format("%8s", Integer.toBinaryString(ch))
                .replaceAll(" ","0")  
                );
        }

        // convert string builder into a normal string 
        String noHeaderPayload = binaryMessage.toString();

        // get the payload length, convert this to a binary string then ensure this is 25 bit long using format
        String payloadSizeBinaryString = Integer.toBinaryString(noHeaderPayload.length()); 
        String header = String.format("%" + 25 + "s", payloadSizeBinaryString).replace(' ', '0');

        // add the header to the front then of the payload then return
        return (header + noHeaderPayload);
    }
    
    // simple method that replaces the last character of a string then returns the new string
    private static String replaceLastChar(String text, char newLastChar){
        String removeLastChar = text.substring(0,text.length()-1);
        return (removeLastChar+ newLastChar);
    }

    // main method that inserts the bit string payload into the image
    private static BufferedImage insertPayload(String payload, BufferedImage image){

        // intialise pointers and the number of pixels that need to be changed
        int payloadPointer = 0;
        int pixelCount = (int)Math.ceil(payload.length()/3);
        int xPixelPointer = 0;
        int yPixelPointer = 0;
        
        // loops through all the pixels that need to be changed in the image
        for(int pixelIterator=0; pixelIterator<=pixelCount; pixelIterator++){

            // update x,y pixel pointers, this traverses the image from the top left to top right then moving down a row at a time
            // this checks if the pointer is pointing past the x (width) of the file, if so it updates x back to the start and increments the y pointer, moving it down a new row
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            // gets the colour data for the current pixel using pointers
            Color originalPixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));
            int newRedInt = originalPixelColour.getRed();
            int newGreenInt = originalPixelColour.getGreen();
            int newBlueInt = originalPixelColour.getBlue();        

            // if statements are used so if a pixel is only half full of payload (end of payload is reached at G for example) the program does not attempt to write past this point  
            if(((payload.length()-1)-payloadPointer)>=0){

                // get the colour pixel data and convert this into a string bit representation 
                String redBinaryRep = Integer.toBinaryString(originalPixelColour.getRed());
                
                // replace the last character of the colour bit representation with the current payload bit, once complete convert this back into a int and increment the payload pointer
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
            
            // create a new pixel colour data with the new colour data from above, if there is no change it remains the same
            Color newColour = new Color(newRedInt, newGreenInt, newBlueInt);

            // update the original image with the new obtained colour data
            image.setRGB(xPixelPointer, yPixelPointer, newColour.getRGB());

            // increment the pointer to the next pixel
            xPixelPointer++;
        }
    // return the resulting encoded image
    return image;
    }
}