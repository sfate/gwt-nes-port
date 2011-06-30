package com.googlecode.gwtnes.client.halfnes;


//HalfNES, Copyright Andrew Hoffman, October 2010

public class Utils {

    public static boolean getbit(final int num, final int bitnum) {
        //returns the nth bit of the int provided
        //bit numbers are zero indexed
        return ((num & (1 << bitnum)) != 0);
    }

    public static int getbitI(final int num, final int bitnum) {
        //returns the nth bit of the int provided
        //as an int, instead. Speeds up things when what you want is an int not a boolean to switch off.
        return (num >> bitnum) & 1;
    }

    public static boolean getbit(final long num, final int bitnum) {
        //returns the nth bit of the int provided
        //bit numbers are zero indexed
        return ((num & (1 << bitnum)) != 0);
    }

    public static String hex(final int num) {
        return Integer.toHexString(num).toUpperCase();
    }

    public static int reverseByte(int nibble) {
        //reverses 8 bits packed into int.
        return (Integer.reverse(nibble) >> 24) & 0xff;
    }


    public static void printarray(final int[] a) {
        StringBuilder s = new StringBuilder();
        for (int i : a) {
            s.append(i);
            s.append(", ");
        }
        s.append("\n");
        System.err.print(s.toString());
    }

    public static void printarray(final short[] a) {
        StringBuilder s = new StringBuilder();
        for (short i : a) {
            s.append(i);
            s.append(", ");
        }
        s.append("\n");
        System.err.print(s.toString());
    }

    public static void printarray(final double[] a) {
        StringBuilder s = new StringBuilder();
        for (double i : a) {
            s.append(i);
            s.append(", ");
        }
        s.append("\n");
        System.err.print(s.toString());
    }

    public static int max(final int[] array) {
        int m = array[0];
        for (Integer i : array) {
            if (i > m) {
                i = m;
            }
        }
        return m;
    }
}
