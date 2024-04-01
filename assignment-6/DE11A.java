// DE11A.java CS5125/6025 Cheng 2024
// the gamma code on bytes 
// gamma code of unsignedInt(byte) + 1
// There is a dange of adding at most 7 0-bits at end to make a byte file
// Usage: java DE11A < DE10Aout > DE11Aout

import java.io.*;
import java.util.*;

public class DE11A{
   static int BLOCKSIZE = 8192;
   static String maxunary = "11111111111111111";
   int buf = 0;        // buffer for outputBits
   int position = 0;        // buffer used for outputBits

 void outputbits(String bitstring){
     for (int i = 0; i < bitstring.length(); i++){
      buf <<= 1;
      if (bitstring.charAt(i) == '1') buf |= 1;
      position++;
      if (position == 8){
         position = 0;
         System.out.write(buf);
         buf = 0;
      }
     }
 }

 String gammacode(int v){  // v >= 1
   String binary = Integer.toBinaryString(v).substring(1);
   return maxunary.substring(0, binary.length()) + "0" + binary;
 }

 void encode(){
   byte[] data = new byte[BLOCKSIZE];
   int length = 0;
   try{
     length = System.in.read(data);
   }catch(IOException e){
      System.err.println(e.getMessage());
      System.exit(1);
   }
   for (int i = 0; i < length; i++)
      outputbits(gammacode(Byte.toUnsignedInt(data[i]) + 1)); 
   if (position > 0){            // leftover bits
     buf <<= (8 - position);
     System.out.write(buf); 
   }
   System.out.flush();
 }


 public static void main(String[] args){
  DE11A de11 = new DE11A();
  de11.encode();
 }
}

