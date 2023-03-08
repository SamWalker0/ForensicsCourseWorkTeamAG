import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;
import java.util.Scanner;

// Decodes an image to get the hidden string
public class LightDecoder{
    
    public static void main(String[] Args){

        // create scanner for user input
        Scanner inputScanner = new Scanner(System.in);

        // get the file from the user
        File encodedFile = getFileFromUser(inputScanner);
        
        // get the image from the file
        BufferedImage encodedImage = readImage(encodedFile);
        
        // get the string from the image
        String decodedMessage = decodeImage(encodedImage);

        // print the message to a file
        printMessageToFile(decodedMessage);

        // print the message to the terminal
        System.out.println("Message decoded to file.");

        // close the input scanner
        inputScanner.close();
    }

    // print the message to DecodedMessage.txt
    private static void printMessageToFile(String message){

        // create a text file called "DecodedMessage.txt"
        File textFile = new File(System.getProperty("user.dir")+File.separator+"DecodedMessage.txt");

        // write the message to the file throw an exception if it fails
        try{
            FileWriter fileWriter = new FileWriter(textFile);
            fileWriter.write(message);
            fileWriter.close();
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // get the file to be read from the user
    private static File getFileFromUser(Scanner userFileScanner){

        // get all the files in the current dir
        File allFilesInDir[];
        File folder = new File(System.getProperty("user.dir"));
        allFilesInDir = folder.listFiles();

        boolean validInput = false;
        File userFile = null;
        String userFileStr;

        
        while(!validInput){
            
            // print all current eligable files to decode to the terminal
            System.out.println("\n\nEnter the filename to decode the message from, below are the possible files:\n");
            for(File filename : allFilesInDir){
                if(filename.getName().contains(".png")){
                    System.out.print(filename.getName() + " | ");
                }
            }

            // get the user input
            System.out.println("\n\nEnter the filename:");
            userFileStr = userFileScanner.nextLine();
            
            // get the File that matches the user input if none do print error message and ask again
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

    // Given a bit string returns a String of the decoded message
    private static String bytesToString(String bytesMessage){

        // split the bits into an array of 8 bits so they are ready to be decoded
        String[] characterByte = bytesMessage.split("(?<=\\G.{8})");

        // create a string builder ot build the decoded message
        StringBuilder decodedMessage = new StringBuilder();

        // turns each 8 bit string into a number then gets the ascii character from this number and adds it to the builder
        for (String by : characterByte){   
            int tempInt = Integer.parseInt(by, 2);
            char c = (char)tempInt;
            decodedMessage.append(c);
        }
        //returns the decoded message string
        return decodedMessage.toString();
    }


    //reads the image from a given imageFile
    private static BufferedImage readImage(File imageFile){

        BufferedImage payloadImg = null;
        
        try{
        payloadImg = ImageIO.read(imageFile);
        } catch(IOException e){
            e.printStackTrace();
        }
        return payloadImg;
    }  

    // decodes the message for an image
    private static String decodeImage(BufferedImage image){

        // init pointers and header size and string builders for the decoded bit strings
        int headerSize = 25;
        int headerPixels = (int)Math.ceil(headerSize/3);

        int bitPointer = 0;
        int xPixelPointer = 0;
        int yPixelPointer = 0;

        StringBuilder headerBuilder = new StringBuilder();
        StringBuilder payloadBuilder = new StringBuilder();

        // loop through all necessary pixels that will contain header information
        for(int headerPixelIterator=0;headerPixelIterator<=headerPixels;headerPixelIterator++){
           
            // update x,y pixel pointers, this traverses the image from the top left to top right then moving down a row at a time
            // this checks if the pointer is pointing past the x (width) of the file, if so it updates x back to the start and increments the y pointer, moving it down a new row
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }
            
            // get pixel colour data for the current pixel using pointers
            Color pixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            // checks to make sure the builder doesnt read past the bits for the header
            if (  ((headerSize-1) - bitPointer) >= 0 ){
                // header builder gets the least significant bit appended if it is not reading past the required colour
                headerBuilder = appendLSB(headerBuilder, pixelColour, "red");
            } else{
                // if the header has all required bits then bits read in the current pixel instead get added to the payload builder since they will be there anyway regardless of message size
                payloadBuilder = appendLSB(payloadBuilder, pixelColour, "red");                  
            }
            // increment bit pointer after every colour read
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
            
            // increment x pointer to traverse the image
            xPixelPointer++;
        }

        // header bits are now all read so convert the builder to string then convert the binary to an int to get the payload size
        String headerBits = headerBuilder.toString();
        int payloadSize = Integer.parseInt(headerBits,2);

        // find the number of pixels needed to be traversed from the now obtained size
        int payloadPixels = (int)Math.ceil(payloadSize/3);

        // loop through all these pixels 
        for(int payloadPixelIterator = 0; payloadPixelIterator <= payloadPixels; payloadPixelIterator++){
            
            // traverse the image same as before
            if(xPixelPointer>=image.getWidth()){
                xPixelPointer=0;
                yPixelPointer++;
            }

            // get the colour information of the current pixel
            Color pixelColour = new Color(image.getRGB(xPixelPointer, yPixelPointer));

            // check if all the required pixels have been read, if not add the lsb of the pixel to the builder and increment bit pointer otherwise skip
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
        // turn the bitstring obtained into a string and return it
        String decodedMessage = bytesToString(payloadBuilder.toString());       
        return decodedMessage;
    }

    // used to append the least significant bit of a colour value to a string
    private static StringBuilder appendLSB (StringBuilder text, Color pixelColour, String colourIdentifier){

        // init a string to be appended to the text string
        String binaryStr = "";

        // switch case to identify which colour value we are taking the lsb from, add the full binary str of this colour value to the binaryStr
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
        // find the lastmost value of the binary value of the colour value then append this to the text string and return it
        return text.append(binaryStr.charAt(binaryStr.length() - 1));
    }

}