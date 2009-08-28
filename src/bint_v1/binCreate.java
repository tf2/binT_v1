/*
 * binCreate - Structure creation for binT
 * NB. Filling of structure is ineffiecnt - will be changed soon.... 
 * 
 */

package bint_v1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author tf2
 */
//
//  binCreate.java
//
//
//  Created by Tom Fitzgerald on 28/08/2009.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

public class binCreate {

    public static int magic_length = 0;
    public static Map CHR_LENGTHS = new HashMap<String, Integer>();

    // Put all chromsome lengths in a hash - currently just human chromosomes...
     public static void LoadCHRS() {

       CHR_LENGTHS.put("1", 247249719);
        CHR_LENGTHS.put("2", 242951149);
        CHR_LENGTHS.put("3", 199501827);
        CHR_LENGTHS.put("4", 191273063);
        CHR_LENGTHS.put("5", 180857866);
        CHR_LENGTHS.put("6", 170899992);
        CHR_LENGTHS.put("7", 158821424);
        CHR_LENGTHS.put("8", 146274826);
        CHR_LENGTHS.put("9", 140273252);
        CHR_LENGTHS.put("10", 135374737);
        CHR_LENGTHS.put("11", 134452384);
        CHR_LENGTHS.put("12", 132349534);
        CHR_LENGTHS.put("13", 114142980);
        CHR_LENGTHS.put("14", 106368585);
        CHR_LENGTHS.put("15", 100338915);
        CHR_LENGTHS.put("16", 88827254);
        CHR_LENGTHS.put("17", 78774742);
        CHR_LENGTHS.put("18", 76117153);
        CHR_LENGTHS.put("19", 63811651);
        CHR_LENGTHS.put("20", 62435964);
        CHR_LENGTHS.put("21", 46944323);
        CHR_LENGTHS.put("22", 49691432);
        CHR_LENGTHS.put("23", 154913754);
        CHR_LENGTHS.put("24", 57772954);
     }


      public static void LoadCHRSFile(String filename) {

      BufferedReader infile = null;
      int len=0;
      String inLine;

        try {
            infile = new BufferedReader(new FileReader (filename));

            while ((inLine=infile.readLine()) != null) {

                String seq[] = inLine.split("\t");
                CHR_LENGTHS.put(seq[0], seq[1]);
            }

        } catch (FileNotFoundException ex) {
            System.out.println("File not found: " + filename);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());

        } finally {

            try {
                if (infile != null) infile.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

      }


     // Create binT header - static header
      public static void CreatebinFileHeader(String filename, String bedName, int base_size, int nr_children, int number_chrs) {

        String magic = "LocusTree_v1";
        magic_length = magic.length()*2;
        int file_name_length = bedName.length()*2;
        long total_number_nodes = 1000000000;
        int header_length = magic_length + file_name_length + (4*5) + 8 + 8;

        File file = null;
        file = new File (filename);

        try {
            FileOutputStream file_output = new FileOutputStream (file);
            DataOutputStream data_out = new DataOutputStream (file_output);
            data_out.writeChars(magic);
            data_out.writeInt(file_name_length);
            data_out.writeChars(bedName);
            data_out.writeInt(header_length);
            data_out.writeInt(base_size);
            data_out.writeInt(nr_children);
            data_out.writeInt(number_chrs);
            data_out.writeLong(total_number_nodes);
            data_out.writeLong(0);

        file_output.close ();
        }
        catch (IOException e) {
            System.out.println ("IO exception = " + e );
        }

    }

      // Puts the node offsets directly after level offsets - all equal zero at this time
      public static void PutNodeOffset(String filename) throws IOException {

          binAgent.ReadHeader(filename);
          long header_length = Long.parseLong(binAgent.header_info[3]);
          long tot_number_nodes = Long.parseLong(binAgent.header_info[7]);
          long offsetHeaderLength = Long.parseLong(binAgent.header_info[8]);
          if (offsetHeaderLength == 0) { System.out.println("Problem in OffsetConverter!!!"); }

          long sekStart = header_length + offsetHeaderLength;
         // System.out.println(tot_number_nodes);

          try {
            RandomAccessFile raf = new RandomAccessFile(filename, "rw");

            raf.seek(sekStart);

            for (int i=0;i<tot_number_nodes;i++) {
                raf.writeLong(0);
            }
            System.out.println(raf.getFilePointer());
            raf.close();

          }catch (IOException eu) {
                    System.out.println ("IO exception = " + eu );
          }

      }


      // The structure creation function - used to create large structure and put offset byte positions
      public static void CreateStructure(String filename, int stop, int reso, int chr, boolean makeStructure) {

       try {

       FileOutputStream file_output = new FileOutputStream (filename, true);
       DataOutputStream data_out = new DataOutputStream (file_output);

        int count=0;
        // This was a hard bug to fix...!!
        int fix_res = reso;
        Vector offsets = new Vector();
        offsets.add(0);


        for (int i=0; ;i++) {

        Vector st_start = new Vector();
        Vector st_stop = new Vector();

        int s= 1;
        int e = 0;
        int ee=0;
        int eee=0;
        int test = 0;

        while(e<stop) {
            test++;
            e= e+reso;
            ee = e;

            eee = e+reso;
            if (eee > stop) { ee = stop; }

            st_start.add(s);
            st_stop.add(ee);

             if (ee==stop) { break;}
            //System.out.println(s + "\t" + ee + "\t" + eee);    // reactivate to check structures
            if (s==1 && ee==stop) { break; }        // found root level 1
            s =s+reso;

        }


         if (makeStructure) {

           File file = null;
           file = new File (filename);
           int remain = fix_res -20;        // This is the size of each node (needs to be solved - cull the final structure and reduce node size...)
           final byte[] buf = new byte[remain];

                for (int j=0; j < st_start.size(); j++) {
                    data_out.writeInt(Integer.parseInt("" + st_start.get(j)));
                    data_out.writeInt(Integer.parseInt("" + st_stop.get(j)));
                    data_out.writeInt(0);     // default aggergate number...
                    data_out.writeDouble(0);  // default double number...
                    data_out.write(buf, 0, remain);
                 }
        }


        count++;
        offsets.add(test);

        if (s==1 && ee==stop) { break; }    // found root level 1

        if (e>stop) { e = stop-reso; }
        reso = reso*2;
        }

               /// Write out offsets
               if (!makeStructure) {

                   File file = null;
                   file = new File (filename);
                   long offset = 0;

                        data_out.writeInt(chr);
                        data_out.writeLong(stop);
                        data_out.writeInt(count);
                        Object[] m = offsets.toArray();

                        for (int i=0;i<m.length;i++) {
                        offset = Integer.parseInt("" + m[i]);
                        data_out.writeLong(offset);
                        }

                }

         file_output.close ();

       } catch (IOException eu) {
            System.out.println ("IO exception = " + eu );
       }
    }

      public static void FillStructure(String filename, String inFilename, int reso, int child_num) throws IOException {

            System.out.println("Filling structure - please be pateient (this only needs to be done once)...");
            binAgent.ReadHeader(filename);
            binAgent.ReadOffsets(filename);
            int headerLen = Integer.parseInt(binAgent.header_info[3]);
            long LevelOffsetLen = Long.parseLong(binAgent.header_info[8]);
            long NodeOffsetLen = Long.parseLong(binAgent.header_info[7]) *8;
            long headoff = headerLen + LevelOffsetLen + NodeOffsetLen;

            filename.trim();
            inFilename.trim();
            String inLine;
            int len=0;
            RandomAccessFile raf = null;
            RandomAccessFile rout = null;

            try {
            raf = new RandomAccessFile(inFilename, "r");
            rout = new RandomAccessFile(filename, "rw");
            long feature = raf.getFilePointer();

                while ((inLine=raf.readLine()) != null) {

                    long h = 0;
                    long nodeoff = 0;
                    int level = 0;
                    int node = 0;
                    int count =0;
                    Object[] Nodedata = null;

                    String[] seq = inLine.split("\t");
                    int chr = Integer.parseInt(seq[0]);
                    int start = Integer.parseInt(seq[1]);
                    int stop = Integer.parseInt(seq[2]);


                    int numLevels = 0;

                    for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                        for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                            if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                                numLevels = Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(2));
                            }
                        }
                    }

                    for (int i=0; ;i++) {

                    Nodedata =binSearch.SeekLevelAndNode(filename, reso, chr, level, node);

                    if (level == numLevels) { level--; node = node/2;  break; }

                    if (start >= Integer.parseInt("" + Nodedata[0]) && stop <= Integer.parseInt("" + Nodedata[1])) {
                        level++;
                        node = node*2;
                    }

                    if (start>Integer.parseInt("" + Nodedata[1])) {
                        node++;
                    }

                    if (start<=Integer.parseInt("" + Nodedata[1]) && stop > Integer.parseInt("" + Nodedata[1])) { node = node/2; level--; break; }

                    if (start<Integer.parseInt("" + Nodedata[1]) && stop >= Integer.parseInt("" + Nodedata[1])) { node = node/2; level--; break; }

                    if (start==Integer.parseInt("" + Nodedata[1]) && stop == Integer.parseInt("" + Nodedata[1])) { break; }

                    if (start <= Integer.parseInt("" + Nodedata[0]) && stop >= Integer.parseInt("" + Nodedata[1])) { break; }

                    }

                    h = 0;
                    nodeoff = 0;
                    int  count3 =0;

                    for (int i=0;i<node;i++) {
                        nodeoff+=reso;
                    }

                    for (int i=0;i<binAgent.NUMBER_NODES.length;i++) {
                        for (int j=binAgent.NUMBER_NODES[i].size()-2;j>2;j--) {
                            if (Integer.parseInt("" + binAgent.NUMBER_NODES[i].get(0)) == chr) {
                                if (count3 <= level) {
                                    if (count3==level) {
                                    h = Long.parseLong("" + binAgent.NUMBER_NODES[i].get(j));
                                    long offset =h+nodeoff;
                                    long byteoff = offset + headoff;
                                    rout.seek(byteoff+8);

                                    byte[] tmp = new byte[4];
                                    rout.read(tmp, 0, 4);
                                    ByteBuffer bb= ByteBuffer.wrap(tmp);
                                    int count2 = bb.getInt();
                                    int amot = count2 *8;
                                    rout.seek(byteoff+20 + amot);
                                    rout.writeLong(feature);

                                    count2++;
                                    rout.seek(byteoff+8);
                                    rout.writeInt(count2);

                                    /// Dont really need this....
                                    rout.seek(byteoff);
                                    tmp = new byte[20+(count2*8)];
                                    rout.read(tmp, 0, 20+(count2*8));
                                    Object[] nodeData = binSearch.ExtractFilledNode(tmp);

                                        if (Main.DEBUG) {
                                            for (int k = 0;k<nodeData.length;k++) {
                                                System.out.print(nodeData[k] + "\t");
                                                if (k == nodeData.length-1) {
                                                    System.out.print("\n");
                                                }
                                            }
                                        }
                                    }
                            count3++;
                            }
                        }
                    }
                }
                    //System.out.println(feature);
            feature = raf.getFilePointer();
            }

            raf.close();
            rout.close();

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

        System.out.println("Structure filled!!!");

      }

      // Make the overall structure and put offsets and header info
      public static void MakeStructure(String filename, String BedName, int reso, int numChild) throws IOException {

        //LoadCHRS();
        Object[] keys = CHR_LENGTHS.keySet().toArray();

        CreatebinFileHeader(filename, BedName, reso, numChild, keys.length);
        binAgent.ReadHeader(filename);

        // Put offsets
        for (int i=0;i<keys.length;i++) {
        CreateStructure(filename, Integer.parseInt("" + CHR_LENGTHS.get(keys[i])), reso,  Integer.parseInt("" + keys[i]), false);
        }
        System.out.println("Header and offsets created!!");

        // Read offsets and convert to byte (they are number of nodes at this point)
        binAgent.ReadOffsets(filename);
        binAgent.OffsetConverter(filename, reso);

        PutNodeOffset(filename);

        System.out.println("Offsets converted to byte positions!!");
        binAgent.ReadOffsets(filename);

        System.out.println("Creating empty struture...");
        // Create big structure
        for (int i=0;i<keys.length;i++) {
        CreateStructure(filename, Integer.parseInt("" + CHR_LENGTHS.get(keys[i])), reso,  Integer.parseInt("" + CHR_LENGTHS.get(keys[i])), true);
        }
        System.out.println("Structure created!!");


      }

}
