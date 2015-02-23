package org.gisandchips.ctmdroid.exif;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.gisandchips.ctmdroid.R;
import org.gisandchips.ctmdroid.exif.ExifDriver.ExifValue;
import org.gisandchips.ctmdroid.exif.ExifDriver.ValueAsciiStrings;
import org.gisandchips.ctmdroid.exif.ExifDriver.ValueByteArray;
import org.gisandchips.ctmdroid.exif.ExifDriver.ValueNumbers;
import org.gisandchips.ctmdroid.exif.ExifDriver.ValueRationals;
import org.gisandchips.ctmdroid.exif.ExifDriver.ValueUndefined;

public class ExifManager {
    ExifDriver driver;
    Context context;

    public ExifManager(ExifDriver _driver, Context _context) {
        driver = _driver;
        context = _context;
    }

    private byte[] convertByteArray(Byte[] _byte) {
        byte[] result = new byte[_byte.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = _byte[i];
        }
        return result;
    }

    private Byte[] convertByteArray(byte[] _byte) {
        Byte[] result = new Byte[_byte.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = _byte[i];
        }
        return result;
    }

    /**
     * Method, for getting HR (human readable), localized Stirng pair -
     * tag_name,tag value from IFD0
     *
     * @param _tag
     *          Tag code got from ExifDriver public static tags
     * @return two-member String array. 0-th item holds tag name, 1-st holds
     *         value
     */
    public String[] getMainImageRelated(int _tag) {
        return getImageRelated(driver.getIfd0(), _tag);
    }

    /**
     * Method, for getting HR (human readable), localized Stirng pair -
     * tag_name,tag value from IFD0
     *
     * @param _tag
     *          Tag code got from ExifDriver public static tags
     * @return two-member String array. 0-th item holds tag name, 1-st holds
     *         value
     */
    public String[] getThumbnailRelated(int _tag) {
        return getImageRelated(driver.getIfd1(), _tag);
    }

    /**
     * Method, for getting HR (human readable), localized String pair -
     * tag_name,tag value from IFD related to image (IFD0, IFD1)
     *
     * @param _tag
     *          Tag code got from ExifDriver public static tags
     * @param _ifd
     *          IFD which to read values from (will be ifd0 or ifd1)
     * @return two-member String array. 0-th item holds tag name, 1-st holds
     *         value
     */
    private String[] getImageRelated(HashMap<Integer, ExifValue> _ifd, int _tag) {
        String[] result = null;
        String hrTag = "";
        String hrValue = "";
        switch (_tag) {
            // IFD0-related tags, some of them are presented in IFD1 too
            case ExifDriver.TAG_IMAGE_WIDTH:
                hrTag = (String) context.getText(R.string.exif_tag_width);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_IMAGE_HEIGHT:
                hrTag = (String) context.getText(R.string.exif_tag_height);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_BITS_PER_SAMPLE:
                hrTag = (String) context.getText(R.string.exif_tag_bits_per_sample);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_COMPRESSION:
                hrTag = (String) context.getText(R.string.exif_tag_compression);
                Integer compression = getNumber(_ifd, _tag);
                if (compression != null) {
                    if (compression == 1) {
                        hrValue = (String) context.getText(R.string.exif_value_uncompressed);
                    } else if (compression == 6) {
                        hrValue = "JPEG ("
                                + (String) context.getText(R.string.exif_value_thumbnails_only)
                                + ")";
                    } else {
                        hrValue = (String) context.getText(R.string.exif_value_unknown);
                    }
                } else {
                    hrValue = "JPEG";
                }
                break;
            case ExifDriver.TAG_PHOTOMETRIC_INTERPRETATION:
                hrTag = (String) context
                        .getText(R.string.exif_tag_photometric_interpretation);
                Integer phInter = getNumber(_ifd, _tag);
                if (phInter != null) {
                    if (phInter == 1) {
                        hrValue = "RGB";
                    } else if (phInter == 6) {
                        hrValue = "YCbCr";
                    } else {
                        hrValue = (String) context.getText(R.string.exif_value_unknown);
                    }
                }
                break;
            case ExifDriver.TAG_ORIENTATION:
                hrTag = (String) context.getText(R.string.exif_tag_orientation);
                Integer orientation = getNumber(_ifd, _tag);
                if (orientation != null) {
                    switch (orientation) {
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_normal);
                            break;
                        case 2:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_flip_horizontal);
                            break;
                        case 3:
                            hrValue = (String) context.getText(R.string.exif_value_rotate180);
                            break;
                        case 4:
                            hrValue = (String) context.getText(R.string.exif_value_flip_vertical);
                            break;
                        case 5:
                            hrValue = (String) context.getText(R.string.exif_value_transpose);
                            break;
                        case 6:
                            hrValue = (String) context.getText(R.string.exif_value_rotate90);
                            break;
                        case 7:
                            hrValue = (String) context.getText(R.string.exif_value_transverse);
                            break;
                        case 8:
                            hrValue = (String) context.getText(R.string.exif_value_rotate270);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_SAMPLES_PER_PIXEL:
                hrTag = (String) context.getText(R.string.exif_tag_samples_per_pixel);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_PLANAR_CONFIGURATION:
                hrTag = (String) context.getText(R.string.exif_tag_planar_configuration);
                Integer planarConf = getNumber(_ifd, _tag);
                if (planarConf != null && planarConf == 2) {
                    hrValue = (String) context.getText(R.string.exif_value_planar);
                } else {
                    hrValue = (String) context.getText(R.string.exif_value_chunky);
                }
                break;
            case ExifDriver.TAG_YCBCR_SUBSAMPLING:
                hrTag = (String) context.getText(R.string.exif_tag_ycbcr_subsampling);
                Integer[] ycbcrSubs = getNumbers(_ifd, _tag);
                if (ycbcrSubs != null && ycbcrSubs.length == 2) {
                    if (ycbcrSubs[0] == 2 && ycbcrSubs[1] == 1) {
                        hrValue = "YCbCr4:2:2";
                    } else if (ycbcrSubs[0] == 2 && ycbcrSubs[1] == 2) {
                        hrValue = "YCbCr4:2:0";
                    }
                }
                break;
            case ExifDriver.TAG_YCBCRPOSITIONING:
                hrTag = (String) context.getText(R.string.exif_tag_ycbcr_positioning);
                Integer ycbcrPos = getNumber(_ifd, _tag);
                if (ycbcrPos != null && ycbcrPos == 2) {
                    hrValue = (String) context.getText(R.string.exif_value_co_sited);
                } else {
                    hrValue = (String) context.getText(R.string.exif_value_centered);
                }
                break;
            case ExifDriver.TAG_XRESOLUTION:
                hrTag = (String) context.getText(R.string.exif_tag_xresolution);
                int[] xRes = getRational(_ifd, _tag);
                if (xRes != null && xRes.length == 2) {
                    hrValue = xRes[0] + ":" + xRes[1];
                } else {
                    hrValue = "1:75 ("
                            + (String) context.getText(R.string.exif_value_assumed) + ")";
                }
                break;
            case ExifDriver.TAG_YRESOLUTION:
                hrTag = (String) context.getText(R.string.exif_tag_yresolution);
                int[] yRes = getRational(_ifd, _tag);
                if (yRes != null && yRes.length == 2) {
                    hrValue = yRes[0] + ":" + yRes[1];
                } else {
                    yRes = getRational(_ifd, ExifDriver.TAG_XRESOLUTION);
                    if (yRes != null && yRes.length == 2) {
                        hrValue = yRes[0] + ":" + yRes[1];
                    } else {
                        hrValue = "1:75 ("
                                + (String) context.getText(R.string.exif_value_assumed) + ")";
                    }
                }
                break;
            case ExifDriver.TAG_RESOLUTION_UNIT:
                hrTag = (String) context.getText(R.string.exif_tag_resolution_unit);
                Integer resUnit = getNumber(_ifd, _tag);
                if (resUnit != null && resUnit == 3) {
                    hrValue = (String) context.getText(R.string.exif_value_inches);
                } else {
                    hrValue = (String) context.getText(R.string.exif_value_centimeters);
                }
                break;
            case ExifDriver.TAG_STRIP_OFFSETS:
                hrTag = (String) context.getText(R.string.exif_tag_strip_offsets);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_ROWS_PER_STRIP:
                hrTag = (String) context.getText(R.string.exif_tag_rows_per_strip);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_STRIP_BYTECOUNTS:
                hrTag = (String) context.getText(R.string.exif_tag_strip_byte_counts);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_JPEG_INTERCHANGE_FORMAT:
                hrTag = (String) context
                        .getText(R.string.exif_tag_jpeg_interchange_format);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH:
                hrTag = (String) context
                        .getText(R.string.exif_tag_jpeg_interchange_format_length);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_TRANSFER_FUNCTION:
                hrTag = (String) context.getText(R.string.exif_tag_transfer_function);
                hrValue = getNumbersString(_ifd, _tag);
                break;
            case ExifDriver.TAG_WHITE_POINT:
                hrTag = (String) context.getText(R.string.exif_tag_white_point);
                hrValue = getRationalsString(_ifd, _tag);
                break;
            case ExifDriver.TAG_PRIMARY_CHROMATICITIES:
                hrTag = (String) context
                        .getText(R.string.exif_tag_primary_chromaticities);
                hrValue = getRationalsString(_ifd, _tag);
                break;
            case ExifDriver.TAG_YCBCR_COEFICIENTS:
                hrTag = (String) context.getText(R.string.exif_tag_ycbcr_coeficients);
                hrValue = getRationalsString(_ifd, _tag);
                break;
            case ExifDriver.TAG_REFERENCE_BLACK_WHITE:
                hrTag = (String) context.getText(R.string.exif_tag_reference_black_white);
                hrValue = getRationalsString(_ifd, _tag);
                break;
            case ExifDriver.TAG_DATETIME:
                hrTag = (String) context.getText(R.string.exif_tag_datetime);
                hrValue = getDateTime(_ifd, _tag);
                break;
            case ExifDriver.TAG_IMAGE_DESCRIPTION:
                hrTag = (String) context.getText(R.string.exif_tag_image_description);
                hrValue = getString(_ifd, _tag);
                break;
            case ExifDriver.TAG_MAKE:
                hrTag = (String) context.getText(R.string.exif_tag_make);
                hrValue = getString(_ifd, _tag);
                break;
            case ExifDriver.TAG_MODEL:
                hrTag = (String) context.getText(R.string.exif_tag_model);
                hrValue = getString(_ifd, _tag);
                break;
            case ExifDriver.TAG_SOFTWARE:
                hrTag = (String) context.getText(R.string.exif_tag_software);
                hrValue = getString(_ifd, _tag);
                break;
            case ExifDriver.TAG_ARTIST:
                hrTag = (String) context.getText(R.string.exif_tag_artist);
                hrValue = getString(_ifd, _tag);
                break;
            case ExifDriver.TAG_COPYRIGHT:
                hrTag = (String) context.getText(R.string.exif_tag_copyright);
                String[] copyright = getCopyright();
                if (copyright != null
                        && (copyright[0] != null && !copyright[0].trim().equals(""))
                        || (copyright[1] != null && !copyright[1].trim().equals(""))) {
                    hrTag = (String) context.getText(R.string.exif_value_author) + ": ";
                    hrTag += "; ";
                    hrTag += (String) context.getText(R.string.exif_value_editor) + ": ";
                    hrTag += ". ";
                }
                hrValue = getString(_ifd, _tag);
                break;
        }
        if (!(hrTag.equals("") || hrValue.equals(""))) {
            result = new String[2];
            result[0] = hrTag;
            result[1] = hrValue;
        }
        return result;
    }

    /**
     * Method, for getting HR (human readable), localized String pair -
     * tag_name,tag value from IFDExif
     *
     * @param _tag
     *          Tag code got from ExifDriver public static tags
     * @return two-member String array. 0-th item holds tag name, 1-st holds
     *         value
     */
    public String[] getExifRelated(int _tag) {
        String[] result = null;
        String hrTag = "";
        String hrValue = "";
        switch (_tag) {
            case ExifDriver.TAG_EXIF_VERSION:
                hrTag = (String) context.getText(R.string.exif_tag_exif_version);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_FLASHPIX_VERSION:
                hrTag = (String) context.getText(R.string.exif_tag_flashpix_version);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_COLOR_SPACE:
                hrTag = (String) context.getText(R.string.exif_tag_color_space);
                Integer colorSpace = getNumber(driver.getIfdExif(), _tag);
                if (colorSpace != null && colorSpace == 1) {
                    hrValue = "sRGB";
                } else {
                    hrValue = (String) context.getText(R.string.exif_value_uncalibrated);
                }
                break;
            case ExifDriver.TAG_COMPONENT_CONFIGURATION:
                hrTag = (String) context
                        .getText(R.string.exif_tag_component_configuration);
                hrValue = getString(driver.getIfdExif(), _tag).replace('0', ' ');
                hrValue = hrValue.replace('1', 'Y');
                hrValue = hrValue.replace("2", "Cb");
                hrValue = hrValue.replace("3", "Cr");
                hrValue = hrValue.replace('4', 'R');
                hrValue = hrValue.replace('5', 'G');
                hrValue = hrValue.replace('6', 'B');
                break;
            case ExifDriver.TAG_COMPRESSED_BITS_PER_PIXEL:
                hrTag = (String) context.getText(R.string.exif_tag_bits_per_pixel);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_PIXEL_X_DIMENSION:
                hrTag = (String) context.getText(R.string.exif_tag_pixel_xdimension);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_PIXEL_Y_DIMENSION:
                hrTag = (String) context.getText(R.string.exif_tag_pixel_ydimension);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_MARKER_NOTE:
                hrTag = (String) context.getText(R.string.exif_tag_marker_note);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_USER_COMMENT:
                hrTag = (String) context.getText(R.string.exif_tag_user_comment);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_RELATED_SOUND_FILE:
                hrTag = (String) context.getText(R.string.exif_tag_related_audio_file);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_DATETIME_ORIGINAL:
                hrTag = (String) context.getText(R.string.exif_tag_datetime_original);
                hrValue = getDateTime(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_DATETIME_DIGITIZED:
                hrTag = (String) context.getText(R.string.exif_tag_datetime_digitized);
                hrValue = getDateTime(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_SUB_SEC_TIME:
                // TODO
                break;
            case ExifDriver.TAG_SUB_SEC_TIME_ORIGINAL:
                // TODO
                break;
            case ExifDriver.TAG_SUB_SEC_TIME_DIGITIZED:
                // TODO
                break;
            case ExifDriver.TAG_IMAGE_UNIQUE_ID:
                hrTag = (String) context.getText(R.string.exif_tag_unique_id);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_EXPOSURE_TIME:
                hrTag = (String) context.getText(R.string.exif_tag_exposure_time);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_FNUMBER:
                hrTag = (String) context.getText(R.string.exif_tag_fnumber);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_EXPOSURE_PROGRAM:
                hrTag = (String) context.getText(R.string.exif_tag_exposure_program);
                Integer exposureProgram = getNumber(driver.getIfdExif(), _tag);
                if (exposureProgram != null) {
                    switch (exposureProgram) {
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_manual);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_normal);
                            break;
                        case 3:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_aperture_priority);
                            break;
                        case 4:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_shutter_priority);
                            break;
                        case 5:
                            hrValue = (String) context.getText(R.string.exif_value_creative);
                            break;
                        case 6:
                            hrValue = (String) context.getText(R.string.exif_value_action);
                            break;
                        case 7:
                            hrValue = (String) context.getText(R.string.exif_value_portrait);
                            break;
                        case 8:
                            hrValue = (String) context.getText(R.string.exif_value_landscape);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_SPECTRAL_SENSITIVITY:
                hrTag = (String) context.getText(R.string.exif_tag_spectral_sensitivity);
                hrValue = getString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_ISO_SPEED_RATINGS:
                hrTag = (String) context.getText(R.string.exif_tag_iso_speed_ratings);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_OECF:
                // TODO
                break;
            case ExifDriver.TAG_SHUTTER_SPEED_VALUE:
                hrTag = (String) context.getText(R.string.exif_tag_shooter_speed_value);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_APERTURE_VALUE:
                hrTag = (String) context.getText(R.string.exif_tag_aperture_value);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_BRIGHTNESS_VALUE:
                hrTag = (String) context.getText(R.string.exif_tag_brightness_value);
                int[] brightness = getRational(driver.getIfdExif(), _tag);
                if (brightness != null && brightness.length > 0
                        && brightness[0] == 0xFFFFFFFF) {
                    hrValue = (String) context.getText(R.string.exif_value_unknown);
                } else {
                    hrValue = getRationalsString(driver.getIfdExif(), _tag);
                }
                break;
            case ExifDriver.TAG_EXPOSURE_BIAS_VALUE:
                hrTag = (String) context.getText(R.string.exif_tag_exposure_bias_value);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_MAX_APERTURE_VALUE:
                hrTag = (String) context.getText(R.string.exif_tag_max_aperture_value);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_SUBJECT_DISTANCE:
                hrTag = (String) context.getText(R.string.exif_tag_subject_distance);
                int[] subjDistance = getRational(driver.getIfdExif(), _tag);
                if (subjDistance != null && subjDistance.length > 0) {
                    if (subjDistance[0] == 0xFFFFFFFF) {
                        hrValue = (String) context.getText(R.string.exif_value_infinity);
                        break;
                    } else if (subjDistance[0] == 0) {
                        hrValue = (String) context.getText(R.string.exif_value_unknown);
                        break;
                    }
                } else {
                    hrValue = getRationalsString(driver.getIfdExif(), _tag);
                }
                break;
            case ExifDriver.TAG_METERING_MODE:
                hrTag = (String) context.getText(R.string.exif_tag_metering_mode);
                Integer meteringMode = getNumber(driver.getIfdExif(), _tag);
                if (meteringMode != null) {
                    switch (meteringMode) {
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_average);
                            break;
                        case 2:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_center_weighted_average);
                            break;
                        case 3:
                            hrValue = (String) context.getText(R.string.exif_value_spot);
                            break;
                        case 4:
                            hrValue = (String) context.getText(R.string.exif_value_multispot);
                            break;
                        case 5:
                            hrValue = (String) context.getText(R.string.exif_value_pattern);
                            break;
                        case 6:
                            hrValue = (String) context.getText(R.string.exif_value_partial);
                            break;
                        case 255:
                            hrValue = (String) context.getText(R.string.exif_value_other);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_LIGHT_SOURCE:
                hrTag = (String) context.getText(R.string.exif_tag_light_source);
                Integer lightSource = getNumber(driver.getIfdExif(), _tag);
                if (lightSource != null) {
                    switch (lightSource) {
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_daylight);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_fluorescent);
                            break;
                        case 3:
                            hrValue = (String) context.getText(R.string.exif_value_tungsten);
                            break;
                        case 4:
                            hrValue = (String) context.getText(R.string.exif_value_flash);
                            break;
                        case 9:
                            hrValue = (String) context.getText(R.string.exif_value_fine_weather);
                            break;
                        case 10:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_cloudy_weather);
                            break;
                        case 11:
                            hrValue = (String) context.getText(R.string.exif_value_shade);
                            break;
                        case 12:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_daylight_fluorescent);
                            break;
                        case 13:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_day_white_fluorescent);
                            break;
                        case 14:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_cool_white_fluorescent);
                            break;
                        case 15:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_white_fluorescent);
                            break;
                        case 17:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_standard_light_A);
                            break;
                        case 18:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_standard_light_B);
                            break;
                        case 19:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_standard_light_C);
                            break;
                        case 20:
                            hrValue = "D55";
                            break;
                        case 21:
                            hrValue = "D65";
                            break;
                        case 22:
                            hrValue = "D75";
                            break;
                        case 23:
                            hrValue = "D50";
                            break;
                        case 24:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_iso_studio_tungsten);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_FLASH:
                hrTag = (String) context.getText(R.string.exif_tag_flash);
                Integer flash = getNumber(driver.getIfdExif(), _tag);
                if (flash != null) {
                    int flashFired = flash & 0x01;
                    int flashReturn = (flash & 0x06)>>1;
                    int flashMode = (flash & 0x18)>>3;
                    int flashFunction = (flash & 0x20)>>5;
                    int flashRedEye = (flash & 0x40)>>6;
                    if (flashFunction >1) {
                        hrValue += (String) context
                                .getText(R.string.exif_value_flash_not_present) + ";";
                    } else {
                        hrValue += (String) context
                                .getText(R.string.exif_value_flash_present) + ";";
                    }
                    if (flashFired > 0) {
                        hrValue += (String) context.getText(R.string.exif_value_flash_fired)
                                + ";";
                    } else {
                        hrValue += (String) context
                                .getText(R.string.exif_value_flash_did_not_fire) + ";";
                    }
                    switch (flashReturn) {
                        case 0:
                            hrValue += (String) context
                                    .getText(R.string.exif_value_strobe_no_function) + ";";
                            break;
                        case 2:
                            hrValue += (String) context
                                    .getText(R.string.exif_value_strobe_not_detected) + ";";
                            break;
                        case 3:
                            hrValue += (String) context
                                    .getText(R.string.exif_value_strobe_detected) + ";";
                            break;
                    }
                    switch (flashMode) {
                        case 1:
                            hrValue += (String) context
                                    .getText(R.string.exif_value_compulsory_finning) + ";";
                            break;
                        case 2:
                            hrValue += (String) context
                                    .getText(R.string.exif_value_compulsory_suppression) + ";";
                            break;
                        case 3:
                            hrValue += (String) context.getText(R.string.exif_value_auto) + ";";
                            break;
                    }
                    if (flashRedEye > 0) {
                        hrValue += (String) context
                                .getText(R.string.exif_value_red_eye_supported) + ".";
                    } else {
                        hrValue += (String) context
                                .getText(R.string.exif_value_red_eye_not_supported) + ".";
                    }
                }
                break;
            case ExifDriver.TAG_FOCAL_LENGTH:
                hrTag = (String) context.getText(R.string.exif_tag_focal_length);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_SUBJECT_AREA:
                hrTag = (String) context.getText(R.string.exif_tag_subject_area);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_FLASH_ENERGY:
                hrTag = (String) context.getText(R.string.exif_tag_flash_energy);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_SPATIAL_FREQUENCY_RESPONSE:
                // TODO
                break;
            case ExifDriver.TAG_FOCAL_PLANE_X_RESOLUTION:
                hrTag = (String) context
                        .getText(R.string.exif_tag_focal_plane_x_resolution);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_FOCAL_PLANE_Y_RESOLUTION:
                hrTag = (String) context
                        .getText(R.string.exif_tag_focal_plane_y_resolution);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_FOCAL_PLANE_RESOLUTION_UNIT:
                hrTag = (String) context
                        .getText(R.string.exif_tag_focal_plane_resolution_unit);
                hrValue = (String) context.getText(R.string.exif_value_inches);
                break;
            case ExifDriver.TAG_SUBJECT_LOCATION:
                hrTag = (String) context.getText(R.string.exif_tag_subject_location);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_EXPOSURE_INDEX:
                hrTag = (String) context.getText(R.string.exif_tag_exposure_index);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_SENSING_METHOD:
                hrTag = (String) context.getText(R.string.exif_tag_sensing_method);
                Integer sensingMethod = getNumber(driver.getIfdExif(), _tag);
                if (sensingMethod != null) {
                    switch (sensingMethod) {
                        case 2:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_one_chip_area_sensor);
                            break;
                        case 3:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_two_chip_area_sensor);
                            break;
                        case 4:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_three_chip_area_sensor);
                            break;
                        case 5:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_sequential_area_sensor);
                            break;
                        case 7:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_trilinear_sensor);
                            break;
                        case 8:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_sequential_linear_sensor);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_FILE_SOURCE:
                // TODO
                break;
            case ExifDriver.TAG_SCENE_TYPE:
                // TODO
                break;
            case ExifDriver.TAG_CFA_PATTERN:
                // TODO
                break;
            case ExifDriver.TAG_CUSTOM_RENDERED:
                hrTag = (String) context.getText(R.string.exif_tag_custom_rendered);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_EXPOSURE_MODE:
                hrTag = (String) context.getText(R.string.exif_tag_exposure_mode);
                Integer expMode = getNumber(driver.getIfdExif(), _tag);
                if (expMode != null) {
                    switch (expMode) {
                        case 0:
                            hrValue = (String) context.getText(R.string.exif_value_auto_exposure);
                            break;
                        case 1:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_manual_exposure);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_auto_bracket);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_WHITE_BALANCE:
                hrTag = (String) context.getText(R.string.exif_tag_white_balance);
                Integer whiteBalance = getNumber(driver.getIfdExif(), _tag);
                if (whiteBalance != null && whiteBalance == 1) {
                    hrValue = (String) context.getText(R.string.exif_value_manual);
                } else {
                    hrValue = (String) context.getText(R.string.exif_value_auto);
                }
                break;
            case ExifDriver.TAG_DIGITAL_ZOOM_RATIO:
                hrTag = (String) context.getText(R.string.exif_tag_zoom_ratio);
                hrValue = getRationalsString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_FOCAL_LENGTH_35MM_FILM:
                hrTag = (String) context.getText(R.string.exif_tag_focal_length_35mm);
                hrValue = getNumbersString(driver.getIfdExif(), _tag);
                break;
            case ExifDriver.TAG_SCENE_CAPTURE_TYPE:
                hrTag = (String) context.getText(R.string.exif_tag_screen_capture_type);
                Integer captureType = getNumber(driver.getIfdExif(), _tag);
                if (captureType != null) {
                    switch (captureType) {
                        case 0:
                            hrValue = (String) context.getText(R.string.exif_value_standard);
                            break;
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_landscape);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_portrait);
                            break;
                        case 3:
                            hrValue = (String) context.getText(R.string.exif_value_night_scene);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_GAIN_CONTROL:
                hrTag = (String) context.getText(R.string.exif_tag_gain_control);
                Integer gainCotrol = getNumber(driver.getIfdExif(), _tag);
                if (gainCotrol != null) {
                    switch (gainCotrol) {
                        case 0:
                            hrValue = (String) context.getText(R.string.exif_value_none);
                            break;
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_low_up);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_high_up);
                            break;
                        case 3:
                            hrValue = (String) context.getText(R.string.exif_value_low_down);
                            break;
                        case 4:
                            hrValue = (String) context.getText(R.string.exif_value_high_down);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_CONTRAST:
                hrTag = (String) context.getText(R.string.exif_tag_contrast);
                Integer contrast = getNumber(driver.getIfdExif(), _tag);
                if (contrast != null) {
                    switch (contrast) {
                        case 0:
                            hrValue = (String) context.getText(R.string.exif_value_normal);
                            break;
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_soft);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_hard);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_SATURATION:
                hrTag = (String) context.getText(R.string.exif_tag_saturation);
                Integer saturation = getNumber(driver.getIfdExif(), _tag);
                if (saturation != null) {
                    switch (saturation) {
                        case 0:
                            hrValue = (String) context.getText(R.string.exif_value_normal);
                            break;
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_low);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_high);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_SHARPNESS:
                hrTag = (String) context.getText(R.string.exif_tag_sharpness);
                Integer sharpness = getNumber(driver.getIfdExif(), _tag);
                if (sharpness != null) {
                    switch (sharpness) {
                        case 0:
                            hrValue = (String) context.getText(R.string.exif_value_normal);
                            break;
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_soft);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_hard);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_DEVICE_SETTING_DESCRIPTION:
                // TODO
                break;
            case ExifDriver.TAG_SUBJECT_DISTANCE_RANGE:
                hrTag = (String) context.getText(R.string.exif_tag_distance_range);
                Integer dRange = getNumber(driver.getIfdExif(), _tag);
                if (dRange != null) {
                    switch (dRange) {
                        case 1:
                            hrValue = (String) context.getText(R.string.exif_value_macro);
                            break;
                        case 2:
                            hrValue = (String) context.getText(R.string.exif_value_close_view);
                            break;
                        case 3:
                            hrValue = (String) context.getText(R.string.exif_value_distant_view);
                            break;
                    }
                }
                break;
            case ExifDriver.TAG_INTEROPERABILITY_1:
            case ExifDriver.TAG_INTEROPERABILITY_2:
                // TODO
                break;
        }
        if (!(hrTag.equals("") || hrValue.equals(""))) {
            result = new String[2];
            result[0] = hrTag;
            result[1] = hrValue;
        }
        return result;
    }

    /**
     * Method, for getting HR (human readable), localized Stirng pair -
     * tag_name,tag value from IFDGps
     *
     * @param _tag
     *          Tag code got from ExifDriver public static tags
     * @return two-member String array. 0-th item holds tag name, 1-st holds
     *         value
     */
    public String[] getGpsRelated(int _tag) {
        String[] result = null;
        String hrTag = "";
        String hrValue = "";
        switch (_tag) {
            case ExifDriver.TAG_GPS_VERSION_ID:
                hrTag = (String) context.getText(R.string.exif_tag_gps_version_id);
                hrValue = getNumbersString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_LATITUDE_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_latitude_ref);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_LATITUDE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_latitude);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_LONGITUDE_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_longitude_ref);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_LONGITUDE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_longitude);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_ALTITUDE_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_altitude_ref);
                Integer altitudeRef = getNumber(driver.getIfdGps(), _tag);
                if (altitudeRef != null && altitudeRef == 1) {
                    hrValue = (String) context.getText(R.string.exif_value_below_sea_level);
                } else {
                    hrValue = (String) context.getText(R.string.exif_value_above_sea_level);
                }
                break;
            case ExifDriver.TAG_GPS_ALTITUDE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_altitude);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_TIME_STAMP:
                hrTag = (String) context.getText(R.string.exif_tag_gps_timestamp);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_SATELITES:
                hrTag = (String) context.getText(R.string.exif_tag_gps_satelites);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_STATUS:
                hrTag = (String) context.getText(R.string.exif_tag_gps_status);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_MEASURE_MODE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_measure_mode);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DOP:
                hrTag = (String) context.getText(R.string.exif_tag_gps_gpsdop);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_SPEED_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_speed_ref);
                hrValue = getString(driver.getIfdGps(), _tag);
                if (hrValue.equals("K")) {
                    hrValue = (String) context
                            .getText(R.string.exif_value_kilometers_per_hour);
                } else if (hrValue.equals("M")) {
                    hrValue = (String) context.getText(R.string.exif_value_miles_per_hour);
                } else if (hrValue.equals("K")) {
                    hrValue = (String) context.getText(R.string.exif_value_knots);
                }
                break;
            case ExifDriver.TAG_GPS_SPEED:
                hrTag = (String) context.getText(R.string.exif_tag_gps_speed);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_TRACK_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_track_ref);
                hrValue = getDirectionRef(_tag);
                break;
            case ExifDriver.TAG_GPS_TRACK:
                hrTag = (String) context.getText(R.string.exif_tag_gps_track);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_SLMG_DIRECTION_REF:
                hrTag = (String) context
                        .getText(R.string.exif_tag_gps_slmg_direction_ref);
                hrValue = getDirectionRef(_tag);
                break;
            case ExifDriver.TAG_GPS_SLMG_DIRECTION:
                hrTag = (String) context.getText(R.string.exif_tag_gps_slmg_direction);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_MAP_DATUM:
                hrTag = (String) context.getText(R.string.exif_tag_gps_map_datum);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DEST_LATITUDE_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_latitude_ref);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DEST_LATITUDE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_latitude);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DEST_LONGITUDE_REF:
                hrTag = (String) context
                        .getText(R.string.exif_tag_gps_dest_longitude_ref);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DEST_LONGITUDE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_longitude);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DEST_BEARING_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_bearing_ref);
                hrValue = getDirectionRef(_tag);
                break;
            case ExifDriver.TAG_GPS_DEST_BEARING:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_bearing);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DEST_DISTANCE_REF:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_distance_ref);
                hrValue = getString(driver.getIfdGps(), _tag);
                if (hrValue.equals("K")) {
                    hrValue = (String) context.getText(R.string.exif_value_kilometers);
                } else if (hrValue.equals("M")) {
                    hrValue = (String) context.getText(R.string.exif_value_miles);
                } else if (hrValue.equals("K")) {
                    hrValue = (String) context.getText(R.string.exif_value_knots);
                }
                break;
            case ExifDriver.TAG_GPS_DEST_DISTANCE:
                hrTag = (String) context.getText(R.string.exif_tag_gps_dest_distance);
                hrValue = getRationalsString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_PROCESSING_METHOD:
                hrTag = (String) context.getText(R.string.exif_tag_gps_processing_method);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_AREA_INFORMATION:
                hrTag = (String) context.getText(R.string.exif_tag_gps_area_information);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DATE_STAMP:
                hrTag = (String) context.getText(R.string.exif_tag_gps_date_stamp);
                hrValue = getString(driver.getIfdGps(), _tag);
                break;
            case ExifDriver.TAG_GPS_DIFFERENTIAL:
                hrTag = (String) context.getText(R.string.exif_tag_gps_differential);
                Integer differential = getNumber(driver.getIfdGps(), _tag);
                if (differential != null) {
                    switch (differential) {
                        case 0:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_no_differential_correction);
                            break;
                        case 1:
                            hrValue = (String) context
                                    .getText(R.string.exif_value_differential_correction_used);
                            break;
                    }
                }
        }
        if (!(hrTag.equals("") || hrValue.equals(""))) {
            result = new String[2];
            result[0] = hrTag;
            result[1] = hrValue;
        }
        return result;
    }

    private String getDirectionRef(int _tag) {
        String result = getString(driver.getIfdGps(), _tag);
        if (result.equals("T")) {
            result = (String) context.getText(R.string.exif_value_true_direction);
        } else if (result.equals("M")) {
            result = (String) context.getText(R.string.exif_value_magnetic_direction);
        }
        return result;
    }

    private String getDateTime(HashMap<Integer, ExifValue> _ifd, int _tag) {
        String result = "";
        String dateString = getString(_ifd, _tag);
        SimpleDateFormat format = new SimpleDateFormat(
                (String) context.getText(R.string.exif_value_datetime_format_exif));
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat hrFormat = new SimpleDateFormat(
                    (String) context.getText(R.string.exif_value_datetime_format));
            result = hrFormat.format(date);
        } catch (java.text.ParseException e) {
            // TODO
        }
        return result;
    }

    private String getString(HashMap<Integer, ExifValue> _ifd, int _tag) {
        String result = new String();
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueByteArray) {
            ValueByteArray number = (ValueByteArray) value;
            Byte[] components = number.getValues();
            result = new String(convertByteArray(components));
        }
        return result;
    }

    private String getNumbersString(HashMap<Integer, ExifValue> _ifd, int _tag) {
        StringBuffer result = new StringBuffer();
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueNumbers) {
            ValueNumbers number = (ValueNumbers) value;
            Integer[] components = number.getValues();
            result.append(components[0]);
            for (int i = 1; i < components.length; i++) {
                result.append(",");
                result.append(components[i]);
            }
        }
        return result.toString();
    }

    private Integer getNumber(HashMap<Integer, ExifValue> _ifd, int _tag) {
        Integer result = null;
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueNumbers) {
            ValueNumbers number = (ValueNumbers) value;
            result = number.getValues()[0];
        }
        return result;
    }

    private Integer[] getNumbers(HashMap<Integer, ExifValue> _ifd, int _tag) {
        Integer[] result = null;
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueNumbers) {
            ValueNumbers number = (ValueNumbers) value;
            result = number.getValues();
        }
        return result;
    }

    private int[] getRational(HashMap<Integer, ExifValue> _ifd, int _tag) {
        int[] result = null;
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueRationals) {
            ValueRationals number = (ValueRationals) value;
            result = number.getValues()[0];
        }
        return result;
    }

    private int[][] getRationals(HashMap<Integer, ExifValue> _ifd, int _tag) {
        int[][] result = null;
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueRationals) {
            ValueRationals number = (ValueRationals) value;
            result = number.getValues();
        }
        return result;
    }

    private String getRationalsString(HashMap<Integer, ExifValue> _ifd, int _tag) {
        StringBuffer result = new StringBuffer();
        Object value = _ifd.get(_tag);
        if (value != null && value instanceof ValueRationals) {
            ValueRationals number = (ValueRationals) value;
            int[][] components = number.getValues();
            result.append(components[0][0]);
            result.append(':');
            result.append(components[0][1]);
            for (int i = 1; i < components.length; i++) {
                result.append(",");
                result.append(components[i][0]);
                result.append(':');
                result.append(components[i][1]);
            }
        }
        return result.toString();
    }

  /*
   * Here go the user-space set/get methods
   */
    /**
     * Get photographer/editor copyright pair string. In case, that only editor
     * is
     * specified, photographer copyright will contain space character, so it is
     * recommended to trim it, or use specialized getPhotographerCopyright()
     * method
     *
     * @return Array of Strings. Item 0 is photographer copyright, 1 holds
     *         the editor copyright
     */
    public String[] getCopyright() {
        byte[][] result = new byte[2][];
        result[0] = new byte[] { 0 };
        result[1] = new byte[] { 0 };
        Object exifValue = driver.getIfd0().get(ExifDriver.TAG_COPYRIGHT);
        if (exifValue != null && exifValue instanceof ValueAsciiStrings) {
            ValueAsciiStrings copyright = (ValueAsciiStrings) exifValue;
            Byte[] values = copyright.getValues();
            int copyrightIndex = 0;
            result[0] = new byte[values.length];
            Arrays.fill(result[0], (byte) 0);
            result[1] = new byte[values.length];
            Arrays.fill(result[1], (byte) 0);
            int index = 0;
            for (int i = 0; i < values.length && copyrightIndex < 2; i++) {
                if (values[i] != 0) {
                    result[copyrightIndex][index] = values[i];
                    index++;
                } else {
                    copyrightIndex++;
                    index = 0;
                }
            }
        }
        return new String[] { new String(result[0]).trim(),
                new String(result[1]).trim() };
    }

    /**
     * Photographer copyright in one single string
     *
     * @return Photographer copyright
     */
    public String getPhotographerCopyright() {
        return getCopyright()[0];
    }

    /**
     * Editor copyright in one single string
     *
     * @return Editor copyright
     */
    public String getEditorCopyright() {
        return getCopyright()[1];
    }

    /**
     * Get marker note - the marker specific (possibly binary) data. User should
     * know, what the tag holds.
     *
     * @return Marker note byte[] or null if Marker note could not be found
     */
    public byte[] getMarkerNote() {
        Object exifValue = driver.getIfdExif().get(ExifDriver.TAG_MARKER_NOTE);
        if (exifValue != null && exifValue instanceof ValueUndefined) {
            ValueUndefined mNote = (ValueUndefined) exifValue;
            return convertByteArray(mNote.getValues());
        } else {
            return null;
        }
    }

    /**
     * Get artist, which is actually an author of the image
     *
     * @return Name of artist or null if the tag could not be found
     */
    public String getArtist() {
        Object exifValue = driver.getIfd0().get(ExifDriver.TAG_ARTIST);
        if (exifValue != null && exifValue instanceof ValueAsciiStrings) {
            ValueAsciiStrings mNote = (ValueAsciiStrings) exifValue;
            return new String(convertByteArray(mNote.getValues()));
        } else {
            return null;
        }
    }


    /**
     * Get software
     *
     * @return Name of artist or null if the tag could not be found
     */
    public String getSoftware() {
        Object exifValue = driver.getIfd0().get(ExifDriver.TAG_SOFTWARE);
        if (exifValue != null && exifValue instanceof ValueAsciiStrings) {
            ValueAsciiStrings mNote = (ValueAsciiStrings) exifValue;
            return new String(convertByteArray(mNote.getValues()));
        } else {
            return null;
        }
    }


    /**
     *
     * @return
     */
    public String getImageDescription() {
        Object exifValue = driver.getIfd0().get(ExifDriver.TAG_IMAGE_DESCRIPTION);
        if (exifValue != null && exifValue instanceof ValueAsciiStrings) {
            ValueAsciiStrings mNote = (ValueAsciiStrings) exifValue;
            return new String(convertByteArray(mNote.getValues()));
        } else {
            return null;
        }
    }

    /**
     * Get user comment it differs from image description - it can hold wide
     * characters
     *
     * @return
     */
    public String getUserComment() {
        Object exifValue = driver.getIfdExif().get(ExifDriver.TAG_USER_COMMENT);
        if (exifValue != null && exifValue instanceof ValueUndefined) {
            ValueUndefined mNote = (ValueUndefined) exifValue;
            return new String(convertByteArray(mNote.getValues()));
        } else {
            return null;
        }
    }

    /**
     * Set the Marker note. the value can be whatever byte array
     *
     * @param _value
     *          byte array - binary or text information
     */
    public void setMarkerNote(byte[] _value) {
        ValueUndefined baValue = driver.new ValueUndefined();
        baValue.setValue(convertByteArray(_value));
        driver.getIfdExif().put(ExifDriver.TAG_MARKER_NOTE, baValue);
    }

    /**
     * Artist - or author - should be an ASCII string, but sometimes even
     * unicode
     * works fine
     *
     * @param _artist
     *          Name of the artist
     */
    public void setArtist(String _artist) {
        ValueAsciiStrings baValue = driver.new ValueAsciiStrings();
        baValue.setValue(convertByteArray(_artist.getBytes()));
        driver.getIfd0().put(ExifDriver.TAG_ARTIST, baValue);
    }


    /**
     * Software - should be an ASCII string, but sometimes even
     * unicode
     * works fine
     *
     * @param _software
     *          Name of the software
     */
    public void setSoftware(String _software) {
        ValueAsciiStrings baValue = driver.new ValueAsciiStrings();
        baValue.setValue(convertByteArray(_software.getBytes()));
        driver.getIfd0().put(ExifDriver.TAG_SOFTWARE, baValue);
    }


    /**
     * Copyright for the photographer and editor in common it is used internally
     * by specialized methods for setting photographer and editor copyright
     * separately
     *
     * @param _author
     *          Photographer copyright can be null
     * @param _editor
     *          Editor copyright can be null
     */
    public void setCopyright(String _author, String _editor) {
        boolean editorPresented = false;
        String author = _author;
        if (author == null) {
            author = "";
        }
        author = author.trim();
        if (author.equals("")) {
            author = " ";
        }
        String editor = _editor;
        if (editor == null) {
            editor = "";
        }
        editor = editor.trim();
        editorPresented = !(editor.equals(""));
        byte[] authorBytes = author.getBytes();
        byte[] editorBytes = _editor.getBytes();
        int size = authorBytes.length + 1;
        if (editorPresented) {
            size += editorBytes.length + 1;
        }
        byte[] value = new byte[size];
        System.arraycopy(authorBytes, 0, value, 0, authorBytes.length);
        value[authorBytes.length] = 0;
        if (editorPresented) {
            System.arraycopy(editorBytes, 0, value, authorBytes.length + 1,
                    editorBytes.length);
            value[value.length - 1] = 0;
        }
        ValueAsciiStrings baValue = driver.new ValueAsciiStrings();
        baValue.setValue(convertByteArray(value));
        driver.getIfd0().put(ExifDriver.TAG_COPYRIGHT, baValue);
    }

    /**
     * Copyright for the photographer (editor) ASCII string
     *
     * @param _copyright
     *          Copyright string
     */
    public void setPhotographerCopyright(String _copyright) {
        setCopyright(_copyright, getEditorCopyright());
    }

    /**
     * Copyright for the editor ASCII string
     *
     * @param _copyright
     *          Copyright string
     */
    public void setEditorCopyright(String _copyright) {
        setCopyright(getPhotographerCopyright(), _copyright);
    }

    /**
     * Some nice ASCII image description, like "Picnic in the summer A.D. 2012"
     *
     * @param _desc
     *          Description of the image
     */
    public void setImageDescription(String _desc) {
        ValueAsciiStrings baValue = driver.new ValueAsciiStrings();
        baValue.setValue(convertByteArray(_desc.getBytes()));
        driver.getIfd0().put(ExifDriver.TAG_IMAGE_DESCRIPTION, baValue);
    }

    /**
     * User comment - alternative to image description. Difference is, that this
     * tag is expected to hold wide characters, so UTF-8 string is possible to
     * store here
     *
     * @param _comment
     */
    public void setUserComment(String _comment) {
        ExifDriver.ValueUndefined baValue = driver.new ValueUndefined();
        baValue.setValue(convertByteArray(_comment.getBytes()));
        driver.getIfdExif().put(ExifDriver.TAG_USER_COMMENT, baValue);
    }

    private int[][] toDdMmSs(double _value) {
        double value = Math.abs(_value);
        int[][] ddmmss = new int[3][2];
        ddmmss[0][0] = (int) Math.floor(value);
        ddmmss[0][1] = 1;
        value -= Math.floor(value);
        value *= 60;
        ddmmss[1][0] = (int) Math.floor(value);
        ddmmss[1][1] = 1;
        value -= Math.floor(value);
        value *= 60000;
        ddmmss[2][0] = (int) Math.floor(value);
        ddmmss[2][1] = 1000;
        return ddmmss;
    }

    private void setGpsVersion() {
        ExifDriver.ValueUBytes version = driver.new ValueUBytes();
        version.setValue(new Integer[] { 2, 2, 0, 0 });
        driver.getIfdExif().put(ExifDriver.TAG_GPS_VERSION_ID, version);
    }

    public void setGPSLocation(double _lat, double _lon, double _alt) {
        setGpsVersion();
        // Latitude
        ExifDriver.ValueAsciiStrings latRef = driver.new ValueAsciiStrings();
        ExifDriver.ValueURationals lat = driver.new ValueURationals();
        lat.setValue(toDdMmSs(_lat));
        if (_lat > 0) {
            latRef.setValue(new Byte[] { 'N' });
        } else {
            latRef.setValue(new Byte[] { 'S' });
        }
        driver.getIfdGps().put(ExifDriver.TAG_GPS_LATITUDE, lat);
        driver.getIfdGps().put(ExifDriver.TAG_GPS_LATITUDE_REF, latRef);
        // Longitude
        ExifDriver.ValueAsciiStrings lonRef = driver.new ValueAsciiStrings();
        ExifDriver.ValueURationals lon = driver.new ValueURationals();
        lon.setValue(toDdMmSs(_lon));
        if (_lon > 0) {
            lonRef.setValue(new Byte[] { 'E' });
        } else {
            lonRef.setValue(new Byte[] { 'W' });
        }
        driver.getIfdGps().put(ExifDriver.TAG_GPS_LONGITUDE, lon);
        driver.getIfdGps().put(ExifDriver.TAG_GPS_LONGITUDE_REF, lonRef);
        // Altitude
        ExifDriver.ValueUBytes altRef = driver.new ValueUBytes();
        ExifDriver.ValueURationals alt = driver.new ValueURationals();
        int[][] altValue = new int[1][];
        altValue[0] = new int[] { (int) Math.abs(_alt), 1 };
        alt.setValue(altValue);
        if (_alt >= 0) {
            altRef.setValue(new Integer[] { 0 });
        } else {
            altRef.setValue(new Integer[] { 1 });
        }
        driver.getIfdGps().put(ExifDriver.TAG_GPS_ALTITUDE, alt);
        driver.getIfdGps().put(ExifDriver.TAG_GPS_ALTITUDE_REF, altRef);
    }

    public void setImgDirection(double _dir) {

        // Direction
        ExifDriver.ValueAsciiStrings dirRef = driver.new ValueAsciiStrings();
        ExifDriver.ValueURationals dir = driver.new ValueURationals();
        int[][] dirValue = new int[1][];
        dirValue[0] = new int[] { (int) Math.abs(_dir), 1 };
        dir.setValue(dirValue);

        //Always magnetic north
        dirRef.setValue(new Byte[] { 'M' });

        driver.getIfdGps().put(ExifDriver.TAG_GPS_SLMG_DIRECTION, dir);
        driver.getIfdGps().put(ExifDriver.TAG_GPS_SLMG_DIRECTION_REF, dirRef);
    }

}
