import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.text.StyledEditorKit.BoldAction;

import java.lang.Math;
import java.util.Scanner;

public class Encoder {

    public static void main(String[] Args){

        Scanner inputScanner = new Scanner(System.in);

        File imageFile = getFileFromUser(inputScanner);
        BufferedImage originalImage = readImage(imageFile);
        String message = getSecretMessage(originalImage,inputScanner);
        String noHeaderPayload = stringToBytes(message);
        String payload = addHeader(noHeaderPayload, originalImage);
        BufferedImage encodedImage = insertPayload(payload, originalImage);
        writeImageToFile(encodedImage, imageFile);
        System.out.println("Message Successfully Encoded");

        inputScanner.close();
    }

    private static File getFileFromUser(Scanner userFileScanner){

        File allFilesInDir[];
        File folder = new File(System.getProperty("user.dir"));
        allFilesInDir = folder.listFiles();

        boolean validInput = false;
        File userFile = null;
        String userFileStr;

        while(!validInput){
            System.out.println("\n\nEnter the filename to encode the message into, below are the possible files:\n");

            for(File filename : allFilesInDir){
                if(filename.getName().contains(".png")){
                    System.out.print(filename.getName() + " | ");
                }
            }

            System.out.println("\n\nEnter the filename:");
            userFileStr = userFileScanner.nextLine();
            
            for(File currentFile : allFilesInDir){
                if(currentFile.getName().equals(userFileStr)){
                    userFile = currentFile;
                    validInput = true;
                }
            }
            if(userFileStr.equals("exit")){
                System.exit(0);
            }
            if(!validInput){
                System.out.println("\n______________________________________________________________________________"+
                                    "\nINVALID INPUT: Please enter a valid full filename, or exit to exit the program\n"+
                                    "______________________________________________________________________________");
            }
        }

        return userFile;
    }

    private static String getSecretMessage(BufferedImage image, Scanner secretMessageScanner){

        int maximumPayloadSize = image.getWidth()*image.getHeight()*3;
        int headerSize = (Integer.toBinaryString(maximumPayloadSize)).length();
        int maxPayloadCharacters = (maximumPayloadSize - headerSize)/8;

        boolean encodedMessageFitsPayload = false;
        String secretMessage="";
        
        while(!encodedMessageFitsPayload){
            System.out.println("\nEnter your secret Message\nSecret message character length: " + maxPayloadCharacters +
                                ", this is approximately " + (maxPayloadCharacters/6) +" words");
            
            secretMessage = secretMessageScanner.nextLine(); 

            if(secretMessage.length()>maxPayloadCharacters){
                System.out.println("\n______________________________________________________________________________________________________________"+
                                    "\nPAYLOAD TOO LARGE: Your entered will not fit the image " + 
                                    "either shorten payload or start again with a larger image" +
                                    "\n_____________________________________________________________________________________________________________");
            } else{
                encodedMessageFitsPayload = true;
            }
        }

        return secretMessage;
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
    private static String addHeader(String noHeaderPayload, BufferedImage image){
        
        int maximumPayloadSize = image.getWidth()*image.getHeight()*3;
        int headerSize = (Integer.toBinaryString(maximumPayloadSize)).length();
        
        String payloadSizeBinaryString = Integer.toBinaryString(noHeaderPayload.length());

        String header = String.format("%" + headerSize + "s", payloadSizeBinaryString).replace(' ', '0');

        return (header + noHeaderPayload);
    }

    //Basic Read image method, could change in future
    private static BufferedImage readImage(File imageFile){

        BufferedImage payloadImg = null;
        
        try{
        payloadImg = ImageIO.read(imageFile);
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

    //Writes the new image to the file
    private static void writeImageToFile(BufferedImage image, File writeFile){
        
        try{
            ImageIO.write(image, "png", writeFile);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Payload inserted into the image
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