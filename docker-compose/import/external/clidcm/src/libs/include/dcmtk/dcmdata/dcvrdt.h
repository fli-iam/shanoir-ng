/*
 *
 *  Copyright (C) 1994-2005, OFFIS
 *
 *  This software and supporting documentation were developed by
 *
 *    Kuratorium OFFIS e.V.
 *    Healthcare Information and Communication Systems
 *    Escherweg 2
 *    D-26121 Oldenburg, Germany
 *
 *  THIS SOFTWARE IS MADE AVAILABLE,  AS IS,  AND OFFIS MAKES NO  WARRANTY
 *  REGARDING  THE  SOFTWARE,  ITS  PERFORMANCE,  ITS  MERCHANTABILITY  OR
 *  FITNESS FOR ANY PARTICULAR USE, FREEDOM FROM ANY COMPUTER DISEASES  OR
 *  ITS CONFORMITY TO ANY SPECIFICATION. THE ENTIRE RISK AS TO QUALITY AND
 *  PERFORMANCE OF THE SOFTWARE IS WITH THE USER.
 *
 *  Module:  dcmdata
 *
 *  Author:  Gerd Ehlers, Joerg Riesmeier
 *
 *  Purpose: Interface of class DcmDateTime
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:28:57 $
 *  CVS/RCS Revision: $Revision: 1.19 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCVRDT_H
#define DCVRDT_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/dcmdata/dcbytstr.h"
#include "dcmtk/ofstd/ofdatime.h"


/** a class representing the DICOM value representation 'Date Time' (DT)
 */
class DcmDateTime
  : public DcmByteString
{

  public:

    /** constructor.
     *  Create new element from given tag and length.
     *  @param tag DICOM tag for the new element
     *  @param len value length for the new element
     */
    DcmDateTime(const DcmTag &tag,
                const Uint32 len = 0);

    /** copy constructor
     *  @param old element to be copied
     */
    DcmDateTime(const DcmDateTime &old);

    /** destructor
     */
    virtual ~DcmDateTime();

    /** assignment operator
     *  @param obj element to be assigned/copied
     *  @return reference to this object
     */
    DcmDateTime &operator=(const DcmDateTime &obj);

    /** clone method
     *  @return deep copy of this object
     */
    virtual DcmObject *clone() const
    {
      return new DcmDateTime(*this);
    }

    /** get element type identifier
     *  @return type identifier of this class (EVR_DT)
     */
    virtual DcmEVR ident() const;

    /** get a copy of a particular string component
     *  @param stringValue variable in which the result value is stored
     *  @param pos index of the value in case of multi-valued elements (0..vm-1)
     *  @param normalize delete trailing spaces if OFTrue
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition getOFString(OFString &stringValue,
                                    const unsigned long pos,
                                    OFBool normalize = OFTrue);

    /** set the element value to the current system date and time.
     *  The DICOM DT format supported by this function is "YYYYMMDDHHMM[SS[.FFFFFF]][&ZZZZ]"
     *  where the brackets enclose optional parts. If the current system date/time or parts
     *  of it are unavailable the corresponding values are set to "0" and an error code is
     *  returned - in fact, the date is set to "19000101" if unavailable.
     *  @param seconds add optional seconds ("SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&ZZZZ" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from Coordinated Universal
     *   Time (UTC).
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition setCurrentDateTime(const OFBool seconds = OFTrue,
                                   const OFBool fraction = OFFalse,
                                   const OFBool timeZone = OFFalse);

    /** set the element value to the given date and time
     *  @param dateTimeValue date to be set (should be a valid date and time)
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition setOFDateTime(const OFDateTime &dateTimeValue);

    /** get the current element value in OFDateTime format.
     *  Please note that the element value is expected to be in valid DICOM DT format
     *  ("YYYYMMDD[HH[MM[SS[.FFFFFF]]]][&ZZZZ]"). If the optional time zone ("&ZZZZ") is
     *  missing the local time zone is used.
     *  If this function fails the result variable 'dateTimeValue' is cleared automatically.
     *  @param dateTimeValue reference to OFDateTime variable where the result is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getOFDateTime(OFDateTime &dateTimeValue,
                              const unsigned long pos = 0);

    /** get the current element value in ISO date/time format.
     *  The ISO date/time format supported by this function is "YYYY-MM-DD HH:MM[:SS[.FFFFFF]]
     *  [&HH:MM]" where the brackets enclose optional parts. Please note that the element value
     *  is expected to be in valid DICOM DT format ("YYYYMMDD[HH[MM[SS[.FFFFFF]]]][&ZZZZ]"). If
     *  this function fails the result variable 'formattedDateTime' is cleared automatically.
     *  @param formattedDateTime reference to string variable where the result is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @param seconds add optional seconds (":SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&HH:MM" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from Coordinated Universal
     *   Time (UTC). Please note that the formatted time output is not adapted to the local
     *   time if the time zone is omitted.
     *  @param createMissingPart if OFTrue create optional parts (seconds, fractional part of
     *   a seconds and/or time zone) if absent in the element value
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getISOFormattedDateTime(OFString &formattedDateTime,
                                        const unsigned long pos = 0,
                                        const OFBool seconds = OFTrue,
                                        const OFBool fraction = OFFalse,
                                        const OFBool timeZone = OFTrue,
                                        const OFBool createMissingPart = OFFalse);

    /** get the current element value in ISO date/time format.
     *  Same as above but allows to specify the separator between date and time value.
     *  Only required since Sun CC 2.0.1 compiler does not support default parameter values for
     *  "complex types" like OFString.  Reports the error message: "Sorry not implemented" :-/
     *  @param formattedDateTime reference to string variable where the result is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @param seconds add optional seconds (":SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&HH:MM" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from Coordinated Universal
     *   Time (UTC). Please note that the formatted time output is not adapted to the local
     *   time if the time zone is omitted.
     *  @param createMissingPart if OFTrue create optional parts (seconds, fractional part of
     *   a seconds and/or time zone) if absent in the element value
     *  @param dateTimeSeparator separator between ISO date and time value (default: " ")
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getISOFormattedDateTime(OFString &formattedDateTime,
                                        const unsigned long pos /*= 0*/,
                                        const OFBool seconds /*= OFTrue*/,
                                        const OFBool fraction /*= OFFalse*/,
                                        const OFBool timeZone /*= OFTrue*/,
                                        const OFBool createMissingPart /*= OFFalse*/,
                                        const OFString &dateTimeSeparator /*= " "*/);

    /* --- static helper functions --- */

    /** get the current system date and time.
     *  The DICOM DT format supported by this function is "YYYYMMDDHHMM[SS[.FFFFFF]][&ZZZZ]"
     *  where the brackets enclose optional parts. If the current system date/time or parts
     *  of it are unavailable the corresponding values are set to "0" and an error code is
     *  returned - in fact, the date is set to "19000101" if unavailable.
     *  @param dicomDateTime reference to string variable where the result is stored
     *  @param seconds add optional seconds ("SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&ZZZZ" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from Coordinated Universal
     *   Time (UTC).
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getCurrentDateTime(OFString &dicomDateTime,
                                          const OFBool seconds = OFTrue,
                                          const OFBool fraction = OFFalse,
                                          const OFBool timeZone = OFFalse);

    /** get the specified OFDateTime value in DICOM format.
     *  The DICOM DT format supported by this function is "YYYYMMDDHHMM[SS[.FFFFFF]][&ZZZZ]"
     *  where the brackets enclose optional parts. If the current system date/time or parts
     *  of it are unavailable the corresponding values are set to "0" and an error code is
     *  returned - in fact, the date is set to "19000101" if unavailable.
     *  @param dateTimeValue date and time to be converted to DICOM format
     *  @param dicomDateTime reference to string variable where the result is stored
     *  @param seconds add optional seconds ("SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&ZZZZ" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from Coordinated Universal
     *   Time (UTC).
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getDicomDateTimeFromOFDateTime(const OFDateTime &dateTimeValue,
                                                      OFString &dicomDateTime,
                                                      const OFBool seconds = OFTrue,
                                                      const OFBool fraction = OFFalse,
                                                      const OFBool timeZone = OFFalse);

    /** get the specified DICOM date and time value in OFDateTime format.
     *  Please note that the element value is expected to be in valid DICOM DT format
     *  ("YYYYMMDD[HH[MM[SS[.FFFFFF]]]][&ZZZZ]"). If the optional time zone ("&ZZZZ") is
     *  missing the local time zone is used.
     *  If this function fails the result variable 'dateTimeValue' is cleared automatically.
     *  @param dicomDateTime string value in DICOM DT format to be converted to ISO format
     *  @param dateTimeValue reference to OFDateTime variable where the result is stored
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getOFDateTimeFromString(const OFString &dicomDateTime,
                                               OFDateTime &dateTimeValue);

    /** get the specified DICOM datetime value in ISO format.
     *  The ISO date/time format supported by this function is "YYYY-MM-DD HH:MM[:SS[.FFFFFF]]
     *  [&HH:MM]" where the brackets enclose optional parts. Please note that the specified
     *  value is expected to be in valid DICOM DT format ("YYYYMMDD[HH[MM[SS[.FFFFFF]]]][&ZZZZ]").
     *  If this function fails the result variable 'formattedDateTime' is cleared automatically.
     *  @param dicomDateTime string value in DICOM DT format to be converted to ISO format
     *  @param formattedDateTime reference to string variable where the result is stored
     *  @param seconds add optional seconds (":SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&HH:MM" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from the Coordinated Universal
     *   Time (UTC). Please note that the formatted time output is not adapted to the local time
     *   if the time zone is omitted.
     *  @param createMissingPart if OFTrue create optional parts (seconds, fractional part of
     *   a seconds and/or time zone) if absent in the element value
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getISOFormattedDateTimeFromString(const OFString &dicomDateTime,
                                                         OFString &formattedDateTime,
                                                         const OFBool seconds = OFTrue,
                                                         const OFBool fraction = OFFalse,
                                                         const OFBool timeZone = OFTrue,
                                                         const OFBool createMissingPart = OFFalse);

    /** get the specified DICOM datetime value in ISO format.
     *  Same as above but allows to specify the separator between date and time value.
     *  Only required since Sun CC 2.0.1 compiler does not support default parameter values for
     *  "complex types" like OFString.  Reports the error message: "Sorry not implemented" :-/
     *  @param dicomDateTime string value in DICOM DT format to be converted to ISO format
     *  @param formattedDateTime reference to string variable where the result is stored
     *  @param seconds add optional seconds (":SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param timeZone add optional time zone ("&HH:MM" where "&" is "+" or "-") if OFTrue.
     *   The time zone is given as the offset (hours and minutes) from the Coordinated Universal
     *   Time (UTC). Please note that the formatted time output is not adapted to the local time
     *   if the time zone is omitted.
     *  @param createMissingPart if OFTrue create optional parts (seconds, fractional part of
     *   a seconds and/or time zone) if absent in the element value
     *  @param dateTimeSeparator separator between ISO date and time value (default: " ")
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getISOFormattedDateTimeFromString(const OFString &dicomDateTime,
                                                         OFString &formattedDateTime,
                                                         const OFBool seconds /*= OFTrue*/,
                                                         const OFBool fraction /*= OFFalse*/,
                                                         const OFBool timeZone /*= OFTrue*/,
                                                         const OFBool createMissingPart /*= OFFalse*/,
                                                         const OFString &dateTimeSeparator /*= " "*/);
};


#endif // DCVRDT_H


/*
** CVS/RCS Log:
** $Log: dcvrdt.h,v $
** Revision 1.19  2005/12/08 16:28:57  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.18  2004/07/01 12:28:25  meichel
** Introduced virtual clone method for DcmObject and derived classes.
**
** Revision 1.17  2004/04/16 12:49:25  joergr
** Restructured code to avoid default parameter values for "complex types" like
** OFString. Required for Sun CC 2.0.1.
**
** Revision 1.16  2004/01/16 14:04:11  joergr
** Introduced new parameter "dateTimeSeparator" in getISOFormattedXXX() methods
** to support ISO 8601 format as required by XML Schema type "dateTime".
**
** Revision 1.15  2002/12/06 12:49:15  joergr
** Enhanced "print()" function by re-working the implementation and replacing
** the boolean "showFullData" parameter by a more general integer flag.
** Added doc++ documentation.
** Made source code formatting more consistent with other modules/files.
**
** Revision 1.14  2002/04/25 09:51:08  joergr
** Removed getOFStringArray() implementation.
**
** Revision 1.13  2002/04/11 12:25:09  joergr
** Enhanced DICOM date, time and date/time classes. Added support for new
** standard date and time functions.
**
** Revision 1.12  2001/10/10 15:17:37  joergr
** Updated comments.
**
** Revision 1.11  2001/10/01 15:01:39  joergr
** Introduced new general purpose functions to get/set person names, date, time
** and date/time.
**
** Revision 1.10  2001/09/25 17:19:31  meichel
** Adapted dcmdata to class OFCondition
**
** Revision 1.9  2001/06/01 15:48:49  meichel
** Updated copyright header
**
** Revision 1.8  2000/03/08 16:26:23  meichel
** Updated copyright header.
**
** Revision 1.7  1999/03/31 09:24:59  meichel
** Updated copyright header in module dcmdata
**
** Revision 1.6  1998/11/12 16:47:48  meichel
** Implemented operator= for all classes derived from DcmObject.
**
** Revision 1.5  1997/09/11 15:13:14  hewett
** Modified getOFString method arguments by removing a default value
** for the pos argument.  By requiring the pos argument to be provided
** ensures that callers realise getOFString only gets one component of
** a multi-valued string.
**
** Revision 1.4  1997/08/29 08:32:41  andreas
** - Added methods getOFString and getOFStringArray for all
**   string VRs. These methods are able to normalise the value, i. e.
**   to remove leading and trailing spaces. This will be done only if
**   it is described in the standard that these spaces are not relevant.
**   These methods do not test the strings for conformance, this means
**   especially that they do not delete spaces where they are not allowed!
**   getOFStringArray returns the string with all its parts separated by \
**   and getOFString returns only one value of the string.
**   CAUTION: Currently getString returns a string with trailing
**   spaces removed (if dcmEnableAutomaticInputDataCorrection == OFTrue) and
**   truncates the original string (since it is not copied!). If you rely on this
**   behaviour please change your application now.
**   Future changes will ensure that getString returns the original
**   string from the DICOM object (NULL terminated) inclusive padding.
**   Currently, if you call getOF... before calling getString without
**   normalisation, you can get the original string read from the DICOM object.
**
** Revision 1.3  1996/01/05 13:23:05  andreas
** - changed to support new streaming facilities
** - more cleanups
** - merged read / write methods for block and file transfer
**
*/
