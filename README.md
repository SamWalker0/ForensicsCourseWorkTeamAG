# ForensicsCourseWorkTeamAG
Git for the programming task for the forensics courseWork

Code was created using java 17 and successfully tested to work on Java 18 and 19 too. The latest library used was from Java 8 so should be very backwards compatible for previous versions.

The encoder provided takes a text from a txt file and encodes this information into the least significant bit of each pixel in a png file replaceing the original image. Using the decoder this text can then be retrieved from the encoded image and is printed to a new text file in the directory. The files are made to be lightweight so both run in the terminal using simple commands. In order to keep the size of files small the files are compiled to class files and ran as these class files, full instructions of how to compile and run the files are below.

We have two working versions of this code; v1 and v2. Version 1 contains more error correction and a dynamic header however was replaced with v2 that uses a fixed header and less error correction in order to make the file shorter to better fit the case study provided. Version 2 was made the primary version of the program so version 1 does not have all the upgrades and fixes from version 2 such as handling none ascii characters. As version 2 is the primary version all instructions provided below are for this version, this is the "LightEncoder".


## Setting up Encoder/Decoder

Compile the .java files by using javac, use `javac LightEncoder.java LightDecoder.java` to compile both into two class files at the same time.

In order to run you require:
* A text file of the text that you want to hide in the image, this file must be a txt.
* An image file to hide the text in, this must be a png.
These files can be named whatever you like as you will be prompted for the name when running the program.

**In order to Encode the payload text file and image file must be in the same directory as where the runnable class file was compiled. The LightEncoder uses simple IO to read the current directory and will only read the directory for where the class file is compiled. This means if the class file is compiled in directory x then moved to directory y it will still read x for the image and text file.**
In order to Decode the image the encoded image file must be located in the same directory as the runnable Decoder.class file. This file has better IO so the class file can be moved between directories and will read whatever directory the class file is in. The difference here was one of the ways the LightEncoder was made shorter to be easier to write quickly as the case study suggests.

## Running the Encoder/Decoder

### Encoder
To run open a command prompt in the directory with the image file, the payload text file and Encoder.class files.

1. In the prompt run `java LightEncoder`
2. This takes you to a page asking for the file to encode the image on. Enter the filename you wish to use, this must be exact and include the file extension or it will not work. If you enter an invalid string (filename doesn't exist or not exit) an exception will be thrown and the program will close.
3. Once a image file is selected you will then be prompted for which text file to encode to the image. Same rules apply as for the png file, the entered file must be exact. This stage also throw an exception if there is an error encoding the file such as if the message from the text is too long or the file does not exist, this will cause the program to close.
4. Once the payload file is selected the program will then insert the payload and replace the previous png file with a new one of the same name that has been encoded with the secret message. Once this is complete the program will display an encoding successful message and close the program.

### Decoder
To run open a command prompt in the directory with the image file believed to have a message and the Encoder.class files.

1. In the prompt run `java LightDecoder`
2. This will display the page asking to enter the name of the image file to decode. Like the Encoder the filename entered must be perfect and include the file extension in order to function. Similarly the user can enter `exit` to quit the program at this stage.
3. Once the filename is entered successfully the program will then decode this image and place the decoded payload into `decodedMessage.txt` in the same directory as the class file.

## How it works

For complete details for how the code works it is recommended to read the java files of LightEncoder.java and LightDecoder.java as they feature extensively commented code however the basic steps are below:

### Encoder

1. Get a string from a text file
2. Convert this string into a string of bits where each character is converted to an 8 bit ASCII character and concatanated together
3. Create a 25 bit header to this bit string which is the size of the bit string in binary
4. Concatanate this header to the start of the bit string
5. Get an image from the user
6. Go through every pixel of this image replacing the least significant bit of the R, G, and B values with the values of the bit string
7. Once the full message has been encoded write the new image created back to replace the original png file

### Decoder

1. Get the png file to decode from the user
2. Read the first 25 least significant R, G and B values from pixels in the image adding each LSB to a bit string
3. Take this 25 binary string and convert it to an integer, this is the number of bits that need to be read
4. Continue to read this number of LSB bits from the image concatanating each to a bit string 
5. Once this has been read, break up the bit string into 8 bit chunks and turn each into an ASCII character
6. Concatanate these characters together and we return to the original message
7. Print this message to decodedMessage.txt ready to be read by the user

## Current known limitations and issues

* The program only can encode png files when it could theoretically work with other file types like .bmp .tiff and .gif however this would make the program longer
* The LightDecoder will attempt to decode any message regardless of if it has actually been encoded. This means it can attempt to decode images that contain no text causing eiter an error or a decoded message that contains garbage output. There is no way around this really only adding potentially another header that can identify if the image has been modified but this would make the code longer for very little gain.
* Can only Encode ASCII characters. UNICODE characters can be longer than 8 bits, this would cause enormous difficulty when decoding as in order to decode all letters must be the same bit length so to include unicode characters all characters would have to be normalised to 16 bits long. This would cut the capacity of character hiding in half. Instead our program will when encoding remove any non ASCII characters from the text. This means that for languages with latin letters the encoded text would be almost entirely the same but for other character sets the program would not cope well. The decision was made that using ASCII to keep capacity made sense for the case study mentioned and any test cases so this would be good enough for our purposes.
* Interface is a bit more cumbersome than desired. Due to the limitation to try to keep the program short the code must be run through a terminal and with text files. This is not ideal as a basic gui would make the program easier to use, however, this is not really possible while keeping the program as short as possible.
* There is a maximum size of message that can be encoded. The header is set to be 25 bits long this means any message longer than the highest 25 binary number cannot be encoded. This number was set to the number of bits necessary to completely encode a 4k image. Version 1 does work for any size of image however this is achieved with greater complexity that was removed to Lighten the code.

## Differences between v1 and v2

Version 1 contains more error handling, a dynamic header, and a few bugs.

* Version 1 was the first version created so is more robust when getting input from the user. The user when encoding images will be presented with all the relavant options and if the user does not enter a file name correctly will be prompted again. Also, when a user enters a text file into an image if the file does not fit they will be presented with a relavant error message then the program would close rather than throwing an exception. These features were removed in v2/LightEncoder since they were not seen as necessary and it was seen as more important to make the encoder shorter.

* Version 1 also uses a dynamic header size. In this version the header is created to only be as long as necessary rather than a fixed size. The length is found by; first finding the maximum number of bits that can be modified, then setting the header to the smallest binary representation of this number. This method is much more efficient for smaller images and means that the code should work for images of any size even extremely large images. This feature was removed as again it added complexity to the program where making the code lighter was seen as more important.

* Version 1 also will feature more bugs. While version 1 features better error handling this version it does not have the same upgrades that were given to version 2 when it took over as the main version of the code. For example version 1 will handle non ASCII characters worse where it will still attempt to encode an unicode character causing the rest of the text to be turned to garbage. It also does not feature the change to a buffered reader from a scanner that was found to be causing issues.