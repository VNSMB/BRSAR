package nw4r;

import static nw4r.BinaryUtil.loadRegion;
import static nw4r.BinaryUtil.toShort;
import static nw4r.BinaryUtil.toInt;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Namespace containing base functionality for {@code binary
 * files} in the NW4R library.
 *
 * @author VNSMB
 * @author SMTuX
 * @version 1.1
 */
public final class Format {

    /**
     * Byte order mark for "big endian".  No support
     * for little endian, since there is no known instance
     * where it was ever used.
     */
    public static final short BIG_ENDIAN = (short) 0xFEFF;

    /**
     * Byte order mark for "little endian".  This BOM
     * is not supported (or at least there is no known instance
     * of where it was actually used).  This constant is
     * here only for convenience purposes.
     */
    @Deprecated
    public static final short LITTLE_ENDIAN = (short) 0xFFFF;
    
    /**
     * Default byte order mark.  Will be set by startup but will
     * be {@link #BIG_ENDIAN} in most cases.
     */
    public static final short BOM;
    
    //Set default byte order mark.
    static {
        BOM = BIG_ENDIAN;
    }

    /**
     * Default header size of every NW4R binary file, in other words the
     * minimum expected size a header should have.
     */
    public static final int DEFAULT_HEADER_SIZE = 16; // 0x00 - 0x20
    
    /**
     * Convenience constant for an invalid offset.
     */
    public static final int INVALID_OFFSET = 0xFFFFFFFF;
    
    /**
     * Default header size of every NW4R section header inside a binary
     * file, in other words the minimum expected size a section header
     * should have.
     */
    public static final int DEFAULT_SECTION_SIZE = 8; // 0x00 - 0x08
    
    /**
     * Default size for signature words.
     */
    public static final int SIG_SIZE = 4;
    
    //No instance for you.
    private Format() {}
   
    /**
     * Creates an appropriate {@code byte array} for the provided
     * signature and returns it.  If the {@link #BOM} is set to
     * {@link #LITTLE_ENDIAN} {@code null} is returned.
     * 
     * @param s - The signature word.
     * @return the bytes for the signature word.
     * @throws IllegalStateException If the {@link #BOM} is neither
     *  {@link #BIG_ENDIAN} nor {@link #LITTLE_ENDIAN}.
     */
    public static byte[] createSignature(String s) {
        return switch(BOM) {
            case BIG_ENDIAN -> s.getBytes();
            case LITTLE_ENDIAN -> {
                System.err.println("Warning! The BOM is set to little endian, which might not be supported.");
                yield null;
            }
            default -> throw new IllegalStateException("Unknown byte order mark!");
        };
    }
    
    /**
     * Creates an appropriate {@code byte array} for the provided
     * version and returns it.
     * 
     * @param major - The major version.
     * @param minor - The minor version.
     * @return the bytes representing the version.
     */
    public static byte[] createVersion(int major, int minor) {
        return ByteBuffer.allocate(Short.BYTES).put((byte) major).put((byte) minor).array();
    }
    
    /**
     * @return the binary data for the {@link #BOM}.
     */
    public static byte[] getByteOrderMark() {
        return ByteBuffer.allocate(Short.BYTES).putShort(BOM).array();
    }
    
    /**
     * Asserts whether the provided binary data matches the expected values
     * and valid format of a NW4R binary file.
     * 
     * @param data            The data to check.
     * @param expectedName    The expected name.
     * @param expectedVersion The expected version.
     * @throws IllegalStateException If the file is invalid.
     */
    public static void assertFile(byte[] data, String expectedName, Version expectedVersion) {
        String name = new String(loadRegion(data, 0, 0x04));
        short bom = toShort(data, 0x04, 0x06);
        short version = toShort(data, 0x06, 0x08);
        int fileSize = toInt(data, 0x08, 0x0C);
        //short size = toShort(data, 0x0C, 0x0E);
        short numberOfSections = toShort(data, 0x0E, 0x10);
        
        if (bom != BOM || !name.equals(expectedName) || version != expectedVersion.getVersion()
              || fileSize < File.MIN_SIZE + (Section.MIN_SIZE * File.MIN_SECTIONS) || numberOfSections < File.MIN_SECTIONS) {
            throw new IllegalStateException("Read an invalid NW4R binary file! The header is not valid!");
        }
    }
    
    /**
     * Base class for all binary files in the NW4R library.  Every
     * valid binary file extends and implements this class.  A valid
     * file header contains:
     * <ul>
     *   <li><i>Signature word</i> - Every binary file is marked by a
     *    signature word, for example {@code RSAR}, {@code RSEQ} or
     *    {@code RLYT}.  Per default all signatures are <b>4</b> bit long
     *    but the implementation allows for longer words.
     *   <li><i>Byte Order Mark (BOM)</i> - Byte order mark.  Always
     *    {@link Format#BIG_ENDIAN}.  Always <b>2</b> bytes.
     *   <li><i>Major/Minor version</i> - Version of the SDK this file
     *    was created in.  Always <b>2</b> bytes.
     *   <li><i>File size</i> - The size of the file as {@code integer}.
     *    Always <b>4</b> bytes.
     *   <li><i>Header size</i> - Total size of the header.  Always <b>2</b>
     *    bytes.
     *   <li><i>Number of sections</i> - Total number of sections this file
     *    has. Always <b>2</b> bytes.
     *   <li><i>Additional data</i> - The file header can be extended with
     *    more additional data.
     * </ul>
     */
    public static abstract class File {
        /** Minimum size of the file header in bytes. */
        public static final int MIN_SIZE = 16;
        
        /** 
         * Minimum amount of sections a binary file needs to have. 
         * */
        private static final int MIN_SECTIONS = 1;
        
        /**
         * All sections of this file.
         */
        protected final List<Section> sections;
       
        /**
         * The signature of this file.
         */
        protected final String signature;
        
        /**
         * The byte order mark of this file.  It will be
         * always preset to {@link Format#BOM}.
         */
        protected final short bom;
        
        /**
         * Major version of the SDK this file was created
         * in.
         */
        protected int majorVersion;
        
        /**
         * Minor version of the SDK this file was created
         * in.
         */
        protected int minorVersion;
        
        /**
         * The size of the file.
         */
        protected int fileSize = 0;
        
        /**
         * The size of the header.
         */
        protected short headerSize;
        
        /**
         * The number of sections inside this file.
         */
        protected short numberOfSections;
        
        /**
         * Creates a new {@code binary file} with the provided {@code signature},
         * {@code major version}, {@code minor version} and {@linkplain Section sections}.
         * 
         * @param signature - The signature of this file.
         * @param majorVersion - The major version of this file.
         * @param minorVersion - The minor version of this file.
         * @param sections - The sections of this file.
         */
        protected File(String signature, int majorVersion, int minorVersion, List<Section> sections) {
            if (sections.size() < MIN_SECTIONS)
                throw new IllegalStateException("A valid binary file needs at least 1 section!");
            this.signature = signature;
            this.bom = BOM;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.sections = sections;
            this.numberOfSections = (short) this.sections.size();
        }
        
        /**
         * Creates the {@code binary data} for this file and returns
         * it as a {@code byte array}.
         * 
         * @return the binary data as an array.
         */
        public abstract byte[] createBinaryFile();
    }
    
    /**
     * Base class for all section blocks (i.e. {@code DATA} or  {@code SYMB})
     * inside NW4R binary files.  Every valid section block extends and
     * implements this class.  A valid section header consists of:
     * <ul>
     *   <li><i>Signature word</i> - Every binary file is marked by a
     *    signature word, for example {@code RSAR}, {@code RSEQ} or
     *    {@code RLYT}.  Per default all signatures are <b>4</b> bit long
     *    but the implementation allows for longer words.  
     *   <li><i>Size</i> - The size of this block as {@code integer}.
     *    Always <b>4</b> bytes.
     * </ul>
     */
    public static abstract class Section {
        /** Minimum size in bytes. */
        public static final int MIN_SIZE = 8;
        
        /** 
         * The signature word of this section. 
         */
        protected final String signature;
        
        /**
         * Total size of this section (in bytes).
         */
        protected int size;
        
        /**
         * Creates a new section with the provided 
         * {@code signature}.
         * 
         * @param signature - The signature of this section.
         */
        protected Section(String signature) {
            this.signature = signature;
        }

        /**
         * Sets the size for this section.
         * @param size The size to set.
         */
        public void setSize(int size) {
            if (size < 0) throw new IllegalArgumentException("Invalid size!");
            this.size = size;
        }
    }
}