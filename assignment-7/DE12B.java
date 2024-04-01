// DE12B.java CS5125/6025 Cheng 2024
// making PNG with color type 2 and bit depth 8 (RGB image)
// select filter type 0-4 for each scanline 
// Usage: java DE12B < LenaRGB.bmp > lena.png

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class DE12B{

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
   int readLen = 0;
   int dataSize = 0;
   CRC32 crc32 = new CRC32();
   int[][][] raw = null;
 
 void readBMPHeader(){
   byte[] header = new byte[54]; // 54 bytes for header
   try {
      readLen = System.in.read(header);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   if (readLen < 54){
     System.err.println("missing header");
     System.exit(1);
   }
   if (header[0] != 'B' || header[1] != 'M'
      || header[14] != 40 || header[28] != 24){
     System.err.println("wrong header");
     System.exit(1);
   }
   width = Byte.toUnsignedInt(header[19]) * 256 + Byte.toUnsignedInt(header[18]);
   height = Byte.toUnsignedInt(header[23]) * 256 + Byte.toUnsignedInt(header[22]); 
   // BMP uses little endian
 }

void readImage(){
   byte[] image = new byte[height * width * 3];
   try {
      readLen = System.in.read(image);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   if (readLen < height * width * 3){
     System.err.println("missing data");
     System.exit(1);
   }
   raw = new int[height][width][3]; 
   for (int i = 0; i < height; i++){
      int offset = (height - 1 - i) * width * 3;
      for (int j = 0; j < width; j++)
         for (int k = 0; k < 3; k++) // little endian in BMP 
            raw[i][j][k] = Byte.toUnsignedInt(image[offset + j * 3 + (2 - k)]);
   }
   dataSize = height * (width * 3 + 1);
   data = new byte[dataSize]; 
 }

 int filter(int ftype, int a, int b, int c){
   switch(ftype){
      case 1: return a;
      case 2: return b;
      case 3: return (a + b) / 2;
      case 4: int p = a + b - c;
              int pa = a <= p ? p - a : a - p;
              int pb = b <= p ? p - b : b - p;
              int pc = c <= p ? p - c : c - p;
              return (pa <= pb && pa <= pc) ? a : 
                 (pb <= pc ? b : c);
      default: return 0;
   }
  }

 void fillRow(int row, int ftype){ // rwo > 0
   int index = row * (width * 3 + 1); // start row position at data
   data[index++] = (byte)ftype; // the filter type byte
   for (int j = 0; j < width; j++)
      for (int k = 0; k < 3; k++){
         int a = j == 0 ? 0 : raw[row][j-1][k];
         int b = row == 0 ? 0 : raw[row-1][j][k];
         int c = j == 0 ? 0 : row == 0 ? 0 : raw[row-1][j-1][k];
         data[index++] = (byte)(raw[row][j][k] - filter(ftype, a, b, c)); 
      }
 }

 void fillData(int type){ // example: all rows have filter type 0
     if (type == 0) {
         fillRow(0, 0);  // row 0 can only have type 0 or 1
         for (int row = 1; row < height; row++) fillRow(row, 0);
     } else if (type == 1) {
         fillRow(0, 1);
         for (int row = 1; row < height; row++) fillRow(row, 1);
     } else if (type == 2) {
         fillRow(0, 1);
         for (int row = 1; row < height; row++) fillRow(row, 2);
     } else if (type == 3) {
         fillRow(0, 1);
         for (int row = 1; row < height; row++) fillRow(row, 3);
     } else if (type == 4) {
         fillRow(0, 1);
         for (int row = 1; row < height; row++) fillRow(row, 4);
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
   ihdr[24] = 8; ihdr[25] = 2;
   for (int i = 26; i < 29; i++) ihdr[i] = 0;
   crc32.reset();
   crc32.update(ihdr, 12, 17);
   fillNumber(ihdr, 29, crc32.getValue());
 }

 void fillIDAT(){
   idat = new byte[dataSize];
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
      DE12B de12 = new DE12B(); 
      de12.readBMPHeader();
      de12.readImage();
      de12.fillData(4);
      de12.fillIHDR();
      de12.fillIDAT();
      de12.fillIEND();
      de12.writePNG();
 }
}
