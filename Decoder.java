public class Decoder{

    
    public static void main(String[] Args){

    }


    //Given a bit string returns a String of the decoded message
    public static String bytesToString(String bytesMessage){


        String[] characterByte = bytesMessage.split("(?<=\\G.{8})");
        StringBuilder decodedMessage = new StringBuilder();

        for (String by : characterByte){   

            int tempInt = Integer.parseInt(by, 2);
            char c = (char)tempInt;
            decodedMessage.append(c);

        }

        return decodedMessage.toString();
    }

}