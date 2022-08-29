package nw4r;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Utility class containing all shared values/methods of all nw4r/sound
 * classes.
 * 
 * @author Ogu99
 * @version 1.0
 */
public final class BinaryUtil {
	
	/**
     * HEADER FLAG: Describes used endian (big endian).
     */
    public static final short BIG_ENDIAN = (short) 0xFEFF;

    /**
     * HEADER FLAG: Describes used endian (little endian).
     */
    @Unused
    public static final short LITTLE_ENDIAN = (short) 0xFFFF;
    
    /**
     * There must be at least ONE section inside each file.
     */
    public static final short MIN_SECTIONS = 1;
    
    /**
     * VERSION FLAG: Represents the file format's version. In NSMBWii it (seems)
     * to be always {@code 0x0104}.
     */
    public static final short NSMBWII_VERSION = 0x0104;
    
	//No object for you.
	private BinaryUtil() {}

	/**
	 * Returns a byte array representing the given string in
	 * {@link #BIG_ENDIAN}.
	 * 
	 * @param s The string.
	 * @return  the string as bytes in {@link #BIG_ENDIAN}.
	 * @see #createFileMagic(String, short)
	 */
	public static byte[] createFileMagic(String s) {
		return createFileMagic(s, BIG_ENDIAN);
	}
	
	/**
	 * Returns a byte array representing the given string. Used
	 * internally in many files as signature words, to distinguish
	 * special parts of a file.
	 * 
	 * Internally, there was an option for {@link #BIG_ENDIAN} and
	 * {@link #LITTLE_ENDIAN}, but only the former was ever used.
	 * For convenience purposes, we shall provide both.
	 *
	 * <p>
	 * <i>Note: Since only the former was used (in most cases) there is
	 * likely no compatibility/support for the little endian option</i>.
	 * <p>
	 * 
	 * @param s    The string.
	 * @param mode Either {@link #BIG_ENDIAN} or <b>unused</b> {@link #LITTLE_ENDIAN}.
	 * @return     the string as bytes.
	 */
	public static final byte[] createFileMagic(String s, short mode) {
		switch(mode) {
		    case BIG_ENDIAN:
		    	return s.getBytes();
		    case LITTLE_ENDIAN:
		    	return toLittleEndian(s);
		    default:
		    	throw new IllegalArgumentException("You chose an invalid type! Choose either big endina or little endian!");
		}
	}
	
	/*
	 * Converts the given string to a byte array in
	 * little endian.
	 */
	private static byte[] toLittleEndian(String s) {
		byte[] buffer = new byte[s.length()];
		for (int i = s.length() - 1; i >= 0; i--) {
			buffer[(s.length() - 1) - i] = (byte) s.charAt(i);
		}
		return buffer;
	}
	
	/**
	 * All files in the nw4r/sound library use the same binary
	 * file header format consisting of:
	 * <ul>
	 *   <li><b>SECTION MAGIC</b>: The name of the section/file in ASCII.
	 *   <li><b>BYTE ORDER MARK</b>: The byte order (either {@link BinaryUtil#BIG_ENDIAN}
	 *                               or {@link BinaryUtil#LITTLE_ENDIAN}).
	 *   <li><b>VERSION</b>: The version of the file.
	 *   <li><b>FILE SIZE</b>: The size of the file.
	 *   <li><b>HEADER SIZE</b>: The size of the header.
	 *   <li><b>SECTION AMOUNT</b>: The number of sections.
	 * </ul>
	 */
	public static class Header {
		public static final int SIZE = 64; //0x00 - 0x40
		private String name;
		private short bom;
		private short version;
		private int fileSize;
		private short size;
		private short numberSections;
	}
	
	/**
	 * All files in the nw4r/sound library use the same binary
	 * file headers for every section they have. 
	 * So every section itself has its own header consisting of:
	 * <ul>
	 *   <li><b>SECTION MAGIC</b>: The name of the section/file in ASCII.
	 *   <li><b>SIZE</b>: The size of the block.
	 * </ul>
	 */
	public static class SectionHeader {
		public static final int SIZE = 32; //0x00 - 0x20
		private String name;
		private int size;
		
		public int getSize() {
		    return size;
		}
		
		public void setSize(int i) {
		    if (i >= 0) {
		        this.size = i;
		    }
		}
	}
	
	/**
	 * Creates a new binary file header.
	 * 
	 * @param name The name of the file format.
	 * @return the created header.
	 */
	public static Header createHeader(String name, short version) {
        var header = new Header();
        header.name = name;
        header.size = 0;
        header.fileSize = 0;
        header.bom = BIG_ENDIAN; //ALWAYS
        header.version = version;
        header.numberSections = 0;
        
        return header;
	}
	
	/**
	 * Creates a new section header.
	 * 
	 * @param name The name of the section.
	 * @return the created header.
	 */
    public static SectionHeader createHeader(String name) {
         var header = new SectionHeader();
         header.name = name;
         header.size = 0;
         
         return header;
    }
	
	/**
	 * Returns {@code true} if the section header is valid otherwise
	 * {@code false}.
	 * 
	 * @param header The header to check.
	 * @param name   The expected name of the section.
	 * @return {@code true} if valid.
	 */
	public static boolean verifyHeader(SectionHeader header, String name) {
		if (!header.name.equals(name)) {
            System.err.println("Invalid Section Header Signature: Expected " + name + " but read " + header.name);
            return false;
        }
		
		return true;
	}
	
	/**
	 * Returns {@code true} if the given binary file header 
	 * has a valid format otherwise {@code false}.
	 * 
	 * @param header  The header to check.
	 * @param name    The expected name of the header.
	 * @param version The expected version of the file.
	 * @return {@code true} if valid.
	 */
    public static boolean verifyHeader(Header header, String name, short version) {
        if (!header.name.equals(name)) {
            System.err.println("Invalid File Header Signature: Expected " + name + " but read " + header.name);
            return false;
        }
        
        if (header.version != version) {
        	System.err.println("Invalid File Header Version: Expected " + version + " but read " + header.version);
            return false;
        }
        
        if (header.bom != BIG_ENDIAN) {
        	System.err.println("Invalid File Header Byte Order Mark: Expected BIG ENDIAN but read " 
                + String.format("0x%08X", header.bom));
            return false;
        }
        
        if (header.fileSize < Header.SIZE + (SectionHeader.SIZE * MIN_SECTIONS)) {
        	System.err.println("Too small file size! It has to be bigger than 24 but your size is " 
                + header.fileSize);
        	return false;
        }
        
        if (header.numberSections < MIN_SECTIONS) {
            System.err.println("Too low amount of sections!");
            return false;
        }
        
        return true;
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
     * Gets value of the given data inside a specified region
     * and returns it as short.
     */
    public static short toShort(byte[] data, int from, int to) {
        return (short) (ByteBuffer.wrap(loadRegion(data, from, to)).getShort());
    }
    
    /**
     * Gets value of the given data inside a specified region
     * and returns it as an integer.
     */
    public static int toInt(byte[] data, int from, int to) {
        return ByteBuffer.wrap(loadRegion(data, from, to)).getInt();
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
     * Convert byte to its hex representation as string.
     */
    static String hex(byte b) {
        return Integer.toHexString((b & 0xff) + 256).substring(1).toUpperCase();
    }
    
    /**
     * Tries to load and verify a binary file header. It ONLY verifies
     * if the binary file header is valid, not the files header.
     * 
     * @param data    The data to check.
     * @param v       The version this file should have.
     * @return        the read header if it was valid, otherwise {@code null}.
     */
    public static Header loadHeader(byte[] data, short v) {
        Header h;
        //Load in all the data first.
        String name = new String(loadRegion(data, 0, 0x04));
        short bom = toShort(data, 0x04, 0x06);
        short version = toShort(data, 0x06, 0x08);
        int fileSize = toInt(data, 0x08, 0x0C);
        short size = toShort(data, 0x0C, 0x0E);
        short numberSections = toShort(data, 0x0E, 0x10);
        
        h = createHeader(name, version);
        h.fileSize = fileSize;
        h.size = size;
        h.numberSections = numberSections;
        h.bom = bom;
        
        if (!verifyHeader(h, "RSEQ", v)) {
            System.err.print("------ INVALID HEADER READ! ------");
            return null;
        }
        
        return h;
    }
    
    /**
     * Tries to load and verify a binary file header. It ONLY verifies
     * if the binary file header is valid, not the files header. It only
     * accepts file compatible with NSMBWii
     * 
     * @param data    The data to check.
     * @return        the read header if it was valid, otherwise {@code null}.
     */
    public static Header loadHeader(byte[] data) {
        return loadHeader(data, NSMBWII_VERSION);
    }
    
    /**
     * Reads the binary data of a given file and returns the
     * data as byte array.
     * 
     * @param file The file to read.
     * @return     the binary data of the file.
     */
    public static byte[] read(String file) {
	    String filename = file;
	    try (FileInputStream fis = new FileInputStream(filename)) {
	        byte[] bytes = fis.readAllBytes();
	        return bytes;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}