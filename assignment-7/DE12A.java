// DE12A.java CS5125/6025 Cheng 2024
// making PNG with color type 0 and bit depth 1 (bi-level image)
// with random artefact
// Usage: java DE12A < hand.pbm > hand.png

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class DE12A{

   static final int bufferSize = 8192;
   static final int IHDRSize = 33;
   static final byte[] signature = new byte[]{
    137 - 256, 80, 78, 71, 13, 10, 26, 10,
    0, 0, 0, 13, 73, 72, 68, 82 };
   static byte[] iend = new byte[]{
    0, 0, 0, 0, 'I', 'E', 'N', 'D', 0, 0, 0, 0 };
   byte[] ihdr = new byte[IHDRSize];
   byte[] data = null;
   byte[] idat = null;
   int compressedDataLength = 0;

   int width = 0;
   int height = 0;
   int bytesPerRow = 0;
   byte[] buffer = new byte[bufferSize];
   int readLen = 0;
   int index =0;
   CRC32 crc32 = new CRC32();
 
 void readPBMHeader(){
  try{
    readLen = System.in.read(buffer);
  } catch (IOException e){
    System.err.println(e.getMessage());
    System.exit(1);
  }
  if (buffer[0] != 'P' || buffer[1] != '4'){
    System.err.println(" not P4");
    System.exit(1);
  }
  int pos = 0; 
  while (buffer[pos++] != '\n');
  int pos2 = pos + 1;
  while (buffer[pos2++] != '\n');
  String secondLine = new String(buffer, pos, pos2 - pos - 1);
  String[] terms = secondLine.split(" ");
  width = Integer.parseInt(terms[0]);
  height = Integer.parseInt(terms[1]);
  bytesPerRow = width / 8; 
  index = pos2;
  data = new byte[height * (bytesPerRow + 1)];
}

byte getNextByte(){
    if (index >= readLen){
      try{
        readLen = System.in.read(buffer);
      } catch (IOException e){
         System.err.println(e.getMessage());
         System.exit(1);
      }
      if (readLen < 0) return -1;
      index = 0; 
    }
    // To turn 0 into 1 and 1 into 0, you need to flip the byte.
    // Hint: turn byte into int, bitwise unary operator ~, and (byte) casting
    return buffer[index++];
 }

void readData(){
   int offset = 0;
   for (int i = 0; i < height; i++){
     data[offset++] = 0; // the filter type byte
     for (int j = 0; j < bytesPerRow; j++)
        data[offset++] = getNextByte();
   }
}

 // fill 4 bytes in buffer at offset with a number
 void fillNumber(byte[] buffer, int offset, long number){
   int k = 0; for (; k < 4; k++){
     buffer[offset + 3 - k] = (byte)(number & 0xff);
     number >>= 8;
   }
 }

 void fillIHDR(){
   for (int i = 0; i < 16; i++) ihdr[i] = signature[i];
   fillNumber(ihdr, 16, width);
   fillNumber(ihdr, 20, height);
   ihdr[24] = 1;
   for (int i = 25; i < 29; i++) ihdr[i] = 0;
   crc32.reset();
   crc32.update(ihdr, 12, 17);
   fillNumber(ihdr, 29, crc32.getValue());
 }
 void fillIDAT(){
   idat = new byte[height * (bytesPerRow + 1)];
   idat[4] = 'I'; idat[5] = 'D'; idat[6] = 'A'; idat[7] = 'T';
   Deflater compresser = new Deflater();
   compresser.setInput(data);
   compresser.finish();

   // 1. deflate data into idat at position 8
   compressedDataLength = compresser.deflate(idat, 8, idat.length - 8);
   // 2. place compressedDataLength at position 0 of idat
     fillNumber(idat, 0, compressedDataLength);
   // 3. compute CRC for idat without the length
     crc32.reset();
     crc32.update(idat, 8, compressedDataLength);
     // 4. append CRC after compressed data
     fillNumber(idat, 8 + compressedDataLength, crc32.getValue());
   // idat = |length|"IDAT"|compressed data|CRC|

 }

 void fillIEND(){
   crc32.reset();
   crc32.update(iend, 4, 4);
   fillNumber(iend, 8, crc32.getValue());
 }

 void writePNG(){
  try {
   System.out.write(ihdr);
   System.out.write(idat, 0, compressedDataLength + 12);
   System.out.write(iend);
  } catch (IOException e){
    System.err.println(e.getMessage());
    System.exit(1);
  }
 }


 public static void main(String[] args){
      DE12A de12 = new DE12A(); 
      de12.readPBMHeader();
      de12.readData();
      de12.fillIHDR();
      de12.fillIDAT();
      de12.fillIEND();
      de12.writePNG();
 }
}
