# ForensicsCourseWorkTeamAG
Git for the programming task for the forensics courseWork

Code was created and tested using java 17.0.1, it should work with any later versions too.

## Setting up Encoder/Decoder

Compile the .java files by using javac, use `javac Encoder.java Decoder.java` to compile both into two class files at the same time.

In order to run you require:
* A text file of the text that you want to hide in the image, this file must be a txt.
* An image file to hide the text in, this must be a png.
These files can be named whatever you like as you will be prompted for the name when running the program.

In order to Encode the payload text file and image file must be in the same directory as the runnable Encoder.class file. 
In order to Decode the image the encoded image file must be located in the same directory as the runnable Decoder.class file.

## Running the Encoder/Decoder

### Encoder
To run open a command prompt in the directory with the image file, the payload text file and Encoder.class files.

1. In the prompt run `java Encoder`
2. This takes you to a page asking for the file to encode the image on. Enter the filename you wish to use, this must be exact and include the file extension or it will not work. Alternatively you can enter `exit` to quit the program. If you enter an invalid string (filename doesn't exist or not exit) you will be presented with an error message and prompted again.
3. Once a image file is selected you will then be prompted for which text file to encode to the image. Same rules apply as for the png file, the entered file must be exact, and the user can use exit to close the program. This stage also will let the user know if the text file selected is too long to encode into the image. If it is it will display and error message and close the program.
4. Once the payload file is selected the program will then insert the payload and replace the previous png file with a new one of the same name that has been encoded with the secret message. Once this is complete the program will display an encoding successful message and close the program.

### Decoder
To run open a command prompt in the directory with the image file believed to have a message and the Encoder.class files.

1. In the prompt run `java Decoder`
2. This will display the page asking to enter the name of the image file to decode. Like the Encoder the filename entered must be perfect and include the file extension in order to function. Similarly the user can enter `exit` to quit the program at this stage.
3. Once the filename is entered successfully the program will then decode this image and place the decoded payload into `decodedMessage.txt` in the same directory as the class file.

## Current known limitations and issues

* The program only can encode png files when it could theoretically work with other file types like .bmp .tiff and .gif
* The only way to enter a payload is first to place it into a text file in the same directory. This is a bit annoying and could be overcome with a basic GUI.
* It is possible to create an error when attempting to decode a file that has not been encoded. If the unencoded file happens to contain a header that is greater than the maximum possible payload it will attempt to write to more pixels than exist in the file causing an Index out of bounds error. This could be fixed in the code where a check flags if the size obtained by the header is greater than the possible payload length displaying an error then closing the program.
* If the decoder decodes an unencoded image and it does not have the error above it will be decoded successfully however the message will obviously be garbage. I don't think there is anyway around this just worth stating. 
* Program is not light enough. The program stated in the task says the program should be lightweight enough for an engineer to create and discard in one session. This version is too large for that. Another lighter version could be created as this one as a base, LightEncoder.java and LightDecoder.java ?
* Can only Encode ASCII characters. If unicode characters are reached the program will not encode or decode correctly causing output to be reduced to garbage