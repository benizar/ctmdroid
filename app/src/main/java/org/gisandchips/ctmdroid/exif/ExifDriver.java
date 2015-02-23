package org.gisandchips.ctmdroid.exif;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//import android.util.Log;
import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Driver for reading/writting EXIF meta data to JPEG images. It tries to
 * conform Exif Version 2.2. For more start_screen see http://exif.org/ JPEG data from
 * the (very brief) point of view of the driver looks like this:
 * 2B SOI - Start Of Image FF D8
 * 2B APP1 marker FF E1 2B APP1 size (includes itself)
 * 6B EXIF header 45 78 69 66 00 00
 * 6B TIFF header 49 49 2A 00 08 00 00 00 (Intel endian)
 * or 4D 4D 00 2A 00 00 00 08 (Motorola endian)
 * Following EXIF data are encoded with endian declared above
 *
 * IFD0 - main IFD directory. It contains tags, which holds offsets of IFDExif
 * and IFDGPS subdirectories. It's last 4B holds offset of IFD1
 * IFD0-related data
 *
 * IFDExif - Exif subdirectory
 * IFDExif-related data
 *
 * IFDInteroperability - Interoperability subdirectory
 * IFDInteroperability-related data
 *
 * IFDGPS - GPS subdirectory
 * IFDGPS-related data
 *
 * IFD1 - Thumbnail information directory - it's offset is in the last 4B of
 * IFD0
 * IFD1-related data
 * * * End of area this driver modifies
 * 2B SOI - Start Of Image (Thumbnail actually) FF D8
 * Thumbnail data
 * 2B EOI - End Of Image FF D9
 * * * End of area this driver works with ... "Not-interesting" stuff follows
 *
 * IFD (sub)directory structure is always the following:
 * 2B count of entries
 * n of entries - 12B per entry (2B tag, 2B format, 4B number of values, 4B
 * values or offset to them).
 * 4B "Next" IFD - is used only in IFD0 for reference to IFD1 in other IFD's 0.
 *
 * @author kocian
 */
public class ExifDriver {
    // Private constants

    private final String LOGTAG = getClass().getName();
    // Datatypes
    private final int FORMAT_UNSIGNED_BYTE = 0x01;
    private final int FORMAT_ASCII_STRINGS = 0x02;
    private final int FORMAT_UNSIGNED_SHORT = 0x03;
    private final int FORMAT_UNSIGNED_LONG = 0x04;
    private final int FORMAT_UNSIGNED_RATIONAL = 0x05;
    private final int FORMAT_SIGNED_BYTE = 0x06;
    private final int FORMAT_UNDEFINED = 0x07;
    private final int FORMAT_SIGNED_SHORT = 0x08;
    private final int FORMAT_SIGNED_LONG = 0x09;
    private final int FORMAT_SIGNED_RATIONAL = 0x0a;
    private final int TAG_EXIF_POINTER = 0x8769;
    private final int TAG_GPS_POINTER = 0x8825;
    private final int TAG_INTEROPERABILITY_POINTER = 0xa005;
    // Public constants
    // IFD0-related tags, some of them are presented in IFD1 too
    public static final int TAG_IMAGE_WIDTH = 0x0100;
    public static final int TAG_IMAGE_HEIGHT = 0x0101;
    public static final int TAG_BITS_PER_SAMPLE = 0x0102;
    public static final int TAG_COMPRESSION = 0x0103;
    public static final int TAG_PHOTOMETRIC_INTERPRETATION = 0x0106;
    public static final int TAG_ORIENTATION = 0x0112;
    public static final int TAG_SAMPLES_PER_PIXEL = 0x0115;
    public static final int TAG_PLANAR_CONFIGURATION = 0x011c;
    public static final int TAG_YCBCR_SUBSAMPLING = 0x0212;
    public static final int TAG_YCBCRPOSITIONING = 0x0213;
    public static final int TAG_XRESOLUTION = 0x011a;
    public static final int TAG_YRESOLUTION = 0x011b;
    public static final int TAG_RESOLUTION_UNIT = 0x0128;
    public static final int TAG_STRIP_OFFSETS = 0x0111;
    public static final int TAG_ROWS_PER_STRIP = 0x0116;
    public static final int TAG_STRIP_BYTECOUNTS = 0x0117;
    public static final int TAG_JPEG_INTERCHANGE_FORMAT = 0x0201;
    public static final int TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = 0x0202;
    public static final int TAG_TRANSFER_FUNCTION = 0x012d;
    public static final int TAG_WHITE_POINT = 0x013e;
    public static final int TAG_PRIMARY_CHROMATICITIES = 0x013f;
    public static final int TAG_YCBCR_COEFICIENTS = 0x0211;
    public static final int TAG_REFERENCE_BLACK_WHITE = 0x0214;
    public static final int TAG_DATETIME = 0x0132;
    public static final int TAG_IMAGE_DESCRIPTION = 0x010e;
    public static final int TAG_MAKE = 0x010f;
    public static final int TAG_MODEL = 0x0110;
    public static final int TAG_SOFTWARE = 0x0131;
    public static final int TAG_ARTIST = 0x013b;
    public static final int TAG_COPYRIGHT = 0x8298;
    // IFD Exif tags
    public static final int TAG_EXIF_VERSION = 0x9000;
    public static final int TAG_FLASHPIX_VERSION = 0xa000;
    public static final int TAG_COLOR_SPACE = 0xa001;
    public static final int TAG_COMPONENT_CONFIGURATION = 0x9101;
    public static final int TAG_COMPRESSED_BITS_PER_PIXEL = 0x9102;
    public static final int TAG_PIXEL_X_DIMENSION = 0xa002;
    public static final int TAG_PIXEL_Y_DIMENSION = 0xa003;
    public static final int TAG_MARKER_NOTE = 0x927c;
    public static final int TAG_USER_COMMENT = 0x9286;
    public static final int TAG_RELATED_SOUND_FILE = 0xa004;
    public static final int TAG_DATETIME_ORIGINAL = 0x9003;
    public static final int TAG_DATETIME_DIGITIZED = 0x9004;
    public static final int TAG_SUB_SEC_TIME = 0x9290;
    public static final int TAG_SUB_SEC_TIME_ORIGINAL = 0x9291;
    public static final int TAG_SUB_SEC_TIME_DIGITIZED = 0x9292;
    public static final int TAG_IMAGE_UNIQUE_ID = 0xa420;
    public static final int TAG_EXPOSURE_TIME = 0x829a;
    public static final int TAG_FNUMBER = 0x829d;
    public static final int TAG_EXPOSURE_PROGRAM = 0x8822;
    public static final int TAG_SPECTRAL_SENSITIVITY = 0x8824;
    public static final int TAG_ISO_SPEED_RATINGS = 0x8827;
    public static final int TAG_OECF = 0x8828;
    public static final int TAG_SHUTTER_SPEED_VALUE = 0x9201;
    public static final int TAG_APERTURE_VALUE = 0x9202;
    public static final int TAG_BRIGHTNESS_VALUE = 0x9203;
    public static final int TAG_EXPOSURE_BIAS_VALUE = 0x9204;
    public static final int TAG_MAX_APERTURE_VALUE = 0x9205;
    public static final int TAG_SUBJECT_DISTANCE = 0x9206;
    public static final int TAG_METERING_MODE = 0x9207;
    public static final int TAG_LIGHT_SOURCE = 0x9208;
    public static final int TAG_FLASH = 0x9209;
    public static final int TAG_FOCAL_LENGTH = 0x920a;
    public static final int TAG_SUBJECT_AREA = 0x9214;
    public static final int TAG_FLASH_ENERGY = 0xa20b;
    public static final int TAG_SPATIAL_FREQUENCY_RESPONSE = 0xa20c;
    public static final int TAG_FOCAL_PLANE_X_RESOLUTION = 0xa20e;
    public static final int TAG_FOCAL_PLANE_Y_RESOLUTION = 0xa20f;
    public static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = 0xa210;
    public static final int TAG_SUBJECT_LOCATION = 0xA214;
    public static final int TAG_EXPOSURE_INDEX = 0xA215;
    public static final int TAG_SENSING_METHOD = 0xA217;
    public static final int TAG_FILE_SOURCE = 0xA300;
    public static final int TAG_SCENE_TYPE = 0xA301;
    public static final int TAG_CFA_PATTERN = 0xA302;
    public static final int TAG_CUSTOM_RENDERED = 0xA401;
    public static final int TAG_EXPOSURE_MODE = 0xA402;
    public static final int TAG_WHITE_BALANCE = 0xA403;
    public static final int TAG_DIGITAL_ZOOM_RATIO = 0xA404;
    public static final int TAG_FOCAL_LENGTH_35MM_FILM = 0xA405;
    public static final int TAG_SCENE_CAPTURE_TYPE = 0xA406;
    public static final int TAG_GAIN_CONTROL = 0xA407;
    public static final int TAG_CONTRAST = 0xA408;
    public static final int TAG_SATURATION = 0xA409;
    public static final int TAG_SHARPNESS = 0xA40A;
    public static final int TAG_DEVICE_SETTING_DESCRIPTION = 0xA40B;
    public static final int TAG_SUBJECT_DISTANCE_RANGE = 0xA40C;
    // IFD GPS tags
    public static final int TAG_GPS_VERSION_ID = 0x0;
    public static final int TAG_GPS_LATITUDE_REF = 0x1;
    public static final int TAG_GPS_LATITUDE = 0x2;
    public static final int TAG_GPS_LONGITUDE_REF = 0x3;
    public static final int TAG_GPS_LONGITUDE = 0x4;
    public static final int TAG_GPS_ALTITUDE_REF = 0x5;
    public static final int TAG_GPS_ALTITUDE = 0x6;
    public static final int TAG_GPS_TIME_STAMP = 0x7;
    public static final int TAG_GPS_SATELITES = 0x8;
    public static final int TAG_GPS_STATUS = 0x9;
    public static final int TAG_GPS_MEASURE_MODE = 0xa;
    public static final int TAG_GPS_DOP = 0xb;
    public static final int TAG_GPS_SPEED_REF = 0xc;
    public static final int TAG_GPS_SPEED = 0xd;
    public static final int TAG_GPS_TRACK_REF = 0xe;
    public static final int TAG_GPS_TRACK = 0xf;
    public static final int TAG_GPS_SLMG_DIRECTION_REF = 0x10;
    public static final int TAG_GPS_SLMG_DIRECTION = 0x11;
    public static final int TAG_GPS_MAP_DATUM = 0x12;
    public static final int TAG_GPS_DEST_LATITUDE_REF = 0x13;
    public static final int TAG_GPS_DEST_LATITUDE = 0x14;
    public static final int TAG_GPS_DEST_LONGITUDE_REF = 0x15;
    public static final int TAG_GPS_DEST_LONGITUDE = 0x16;
    public static final int TAG_GPS_DEST_BEARING_REF = 0x17;
    public static final int TAG_GPS_DEST_BEARING = 0x18;
    public static final int TAG_GPS_DEST_DISTANCE_REF = 0x19;
    public static final int TAG_GPS_DEST_DISTANCE = 0x1a;
    public static final int TAG_GPS_PROCESSING_METHOD = 0x1b;
    public static final int TAG_GPS_AREA_INFORMATION = 0x1c;
    public static final int TAG_GPS_DATE_STAMP = 0x1d;
    public static final int TAG_GPS_DIFFERENTIAL = 0x1e;
    // IFD Interoperability tags
    public static final int TAG_INTEROPERABILITY_1 = 0x1;
    public static final int TAG_INTEROPERABILITY_2 = 0x2;
    // Others
    private final HashMap<Integer, Integer> COMP_WIDTHS = new HashMap<Integer, Integer>();
    // Length of Exif data size declaration - 2B
    private final int LENGTH_EXIF_SIZE_DECL = 2;
    private final int LENGTH_APP1_EXIF_HEADER = 10;// APP1Marker+EXIF size + EXIF
    // header
    private String sourceFile;
    private byte[] origEXIFdata;
    //JPEG's Start of image
    private final byte[] SOI = new byte[] { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] APP1Marker = new byte[] { (byte) 0xFF, (byte) 0xE1 };
    private final byte[] EXIFHeader = new byte[] { 'E', 'x', 'i', 'f', '\0', '\0' };
    // Note it's a "Intel fashion one" we use it's values only during saving
    private final byte[] TIFFHeader = new byte[] { 'I', 'I', (byte) 0x2A, '\0',
            (byte) 0x08, '\0', '\0', '\0' };
    // Specification requires this tag to have this value
    private final Byte[] GPSVersionID = new Byte[] { 2, 2, 0, 0 };
    private int origAPP1MarkerOffset = 2;
    private int origThumbnailOffset = -1;
    private int origThumbnailLength = 0;
    private final int ALIGN_II = 0x4949; // Intel endian
    private final int ALIGN_MM = 0x4D4D; // Motorola endian
    private int originalAlign; // endian
    // IFD directories are represented as simple tag-value hashes
    private HashMap<Integer, ExifValue> ifd0 = new HashMap<Integer, ExifValue>();
    private HashMap<Integer, ExifValue> ifdExif = new HashMap<Integer, ExifValue>();
    private HashMap<Integer, ExifValue> ifdGps = new HashMap<Integer, ExifValue>();
    private HashMap<Integer, ExifValue> ifd1 = new HashMap<Integer, ExifValue>();
    private HashMap<Integer, ExifValue> ifdIOper = new HashMap<Integer, ExifValue>();
    private boolean readyToWork = false;

    /**
     * Get instance of driver for given image file. If everything works well (file
     * can be read, it is Exif file .. etc.), this method reurns the driver
     * object. In opposite case it reurns null.
     *
     * @param _file
     *          image file
     * @return ExifDriver or null in case, that anyhing went wrong
     */
    public static ExifDriver getInstance(String _file) {
        ExifDriver result = new ExifDriver(_file);
        if (result.readyToWork()) {
            return result;
        } else {
            return null;
        }
    }

    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Constructor. Do the basics like find Exif data and prepare them to array.
     * Then call parser to read information from the array. Variable readyToWork
     * is set during consturction. Constructor is private and can be called only
     * through getInstance method, which desides, if user can obtain he driver
     * object or null.
     *
     * @param _file
     *          Path of file to work with
     */
    private ExifDriver(String _file) {
        sourceFile = _file;
        readyToWork = true; // Hope for the best;
        initHashes();
        byte[] findBuffer = new byte[100];
        FileInputStream fis = null;
        FileChannel channel = null;
        origAPP1MarkerOffset = -1;
        int exifDataSize = -1;
        int read;
        try {
            fis = new FileInputStream(sourceFile);
            channel = fis.getChannel();
            read = fis.read(findBuffer);
            // Make sure, that image is JPG
            if (findBuffer[0] == SOI[0] && findBuffer[1] == SOI[1]) {
                // Make sure, that image is the Exif one. Find APP1 marker and
                // also remember it's offset from start of file
                int findOffset = 0;
                while (origAPP1MarkerOffset < SOI.length
                        && read > LENGTH_APP1_EXIF_HEADER) {
                    for (int i = 0; i - 1 < read - LENGTH_APP1_EXIF_HEADER; i++) {
                        if (findBuffer[i] == APP1Marker[0]
                                && findBuffer[i + 1] == APP1Marker[1]
                                && findBuffer[i + 4] == EXIFHeader[0]
                                && findBuffer[i + 5] == EXIFHeader[1]
                                && findBuffer[i + 6] == EXIFHeader[2]
                                && findBuffer[i + 7] == EXIFHeader[3]
                                && findBuffer[i + 8] == EXIFHeader[4]
                                && findBuffer[i + 9] == EXIFHeader[5]) {
                            origAPP1MarkerOffset = i + findOffset; // APP1marker and EXIF
                            // header found
                            exifDataSize = (findBuffer[i + 2] & 0xFF) << 8;
                            exifDataSize += findBuffer[i + 3] & 0xFF;
                            break;
                        }
                    }
                    // shift the finding window
                    findOffset += read - LENGTH_APP1_EXIF_HEADER;
                    channel.position(findOffset);
                    read = fis.read(findBuffer);
                }
                if (origAPP1MarkerOffset >= SOI.length) {
                    origEXIFdata = new byte[exifDataSize
                            - (LENGTH_EXIF_SIZE_DECL + EXIFHeader.length)];
                    System.out.println("APP1 data size: "
                            + Integer.toHexString(exifDataSize));
                    // data will start with TIFF header
                    channel.position(origAPP1MarkerOffset + LENGTH_APP1_EXIF_HEADER);
                    fis.read(origEXIFdata);
                    readExifData(origEXIFdata);
                } else {
                    // Log.v(LOGTAG, "APP1 marker not found, offset is "
                    // + origAPP1MarkerOffset);
                    readyToWork = false;
                }
            } else {
                // Log.v(LOGTAG, "Not a JPG image");
                readyToWork = false;
            }
            channel.close();
            fis.close();
        } catch (EOFException e) {
            e.printStackTrace();
            readyToWork = false;
        } catch (Exception e) {
            e.printStackTrace();
            readyToWork = false;
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ExifDriver.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Tells the caller if the driver has been initialized corectly and we can
     * work with it. It is used by getInstance method. In case, that readyToWork
     * returns false, getInstance returns null
     *
     * @return true if driver has been initialized correctly
     */
    private boolean readyToWork() {
        return readyToWork;
    }

    /**
     * Init hash of datatypes and their lengths
     */
    private void initHashes() {
        // Convenience hash for finding component bit width
        COMP_WIDTHS.put(FORMAT_UNSIGNED_BYTE, 1);
        COMP_WIDTHS.put(FORMAT_ASCII_STRINGS, 1);
        COMP_WIDTHS.put(FORMAT_UNSIGNED_SHORT, 2);
        COMP_WIDTHS.put(FORMAT_UNSIGNED_LONG, 4);
        COMP_WIDTHS.put(FORMAT_UNSIGNED_RATIONAL, 8);
        COMP_WIDTHS.put(FORMAT_SIGNED_BYTE, 1);
        COMP_WIDTHS.put(FORMAT_UNDEFINED, 1);
        COMP_WIDTHS.put(FORMAT_SIGNED_SHORT, 2);
        COMP_WIDTHS.put(FORMAT_SIGNED_LONG, 4);
        COMP_WIDTHS.put(FORMAT_SIGNED_RATIONAL, 8);
    }

    /**
     * Method reads sequentially the given source data and fills the structures
     * with information.
     *
     * @param _data
     */
    private void readExifData(byte[] _data) {
        originalAlign = (_data[0] << 8) + (_data[1] & 0xFF);
        int ifdStart = readUInt(_data, 4, 4); // See the TIFF header
        ifdStart = readIfd(ifd0, _data, ifdStart);
        // Was there any IFD1 reference?
        if (ifdStart > 0) {
            readIfd(ifd1, _data, ifdStart);
            // Remember thumbnail start_screen
            if (ifd1.containsKey(TAG_JPEG_INTERCHANGE_FORMAT)) {
                origThumbnailOffset = (Integer) ifd1.get(TAG_JPEG_INTERCHANGE_FORMAT)
                        .getValues()[0];
                origThumbnailLength = (Integer) ifd1.get(
                        TAG_JPEG_INTERCHANGE_FORMAT_LENGTH).getValues()[0];
        /*
         * Log.v(
         * LOGTAG,
         * "Thumbnail offs/length: "
         * + Integer.toHexString(origThumbnailOffset) + "/"
         * + Integer.toHexString(origThumbnailLength));
         */
            } else {
                // Log.e(LOGTAG, "No thumbnail address");
            }
        }
        // Is there a IFDExif reference?
        if (ifd0.get(TAG_EXIF_POINTER) != null) {
            ifdStart = ((ValueLongs) ifd0.get(TAG_EXIF_POINTER)).getValues()[0];
            readIfd(ifdExif, _data, ifdStart);
            if (ifdExif.get(TAG_INTEROPERABILITY_POINTER) != null) {
                ifdStart = ((ValueLongs) ifdExif.get(TAG_INTEROPERABILITY_POINTER))
                        .getValues()[0];
                readIfd(ifdIOper, _data, ifdStart);
            }
        }
        // Is there a IFDGPS reference?
        if (ifd0.get(TAG_GPS_POINTER) != null) {
            ifdStart = ((ValueLongs) ifd0.get(TAG_GPS_POINTER)).getValues()[0];
            readIfd(ifdGps, _data, ifdStart);
        }
    }

    /**
     * Read information from one IFD directory
     *
     * @param _ifd
     *          Structure to store data in
     * @param _data
     *          Source data byte array
     * @param _start
     *          Offset, where the ifd directory starts
     * @return Long value from the last 4 bytes of the directory, which, if
     *         non-zero, refers to next ifd. As a matter of fact, the non-zero is
     *         expected
     *         only in IFD0.
     */
    private int readIfd(HashMap<Integer, ExifValue> _ifd, byte[] _data, int _start) {
        int entriesNumber = readUInt(_data, _start, 2);
        // Log.v(LOGTAG, entriesNumber + " entries found in directory begining at "
        // + Integer.toHexString(_start + 12));
        for (int i = 0; i < entriesNumber; i++) {
            int entryStart = _start + 2 + i * 12;// 2B is size, 12B is the
            // length of each item
            // Parse item structure (2B-tag, 2B-datatype, 4B number of
            // components, 4B value(or offset to value))
            int tag = readUInt(_data, entryStart, 2);
            int datatype = readUInt(_data, entryStart + 2, 2);
            int components = readUInt(_data, entryStart + 4, 4);
            // If the totalLength is >4 it does not fit in directory
            int totalLength = components * (Integer) COMP_WIDTHS.get(datatype);
            // Offset right in directory
            int offset = entryStart + 8;
            if (totalLength > 4) {
                // Offset in data area
                offset = readUInt(_data, offset, 4);
            }
            // if (tag == 0xa005 || tag == TAG_EXIF_POINTER || tag ==
            // TAG_GPS_POINTER) {
            // System.out.println("Tag: " + Integer.toHexString(tag));
            // System.out.println("Datatype: " + Integer.toHexString(datatype));
            // System.out.println("Components: " +
            // Integer.toHexString(components));
            // System.out.println("Offset: " + Integer.toHexString(offset));
            // }
            switch (datatype) {
                case FORMAT_UNSIGNED_BYTE:
                    ValueUBytes uByteValue = new ValueUBytes();
                    uByteValue.setValue(uByteValue.readUnsigned(_data, offset, components));
                    _ifd.put(tag, uByteValue);
                    break;
                case FORMAT_ASCII_STRINGS:
                    ValueAsciiStrings baValue = new ValueAsciiStrings();
                    baValue.setValue(baValue.readBytes(_data, offset, components));
                    _ifd.put(tag, baValue);
                    break;
                case FORMAT_UNDEFINED:
                    ValueUndefined unValue = new ValueUndefined();
                    unValue.setValue(unValue.readBytes(_data, offset, components));
                    _ifd.put(tag, unValue);
                    break;
                case FORMAT_UNSIGNED_SHORT:
                    ValueUShorts uShortValue = new ValueUShorts();
                    uShortValue.setValue(uShortValue
                            .readUnsigned(_data, offset, components));
                    _ifd.put(tag, uShortValue);
                    break;
                case FORMAT_UNSIGNED_LONG:
                    ValueLongs uLongValue = new ValueLongs();
                    uLongValue.setValue(uLongValue.readUnsigned(_data, offset, components));
                    _ifd.put(tag, uLongValue);
                    break;
                case FORMAT_UNSIGNED_RATIONAL:
                    ValueURationals uRatValue = new ValueURationals();
                    uRatValue.setValue(uRatValue.readUnsigned(_data, offset, components));
                    _ifd.put(tag, uRatValue);
                    break;
                case FORMAT_SIGNED_BYTE:
                    ValueSBytes sByteValue = new ValueSBytes();
                    sByteValue.setValue(sByteValue.readSigned(_data, offset, components));
                    _ifd.put(tag, sByteValue);
                    break;
                case FORMAT_SIGNED_SHORT:
                    ValueSShorts sShortValue = new ValueSShorts();
                    sShortValue.setValue(sShortValue.readSigned(_data, offset, components));
                    _ifd.put(tag, sShortValue);
                    break;
                case FORMAT_SIGNED_LONG:
                    ValueSLongs sLongValue = new ValueSLongs();
                    sLongValue.setValue(sLongValue.readSigned(_data, offset, components));
                    _ifd.put(tag, sLongValue);
                    break;
                case FORMAT_SIGNED_RATIONAL:
                    ValueSRationals sRatValue = new ValueSRationals();
                    sRatValue.setValue(sRatValue.readSigned(_data, offset, components));
                    _ifd.put(tag, sRatValue);
                    break;
            }
        }
        // Return the long value represented by 4B after the last entry
        return readUInt(_data, _start + entriesNumber * 12 + 2, 4);
    }

    /**
     * Read signed int from source byte array. Handles endianes.
     *
     * @param _data
     *          Data array to read from
     * @param _offset
     *          offset, where the value starts
     * @param _bytesNumber
     *          Number of bytes (1,2,4 for byte, short, long)
     * @return Integer value
     */
    private Integer readSInt(byte[] _data, int _offset, int _bytesNumber) {
        int signMask = 1 << (_bytesNumber * 8 - 1);
        int valueMask = 0xFFFFFFFF >>> (4 - _bytesNumber) * 8 + 1;
        int value = readUInt(_data, _offset, _bytesNumber) & valueMask;
        if ((value & signMask) > 0) {
            value = -value;
        }
        return value;
    }

    /**
     * Read unsigned int from source byte array. Handles endianes.
     *
     * @param _data
     *          Data array to read from
     * @param _offset
     *          offset, where the value starts
     * @param _bytesNumber
     *          Number of bytes (1,2,4 for byte, short, long)
     * @return Integer value
     */
    private Integer readUInt(byte[] _data, int _offset, int _bytesNumber) {
        int result = 0;
        int shift = 0;
        switch (originalAlign) {
            case ALIGN_MM:
                shift = _bytesNumber * 8;
                break;
            case ALIGN_II:
                shift = 0;
                break;
        }
        for (int i = _offset; i < _bytesNumber + _offset; i++) {
            switch (originalAlign) {
                case ALIGN_MM:
                    shift -= 8;
                    result += (_data[i] & 0xFF) << shift;
                    break;
                case ALIGN_II:
                    result += (_data[i] & 0xFF) << shift;
                    shift += 8;
                    break;
            }
        }
        return result;
    }

  /*
   * private void printData(ArrayList<Byte> _data) { String text = ""; for (byte
   * value : _data) { text += "|" + Integer.toHexString(value & 0xff); }
   * System.out.println(text); }
   *
   * private void printData(byte[] _data) { String text = ""; for (byte value :
   * _data) { text += "|" + Integer.toHexString(value & 0xff); }
   * System.out.println(text); }
   */
    /**
     * The total space, that the given IFD requires. It is space required by
     * directory itself and it's related data too.
     *
     * @param _ifd
     *          Given IFD
     * @return Required space in bytes
     */
    private int requiredSpace(HashMap<Integer, ExifValue> _ifd) {
        int result = 0;
        Object[] oKeys = _ifd.keySet().toArray();
        for (int i = 0; i < oKeys.length; i++) {
            result += 12;
            ExifValue val = _ifd.get((Integer) oKeys[i]);
            if (val != null) {
                result += val.getExtraSize();
            }
        }
        if (result > 0) {
            result += 6;// 2B number of items, 4B the "next" address
        }
        return result;
    }

    /**
     * Write one "integer" number to output byte array. The method uses Intel
     * endian.
     *
     * @param _data
     *          Output byte array
     * @param _offset
     *          offset, where the number will written
     * @param _value
     *          numeric value of the namber
     * @param _width
     *          number of bytes, the number covers
     */
    private void writeNumber(byte[] _data, int _offset, int _value, int _width) {
        int mask = 0xFF;
        for (int i = 0; i < _width; i++) {
            _data[_offset + i] = (byte) ((_value & (mask << i * 8)) >>> (i * 8));
        }
    }

    /**
     * Write the specified IFD into given output byte array. The method writes to
     * array the directory itself, same as all the "extra" values to
     * directory-related area.
     *
     * @param _data
     *          Output byte array
     * @param _ifd
     *          the directory to write
     * @param _offset
     *          Offset in the byte array, where the data goes
     * @param _nextOffset
     *          Value, which will be written to the "next" area of the
     *          directory. In fact only in case of IFD0 it will be a nonzero.
     */
    private void writeIfd(byte[] _data, HashMap<Integer, ExifValue> _ifd,
                          int _offset, int _nextOffset) {
        Object[] oKeys = _ifd.keySet().toArray();
        if (oKeys.length > 0) {
            int valuesOffset = _offset + 2 + oKeys.length * 12 + 4;
            writeNumber(_data, _offset, oKeys.length, 2);
            int itemOffset = _offset + 2;
            Arrays.sort(oKeys);
            for (int i = 0; i < oKeys.length; i++) {
                Integer key = (Integer) oKeys[i];
                writeNumber(_data, itemOffset, key, 2);
                valuesOffset = _ifd.get(key).write(_data, itemOffset, valuesOffset);
                itemOffset += 12;
            }
            writeNumber(_data, itemOffset, _nextOffset, 4);
        }
    }

    /**
     * Saves new image file with current Exif information. It is quite expensive
     * operation, so it is recomended to call it only at the end of work.
     *
     * @param _name
     *          name of the new file
     */
    public void save(String _name) {
        int startOfIfd0 = TIFFHeader.length;
        int startOfIfdExif = startOfIfd0 + requiredSpace(ifd0);
        int startOfIfdIOper = startOfIfdExif + requiredSpace(ifdExif);
        int startOfIfdGps = startOfIfdIOper + requiredSpace(ifdIOper);
        int startOfIfd1 = startOfIfdGps + requiredSpace(ifdGps);
        int startOfThumbnail = startOfIfd1 + requiredSpace(ifd1);
        int reqSize = startOfThumbnail + origThumbnailLength;
        // Write directory referencies
        if (!ifdExif.isEmpty()) {
            ifd0.put(TAG_EXIF_POINTER, new ValueLongs(startOfIfdExif));
        }
        if (!ifdGps.isEmpty()) {
            ifd0.put(TAG_GPS_POINTER, new ValueLongs(startOfIfdGps));
        }
        if (!ifdIOper.isEmpty()) {
            ifdExif
                    .put(TAG_INTEROPERABILITY_POINTER, new ValueLongs(startOfIfdIOper));
        }
        // Adjust referencies to image data
        if (origThumbnailOffset > 0) {
            ValueLongs val = new ValueLongs(startOfThumbnail);
            ifd1.put(TAG_JPEG_INTERCHANGE_FORMAT, val);
        }
        // Write all headers
        byte[] resultExif = new byte[reqSize];
        byte[] exifHeader = new byte[] { (byte) 0xFF, (byte) 0xE1, 0, 0,
                (byte) 0x45, (byte) 0x78, (byte) 0x69, (byte) 0x66, 0, 0 };
        exifHeader[2] = (byte) (((reqSize + 8) & 0xFF00) >> 8);
        exifHeader[3] = (byte) ((reqSize + 8) & 0xFF);
        // Note, we will always use Intel align
        byte[] tiffHeader = new byte[] { 0x49, 0x49, 0x2A, 0x00, 0x08, 0x00, 0x00,
                0x00 };
        System.arraycopy(tiffHeader, 0, resultExif, 0, tiffHeader.length);
        writeIfd(resultExif, ifd0, startOfIfd0, startOfIfd1);
        writeIfd(resultExif, ifdExif, startOfIfdExif, 0);
        writeIfd(resultExif, ifdIOper, startOfIfdIOper, 0);
        writeIfd(resultExif, ifdGps, startOfIfdGps, 0);
        writeIfd(resultExif, ifd1, startOfIfd1, 0);
        System.arraycopy(origEXIFdata, origThumbnailOffset, resultExif,
                startOfThumbnail, origThumbnailLength);
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(_name);
            fis = new FileInputStream(sourceFile);
            fos.write((byte) 0xFF);
            fos.write((byte) 0xD8);
            fos.write(exifHeader);
            fos.write(resultExif);
            int skipped = 0;
            int imageOffset = origAPP1MarkerOffset + APP1Marker.length
                    + LENGTH_EXIF_SIZE_DECL + EXIFHeader.length + origEXIFdata.length;
            // Log.v(LOGTAG, "Image offset is " + Integer.toHexString(imageOffset));
            while (skipped < imageOffset) {
                int skip = (int) fis.skip(imageOffset - skipped);
                if (skip < 0) {
                    throw (new IOException());
                } else {
                    skipped += skip;
                }
            }
            byte[] buffer = new byte[10240];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fis.close();
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExifDriver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExifDriver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(ExifDriver.class.getName())
                        .log(Level.SEVERE, null, ex);
            }

        }
    }

  /*
   * Private classes representing datatypes
   */
    /**
     * Parent class of all datatypes. It defines basic operations. Some of them
     * are overloaded in derived classes.
     *
     * @param <E>
     */
    public abstract class ExifValue<E> {

        /**
         * Amount of extra space that this value needs. All values store their
         * components right into directory if their total amount is less then 4B. In
         * opposite case they stores to directory only offset, where the data are
         * stored. Extra space is therefore 0 or totalLength of data.
         */
        protected int extraSpace = 0;
        /**
         * Array of components. Scalar types use mostly one component, but many tags
         * are described with more than one component.
         */
        protected E[] components;

        /**
         * Returns array of components. Scalar types use mostly one component, but
         * many tags are described with more than one component.
         *
         * @return Array of components
         */
        public E[] getValues() {
            return components;
        }

        /**
         * Set all components in a one single step. Components therefore have to be
         * prepared in proper array before.
         *
         * @param _values
         */
        public void setValue(E[] _values) {
            components = _values;
            setExtraSize();
        }

        /**
         * Returns size of one single component. For example if Value holds
         * components of type UNSIGNED_BYTE, it returns 1.
         *
         * @return Size of one single component
         */
        public final int getComponentSize() {
            return (Integer) COMP_WIDTHS.get(getDataType());
        }

        /**
         * Total size of components. In case that value holds 4 components of type
         * UNSIGNED_SHORT, it returns 8;
         *
         * @return Total size of components
         */
        public final int getTotalSize() {
            return components.length * getComponentSize();
        }

        protected final void setExtraSize() {
            if (getTotalSize() > 4) {
                extraSpace = getTotalSize();
            } else {
                extraSpace = 0;
            }
        }

        /**
         * Returns of extra space that this value needs. All values store their
         * components right into directory if their total amount is less then 4B. In
         * opposite case they stores to directory only offset, where the data are
         * stored. Extra space is therefore 0 or totalLength of data.
         *
         * @return Extra space that this value uses
         */
        public final int getExtraSize() {
            return extraSpace;
        }

        /**
         * This method implements the special way, in which the value saves it's
         * components.
         *
         * @param _data
         *          - output array
         * @param _offset
         *          - offset where to save the components
         */
        protected abstract void writeValues(byte[] _data, int _offset);

        /**
         * Write this component to specified place in output data.
         *
         * @param _data
         *          Output data array
         * @param _itemOffset
         *          Offset of IFD directory record
         * @param _valuesOffset
         *          Offset of the first free byte in IFD's data
         * @return Offset of the first free byte in IFD's data. In case, that the
         *         value does not use any extra space, return value will equal to
         *         _valuesOffset.
         */
        public final int write(byte[] _data, int _itemOffset, int _valuesOffset) {
            writeNumber(_data, _itemOffset + 2, getDataType(), 2);
            writeNumber(_data, _itemOffset + 4, getValues().length, 4);
            int valueOffset = _itemOffset + 8;
            if (extraSpace > 0) {
                writeNumber(_data, valueOffset, _valuesOffset, 4);
                writeValues(_data, _valuesOffset);
                return _valuesOffset + extraSpace;
            } else {
                writeValues(_data, valueOffset);
                return _valuesOffset;
            }
        }

        /**
         * Returns the datatype of this value
         *
         * @return Datatype
         */
        public abstract int getDataType();
    }

    /**
     * Common abstract superclass for ValueAscii and ValueUndefined, which are
     * treated as simple byte arrays
     */
    public abstract class ValueByteArray extends ExifValue<Byte> {

        /**
         * Read it's value (array of bytes) from given source
         *
         * @param _data
         *          Source array to read from
         * @param _offset
         *          Offset where to start reading
         * @param _count
         *          Amount of bytes to read
         * @return
         */
        public Byte[] readBytes(byte[] _data, int _offset, int _count) {
            byte[] forValue = new byte[_count];
            System.arraycopy(_data, _offset, forValue, 0, _count);
            Byte[] result = new Byte[forValue.length];
            for (int i = 0; i < forValue.length; i++) {
                result[i] = forValue[i];
            }
            return result;
        }

        @Override
        protected void writeValues(byte[] _data, int _offset) {
            for (int i = 0; i < components.length; i++) {
                _data[_offset + i] = (Byte) components[i];
            }
        }
    }

    /**
     * Ascii strings datatype
     */
    public class ValueAsciiStrings extends ValueByteArray {

        public int getDataType() {
            return FORMAT_ASCII_STRINGS;
        }
    }

    /**
     * Undefined datatype
     */
    public class ValueUndefined extends ValueByteArray {

        public int getDataType() {
            return FORMAT_UNDEFINED;
        }
    }

    /**
     * Basic class for scalar number data types. Implements Methods for Reading
     * and writing signed and unsigned values, same as constructor from value.
     */
    public abstract class ValueNumbers extends ExifValue<Integer> {

        public ValueNumbers() {
            super();
        }

        /**
         * Construct from value. Sets the count of components to 1 and it's value to
         * _value.
         *
         * @param _value
         *          Value to set as the only one component.
         */
        public ValueNumbers(int _value) {
            components = new Integer[1];
            components[0] = _value;
            extraSpace = 0;
        }

        /**
         * Read unsigned values from source data. It uses own information about
         * component size to determine, how much bytes read for one component.
         *
         * @param _data
         *          Source byte array
         * @param _offset
         *          Where to start the reading
         * @param _count
         *          How much components to read
         * @return Array of read components
         */
        public Integer[] readUnsigned(byte[] _data, int _offset, int _count) {
            Integer[] uValues = new Integer[_count];
            for (int v = 0; v < _count; v++) {
                uValues[v] = readUInt(_data, _offset + getComponentSize() * v,
                        getComponentSize());
            }
            return uValues;
        }

        /**
         * Read signed values from source data. It uses own information about
         * component size to determine, how much bytes read for one component.
         *
         * @param _data
         *          Source byte array
         * @param _offset
         *          Where to start the reading
         * @param _count
         *          How much components to read
         * @return Array of read components
         */
        public Integer[] readSigned(byte[] _data, int _offset, int _count) {
            Integer[] uValues = new Integer[_count];
            for (int v = 0; v < _count; v++) {
                uValues[v] = readSInt(_data, _offset + getComponentSize() * v,
                        getComponentSize());
            }
            return uValues;
        }

        @Override
        protected void writeValues(byte[] _data, int _offset) {
            for (int i = 0; i < components.length; i++) {
                writeNumber(_data, _offset + getComponentSize() * i, components[i],
                        getComponentSize());
            }
        }
    }

    /**
     * Unsigned byte value. Only overloads the function which returns it's data
     * type
     */
    public class ValueUBytes extends ValueNumbers {

        public int getDataType() {
            return FORMAT_UNSIGNED_BYTE;
        }
    }

    /**
     * Signed byte value. Only overloads the function which returns it's data type
     */
    public class ValueSBytes extends ValueNumbers {

        public int getDataType() {
            return FORMAT_SIGNED_BYTE;
        }
    }

    /**
     * Unsigned shorts (or pants ;-) . Only overloads the function which returns
     * it's data type
     */
    public class ValueUShorts extends ValueNumbers {

        public int getDataType() {
            return FORMAT_UNSIGNED_SHORT;
        }
    }

    /**
     * Signed shorts (or pants ;-). Only overloads the function which returns it's
     * data type
     */
    public class ValueSShorts extends ValueNumbers {

        public int getDataType() {
            return FORMAT_SIGNED_SHORT;
        }
    }

    /**
     * Unsigned longs. Only overloads the function which returns it's data type
     */
    public class ValueLongs extends ValueNumbers {

        public ValueLongs() {
            super();
        }

        public ValueLongs(int _value) {
            super(_value);
        }

        public int getDataType() {
            return FORMAT_UNSIGNED_LONG;
        }
    }

    /**
     * Signed longs. Only overloads the function which returns it's data type
     */
    public class ValueSLongs extends ValueNumbers {

        public ValueSLongs() {
            super();
        }

        public ValueSLongs(int _value) {
            super(_value);
        }

        public int getDataType() {
            return FORMAT_SIGNED_LONG;
        }
    }

    /**
     * Base class for rationals. Implements writting method, which is basically
     * the same for signed and unsigned variant.
     */
    public abstract class ValueRationals extends ExifValue<int[]> {

        @Override
        protected void writeValues(byte[] _data, int _offset) {
            for (int i = 0; i < components.length; i++) {
                writeNumber(_data, _offset + i * getComponentSize(), components[i][0],
                        getComponentSize() / 2);
                writeNumber(_data, _offset + i * getComponentSize()
                        + getComponentSize() / 2, components[i][1], getComponentSize() / 2);
            }
        }
    }

    /**
     * Unsigned rationals. Implements the read method and the 'getDataType' method
     */
    public class ValueURationals extends ValueRationals {

        /**
         * Read pairs of unsigned values from source byte array.
         *
         * @param _data
         *          Byte array to read from
         * @param _offset
         *          Offset where to start
         * @param _count
         *          Count of rationals values
         * @return Array of two-values arrays represeting rationals
         */
        private int[][] readUnsigned(byte[] _data, int _offset, int _count) {
            int[][] result = new int[_count][2];
            for (int v = 0; v < _count; v++) {
                result[v][0] = readUInt(_data, _offset + 8 * v, 4);
                result[v][1] = readUInt(_data, _offset + 8 * v + 4, 4);
            }
            return result;
        }

        public int getDataType() {
            return FORMAT_UNSIGNED_RATIONAL;
        }
    }

    /**
     * Unsigned rationals. Implements the read method and the 'getDataType' method
     */
    public class ValueSRationals extends ValueRationals {

        /**
         * Read pairs of signed values from source byte array.
         *
         * @param _data
         *          Byte array to read from
         * @param _offset
         *          Offset where to start
         * @param _count
         *          Count of rationals values
         * @return Array of two-values arrays represeting rationals
         */
        private int[][] readSigned(byte[] _data, int _offset, int _count) {
            int[][] result = new int[_count][2];
            for (int v = 0; v < _count; v++) {
                result[v][0] = readSInt(_data, _offset + 8 * v, 4);
                result[v][1] = readSInt(_data, _offset + 8 * v + 4, 4);
            }
            return result;
        }

        public int getDataType() {
            return FORMAT_SIGNED_RATIONAL;
        }
    }

    public HashMap<Integer, ExifValue> getIfd0() {
        return ifd0;
    }

    public HashMap<Integer, ExifValue> getIfdExif() {
        return ifdExif;
    }

    public HashMap<Integer, ExifValue> getIfdGps() {
        return ifdGps;
    }

    public HashMap<Integer, ExifValue> getIfd1() {
        return ifd1;
    }

    public HashMap<Integer, ExifValue> getIfdIOper() {
        return ifdIOper;
    }
}
