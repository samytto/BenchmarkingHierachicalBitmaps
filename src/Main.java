/*
 * (c) Samy Chambi and Daniel Lemire
 */

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Locale;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.ArrayContainer;

import net.sourceforge.sizeof.SizeOf;
import it.uniroma3.mat.extendedset.intset.ConciseSet;

/**
 * 
 * This a reproduction of the benchmark used by Colantonio and Di Pietro,
 * Concise: Compressed 'n' Composable Integer Set
 * 
 * While they report "Compression" as the ratio between the number of 32-bit
 * words required to represent the compressed bitmap and the cardinality of the
 * integer set, we report the number of bits per integer.
 * 
 * Like them, we use "Density" mean the ratio between the cardinality of the set
 * and the number range.
 * 
 * Like them, we "Max/Cardinality" to mean the ratio between the maximal value
 * (i.e., the number range) and the cardinality of the set that is, the inverse
 * of the density.
 * 
 * 
 * Time measurement are expressed in nanoseconds. Each experiment is performed
 * 100 times, and the average reported.
 * 
 * @author Daniel Lemire
 * 
 */
public class Main {

	 	private static int bogus = 0;
	
        /**
         * @param a
         *                an array of integers
         * @return a bitset representing the provided integers
         */
        public static BitSet toBitSet(final int[] a) {
                BitSet bs = new BitSet();
                for (int x : a)
                        bs.set(x);
                return bs;
        }

        /**
         * @param a
         *                an array of integers
         * @return a ConciseSet representing the provided integers
         */
        public static ConciseSet toConciseSet(final int[] a) {
                ConciseSet cs = new ConciseSet();
                for (int x : a)
                        cs.add(x);
                return cs;
        }

        /**
         * @param a
         *                an array of integers
         * @return a RoaringBitmap representing the provided integers
         */
        public static RoaringBitmap toRoaringBitmap(int[] a) {
                RoaringBitmap rr = new RoaringBitmap();
                for (int x : a)
                        rr.add(x);
                return rr;
        }

        /**
         * @param a
         *                an array of integers
         * @return a ConciseSet (in WAH mode) representing the provided integers
         */
        public static ConciseSet toWAHConciseSet(int[] a) {
                ConciseSet cs = new ConciseSet(true);
                for (int x : a)
                        cs.add(x);
                return cs;
        }

        /**
         * @param args
         *                command line arguments
         */
        public static void main(final String[] args) {
                Locale.setDefault(Locale.US);
                System.out
                        .println("# This benchmark emulates what Colantonio and Di Pietro,");
                System.out
                        .println("#  did in Concise: Compressed 'n' Composable Integer Set");
                System.out.println("########");
                System.out.println("# " + System.getProperty("java.vendor")
                        + " " + System.getProperty("java.version") + " "
                        + System.getProperty("java.vm.name"));
                System.out.println("# " + System.getProperty("os.name") + " "
                        + System.getProperty("os.arch") + " "
                        + System.getProperty("os.version"));
                System.out.println("# processors: "
                        + Runtime.getRuntime().availableProcessors());
                System.out.println("# max mem.: "
                        + Runtime.getRuntime().maxMemory());
                System.out.println("########");
                int N = 100000;
                boolean sizeof = true;
                try {
                        SizeOf.setMinSizeToLog(0);
                        SizeOf.skipStaticField(true);
                        // SizeOf.skipFinalField(true);
                        SizeOf.deepSizeOf(args);
                } catch (IllegalStateException e) {
                        sizeof = false;
                        System.out
                                .println("# disabling sizeOf, run  -javaagent:lib/SizeOf.jar or equiv. to enable");
                }
                DataGenerator gen = new DataGenerator(N);
                int TIMES = 100;
                gen.setUniform();
                test(gen, false, TIMES, sizeof);
                System.out.println("# start counting benchmark performances");
                test(gen, true, TIMES, sizeof);
                System.out.println();
                gen.setZipfian();
                test(gen, false, TIMES, sizeof);
                test(gen, true, TIMES, sizeof);
                System.out.println();
        }

        /**
         * @param gen
         *                data generator
         * @param verbose
         *                whether to print out the result
         * @param TIMES
         *                how many times should we run each test
         * @param sizeof
         *                whether to use the sizeOf library.
         */
        public static void test(final DataGenerator gen, final boolean verbose,
                final int TIMES, boolean sizeof) {
                if (!verbose)
                        System.out
                                .println("# running a dry run (can take a long time)");
                
                if(!verbose) {
                	int[] seuils = {1};
                	launchBench(seuils, TIMES, gen, sizeof, verbose);
                }
                else {
                	int[] seuils = {1024, 4096, 8192, 16384};
                	launchBench(seuils, TIMES, gen, sizeof, verbose);
                }                
                System.out.println("#ignore = " + bogus);
        }
        
        public static void launchBench(int[] seuils, int TIMES, final DataGenerator gen, boolean sizeof, boolean verbose){
         long bef, aft;
         DecimalFormat df = new DecimalFormat("0.000E0");
         DecimalFormat dfb = new DecimalFormat("000.0");
        for(int seuil=0; seuil<seuils.length; seuil++) {
        	
        	if(verbose) System.out.println("\nRoaring bitmap/array conversion threshold = "+seuils[seuil]+"\n");
        	
        	if (verbose)
                if (gen.is_zipfian())
                        System.out.println("### zipfian test");
                else
                        System.out.println("### uniform test");
        if (verbose)
                System.out
                        .println("# first columns are timings [intersection times in ns], then append times in ns, "
                                + "then removes times in ns, then bits/int, then union times");
        if (verbose && sizeof)
                System.out
                        .println("# For size (last columns), first column is estimated, second is sizeof");
        if (verbose)
                System.out
                        .print("# density\tbitset\t\tconcise\t\twah\t\tHirB\t\tLazyHB\t\tLazyVB\t\troar"
                                +   "\t\t\tbitset\t\tconcise\t\twah\t\tHirB\t\tLazyHB\t\tLazyVB\t\troar"
                                +   "\t\t\tbitset\t\tconcise\t\twah\t\tHirB\t\tLazyHB\t\tLazyVB\t\troar");
        if (verbose)
                if (sizeof)
                        System.out
                                .println("\t\tbitset\tbitset\tconcise\tconcise\twah\twah\tHirB\tHirB\tLazyHB\tLazyHB\tLazyVB\tLazyVB\troar\troar");
                else
                        System.out
                                .println("\t\tbitset\t\tconcise\t\twah\t\tHirB\t\tLazyHB\t\tLazyVB\t\troar");
        	
        	for (double d = 0.001/*0.00001*/; d <= 1.000; d*= 10) {
        		int nbSchemes = 7;
                double[] timings = new double[nbSchemes];
                double[] unions = new double[nbSchemes];
                double[] storageinbits = new double[nbSchemes];
                double[] truestorageinbits = new double[nbSchemes];
                double[] appendTimes = new double[nbSchemes];
                double[] removeTimes = new double[nbSchemes];

                for (int times = 0; times < TIMES; ++times) {
                        int[] v1 = gen.getRandomArray(d);
                        int[] v2 = gen.getRandomArray(d);
//######################// BitSet
                        // Append times
                        bef = System.nanoTime();
                        BitSet borig1 = toBitSet(v1); // we will clone
                                                      // it
                        aft = System.nanoTime();
                        bogus += borig1.length();
                        appendTimes[0] += aft - bef;
                        BitSet b2 = toBitSet(v2);
                        // Storage
                        storageinbits[0] += borig1.size() + b2.size();
                        if (sizeof)
                                truestorageinbits[0] += SizeOf
                                        .deepSizeOf(borig1)
                                        * 8
                                        + SizeOf.deepSizeOf(b2) * 2;
                        // And times.
                        bef = System.nanoTime();
                        BitSet b1 = (BitSet) borig1.clone(); // for fair
                                                             // comparison
                                                             // (not
                                                             // inplace)
                        b1.and(b2);
                        aft = System.nanoTime();
                        timings[0] += aft - bef;
                        bogus += b1.length();
                        // OR times.
                        bef = System.nanoTime();
                        BitSet b1u = (BitSet) borig1.clone(); // for
                                                              // fair
                                                              // comparison
                                                              // (not
                                                              // inplace)
                        b1u.or(b2);
                        aft = System.nanoTime();
                        unions[0] += aft - bef;
                        bogus += b1u.length();
                        // Remove times
                        int toRemove = v1[gen.rand.nextInt(gen.N)];
                        bef = System.nanoTime();
                        b2.clear(toRemove);
                        aft = System.nanoTime();
                        removeTimes[0] += aft - bef;
                        bogus += borig1.size();
                        int[] b2withremoval = verbose? null : toArray(b2);
                        borig1 = null;
                        b2 = null;
                        int[] trueintersection = verbose? null : toArray(b1);
                        int[] trueunion = verbose? null : toArray(b1u);
                        b1u = null;
                        b1 = null;
//######################// Concise
                        // Append times
                        bef = System.nanoTime();
                        ConciseSet cs1 = toConciseSet(v1);
                        aft = System.nanoTime();
                        bogus += cs1.size();
                        appendTimes[1] += aft - bef;
                        ConciseSet cs2 = toConciseSet(v2);
                        storageinbits[1] += cs1.size()
                                * cs1.collectionCompressionRatio() * 4
                                * 8;
                        storageinbits[1] += cs2.size()
                                * cs2.collectionCompressionRatio() * 4
                                * 8;
                        if (sizeof)
                                truestorageinbits[1] += SizeOf
                                        .deepSizeOf(cs1)
                                        * 8
                                        + SizeOf.deepSizeOf(cs2) * 2;
                        bef = System.nanoTime();
                        ConciseSet cs1i = cs1.intersection(cs2);
                        aft = System.nanoTime();
                        // we verify the answer
                        if(!verbose) if (!Arrays.equals(cs1i.toArray(),
                                trueintersection))
                                throw new RuntimeException("bug");
                        bogus += cs1i.size();
                        timings[1] += aft - bef;
                        bef = System.nanoTime();
                        ConciseSet cs1u = cs1.union(cs2);
                        aft = System.nanoTime();
                        // we verify the answer
                        if(!verbose)
                        if (!Arrays.equals(cs1u.toArray(), trueunion))
                               throw new RuntimeException("bug");
                        bogus += cs1u.size();
                        unions[1] += aft - bef;
                        // Removal times
                        bef = System.nanoTime();
                        cs2.remove(toRemove);
                        aft = System.nanoTime();
                        if(!verbose) if (!Arrays
                                .equals(cs2.toArray(), b2withremoval))
                                throw new RuntimeException("bug");
                        removeTimes[1] += aft - bef;
                        bogus += cs1.size();
                        cs1 = null;
                        cs2 = null;
                        cs1i = null;
                        cs1u = null;
//######################// WAHConcise
                        // Append times
                        bef = System.nanoTime();
                        ConciseSet wah1 = toWAHConciseSet(v1);
                        aft = System.nanoTime();
                        bogus += wah1.size();
                        appendTimes[2] += aft - bef;
                        ConciseSet wah2 = toWAHConciseSet(v2);
                        // Storage
                        storageinbits[2] += wah1.size()
                                * wah1.collectionCompressionRatio() * 4
                                * 8;
                        storageinbits[2] += wah2.size()
                                * wah2.collectionCompressionRatio() * 4
                                * 8;
                        if (sizeof)
                                truestorageinbits[2] += SizeOf
                                        .deepSizeOf(wah1)
                                        * 8
                                        + SizeOf.deepSizeOf(wah2) * 2;
                        // Intersect times
                        bef = System.nanoTime();
                        ConciseSet wah1i = wah1.intersection(wah2);
                        aft = System.nanoTime();
                        // we verify the answer
                        if(!verbose) if (!Arrays.equals(wah1i.toArray(),
                                trueintersection))
                                throw new RuntimeException("bug");
                        bogus += wah1i.size();
                        timings[2] += aft - bef;
                        // Union times
                        bef = System.nanoTime();
                        ConciseSet wah1u = wah1.union(wah2);
                        aft = System.nanoTime();
                        // we verify the answer
                        if(!verbose) if (!Arrays.equals(wah1u.toArray(), trueunion))
                                throw new RuntimeException("bug");
                        bogus += wah1u.size();
                        unions[2] += aft - bef;
                        // Removing times
                        bef = System.nanoTime();
                        wah2.remove(toRemove);
                        aft = System.nanoTime();
                        if(!verbose) if (!Arrays.equals(wah2.toArray(),
                                b2withremoval))
                                throw new RuntimeException("bug");
                        removeTimes[2] += aft - bef;
                        bogus += wah1.size();
                        wah1 = null;
                        wah2 = null;
                        wah1i = null;
                        wah1u = null;
//######################// HierarchicalBitmap
                        HierarchiqueBitmap hb1 = new HierarchiqueBitmap();
                        HierarchiqueBitmap hb2 = new HierarchiqueBitmap();
                        hb2.addAll(v2);
                        bef = System.nanoTime();
                        hb1.addAll(v1);
                        aft = System.nanoTime();
                        bogus += hb1.length();
                        appendTimes[3] += aft - bef;                        
                        // Storage
                        storageinbits[3] += hb1.length()*8*5;
                        storageinbits[3] += hb2.length()*8*5;
                        if (sizeof)
                                truestorageinbits[3] += SizeOf
                                        .deepSizeOf(hb1)
                                        * 8
                                        + SizeOf.deepSizeOf(hb2) * 2;
                        // Intersect times
                        bef = System.nanoTime();
                        HierarchiqueBitmap hb1i = HierarchiqueBitmap.AND(hb1, hb2);
                        aft = System.nanoTime();
                      bogus += hb1i.length();
                        // we verify the answer
                        //if(!verbose) if (!Arrays.equals(hb1i.toArray(),
                          //      trueintersection))
                            //    throw new RuntimeException("bug");
                        timings[3] += aft - bef;
                        // Union times
                        bef = System.nanoTime();
                        HierarchiqueBitmap hb1u = HierarchiqueBitmap.OR(hb1, hb2);
                        aft = System.nanoTime();
                        bogus += hb1u.length();
                        // we verify the answer
                        //if(!verbose) if (!Arrays.equals(rb1u.toArray(), trueunion))
                          //      throw new RuntimeException("bug");
                        unions[3] += aft - bef;
                        // Remove times
                        bef = System.nanoTime();
                        //rb2.remove(toRemove);
                        aft = System.nanoTime();
                        //if(!verbose) if (!Arrays
                          //      .equals(rb2.toArray(), b2withremoval))
                            //    throw new RuntimeException("bug");
                        removeTimes[3] += 0.0;//aft - bef;
                        //bogus += hb1.getCardinality();
//######################//LazyHierarchicalBitmap
                        LazyHierarchiqueBitmap lhb1 = new LazyHierarchiqueBitmap();
                        LazyHierarchiqueBitmap lhb2 = new LazyHierarchiqueBitmap();
                        //Add times
                        bef = System.nanoTime();
                        lhb1.addAll(v1);
                        aft = System.nanoTime();
                        bogus += lhb1.length();
                        appendTimes[4] += aft - bef;
                        lhb2.addAll(v2);
                        // Storage
                        storageinbits[4] += lhb1.length()*8*5;
                        storageinbits[4] += lhb2.length()*8*5;
                        if (sizeof)
                                truestorageinbits[4] += SizeOf
                                        .deepSizeOf(lhb1)
                                        * 8
                                        + SizeOf.deepSizeOf(lhb2) * 2;
                        // Intersect times
                        bef = System.nanoTime();
                        LazyHierarchiqueBitmap lhb1i = LazyHierarchiqueBitmap.AND(lhb1, lhb2);
                        aft = System.nanoTime();
                        // we verify the answer
                        //if(!verbose) if (!Arrays.equals(hb1i.toArray(),
                           //trueintersection)) throw new RuntimeException("bug");
                        bogus += lhb1i.length();
                        timings[4] += aft - bef;
                        // Union times
                        bef = System.nanoTime();
                        LazyHierarchiqueBitmap lhb1u = LazyHierarchiqueBitmap.OR(lhb1, lhb2);
                        aft = System.nanoTime();
                        bogus += lhb1u.length();
                        // we verify the answer
                        //if(!verbose) if (!Arrays.equals(rb1u.toArray(), trueunion))
                          //      throw new RuntimeException("bug");
                        unions[4] += aft - bef;
                        // Remove times
                        /*bef = System.nanoTime();
                        rb2.remove(toRemove);
                        aft = System.nanoTime();
                        if(!verbose) if (!Arrays.equals(rb2.toArray(), b2withremoval))
                                throw new RuntimeException("bug");*/
                        removeTimes[4] += 0.0;//aft - bef;
                        //bogus += hb1.getCardinality();*/
//######################//LazyHierarchicalBitmap+VarByte
                        LazyVarByteHierarchBmp lzVrhb1 = new LazyVarByteHierarchBmp();
                        LazyVarByteHierarchBmp lzVrhb2 = new LazyVarByteHierarchBmp();
                        //Add times
                        bef = System.nanoTime();
                        lzVrhb1.addAll(v1);
                        aft = System.nanoTime();
                        bogus += lzVrhb1.length();
                        appendTimes[5] += aft - bef;
                        lhb2.addAll(v2);
                        // Storage
                        storageinbits[5] += lzVrhb1.length()*8*5;
                        storageinbits[5] += lzVrhb2.length()*8*5;
                        if (sizeof)
                                truestorageinbits[5] += SizeOf
                                        .deepSizeOf(lhb1)
                                        * 8
                                        + SizeOf.deepSizeOf(lhb2) * 2;
                        // Intersect times
                        bef = System.nanoTime();
                        LazyVarByteHierarchBmp lzVrhb1i=null;
						try {
							lzVrhb1i = LazyVarByteHierarchBmp.AND(lzVrhb1, lzVrhb2);
						} catch (Exception e) {e.printStackTrace();}
                        aft = System.nanoTime();
                        // we verify the answer
                        //if(!verbose) if (!Arrays.equals(hb1i.toArray(),
                           //trueintersection)) throw new RuntimeException("bug");
                        bogus += lzVrhb1i.length();
                        timings[5] += aft - bef;
                        // Union times
                        LazyVarByteHierarchBmp lzVrhb1u = null;
                        bef = System.nanoTime();
                        try {
                        	lzVrhb1u = LazyVarByteHierarchBmp.OR(lzVrhb1, lzVrhb2);
						} catch (Exception e) {e.printStackTrace();}
                        aft = System.nanoTime();
                        bogus += lzVrhb1u.length();
                        // we verify the answer
                        //if(!verbose) if (!Arrays.equals(rb1u.toArray(), trueunion))
                          //      throw new RuntimeException("bug");
                        unions[5] += aft - bef;
                        // Remove times
                        /*bef = System.nanoTime();
                        rb2.remove(toRemove);
                        aft = System.nanoTime();
                        if(!verbose) if (!Arrays.equals(rb2.toArray(), b2withremoval))
                                throw new RuntimeException("bug");*/
                        removeTimes[5] += 0.0;//aft - bef;
                        //bogus += hb1.getCardinality();*/                        
//######################// RoaringBitmap
                        // Append times
                        ArrayContainer.DEFAULT_MAX_SIZE=seuils[seuil];
                        bef = System.nanoTime();
                        RoaringBitmap rb1 = toRoaringBitmap(v1);
                        aft = System.nanoTime();
                        bogus += rb1.getCardinality();
                        appendTimes[6]+= aft - bef;
                        RoaringBitmap rb2 = toRoaringBitmap(v2);
                        // Storage
                        storageinbits[6] += rb1.getSizeInBytes() * 8;
                        storageinbits[6] += rb2.getSizeInBytes() * 8;
                        if (sizeof)
                                truestorageinbits[6] += SizeOf
                                        .deepSizeOf(rb1)
                                        * 8
                                        + SizeOf.deepSizeOf(rb2) * 2;
                        // Intersect times
                        bef = System.nanoTime();
                        RoaringBitmap rb1i = RoaringBitmap
                                .and(rb1, rb2);
                        aft = System.nanoTime();
                        // we verify the answer
                        if(!verbose) if (!Arrays.equals(rb1i.toArray(),
                                trueintersection))
                                throw new RuntimeException("bug");
                        bogus += rb1i.getCardinality();
                        timings[6] += aft - bef;
                        // Union times
                        bef = System.nanoTime();
                        RoaringBitmap rb1u = RoaringBitmap.or(rb1, rb2);
                        aft = System.nanoTime();
                        // we verify the answer
                        if(!verbose) if (!Arrays.equals(rb1u.toArray(), trueunion))
                                throw new RuntimeException("bug");
                        bogus += rb1u.getCardinality();
                        unions[6] += aft - bef;
                        // Remove times
                        bef = System.nanoTime();
                        rb2.remove(toRemove);
                        aft = System.nanoTime();
                        if(!verbose) if (!Arrays.equals(rb2.toArray(), b2withremoval))
                                throw new RuntimeException("bug");
                        removeTimes[6] += aft - bef;
                        bogus += rb1.getCardinality();
                        rb1 = null;
                        rb2 = null;
                        rb1i = null;
                        rb1u = null;
                }
                if (verbose) {
                        System.out.print(df.format(d) + "\t"
                                + df.format(timings[0] / TIMES)
                                + "\t\t"
                                + df.format(timings[1] / TIMES)
                                + "\t\t"
                                + df.format(timings[2] / TIMES)
                                + "\t\t"
                                + df.format(timings[3] / TIMES)
                                + "\t\t"
                                + df.format(timings[4] / TIMES)
                                + "\t\t"
                                + df.format(timings[5] / TIMES)
                                + "\t\t"
                                + df.format(timings[6] / TIMES));
                        System.out.print("\t\t\t"
                                + df.format(appendTimes[0]
                                        / (TIMES * gen.N))
                                + "\t\t"
                                + df.format(appendTimes[1]
                                        / (TIMES * gen.N))
                                + "\t\t"
                                + df.format(appendTimes[2]
                                        / (TIMES * gen.N))
                                + "\t\t"
                                + df.format(appendTimes[3]
                                        / (TIMES * gen.N))
                                + "\t\t"
                                + df.format(appendTimes[4]
                                        / (TIMES * gen.N))
                                + "\t\t"
                                + df.format(appendTimes[5]
                                        / (TIMES * gen.N))
                                + "\t\t"
                                + df.format(appendTimes[6]
                                      / (TIMES * gen.N)));
                        System.out.print("\t\t\t\t"
                                + df.format(removeTimes[0] / TIMES)
                                + "\t\t"
                                + df.format(removeTimes[1] / TIMES)
                                + "\t\t"
                                + df.format(removeTimes[2] / TIMES)
                                + "\t\t"
                                + df.format(removeTimes[3] / TIMES)
                                + "\t\t"
                                + df.format(removeTimes[4] / TIMES)
                                + "\t\t"
                                + df.format(removeTimes[5] / TIMES)
                                + "\t\t"
                                + df.format(removeTimes[6] / TIMES));
                }
                if (verbose)
                        if (sizeof)
                                System.out
                                        .print("\t\t\t\t"
                                                + dfb.format(storageinbits[0]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[0]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(storageinbits[1]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[1]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(storageinbits[2]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[2]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(storageinbits[3]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[3]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(storageinbits[4]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[4]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(storageinbits[5]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[5]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(storageinbits[6]
                                                        / (2 * TIMES * gen.N))
                                                + "\t"
                                                + dfb.format(truestorageinbits[6]
                                                        / (2 * TIMES * gen.N)));
                        else
                                System.out.print("\t\t\t"
                                        + dfb.format(storageinbits[0]
                                                / (2 * TIMES * gen.N))
                                        + "\t\t"
                                        + dfb.format(storageinbits[1]
                                                / (2 * TIMES * gen.N))
                                        + "\t\t"
                                        + dfb.format(storageinbits[2]
                                                / (2 * TIMES * gen.N))
                                        + "\t\t"
                                        + dfb.format(storageinbits[3]
                                                / (2 * TIMES * gen.N))
                                        + "\t\t"
                                        + dfb.format(storageinbits[4]
                                                / (2 * TIMES * gen.N))
                                        + "\t\t"
                                        + dfb.format(storageinbits[5]
                                                / (2 * TIMES * gen.N))
                                        + "\t\t"
                                        + dfb.format(storageinbits[6]
                                                / (2 * TIMES * gen.N)));
                if (verbose)
                        System.out.print("\t\t\t"
                                + df.format(unions[0] / TIMES) + "\t\t"
                                + df.format(unions[1] / TIMES) + "\t\t"
                                + df.format(unions[2] / TIMES) + "\t\t"
                                + df.format(unions[3] / TIMES) + "\t\t"
                                + df.format(unions[4] / TIMES) + "\t\t"
                                + df.format(unions[5] / TIMES) + "\t\t"
                                + df.format(unions[6] / TIMES));
                if(verbose) System.out.println();
        }
     }
  }

        private static int[] toArray(final BitSet bs) {
                int[] a = new int[bs.cardinality()];
                int pos = 0;
                for (int x = bs.nextSetBit(0); x >= 0; x = bs.nextSetBit(x + 1))
                        a[pos++] = x;
                return a;
        }
}
