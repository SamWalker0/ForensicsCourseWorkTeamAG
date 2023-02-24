public class Encoder {

    public static void main(String[] Args){


    }

    //Takes a string and returns the ascii bit representation for each character 
    public static String stringToBytes(String message){
        
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






} 