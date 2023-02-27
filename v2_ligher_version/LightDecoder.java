import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;
import java.util.Scanner;

public class LightDecoder{
    
    public static void main(String[] Args){

        Scanner inputScanner = new Scanner(System.in);

        File encodedFile = getFileFromUser(inputScanner);
        BufferedImage encodedImage = readImage(encodedFile);
        String decodedMessage = decodeImage(encodedImage);
        printMessageToFile(decodedMessage);
        System.out.println("Message decoded to file.");

        inputScanner.close();
    }

    private static void printMessageToFile(String message){

        File textFile = new File(System.getProperty("user.dir")+File.separator+"DecodedMessage.txt");

        try{
            FileWriter fileWriter = new FileWriter(textFile);
            fileWriter.write(message);
            fileWriter.close();
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static File getFileFromUser(Scanner userFileScanner){

        File allFilesInDir[];
        File folder = new File(System.getProperty("user.dir"));
        allFilesInDir = folder.listFiles();

        boolean validInput = false;
        File userFile = null;
        String userFileStr;

        while(!validInput){
            System.out.println("\n\nEnter the filename to decode the message from, below are the possible files:\n");

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


    private static BufferedImage readImage(File imageFile){

        BufferedImage payloadImg = null;
        
        try{
        payloadImg = ImageIO.read(imageFile);
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }  

    private static String decodeImage(BufferedImage image){

        int headerSize = 25;

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

        for(int payloadPackageIterator = 0; payloadPackageIterator <= payloadPackages; payloadPackageIterator++){
            
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            Color pixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            if( (((headerSize+payloadSize)-1) - bitPointer ) >=0 ){
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "red");
                bitPointer++;
            } if( ((headerSize+payloadSize)-1) - bitPointer >=0 ){
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "green");
                bitPointer++;
            } if( ((headerSize+payloadSize)-1) - bitPointer >=0 ){
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "blue");
                bitPointer++;
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