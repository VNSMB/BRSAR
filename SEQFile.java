package nw4r.sound.rseq;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nw4r.BinaryUtil;
import nw4r.BinaryUtil.Header;
import nw4r.BinaryUtil.SectionHeader;

/**
 * This class provides an implementation of the "BRSEQ" file found
 * in Nintendo's BRSAR files.
 * 
 * @author Ogu99
 * @version 1.0
 */
public class SEQFile {

    /**
     * HEADER MAGIC: RSEQ in ASCII.
     */
    private static final String HEADER_MAGIC = "RSEQ";
    
    /**
     * FILE MAGIC: DATA section starting off with DATA ASCII.
     */
    private static final String DATA_SECTION = "DATA";
    
    /**
     * FILE MAGIC: LABL section starting off with LABL in ASCII.
     */
    private static final String LABL_SECTION = "LABL";
    
    /**
     * Invalid offset.
     */
    private static final int INVALID_OFFSET = 0xFFFFFFFF;
    
    /**
     * The offset to the RSEQ data.
     */
    private static final int RSEQ_OFFSET = 0x10;
    
    /**
     * Number of integer bytes.
     */
    private static final int INT = Integer.BYTES;
    
    private Header header;
    private DATA data;
    private LABL label;
    private int dataBlockOffset;
    private int dataBlockSize;
    private int labelBlockOffset;
    private int labelBlockSize;
    
    private String fileName;
    private byte[] binaryData;
    
    /**
     * Creates a new (NSMBWii) compatible BRSEQ file.
     * @param fileName
     */
    public SEQFile(String fileName) {
       this(fileName, BinaryUtil.NSMBWII_VERSION);
    }
    
    /**
     * Creates a new BRSEQ with forced version.
     * 
     * @param fileName     The name of the file.
     * @param forceVersion The version the file SHOULD have.
     */
    public SEQFile(String fileName, int forceVersion) {
    	this.fileName = fileName;
        this.header = BinaryUtil.createHeader(HEADER_MAGIC, (short) forceVersion);
        this.data = new DATA();
        this.label = new LABL();
        this.dataBlockOffset = this.dataBlockSize 
            = this.labelBlockOffset = this.labelBlockSize = INVALID_OFFSET;
    }
    
    /**
     * Tries to load and read a BRSEQ file. If the format somehow is
     * invalid {@code null} is returned. It only accepts files compatible
     * with NSMBWii. If you want to load another file, 
     * use {@link #load(String, int)}.
     * 
     * @param fileName The file to load.
     * @return         the data wrapped as a {@link SEQFile}.
     */
    public static SEQFile load(String fileName) {
        return load(fileName, BinaryUtil.NSMBWII_VERSION);
    }
    
    /**
     * Tries to load and read a BRSEQ file. If the format somehow is
     * invalid {@code null} is returned.
     * 
     * @param fileName The file to load.
     * @param version  The version the file should have.
     * @return         the data wrapped as a {@link SEQFile}.
     */
    public static SEQFile load(String fileName, int version) {
        byte[] b = BinaryUtil.read(fileName);
        
        Header h = BinaryUtil.loadHeader(b, (short) version);
        if (h != null) {
            SEQFile seq = new SEQFile(fileName);
            seq.header = h;
            //0x10 - 0x14
            seq.dataBlockOffset = BinaryUtil.toInt(b, RSEQ_OFFSET, RSEQ_OFFSET + Integer.BYTES);
            //0x14 - 0x18
            seq.dataBlockSize = BinaryUtil.toInt(b, RSEQ_OFFSET + Integer.BYTES, RSEQ_OFFSET + (Integer.BYTES * 2));
            //0x18 - 0x1B
            seq.labelBlockOffset = BinaryUtil.toInt(b, RSEQ_OFFSET + (Integer.BYTES * 2), RSEQ_OFFSET + (Integer.BYTES * 3));
            //0x1B - 0x20
            seq.labelBlockSize = BinaryUtil.toInt(b, RSEQ_OFFSET + (Integer.BYTES * 3), RSEQ_OFFSET + (Integer.BYTES * 4));
            
            seq.loadSection(b, DATA_SECTION, seq.dataBlockOffset);
            seq.loadSection(b, LABL_SECTION, seq.labelBlockOffset);            
            return seq;
        }
        return null;
    }
    
    /*
     * Load section of the sequence.
     */
    private void loadSection(byte[] data, String sectionName, int startOffset) {
        switch(sectionName) {
            case DATA_SECTION -> {
                loadDATAHeader(data, startOffset);
                loadDATA(data);
            }
            case LABL_SECTION -> {
                loadLABLHeader(data, startOffset);
                loadLABLData(data);
            }
            default -> {
                System.err.println("Invalid section name was read!");
            }
        }
    }
    
    /*
     * Load all data from the DATA section header.
     */
    private void loadDATAHeader(byte[] data, int startOffset) {
        this.data.header.setSize(BinaryUtil.toInt(data, startOffset + INT, startOffset + (INT * 2)));
        this.data.baseOffset = BinaryUtil.toInt(data, startOffset + (INT * 2), startOffset + (INT * 3));
    }
    
    /*
     * Load all data from the DATA section.
     */
    private void loadDATA(byte[] data) {
        int startOffset = this.dataBlockOffset + 0x0C;

        for (int i = startOffset; i < this.labelBlockOffset - startOffset; i++) {
        	List<Byte> l = new ArrayList<>();
        	
        	int j = i;
            while(data[j] != -1) { //Go until we reach 0xFF
                l.add(data[j++]);
            }
            
            l.add((byte) 0xFF); //Every sequence finishes with 0xFF
            
            this.data.dataBlocks.put(i, l.toArray(new Byte[l.size()]));
            i = j;
        }

    }
    
    /*
     * Load all data from the LABL section header.
     */
    private void loadLABLHeader(byte[] data, int startOffset) {
    	this.label.header.setSize(BinaryUtil.toInt(data, startOffset + INT, startOffset + (INT * 2)));
        this.label.numberOfLables = BinaryUtil.toInt(data, startOffset + (INT * 2), startOffset + (INT * 3));
        this.label.offsetToLabel = new int[this.label.numberOfLables];
    }
    
    /*
     * Load all data from the LABL section.
     */
    private void loadLABLData(byte[] data) {
        final int TABLE_SIZE = this.label.numberOfLables * INT;
        
        //We start at the first element inside the label offset table.
        //Every element is a 4 byte integer
        int startOffset = this.labelBlockOffset + this.label.numberOfLables;

        for (int i = startOffset, j = 0; i < (startOffset + TABLE_SIZE) && j < TABLE_SIZE; i += INT, j++) {
            int labelOffset = BinaryUtil.toInt(data, i, i + INT);
            
            this.label.offsetToLabel[j] = labelOffset;
            int offToData = this.labelBlockOffset + 0x08 + labelOffset;
            
            int labelInfoOffset = BinaryUtil.toInt(data, offToData, offToData + INT);
            int labelNameLength = BinaryUtil.toInt(data, offToData + INT, offToData + (INT * 2));
            
            int startName = offToData + (INT * 2);
            int endName = startName + labelNameLength;
            String labelName = new String(BinaryUtil.loadRegion(data, startName, endName));
           
            this.label.info.add(new LABLInfo(labelInfoOffset, labelName));
        }
    }
    
    /**
     * @return all labels this sequence has.
     */
    public List<LABLInfo> getAllLabels() {
        return this.label.info;
    }
    
    /**
     * @return this file's binary header.
     */
    public Header getBinaryHeader() {
        return this.header;
    }
    
    /**
     * @return a string representation of all data blocks.
     */
    public String showData() {
        return this.data.toString();
    }
    
    /**
     * @return this file's name.
     */
    public String getFileName() {
        return this.fileName;
    }
    
    /**
     * @return the DATA section's size.
     */
    public int DATA_size() {
        return this.dataBlockSize;
    }
    
    /**
     * @return the LABL section's size.
     */
    public int LABL_size() {
        return this.labelBlockSize;
    }

    /**
     * Tries to find the label with the id and
     * print all data corresponding to that label.
     *
     * @param i The index of the label.
     */
    public void getLabel(int i) {
        LABLInfo info = this.label.info.get(i);

        System.out.println("Name: " + info.name);
        System.out.println("Data offset: " + Integer.toHexString(info.data));
        System.out.println("DATA (in HEX): " + BinaryUtil.toString(this.data.dataBlocks.get(info.data)));
    }

    /**
     * Represents the DATA section of a RSEQ.
     */
    class DATA {
    	static final int SIZE = 0x0C;
    	Map<Integer, Byte[]> dataBlocks;
        SectionHeader header;
        int baseOffset;
        
        DATA() {
            header = BinaryUtil.createHeader(DATA_SECTION);
            baseOffset = INVALID_OFFSET;
            dataBlocks = new LinkedHashMap<>();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
        	for (int i : dataBlocks.keySet()) {
                sb.append(Integer.toHexString(i) + " = " + BinaryUtil.toString(dataBlocks.get(i)) + "\n");
            }
        	return sb.toString();
        }
    }
    
    /**
     * Represents the LABL section of a RSEQ.
     */
    class LABL {
        static final int SIZE = 0x0C;
        SectionHeader header;
        int numberOfLables;
        int[] offsetToLabel;
        List<LABLInfo> info;
        
        LABL() {
            header = BinaryUtil.createHeader(LABL_SECTION);
            info = new LinkedList<>();
        }
    }
    
    /**
     * Represents the LABLInfo of the LABL section.
     */
    class LABLInfo {
        int offset;
        int nameLength;
        String name;
        int data;
        
        LABLInfo(int offset, String name) {
            this.offset = offset;
            this.nameLength = name.length();
            this.name = name;
            this.data = dataBlockOffset + 0x0C + this.offset;
        }
        
        @Override
        public String toString() {
            return "LABEL(" + Integer.toHexString(offset) + ", " + nameLength + ", " + name + ")"
                   +" SEQ DATA at: " + Integer.toHexString(data);
        }
    }
}