/*
 * binSearch - searching methods for binT
 * 
 */

package bint_v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 *
 * @author tf2
 */
//
//  binSearch.java
//
//
//  Created by Tom Fitzgerald on 28/08/2009.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

public class binSearch {

	

       /* SEARCHING FUNCTIONS!! */


      public static void SearchTabFile(String filename, long[] offset) {

            filename.trim();
            RandomAccessFile raf = null;

            try {
            // Create a buffered stream
            raf = new RandomAccessFile(filename, "r");

                for (int i=0;i<offset.length;i++) {
                    raf.seek(offset[i]);
                    System.out.println(raf.readLine());
                }

            raf.close();
            } catch (FileNotFoundException ex) {
                System.out.println("File not found: " + filename);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }finally {

                try {
                    if (raf != null) raf.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }

        }


      public static long[] getNodeBytes(String filename, int reso, int num_child, int chr, int level, int node) throws IOException {



          long h = 0;
          int count =0;
          long nodeoff = 0;
          Object[] Nodedata = null;

          nodeoff = node * reso;

                for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                    for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                        if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                            if (count <= level) {
                            if (count==level) {
                                h = Long.parseLong("" + binAgent.NUMBER_NODES[i].get(j));
                                Nodedata = ReadaFilledNode(filename, h+nodeoff);
                            }
                         count++;
                         }
                        }
                    }
                }



           long[] LongBytes = new long[Integer.parseInt("" +Nodedata[2])];
           for (int i=4;i<Nodedata.length;i++) {
             LongBytes[i-4] = Long.parseLong("" + Nodedata[i]);
           }

          return LongBytes;
      }


      public static long[] SearchPosition(String filename, int reso, int chr, int start, int stop) throws IOException {

          long h = 0;
          long reso1 = reso;
          int numLevels = FindNumberLevels(filename, chr);
          int count =0;
          Object[] Nodedata = null;
          boolean found = false;
          int level = 0;
          int node = 0;
          Vector v = new Vector();

            for (int i=0; ;i++) {

            Nodedata =SeekLevelAndNode(filename, reso, chr, level, node);

            if (level == numLevels) { level--; node = node/2; break; }

            if (start >= Integer.parseInt("" + Nodedata[0]) && stop <= Integer.parseInt("" + Nodedata[1])) {
                level++;
                node = node*2;
            }

            if (start>Integer.parseInt("" + Nodedata[1])) {
                node++;
            }

            if (start<=Integer.parseInt("" + Nodedata[1]) && stop > Integer.parseInt("" + Nodedata[1])) {
                node = node/2; level--;
               // System.out.println("Check2");
                break;
            }

            if (start<Integer.parseInt("" + Nodedata[1]) && stop >= Integer.parseInt("" + Nodedata[1])) {
                node = node/2; level--;
               // System.out.println("Check3");
               // System.out.println(chr + "\t" + start + "\t" + stop + "\t" + Nodedata[0] + "\t" + Nodedata[1] + "\t" + Nodedata[2] + "\t" + Nodedata[3]);
                break;
            }

            if (start==Integer.parseInt("" + Nodedata[1]) && stop == Integer.parseInt("" + Nodedata[1])) {
               // System.out.println("Check4");
                break;
            }

            if (start <= Integer.parseInt("" + Nodedata[0]) && stop >= Integer.parseInt("" + Nodedata[1])) {
               // System.out.println("Check5");
                break;
            }

            }

          while (level < numLevels) {
             
            Object[] nd = SeekLevelAndNodeForWrite(filename, reso, chr,  level, node, 5);
                
            while( Integer.parseInt("" + nd[1]) <= stop) {

                    node++;
                    nd = SeekLevelAndNodeForWrite(filename, reso, chr,  level, node, 5);

                    if (Integer.parseInt("" + nd[0]) >= start && Integer.parseInt("" + nd[1]) <= stop) {
                    
                        for (int i = 4;i<nd.length;i++) {
                            v.add(nd[i]);                         
                        }
                    }
             }

            level++;
            node=node/2;
            
          }

          long[] longB = new long[v.size()];
          for (int i=0;i<v.size();i++) {
             longB[i] = Long.parseLong("" +v.get(i));
          }

          return longB;

      }


      /* SUPPORT FUNCTIONS!! */


      public static Object[] SeekLevelAndNode(String filename, int reso, int chr, int level, int node) throws IOException {


          long h = 0;
          int count =0;
          long nodeoff = 0;
          Object[] Nodedata = null;

          nodeoff = node * reso;

                for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                    for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                        if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                            if (count <= level) {
                            if (count==level) {
                                h = Long.parseLong("" + binAgent.NUMBER_NODES[i].get(j));
                                Nodedata = ReadaNode(filename, h+nodeoff);
                            }
                         count++;
                         }
                        }
                    }
                }

                return Nodedata;

      }

      // Reads a node at a given offset
      public static Object[] ReadaNode(String filename, long offset) {

          Object[] Nodedata = null;

          try {

                RandomAccessFile raf = new RandomAccessFile(filename, "r");


                int headerLen = Integer.parseInt(binAgent.header_info[3]);
                long LevelOffsetLen = Long.parseLong(binAgent.header_info[8]);
                long NodeOffsetLen = Long.parseLong(binAgent.header_info[7]) *8;

                // skip all headers
                raf.seek(offset + headerLen + LevelOffsetLen + NodeOffsetLen);

                        byte[] tmp = new byte[20];
                        raf.read(tmp, 0, 20);
                        Nodedata= binSearch.ExtractNode(tmp);

                 raf.close();

                } catch (IOException eu) {
                    System.out.println ("IO exception = " + eu );
                }
                return Nodedata;
      }

      // Extract node data from byte[]
       public static Object[] ExtractNode(byte[] nodeBytes) {

        Object[] nodeData = new Object[4];
        ByteBuffer bb;

            byte[] startBytes = new byte[4];
            System.arraycopy(nodeBytes,0,startBytes,0,4);
            bb= ByteBuffer.wrap(startBytes);
            nodeData[0] =  bb.getInt();

            byte[] stopBytes = new byte[4];
            System.arraycopy(nodeBytes,4,stopBytes,0,4);
            bb= ByteBuffer.wrap(stopBytes);
            nodeData[1] =  bb.getInt();

            byte[] AgBytes = new byte[4];
            System.arraycopy(nodeBytes,8,AgBytes,0,4);
            bb= ByteBuffer.wrap(AgBytes);
            nodeData[2] =  bb.getInt();

            byte[] DoBytes = new byte[8];
            System.arraycopy(nodeBytes,12,DoBytes,0,8);
            bb= ByteBuffer.wrap(DoBytes);
            nodeData[3] =  bb.getDouble();

            //System.out.println(nodeData[0] + "\t" + nodeData[1] + "\t" + nodeData[2] + "\t" + nodeData[3]);

        return nodeData;

    }


        public static Object[] ReadaFilledNode(String filename, long offset) throws IOException {

          //ReadHeader(filename);
         // ReadOffsets(filename);
          Object[] Nodedata = null;

          try {

                RandomAccessFile raf = new RandomAccessFile(filename, "rw");


                int headerLen = Integer.parseInt(binAgent.header_info[3]);
                long LevelOffsetLen = Long.parseLong(binAgent.header_info[8]);
                long NodeOffsetLen = Long.parseLong(binAgent.header_info[7]) *8;

                long byteoff = offset + headerLen + LevelOffsetLen + NodeOffsetLen;
                // skip all headers
                raf.seek(byteoff+8);

                byte[] tmp = new byte[4];
                raf.read(tmp, 0, 4);
                ByteBuffer bb= ByteBuffer.wrap(tmp);
                int count = bb.getInt();

                raf.seek(byteoff);
                tmp = new byte[20+(count*8)];
                raf.read(tmp, 0, 20+(count*8));
                Nodedata = ExtractFilledNode(tmp);

                if (Main.DEBUG) {
                    for (int i = 0;i<Nodedata.length;i++) {
                    System.out.print(Nodedata[i] + "\t");
                        if (i == Nodedata.length-1) {
                        System.out.print("\n");
                        }
                    }
                }

                raf.close();

          } catch (IOException eu) {
                    System.out.println ("IO exception = " + eu );
          }
          return Nodedata;
      }


       public static Object[] ExtractFilledNode(byte[] nodeBytes) {

        ByteBuffer bb;
        int pin = 20;

            byte[] startBytes = new byte[4];
            System.arraycopy(nodeBytes,0,startBytes,0,4);
            bb= ByteBuffer.wrap(startBytes);
            int start =  bb.getInt();

            byte[] stopBytes = new byte[4];
            System.arraycopy(nodeBytes,4,stopBytes,0,4);
            bb= ByteBuffer.wrap(stopBytes);
            int stop =  bb.getInt();

            byte[] AgBytes = new byte[4];
            System.arraycopy(nodeBytes,8,AgBytes,0,4);
            bb= ByteBuffer.wrap(AgBytes);
            int count =  bb.getInt();

            byte[] DoBytes = new byte[8];
            System.arraycopy(nodeBytes,12,DoBytes,0,8);
            bb= ByteBuffer.wrap(DoBytes);
            double ave =  bb.getDouble();

            Object[] nodeData = new Object[4 + count];
            nodeData[0] = start;
            nodeData[1] = stop;
            nodeData[2] = count;
            nodeData[3] = ave;

            //System.out.println(count);

                for (int i=4;i<count+4;i++) {
                    DoBytes = new byte[8];
                    System.arraycopy(nodeBytes,pin,DoBytes,0,8);
                    bb= ByteBuffer.wrap(DoBytes);
                    nodeData[i] = bb.getLong();
                    pin = pin+8;
                }

        return nodeData;

    }



       // Prints all nodes over all levels for a chr
      public static void SearchStructure(String filename, int reso, int chr) throws IOException {

          //ReadHeader(filename);
          //ReadOffsets(filename);
          long h = 0;
          long reso1 = reso;

                for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                    for (int j=3;j<binAgent.NUMBER_NODES[i].size()-1;j++) {
                        if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                         int numberLevels = Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(2));

                         h= Long.parseLong("" + binAgent.NUMBER_NODES[i].get(j));

                            for (int k=0;k<numberLevels;k++) {
                                long chrLength = Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(1));
                                if (reso1 > chrLength) { break; }
                                chrLength = chrLength / reso1;
                                for (int y=0;y<chrLength;y++) {
                                ReadaNode(filename, h);
                                h += reso;
                                }
                                reso1 = reso1*2;
                            }
                        }
                    }
                }

      }

      // Prints node 1 at level x for a chr
      public static void FindLevel(String filename,  int reso, int chr, int level) throws IOException {

          //ReadHeader(filename);
          //ReadOffsets(filename);
          long h = 0;
          long reso1 = reso;

          int count =0;

                for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                    for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                        if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                            if (count <= level) {
                            if (count==level) {
                                h = Long.parseLong("" + binAgent.NUMBER_NODES[i].get(j));
                                ReadaNode(filename, h);
                            }
                         count++;
                         }
                        }
                    }
                }


      }

      // Returns the number of levels for a chr
      public static int FindNumberLevels(String filename, int chr) throws IOException {
         // ReadHeader(filename);
         // ReadOffsets(filename);

          int levels = 0;

          for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                    for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                        if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                        levels = Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(2));
                        }
                    }
                }
            return levels;

      }


      public static Object[] SeekLevelAndNodeForWrite(String filename, int reso, int chr, int level, int node, long feature) throws IOException {

          long h = 0;
          long reso1 = reso;
          int count =0;
          long nodeoff = 0;
          Object[] Nodedata = null;

          nodeoff = node * reso;

                for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                    for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                        if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                            if (count <= level) {
                            if (count==level) {
                                h = Long.parseLong("" + binAgent.NUMBER_NODES[i].get(j));
                                Nodedata = WriteaNode(filename, h+nodeoff, feature);
                            }
                         count++;
                         }
                        }
                    }
                }

                return Nodedata;

      }


       // Write a node at a given offset
      public static Object[] WriteaNode(String filename, long offset, long feature) throws IOException {

          //ReadHeader(filename);
         // ReadOffsets(filename);
          Object[] Nodedata = null;

          try {

                RandomAccessFile raf = new RandomAccessFile(filename, "rw");


                int headerLen = Integer.parseInt(binAgent.header_info[3]);
                long LevelOffsetLen = Long.parseLong(binAgent.header_info[8]);
                long NodeOffsetLen = Long.parseLong(binAgent.header_info[7]) *8;

                long byteoff = offset + headerLen + LevelOffsetLen + NodeOffsetLen;
                // skip all headers
                raf.seek(byteoff+8);

                byte[] tmp = new byte[4];
                raf.read(tmp, 0, 4);
                ByteBuffer bb= ByteBuffer.wrap(tmp);
                int count = bb.getInt();

                raf.seek(byteoff);
                tmp = new byte[20+(count*8)];
                raf.read(tmp, 0, 20+(count*8));
                Nodedata = ExtractFilledNode(tmp);

                if (Main.DEBUG) {
                    for (int i = 0;i<Nodedata.length;i++) {
                    System.out.print(Nodedata[i] + "\t");
                        if (i == Nodedata.length-1) {
                        System.out.print("\n");
                        }
                    }
                }

                raf.close();

          } catch (IOException eu) {
                    System.out.println ("IO exception = " + eu );
          }
          return Nodedata;
      }

     
}
