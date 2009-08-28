/*
 * Java implementation for LocusTree_v1 
 * binT - binary Tree 
 * Main class - deals with all command line args
 * Structure creation filling and searching (non-culled structre)
 */

package bint_v1;

import java.io.IOException;

/**
 *
 * Author: Tomas William Fitzgerald
 * Date: 28/08/2009
 *	
 */
 
public class Main {


    public static boolean DEBUG = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        /* MAIN!! */
        int reso = 1000;
        int numChild = 2;
        int makeStructure = 0;
        int SearchStructure = 0;
		int chr = 0, start =0, stop=0, level = 0, node = 0;

        String chr_file = "";
        String filename = "";
        String BedName = "";

        for (int u=0;u<args.length; u++) {

            int pin = u+1;
            String s = args[u];

            if (s.equals("-f")) {
                BedName = args[pin];
            } if (s.equals("-o")) {
                filename = args[pin];
            } if (s.equals("-c")) {
                chr_file = args[pin];
            } if (s.equals("-r")) {
				reso = Integer.parseInt(args[pin]);
            } if (s.equals("-n")) {
                numChild = Integer.parseInt(args[pin]);
            } if (s.equals("-m")) {
				makeStructure = Integer.parseInt(args[pin]);
            } if (s.equals("-s")) {
				SearchStructure = Integer.parseInt(args[pin]);
            }

            if (makeStructure == 0) {

                if (SearchStructure == 0) {

                    if (s.equals("-chr")) {
                        chr = Integer.parseInt(args[pin]);
                    } if (s.equals("-level")) {
                        level = Integer.parseInt(args[pin]);
                    } if (s.equals("-node")) {
                        node = Integer.parseInt(args[pin]);
                    }

                } else {
                    
                    if (s.equals("-chr")) {
                        chr = Integer.parseInt(args[pin]);
                    } if (s.equals("-sta")) {
                        start = Integer.parseInt(args[pin]);
                    } if (s.equals("-sto")) {
                        stop = Integer.parseInt(args[pin]);
                    }
                }
            }
	}

        if (makeStructure == 1) {
        binCreate.LoadCHRSFile(chr_file);
        binCreate.MakeStructure(filename, BedName, reso,  numChild);
        binCreate.FillStructure(filename, BedName, reso,  numChild);
        }

        else if (makeStructure == 0) {
			
			if (SearchStructure == 0) {
				binAgent.ReadHeader(filename);
				binAgent.ReadOffsets(filename);
				long[] my =  binSearch.getNodeBytes(filename, reso, numChild, chr, level, node);
				binSearch.SearchTabFile(BedName, my);
            }
			
			if (SearchStructure == 1) {
				binAgent.ReadHeader(filename);
				binAgent.ReadOffsets(filename);
				long[] my = binSearch.SearchPosition(filename, reso, chr, start, stop);
				binSearch.SearchTabFile(BedName, my);
			}

		}
	}

}
