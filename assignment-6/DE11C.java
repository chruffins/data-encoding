// DE11C.java CS5125/6025 Cheng 2024
// compress output from DE10A.java with RLE only for 0
// each sequence of seqlen 0s is replaced with one 0 and 
// the number (seqlen - 1)
// assuming no strech of 0 is longer than 256
// Usage: java DE11C < DE10Aout1 > DE11Cout

import java.io.*;
import java.util.*;

public class DE11C{
   static int BLOCKSIZE = 8192;

 void encode(){
   byte[] data = new byte[BLOCKSIZE];
   int length = 0;
   try{
     length = System.in.read(data);
   }catch(IOException e){
      System.err.println(e.getMessage());
      System.exit(1);
   }
   boolean isZero = false;
   int runLength = 0;
   for (int i = 0; i < length; i++){
     if (data[i] == 0) if (isZero) runLength++;
                       else{ isZero = true; runLength = 0; }
     else{  // not zero
      if (isZero){ // end of a run of zeros
       System.out.write(0);
       System.out.write(runLength);
       isZero = false;
      } // the non-zero to be copied
      System.out.write(data[i]); 
     }
   }
   if (isZero){  // leftover zeros
       System.out.write(0);
       System.out.write(runLength);
    }
   System.out.flush();
 }


 public static void main(String[] args){
  DE11C de11 = new DE11C();
  de11.encode();
 }
}

