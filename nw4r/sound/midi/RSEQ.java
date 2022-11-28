package nw4r.sound.midi;

import static nw4r.sound.midi.InstructionEnDecoder.decodeMMLInstruction;
import static nw4r.sound.midi.InstructionEnDecoder.decodeMMLEXInstruction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nw4r.BinaryUtil;
import nw4r.Format;
import nw4r.Version;

/**
 * Parser for {@code RSEQ} files in the <b>NW4R</b> library.
 * RSEQs are basically MIDIs defined by the <b>M</b>usic
 * <b>M</b>acro <b>L</b>anguage (<b>MML</b>).  This class
 * provides operations for <i>parsing RSEQ instruction sets
 * to BRSEQ files</i> and vice versa.  The parsed files can
 * also be obtained as {@code RSEQ} object instances.
 * 
 * @author VNSMB
 * @version 1.0
 */
public final class RSEQ {

    /* CONSTANTS */

    /**
     * Different types for the arguments of an
     * instruction are represented by each enum
     * constant.
     */
    enum ArgType {
        NONE,
        U8,
        S16,
        VMIDI,
        RANDOM,
        VARIABLE
    }

    /**
     * BRSEQ file extension.
     */
    private static final String BRSEQ = ".brseq";
   
    /**
     * RSEQ file extension.
     */
    private static final String RSEQ = ".rseq";
    
    /**
     * Signature of this file.
     */
    private static final String SIGNATURE = "RSEQ";
    
    /**
     * DATA section.
     */
    private static final String DATA = "DATA";
    
    /**
     * LABL section.
     */
    private static final String LABEL = "LABL";
    
    /**
     * Minimum value for tempo.
     */
    private static final int MIN_TEMPO = 0;
    
    /**
     * Default value for tempo.
     */
    private static final int DEFAULT_TEMPO = 120;
    
    /**
     * Maximum value for tempo.
     */
    private static final int MAX_TEMPO = 1023;
    
    private static final int PLAYER_VARIABLE_NUM = 16;
    private static final int GLOBAL_VARIABLE_NUM = 16;
    
    /* Music Macro Language (MML) instrucion set*/
    /* They basically used the MML specification for their MIDIs (RSEQ) */
    /* So we need to build (or rebuild) our own parser for this format. */
    
    /**
     * Bit mask for note values.
     */
    private static final int NOTE_MASK = 0x80;
    
    /** Opcode corresponding to this MML instruction. */
    static final byte
        MML_WAIT            = (byte) 0x80,
        MML_PRG             = (byte) 0x81,
    
        // Instructions manipulating the control flow.
        MML_OPEN_TRACK      = (byte) 0x88,
        MML_JUMP            = (byte) 0x89,
        MML_CALL            = (byte) 0x8a,
    
        // Prefix instructions, i.e. _r, _t or _v.
        MML_RANDOM          = (byte) 0xa0,
        MML_VARIABLE        = (byte) 0xa1,
        MML_IF              = (byte) 0xa2,
        MML_TIME            = (byte) 0xa3,
        MML_TIME_RANDOM     = (byte) 0xa4,
        MML_TIME_VARIABLE   = (byte) 0xa5,
    
        // U8 parameter instructions.
        MML_TIMEBASE        = (byte) 0xb0,
        MML_ENV_HOLD        = (byte) 0xb1,
        MML_MONOPHONIC      = (byte) 0xb2,
        MML_VELOCITY_RANGE  = (byte) 0xb3,
        MML_BIQUAD_TYPE     = (byte) 0xb4,
        MML_BIQUAD_VALUE    = (byte) 0xb5,
        MML_PAN             = (byte) 0xc0,
        MML_VOLUME          = (byte) 0xc1,
        MML_MAIN_VOLUME     = (byte) 0xc2,
        MML_TRANSPOSE       = (byte) 0xc3,
        MML_PITCH_BEND      = (byte) 0xc4,
        MML_BEND_RANGE      = (byte) 0xc5,
        MML_PRIO            = (byte) 0xc6,
        MML_NOTE_WAIT       = (byte) 0xc7,
        MML_TIE             = (byte) 0xc8,
        MML_PORTA           = (byte) 0xc9,
        MML_MOD_DEPTH       = (byte) 0xca,
        MML_MOD_SPEED       = (byte) 0xcb,
        MML_MOD_TYPE        = (byte) 0xcc,
        MML_MOD_RANGE       = (byte) 0xcd,
        MML_PORTA_SW        = (byte) 0xce,
        MML_PORTA_TIME      = (byte) 0xcf,
        MML_ATTACK          = (byte) 0xd0,
        MML_DECAY           = (byte) 0xd1,
        MML_SUSTAIN         = (byte) 0xd2,
        MML_RELEASE         = (byte) 0xd3,
        MML_LOOP_START      = (byte) 0xd4,
        MML_VOLUME2         = (byte) 0xd5,
        MML_PRINTVAR        = (byte) 0xd6,
        MML_SURROUND_PAN    = (byte) 0xd7,
        MML_LPF_CUTOFF      = (byte) 0xd8,
        MML_FXSEND_A        = (byte) 0xd9,
        MML_FXSEND_B        = (byte) 0xda,
        MML_MAINSEND        = (byte) 0xdb,
        MML_INIT_PAN        = (byte) 0xdc,
        MML_MUTE            = (byte) 0xdd,
        MML_FXSEND_C        = (byte) 0xde,
        MML_DAMPER          = (byte) 0xdf,
    
        // S16 parameter instructions.
        MML_MOD_DELAY       = (byte) 0xe0,
        MML_TEMPO           = (byte) 0xe1,
        MML_SWEEP_PITCH     = (byte) 0xe3,
    
        // Extended instructions.
        MML_EX_COMMAND      = (byte) 0xf0,
    
        // Other
        MML_ENV_RESET       = (byte) 0xfb,
        MML_LOOP_END        = (byte) 0xfc,
        MML_RET             = (byte) 0xfd,
        MML_ALLOC_TRACK     = (byte) 0xfe,
        MML_FIN             = (byte) 0xff;
    
    /* Music Macro Language Extended (MMLEX) instrucion set*/
    
    /** Opcode corresponding to this MML extended instruction. */
    static final byte 
        MMLEX_SETVAR          = (byte) 0x80,
        MMLEX_ADDVAR          = (byte) 0x81,
        MMLEX_SUBVAR          = (byte) 0x82,
        MMLEX_MULVAR          = (byte) 0x83,
        MMLEX_DIVVAR          = (byte) 0x84,
        MMLEX_SHIFTVAR        = (byte) 0x85,
        MMLEX_RANDVAR         = (byte) 0x86,
        MMLEX_ANDVAR          = (byte) 0x87,
        MMLEX_ORVAR           = (byte) 0x88,
        MMLEX_XORVAR          = (byte) 0x89,
        MMLEX_NOTVAR          = (byte) 0x8a,
        MMLEX_MODVAR          = (byte) 0x8b,

        MMLX_CMP_EQ          = (byte) 0x90,
        MMLX_CMP_GE          = (byte) 0x91,
        MMLX_CMP_GT          = (byte) 0x92,
        MMLX_CMP_LE          = (byte) 0x93,
        MMLX_CMP_LT          = (byte) 0x94,
        MMLX_CMP_NE          = (byte) 0x95,

        MMLX_USERPROC        = (byte) 0xe0;
   
    /* Class implementation */
   
    // Parsing BRSEQ -> RSEQ
    /** Byte order mark (BOM) of this file. Always big endian. */
    private final short bom = Format.BOM;
    private final byte[] data;
    private boolean isBinary = false;
    private short version;
    
    // Parsing RSEQ -> BRSEQ
    /** All read lines of the RSEQ instruction set. */
    private final List<String> lines;
    
    // Shared
    /** The input file path. */
    private final String file;
    
    /**
     * Constructs a new RSEQ object for the specified file
     * path.
     * 
     * @param file The absolute path to the RSEQ/BRSEQ file.
     * @throws IOException If the file could not be found.
     */
    private RSEQ(String file) throws IOException {
        this.isBinary = file.endsWith(BRSEQ);
        if (this.isBinary) {
            this.data = Files.readAllBytes(Path.of(file));
            this.lines = null;
        } else {
            this.data = null;
            this.lines = Files.readAllLines(Path.of(file));
        }
        this.file = file;
    }
   
    /**
     * Creates a new RSEQ object wrapper for the specified
     * <b>RSEQ</b> or <b>BRSEQ</b> file and returns it.
     * 
     * @param file The absolute path to the file.
     * @return the instance representing the loaded file.
     */
    public static RSEQ forFile(String file) {
        try {
            return new RSEQ(file);
        } catch (IOException e) { throw new RuntimeException(e); }
    }
    
    /* Parsing related */
    
    public RSEQ parse() {
        if (this.isBinary) return forFile(toRSEQ());
        else return forFile(toBRSEQ());
    }
    
    private String toRSEQ() {
        BinaryFileParser bin = new BinaryFileParser(this.data);
        bin.decode();
        return this.file.replaceAll(BRSEQ, RSEQ);
    }
    
    private String toBRSEQ() {
        return this.file.replaceAll(RSEQ, BRSEQ);
    }
    
    static class DataSection {
        protected final Map<Label, Byte[]> data = new HashMap<>();
        protected final int size;
        protected final int baseOffset;
        
        DataSection(int size, int baseOffset) {
            this.size = size;
            this.baseOffset = baseOffset;
        }
    }
    
    static class InstructionData {
        private final List<String> instructions = new ArrayList<>();
       
        void add(String instruction) {
            this.instructions.add(instruction);
        }
    }
    
    static class LabelSection {
        protected final int size;
        protected final int offset;
        protected final Label[] labels;
       
        LabelSection(int size, int offset, Label[] labels) {
            this.size = size;
            this.offset = offset;
            this.labels = labels;
        }
    }
    
    /**
     * Encapsulates a single {@code Label} inside the
     * {@link LabelSection} of this <b>BRSEQ</b> file.
     * It holds data about the name of the label and its
     * MIDI sequence data.
     */
    static class Label implements Comparable<Label> {
        /**
         * The relative offset to this label.
         */
        protected final int offset;
        
        /**
         * The absolute offset to the data of
         * this label.
         */
        protected final int dataOffset;
        
        /**
         * The length of the label name.
         */
        protected final int lengthOfName;
        
        /**
         * The name of the label.
         */
        protected final String name;
       
        /**
         * Creates a new label entry for the specified offset
         * and name.  The offset to this labels data is calculated
         * by the provided {@code dataBlockOffset}.
         */
        Label(int offset, String name, int dataBlockOffset) {
            this.offset = offset;
            this.name = name;
            this.lengthOfName = name.length();

            this.dataOffset = dataBlockOffset + 0x0C + this.offset;
        }
       
        @Override
        public int compareTo(Label l) {
            return Integer.compare(this.dataOffset, l.dataOffset);
        }
    }
    
    static class BinaryFileParser {
        private final byte[] data;
        private short version;
        
        /**
         * The byte order mark which was read.  If this is not
         * {@link Format#BIG_ENDIAN} then the file is not
         * a valid BRSEQ file.
         */
        private short readBOM;
        private int pointer = 0;
        
        BinaryFileParser(byte[] data) {
            this.data = data;
        }
        
        /**
         * Advances parser and returns the current byte.
         * 
         * @return the current byte.
         */
        private byte move() {
            return this.data[this.pointer++];
        }
        
        /**
         * @return the read signature of this binary file.
         */
        private String fetchSignature() {
            byte[] b = new byte[Format.SIG_SIZE];
            for (int i = 0; i < b.length; i++) {
                b[i] = move();
            }
            return new String(b);
        }
        
        /**
         * Fetches the next <b>n</b> bytes and returns its
         * integer value.
         * 
         * @param n The amount of bytes to fetch.
         * @return the fetched value.
         */
        private int valueOfNBytes(int n) {
            byte[] b = new byte[n];
            for (int i = 0; i < b.length; i++) {
                b[i] = move();
            }
            
            final ByteBuffer buffer = ByteBuffer.wrap(b);
            return switch(n) {
                case Byte.BYTES -> buffer.get();
                case Short.BYTES -> buffer.getShort();
                case Integer.BYTES -> buffer.getInt();
                default -> throw new IllegalArgumentException("Unexpected chunk size: " + n);
            };
        }
        
        private int readAfter(int offset) {
            int v = 0;
            for(int i = 0; i < offset; i += 8) 
                v |= this.data[i] << i;
            return v;
        }
        
        private int readB(int offset) {
            int v = 0;
            for (int i = offset - 8; i >= 0; i-=8) 
                v |= move() << i;
            return v;
        }
        
        /**
         * Decoded loaded BRSEQ data.
         */
        public void decode() {
            //Decode complete BRSEQ header.
            final String signature = fetchSignature();
            final short bom = (short) valueOfNBytes(2);
            final short version = (short) valueOfNBytes(2);
            final int fileSize = valueOfNBytes(4);
            final short headerSize = (short) valueOfNBytes(2);
            final short numberOfSections = (short) valueOfNBytes(2);
            final int dataBlockOffset = valueOfNBytes(4);
            final int dataBlockSize = valueOfNBytes(4);
            final int labelBlockOffset = valueOfNBytes(4);
            final int labelBlockSize = valueOfNBytes(4);
            
            if (!signature.equals(SIGNATURE) || bom != Format.BOM || version != Version.__VERSION__1__0__.getVersion())
                throw new IllegalStateException(String.format("Invalid RSEQ file read! Read data:"
                    + "Signature: %s\nBOM: %d\nVersion: %d\nFile Size: %d\nHeader Size: %d\n%Number of sections: %d\n"
                    + "DATA offset: %d\nDATA size: %d\nLABL offset: %d\nLABL size: %d",
                    signature,
                    bom,
                    version,
                    fileSize,
                    headerSize,
                    numberOfSections,
                    dataBlockOffset,
                    dataBlockSize,
                    labelBlockOffset,
                    labelBlockSize));
            
            //Stop at DATA chunk
            //final String dataSig = fetchSignature();
            int oldPos = this.pointer;
            
            //Decode LABL chunk
            this.pointer = labelBlockOffset; //Jump
            LabelSection label = decodeLabels(labelBlockOffset, dataBlockOffset);
            //for (Label l : label.labels) {
            //    System.out.println(l.name + " " + Integer.toHexString(l.offset) + " " + Integer.toHexString(l.dataOffset));
            //}
            
            this.pointer = oldPos; //Now fetch data for every label.
            DataSection data = decodeData(dataBlockOffset, labelBlockOffset, Arrays.asList(label.labels));
        }
        
        @Deprecated
        private int readArg(ArgType type) {
            return switch(type) {
                case U8 -> move();
                case S16 -> valueOfNBytes(Short.BYTES);
                case VMIDI -> valueOfNBytes(Integer.BYTES);
                case VARIABLE -> -1;
                case RANDOM -> {
                    int min = valueOfNBytes(Short.BYTES);
                    int max = valueOfNBytes(Short.BYTES);
                    
                    int rand = new Random().nextInt(0x0000, 0xffff) * ((max - min) + 1);
                    rand >>= 16;
                    rand += min;
                    yield rand;
                }
                default -> throw new IllegalStateException("Unknown arg type!");
            };
        }
        
        private int readVarLen() {
            int t = 0;
            while(true) {
                int c = move();
                t = (t << 7) | (c & 0x7f);
                if ((c & NOTE_MASK) == 0) break;
            } 
            return t;
        }
        
        private String mangleName(int address) {
            return "symb_0x" + Integer.toHexString(address);
        }
        
        private DataSection decodeData(int dataBlockOffset, int labelBlockOffset, List<Label> labels) {
            final String signature = fetchSignature();
            if (!signature.equals(DATA))
                throw new IllegalStateException("Expected DATA section at offset 0x" + Integer.toBinaryString(dataBlockOffset));
            
            Collections.sort(labels);
            final Iterator<Label> iter = labels.iterator();
            final int size = valueOfNBytes(4);
            final int baseOffset = valueOfNBytes(4);
            
            List<InstructionData> instructions = new ArrayList<>();
            
            boolean useArgType = false;
            ArgType argType = null;
            ArgType argType2 = ArgType.NONE;
            // Start data parsing process.
            // Having experience in building parsers for langugages
            // but never build one for a binary file.
            InstructionData data = new InstructionData();
            List<String> seq = new ArrayList<>();
            for (;;) {
                if (!iter.hasNext())
                    break;
                final Label label = iter.next();
                final int offset = label.dataOffset;
                this.pointer = offset;
                
                boolean process = true;
                
                while(process) {
                	byte b = move();
                    // Everything from 0x00 - 0x79 is a note value.
                    if ((b & NOTE_MASK) == 0) {
                        byte note = b;  // The note's opcode. | u8
                        int velo = move(); // | u8
                        int length = readVarLen(); // | s32
                    
                        //seq.add(String.format("%s %d, %d", Note.decode(note), velo, length));
                        seq.add(Note.decode(note));
                        seq.add(String.valueOf(velo));
                        seq.add(String.valueOf(length));
                        
                        //System.out.println(String.format("%s %d, %d", Note.decode(note), velo, length));
                        //data.add(String.format("%d %d %d", note, velo, length));
                    }
                    
                    switch(b) {
                        case MML_WAIT -> {
                            byte opcode = b;
                            int value = readVarLen();
                            
                            seq.add(decodeMMLInstruction(opcode));
                            seq.add(String.valueOf(value));
                            //seq.add(String.format("%s %d", decodeMMLInstruction(opcode), value));
                            //System.out.println(String.format("%s %d", decodeMMLInstruction(opcode), value));
                        }
                        
                        case MML_PRG -> {
                            byte opcode = b;
                            int value = move();
                           
                            seq.add(decodeMMLInstruction(opcode));
                            seq.add(String.valueOf(value));
                            //System.out.println(String.format("%s %d", decodeMMLInstruction(opcode), value));
                            //seq.add(String.format("%s %d", decodeMMLInstruction(opcode), value));
                        }
                        
                        case MML_OPEN_TRACK -> {
                            byte opcode = b;
                            int trk = readB(8);
                            int address = readB(24) + offset;
                            
                            seq.add(decodeMMLInstruction(opcode));
                            seq.add(String.valueOf(trk));
                            seq.add(String.valueOf(address));
                            //System.out.println(String.format("%s %d, %s", decodeMMLInstruction(opcode), trk, mangleName(address)));
                            //seq.add(String.format("%s %d, %s", decodeMMLInstruction(opcode), trk, mangleName(address)));
                        }
                        
                        // Credits go also to RedStoneMatt helping me to figure 
                        // out how this works exactly.
                        case MML_JUMP -> {
                            int value = readB(24);
                            int address = value + offset;
                            String s = "";
                            if (address > this.pointer)
                                s = " ; forward jump by " + value + " bytes relative to the start offset of this sequence";
                            else s = " ; backwards jump by " + value + " bytes relative to the start offset of this sequence";
                            
                            //System.out.println("jump " + mangleName(value) + s);
                            seq.add("jump _" + mangleName(value) + s);
                            seq.add(value, "_"+ mangleName(value) + ":");
                            process = false;
                        }
                        //
                        
                        case MML_CALL -> {
                        	int address = offset + readB(24);
                        	seq.add("call _" + mangleName(address) + ":");
                        }
                        
                        // TODO - Bullshit infix
                        case MML_RANDOM -> {
                        	System.out.println("infix random _r");
                        }
                        
                        case MML_VARIABLE -> {
                            System.out.println("infix variable _v");
                        }
                        
                        case MML_IF -> {
                            System.out.println("infix variable _if");
                        }
                        
                        case MML_TIME -> {
                            System.out.println("infix variable _t");
                        }
                        
                        case MML_TIME_RANDOM -> {
                            System.out.println("infix variable _tr");
                        }
                        
                        case MML_TIME_VARIABLE -> {
                            System.out.println("infix variable _tv");
                        }
                        // TODO - Bullshit infix
                        
                        case MML_ALLOC_TRACK -> {
                            byte opcode = b;
                            readB(16);
                            
                            //System.out.println(decodeMMLInstruction(opcode));
                            seq.add(decodeMMLInstruction(opcode));
                        }
                        
                        case MML_FIN -> {
                            //System.out.println("fin");
                            //System.out.println("-----------------------------");
                        	seq.add(decodeMMLInstruction(b));
                            process = false;
                        }
                    }
                    
                }
                
                //for (String s : seq) {
                //    System.out.println(s);
                //}
                //System.out.println(seq);
                
            
            }
            
            System.out.println("---------------------");
            Iterator<String> itr = seq.iterator();
            for (Label label : labels) {
                System.out.println(label.name + "\n");
                String curr = itr.next();
                while(!curr.equals("fin") && !curr.startsWith("jump") && !curr.equals("ret")) {
                    StringBuilder sb = new StringBuilder(curr + " ");
                    while((curr = itr.next()).matches("-?[0-9]+") ) {
                        sb.append(curr).append(' ');
                    }
                    System.out.println(sb);
                }
                System.out.println(curr);
                System.out.println("---------------------------------");
            }
            
            
            
            return null;
        }
        
        /**
         * Decodes the {@code LABL} section of this BRSEQ file
         * and returns the information wrapped in a {@link LabelSection}.
         * 
         * @param labelBlockOffset Offset to the label section.
         * @param dataBlockOffset Offset to the data section.
         * @return the decoded section.
         */
        private LabelSection decodeLabels(int labelBlockOffset, int dataBlockOffset) {
            final int INT = Integer.BYTES; //Convenience constant.
            
            // Fetch signature and be sure this is the actual LABL chunk.
            final String signature = fetchSignature();
            if (!signature.equals(LABEL))
                throw new IllegalStateException("Expected LABL section at offset 0x" + Integer.toBinaryString(labelBlockOffset));
            
            // Process the header of the label first.
            final int size = valueOfNBytes(INT);
            final int numberOfLabels = valueOfNBytes(INT);
            
            final Label[] labels = new Label[numberOfLabels];
            final int[] offsets = new int[numberOfLabels];
            int x = 0;
            
            // Now comes a bit of a back and forth play.
            // The first (numberOfLabels * 4) bytes are the
            // offsets to each inidividual label entry in the label block.
            // So we get them first.
            for (int i = 0; i < numberOfLabels; i++) {
                int offset = valueOfNBytes(INT);
                
                offsets[i] = labelBlockOffset + 0x08 + offset; // 0x08 = base offset
            }
            
            // Now for every offset we have, we fetch the label information.
            // The label information contains always offsetToData (4 bytes),
            // lengthOfName (4 bytes) and the name itself (lengthOfName bytes).
            for (int offset : offsets) {
                int offToData = BinaryUtil.toInt(this.data, offset, offset + INT);
                int nameLength = BinaryUtil.toInt(this.data, offset + INT, offset + (INT * 2));

                int startName = offset + (INT * 2);                
                int endName = startName + nameLength;
                String name = new String(BinaryUtil.loadRegion(this.data, startName, endName));
                labels[x++] = new Label(offToData, name, dataBlockOffset);
            }
            
            return new LabelSection(labelBlockOffset, size, labels);
        }
    }
}