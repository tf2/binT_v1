/*
 * binAgent - Agent for binT
 * contains reading functions for header and offset info 
 */

package bint_v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tf2
 */
//
//  binAgent.java
//
//
//  Created by Tom Fitzgerald on 28/08/2009.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

public class binAgent {


    public static ArrayList[] NUMBER_NODES = null;
    public static ArrayList[] OFFSET_BYTES = null;
    public static String[] header_info = null;

      // Read the header - hard coded (this header will always have the same format).
      public static String[] ReadHeader(String filename) throws IOException {

        header_info = new String[9];
        int head_len=0;
        ByteBuffer bb = null;

        try {
            RandomAccessFile raf = new RandomAccessFile(filename, "r");

            byte[] tmp = new byte[24];
            raf.read(tmp, 0, 24);
            header_info[0] = new String(tmp);
            tmp = new byte[4];
            raf.read(tmp, 0, 4);
            bb= ByteBuffer.wrap(tmp);
            header_info[1] = "" + bb.getInt();
            tmp = new byte[Integer.parseInt(header_info[1])];
            raf.read(tmp, 0, Integer.parseInt(header_info[1]));
            header_info[2] = new String(tmp);
            tmp = new byte[4];
            raf.read(tmp, 0, 4);
            bb= ByteBuffer.wrap(tmp);
            header_info[3] = "" + bb.getInt();
            tmp = new byte[4];
            raf.read(tmp, 0, 4);
            bb= ByteBuffer.wrap(tmp);
            header_info[4] = "" + bb.getInt();
            tmp = new byte[4];
            raf.read(tmp, 0, 4);
            bb= ByteBuffer.wrap(tmp);
            header_info[5] = "" + bb.getInt();
            tmp = new byte[4];
            raf.read(tmp, 0, 4);
            bb= ByteBuffer.wrap(tmp);
            header_info[6] = "" + bb.getInt();
            tmp = new byte[8];
            raf.read(tmp, 0, 8);
            bb= ByteBuffer.wrap(tmp);
            header_info[7] = "" + bb.getLong();
            tmp = new byte[8];
            raf.read(tmp, 0, 8);
            bb= ByteBuffer.wrap(tmp);
            header_info[8] = "" + bb.getLong();

            if (Main.DEBUG) {
                for (int i=0;i<header_info.length;i++)
                    System.out.println(header_info[i]);
            }
            raf.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(binAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return header_info;

      }

      // Reads offset values and puts in NUMBER_NODES[]
      public static void ReadOffsets(String filename) throws IOException {

          header_info = ReadHeader(filename);

          int Number_of_chrs = Integer.parseInt("" + header_info[6]);
          int byteoff = Integer.parseInt("" + header_info[3]);

          //System.out.println(Number_of_chrs + "\t" + byteoff);

            NUMBER_NODES = new ArrayList[Number_of_chrs];
            OFFSET_BYTES = new ArrayList[Number_of_chrs];

            try {

            RandomAccessFile raf = new RandomAccessFile(filename, "r");
            raf.seek(byteoff);

            for (int i=0;i<NUMBER_NODES.length;i++) {
                NUMBER_NODES[i] = new ArrayList();
                OFFSET_BYTES[i] = new ArrayList();
            }

            for (int i=0;i<Number_of_chrs;i++) {

                byte[] tmp = new byte[4];
                raf.read(tmp, 0, 4);
                ByteBuffer bb= ByteBuffer.wrap(tmp);
                int chrNum = bb.getInt();
                NUMBER_NODES[i].add(chrNum);

                tmp = new byte[8];
                raf.read(tmp, 0, 8);
                bb= ByteBuffer.wrap(tmp);
                long chrLen = bb.getLong();
                NUMBER_NODES[i].add(chrLen);

                tmp = new byte[4];
                raf.read(tmp, 0, 4);
                bb= ByteBuffer.wrap(tmp);
                int Num_levels = bb.getInt();
                NUMBER_NODES[i].add(Num_levels);

                    for (int k=0;k<Num_levels+1;k++) {

                        tmp = new byte[8];
                        raf.read(tmp, 0, 8);
                        bb= ByteBuffer.wrap(tmp);
                        long off = bb.getLong();
                        NUMBER_NODES[i].add(off);
                    }
            }

            if (Main.DEBUG) {
                long tot = 0;
                for (int i=0;i<NUMBER_NODES.length;i++) {
                    for (int j=3;j<NUMBER_NODES[i].size();j++) {
                        tot+= Long.parseLong("" + NUMBER_NODES[i].get(j));
                    System.out.println(NUMBER_NODES[i].get(j));
                    }
                }               
            }


            raf.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(binAgent.class.getName()).log(Level.SEVERE, null, ex);
            }

      }


	 // Converts number of nodes to byte offsets - updates header_info and offset data
      public static void OffsetConverter(String filename, int reso) throws IOException {

          header_info = ReadHeader(filename);
          long offLen=0;
          long totNodes = 0;
         

          for (int i=0;i<NUMBER_NODES.length;i++) {
              for (int j=3;j<NUMBER_NODES[i].size();j++) {
                        totNodes+= Integer.parseInt("" + NUMBER_NODES[i].get(j));
                        OFFSET_BYTES[i].add(totNodes*reso);
              }
          }


                long loc = Integer.parseInt(header_info[3]);
                int number_chrs = Integer.parseInt(header_info[6]);

                try {

                RandomAccessFile raf = new RandomAccessFile(filename, "rw");

                raf.seek(loc-16);
                raf.writeLong(totNodes);
                raf.seek(loc);
                long loc1 = loc;
                int off = 0;
                long offl = 0;

                for (int i = 0;i<number_chrs;i++) {

                byte[] tmp = new byte[4];
                raf.read(tmp, 0, 4);
                ByteBuffer bb= ByteBuffer.wrap(tmp);
                off = bb.getInt();
                tmp = new byte[8];
                raf.read(tmp, 0, 8);
                bb= ByteBuffer.wrap(tmp);
                offl = bb.getLong();
                tmp = new byte[4];
                raf.read(tmp, 0, 4);
                bb= ByteBuffer.wrap(tmp);
                off = bb.getInt();

                    for (int j=2;j<off+3;j++) {
                    long my = Long.parseLong("" +OFFSET_BYTES[i].get(j-2));
                    raf.writeLong(my);
                    offLen = raf.getFilePointer();

                        if (Main.DEBUG) {
                        long o = raf.getFilePointer();
                        raf.seek(o-8);
                        tmp = new byte[8];
                        raf.read(tmp, 0, 8);
                        bb= ByteBuffer.wrap(tmp);
                        offl = bb.getLong();
                        System.out.println(offl);
                        }
                    }

                }

                // Store length of level offsets in header...
                int HeaderlastPos = Integer.parseInt(header_info[3]);
                raf.seek(HeaderlastPos-8);
                raf.writeLong(offLen);
                raf.close();

                } catch (IOException eu) {
                    System.out.println ("IO exception = " + eu );
                }


      }


}
