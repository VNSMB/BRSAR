package nw4r.sound.midi;

enum Note {

    //-1
    CNM1,
    CSM1,
    DNM1,
    DSM1,
    ENM1,
    FNM1,
    FSM1,
    GNM1,
    GSM1,
    ANM1,
    ASM1,
    BNM1,

    //0
    CN0,
    CS0,
    DN0,
    DS0,
    EN0,
    FN0,
    FS0,
    GN0,
    GS0,
    AN0,
    AS0,
    BN0,

    //1
    CN1,
    CS1,
    DN1,
    DS1,
    EN1,
    FN1,
    FS1,
    GN1,
    GS1,
    AN1,
    AS1,
    BN1,

    //2
    CN2,
    CS2,
    DN2,
    DS2,
    EN2,
    FN2,
    FS2,
    GN2,
    GS2,
    AN2,
    AS2,
    BN2,

    //3
    CN3,
    CS3,
    DN3,
    DS3,
    EN3,
    FN3,
    FS3,
    GN3,
    GS3,
    AN3,
    AS3,
    BN3,

    //4
    CN4,
    CS4,
    DN4,
    DS4,
    EN4,
    FN4,
    FS4,
    GN4,
    GS4,
    AN4,
    AS4,
    BN4,

    //5
    CN5,
    CS5,
    DN5,
    DS5,
    EN5,
    FN5,
    FS5,
    GN5,
    GS5,
    AN5,
    AS5,
    BN5,

    //6
    CN6,
    CS6,
    DN6,
    DS6,
    EN6,
    FN6,
    FS6,
    GN6,
    GS6,
    AN6,
    AS6,
    BN6,

    //7
    CN7,
    CS7,
    DN7,
    DS7,
    EN7,
    FN7,
    FS7,
    GN7,
    GS7,
    AN7,
    AS7,
    BN7,

    //8
    CN8,
    CS8,
    DN8,
    DS8,
    EN8,
    FN8,
    FS8,
    GN8,
    GS8,
    AN8,
    AS8,
    BN8,

    //9
    CN9,
    CS9,
    DN9,
    DS9,
    EN9,
    FN9,
    FS9,
    GN9;

    public static String decode(byte opcode) {
    	for (Note n : values())
            if (n.ordinal() == opcode)
                return n.name().toLowerCase();
    	throw new IllegalStateException("The opcode " + opcode + " does not correspond to an existing note!");
    }

    public static Note encode(String note) {
        for (Note n : values())
            if (n.name().equalsIgnoreCase(note))
                return n;
        throw new IllegalStateException("The note " + note + " does not exist!");
    }
}