package nw4r;

/**
 * Contains all knwon and compatible NW4R versions used in
 * different files, so in other words all used SDK versions
 * for the binary files.
 *
 * @author SMTux
 * @author VNSMB
 * @version 1.1
 */
public final class Version {
    /* This could also be an enum */
    /**
     * Lowest possible NW4R version.
     */
    public static final Version __VERSION__1__0__ = new Version(1, 0);

    /**
     * Version 1.1
     */
    public static final Version __VERSION__1__1__ = new Version(1, 1);

    /**
     * Version 1.2
     */
    public static final Version __VERSION__1__2__ = new Version(1, 2);

    /**
     * Version 1.3
     */
    public static final Version __VERSION__1__3__ = new Version(1, 3);

    /**
     * Version 1.4
     */
    public static final Version __VERSION__1__4__ = new Version(1, 4);

    /**
     * Array containing all versions.
     */
    private static final Version[] VERSIONS = {__VERSION__1__0__, __VERSION__1__1__,
            __VERSION__1__2__, __VERSION__1__3__, __VERSION__1__4__};

    /**
     * The major version.
     */
    private final int major;
   
    /**
     * The minor version.
     */
    private final int minor;
    
    /**
     * The version.
     */
    private short version;

    /**
     * Creates a new supported NW4R version.
     *
     * @param major Major version
     * @param minor Minor version
     */
    private Version(int major, int minor) {
        if (major > 0 && minor >= 0) { 
            this.version = (short) ((major << 8) | minor); 
            this.major = major;
            this.minor = minor;
        }
        else throw new IllegalArgumentException("Tried to use and invalid version");
    }

    /*
     * Get index of element.
     */
    private int indexOf(Version version) {
        for (int i = 0; i < VERSIONS.length; i++) {
            if (VERSIONS[i].equals(version)) return i;
        }
        return -1;
    }

    /**
     * Returns the appropriate version for the provided
     * values.
     * 
     * @param major The major version.
     * @param minor The minor version.
     * @return the version that matches the values.
     * @throws IllegalStateException If the version does not exist.
     */
    public static Version of(int major, int minor) {
        for (Version v : VERSIONS)
            if (v.major == major && v.minor == minor)
                return v;
        throw new IllegalStateException(String.format("The version %d.%d does not exist!", major, minor));
    }
    
    /**
     * @return the version as binary data.
     */
    public byte[] data() {
        return Format.createVersion(this.major, this.minor);
    }
    
    /**
     * @return the next higher version.
     */
    public Version getNextHigherVersion() {
        int index = indexOf(this);
        if (index < (VERSIONS.length - 1)) return VERSIONS[index + 1];
        else return this;
    }

    /**
     * @return the next lower version.
     */
    public Version getNextLowerVersion() {
        int index = indexOf(this);
        if (index > 0) return VERSIONS[index - 1];
        else return this;
    }

    /**
     * @return this version as an actual {@code short} value.
     */
    public short getVersion() {
        return this.version;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(this.version);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && getClass().equals(o.getClass())) {
            Version other = (Version) o;
            return other.version == this.version;
        }
        return false;
    }

    @Override
    public String toString() {
        return "0x0" + Integer.toHexString(this.version);
    }
}