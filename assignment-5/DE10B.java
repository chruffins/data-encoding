// DE10B.java CS5125/6025 Cheng 2024
// Decoding Burrows-Wheeler
// Usage: java DE10B < transformed(with DE10A) > original

import java.io.*;
import java.lang.*;

class DE10B{
  static int BLOCKSIZE = 8192;
  static int numberOfSymbols = 256;
  int length = 0;  // length of block 
  int[] A = new int[numberOfSymbols]; 
  int[] L = new int[BLOCKSIZE];  // the Burrows-Wheeler transformation
  int[] F = new int[BLOCKSIZE]; 
  int[] T = new int[BLOCKSIZE];
  int I;  // the position of original after suffix sort

 void initializeA(){
    for (int i = 0; i < numberOfSymbols; i++) A[i] = i;    
 }
  
 void readBlock(){
   byte[] buffer = new byte[BLOCKSIZE + 2];
   try{
     length = System.in.read(buffer) - 2;
   }catch(IOException e){
      System.err.println(e.getMessage());
      System.exit(1);
   }
   if (length <= 0) return;
   I = Byte.toUnsignedInt(buffer[0]) * 256 + Byte.toUnsignedInt(buffer[1]);
   for (int i = 0; i < length; i++){
      int j = Byte.toUnsignedInt(buffer[i + 2]);
      int t = A[j];
      L[i] = t;
      for (int k = j; k > 0; k--) A[k] = A[k-1]; 
      A[0] = t; // move to front
   }
 }

 void deBW(){
   for (int i = 0; i < length; i++){
     int j = i - 1; for (; j >= 0; j--)
       if (L[i] < F[j]) F[j + 1] = F[j];
       else break;
     F[j + 1] = L[i];
   }
   int j = 0;
   for (int i = 0; i < length; i++){
    if (i > 0 && F[i] > F[i - 1]) j = 0;
    for (; j < length; j++) if (L[j] == F[i]) break;
    T[i] = j++;
   }
   // Now we have I, L, F, and T 
   // Your code here for printing the decoded block using System.out.write().
   // Write one byte a time
     int current_value = I;
   System.out.write(F[I]);

     for (int i = 0; i < length; i++) {
        current_value = T[current_value];
        System.out.write(F[current_value]);
     }
     System.out.flush();
 }

 void decode(){
   initializeA();
   while (true){
     readBlock();
     if (length <= 0) return;
     deBW();
     if (length < BLOCKSIZE) return;
   }
 }

 public static void main(String[] args){
  DE10B de10 = new DE10B();
  de10.decode();
 }
}


