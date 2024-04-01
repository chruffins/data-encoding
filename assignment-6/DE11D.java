// DE11D.java CS5125/6025 Cheng 2024
// inverse of RLE implemented in DE11C.java
// Usage: java DE11D < DE11Cout > DE11Dout

import java.io.*;
import java.util.*;

public class DE11D{
   static int BLOCKSIZE = 8192;

 void decode(){
   byte[] data = new byte[BLOCKSIZE];
   int length = 0;
   try{
     length = System.in.read(data);
   }catch(IOException e){
      System.err.println(e.getMessage());
      System.exit(1);
   }
   int index = 0;
   while (index < length)
      if (data[index] == 0){
         int runLength = Byte.toUnsignedInt(data[++index]) + 1;
         // Your code to increment index
          index++;
         for (int i = 0; i < runLength; i++) System.out.write(0);
      }else System.out.write(data[index++]);
   System.out.flush();
 }


 public static void main(String[] args){
  DE11D de11 = new DE11D();
  de11.decode();
 }
}

