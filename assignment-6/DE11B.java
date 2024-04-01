// DE11B.java CS5125/6025 cheng 2024
// inverse of DE11A.java
// last byte of the source of DE11A may be repeated at most 7 times
// gamma code decoder
// Usage: java DE11B < DE11Aout > DE11Bout

import java.io.*;
import java.util.*;

public class DE11B{
  static int BLOCKSIZE = 8192;
  byte[] data = new byte[BLOCKSIZE];
  int length = 0;
  int index = 0;        // used by inputBit
  int pos = 0x80;        //  used for inputBit

 void readData(){
   try {
      length = System.in.read(data);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
 }

 boolean inputBit(){ 
   boolean one = ((data[index] & pos) != 0);
   pos >>= 1;
   if (pos == 0){
     pos = 0x80;
     index++;
   }
   return one;
 }

 int nextGamma(){
   int n = 0;
   while (inputBit()) n++;
   int decodedGamma = 1;
   for (int i = 0; i < n; i++){
       decodedGamma <<= 1;
       if (inputBit()) decodedGamma |= 1;
   }
    return decodedGamma;
 }

 void decode(){
   while (index < length)
      System.out.write(nextGamma() + 1);
   System.out.flush();
 }

 public static void main(String[] args){
  DE11B de11 = new DE11B();
  de11.readData();
  de11.decode();
 }
}