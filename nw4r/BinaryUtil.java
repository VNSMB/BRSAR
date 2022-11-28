package nw4r;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Utility class for several binary operations.
 * 
 * @author VNSMB
 * @version 1.0
 */
public final class BinaryUtil {
    //No object for you.
    private BinaryUtil() {}
    
    /**
     * Gets the value of the given data inside a specified region
     * and returns it as short.
     */
    public static short toShort(byte[] data, int from, int to) {
        return ByteBuffer.wrap(loadRegion(data, from, to)).getShort();
    }
    
    /**
     * Gets the value of the given data inside a specified region
     * and returns it as an integer.
     */
    public static int toInt(byte[] data, int from, int to) {
        return ByteBuffer.wrap(loadRegion(data, from, to)).getInt();
    }
    
    /**
     * Loads specific bytes of the given byte array 
     * starting from a given index (inclusive) to 
     * the specified end (exclusive).
     */
    public static byte[] loadRegion(byte[] data, int from, int to) {
        if (from < 0 || from >= to || to < 0) {
           System.err.println("Unable to load region for given byte array! Indeces out of bounds!");
           return null;
        }

        return Arrays.copyOfRange(data, from, to);
    }
    
    /**
     * Loads specific bytes of the given byte array 
     * starting from a given index (inclusive) to 
     * the specified end (exclusive).
     */
    public static Byte[] loadRegionAsObject(byte[] data, int from, int to) {
        byte[] b = loadRegion(data, from, to);
        Byte[] output = new Byte[b.length];
        for (int i = 0; i < b.length; i++)
            output[i] = b[i];
        return output;
    }
    
    /**
     * Reads the binary data of a given file and returns the
     * data as byte array.  If the file could not be found, then
     * {@code null} is returned.
     * 
     * @param file The file to read.
     * @return     the binary data of the file.
     */
    public static byte[] read(String file) {
        try {
    	    return Files.readAllBytes(Path.of(file));
        } catch(IOException e) { return null; }
	}
    
    /**
     * Converts a {@link Byte} array to its primitive counterpart.
     * @param a The array to convert.
     * @return  the converted array.
     */
    public static byte[] toByte(Byte[] a) {
        byte[] b = new byte[a.length];
        for (int i = 0; i < b.length; i++) b[i] = a[i];
        return b;
    }
    
    /**
     * Takes a byte array and returns it string representation
     * with its numbers displayed in hex.
     * 
     * @param b The array to get the string representation of.
     * @return  the array as hex string.
     */
    public static String toString(Byte[] b) {
        StringBuilder sb = new StringBuilder("[" + hex(b[0]));
        for (int i = 1; i < b.length; i++) {
            sb.append(", " + hex(b[i]));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Takes a byte array and returns it string representation
     * with its numbers displayed in hex.
     *
     * @param b The array to get the string representation of.
     * @return  the array as hex string.
     */
    public static String toString(byte[] b) {
        StringBuilder sb = new StringBuilder("[" + hex(b[0]));
        for (int i = 1; i < b.length; i++) {
            sb.append(", " + hex(b[i]));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Convert a byte to its hex representation as string.
     */
    static String hex(byte b) {
        return Integer.toHexString((b & 0xff) + 256).substring(1).toUpperCase();
    }
}