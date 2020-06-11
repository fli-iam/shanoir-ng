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
 *  Purpose: Interface of class DcmTime
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:29:11 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcvrtm.h,v $
 *  CVS/RCS Revision: $Revision: 1.17 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCVRTM_H
#define DCVRTM_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/dcmdata/dcbytstr.h"
#include "dcmtk/ofstd/oftime.h"


/** a class representing the DICOM value representation 'Time' (TM)
 */
class DcmTime
  : public DcmByteString
{

  public:

    /** constructor.
     *  Create new element from given tag and length.
     *  @param tag DICOM tag for the new element
     *  @param len value length for the new element
     */
    DcmTime(const DcmTag &tag,
            const Uint32 len = 0);

    /** copy constructor
     *  @param old element to be copied
     */
    DcmTime(const DcmTime &old);

    /** destructor
     */
    virtual ~DcmTime();

    /** assignment operator
     *  @param obj element to be assigned/copied
     *  @return reference to this object
     */
    DcmTime &operator=(const DcmTime &obj);

    /** clone method
     *  @return deep copy of this object
     */
    virtual DcmObject *clone() const
    {
      return new DcmTime(*this);
    }

    /** get element type identifier
     *  @return type identifier of this class (EVR_TM)
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

    /** set the element value to the current system time.
     *  The DICOM TM format supported by this function is "HHMM[SS[.FFFFFF]]" where
     *  the brackets enclose optional parts. If the current system time or parts of it
     *  are unavailable the corresponding values are set to "0" and an error code is
     *  returned.
     *  @param seconds add optional seconds ("SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition setCurrentTime(const OFBool seconds = OFTrue,
                               const OFBool fraction = OFFalse);

    /** set the element value to the given time
     *  @param timeValue time to be set (should be a valid time)
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition setOFTime(const OFTime &timeValue);

    /** get the current element value in OFTime format.
     *  Please note that the element value is expected to be in valid DICOM TM format
     *  ("[HH[MM[SS[.FFFFFF]]]]", "[HH[:MM[:SS[.FFFFFF]]]]" is also supported for reasons
     *  of backward compatibility). Since there is no time zone for the DICOM TM format
     *  local time is assumed (the time zone of 'timeValue' is set automatically).
     *  If this function fails the result variable 'timeValue' is cleared automatically.
     *  @param timeValue reference to OFTime variable where the result is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @param supportOldFormat if OFTrue support old (prior V3.0) time format (see above)
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getOFTime(OFTime &timeValue,
                          const unsigned long pos = 0,
                          const OFBool supportOldFormat = OFTrue);

    /** get the current element value in ISO time format.
     *  The ISO time format supported by this function is "HH:MM[:SS[.FFFFFF]]"
     *  where the brackets enclose optional parts. Please note that the element value
     *  is expected to be in valid DICOM TM format ("[HH[MM[SS[.FFFFFF]]]]",
     *  "[HH[:MM[:SS[.FFFFFF]]]]" is also supported for reasons of backward compatibility).
     *  If this function fails the result variable 'formattedTime' is cleared automatically.
     *  Please note that if the "Timezone Offset From UTC" attribute (0008,0201) is present,
     *  it applies to all TM attributes in the object. However, the time zone is not taken
     *  into account for the creation of the ISO formatted time.
     *  See also "getTimeZoneFromString()" below.
     *  @param formattedTime reference to string variable where the result is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @param seconds add optional seconds (":SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param createMissingPart if OFTrue create optional parts (seconds and/or fractional
     *   part of a seconds) if absent in the element value
     *  @param supportOldFormat if OFTrue support old (prior V3.0) time format (see above)
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getISOFormattedTime(OFString &formattedTime,
                                    const unsigned long pos = 0,
                                    const OFBool seconds = OFTrue,
                                    const OFBool fraction = OFFalse,
                                    const OFBool createMissingPart = OFFalse,
                                    const OFBool supportOldFormat = OFTrue);

    /* --- static helper functions --- */

    /** get the current system time.
     *  The DICOM TM format supported by this function is "HHMM[SS[.FFFFFF]]" where
     *  the brackets enclose optional parts. If the current system time or parts of it
     *  are unavailable the corresponding values are set to "0" and an error code is
     *  returned.
     *  @param dicomTime reference to string variable where the result is stored
     *  @param seconds add optional seconds ("SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getCurrentTime(OFString &dicomTime,
                                      const OFBool seconds = OFTrue,
                                      const OFBool fraction = OFFalse);

    /** get the specified OFTime value in DICOM format.
     *  The DICOM TM format supported by this function is "HHMM[SS[.FFFFFF]]" where
     *  the brackets enclose optional parts. If the current system time or parts of it
     *  are unavailable the corresponding values are set to "0" and an error code is
     *  returned.
     *  @param timeValue time to be converted to DICOM format
     *  @param dicomTime reference to string variable where the result is stored
     *  @param seconds add optional seconds ("SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getDicomTimeFromOFTime(const OFTime &timeValue,
                                              OFString &dicomTime,
                                              const OFBool seconds = OFTrue,
                                              const OFBool fraction = OFFalse);

    /** get the specified DICOM time value in OFTime format.
     *  Please note that the element value is expected to be in valid DICOM TM format
     *  ("[HH[MM[SS[.FFFFFF]]]]", "[HH[:MM[:SS[.FFFFFF]]]]" is also supported for reasons
     *  of backward compatibility). Since there is no time zone for the DICOM TM format
     *  local time is assumed (the time zone of 'timeValue' is set automatically).
     *  If this function fails the result variable 'timeValue' is cleared automatically.
     *  @param dicomTime string value in DICOM TM format to be converted to ISO format
     *  @param timeValue reference to OFTime variable where the result is stored
     *  @param supportOldFormat if OFTrue support old (prior V3.0) time format (see above)
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getOFTimeFromString(const OFString &dicomTime,
                                           OFTime &timeValue,
                                           const OFBool supportOldFormat = OFTrue);

    /** get the specified DICOM time value in ISO format.
     *  The ISO time format supported by this function is "HH:MM[:SS[.FFFFFF]]"
     *  where the brackets enclose optional parts. Please note that the specified value
     *  is expected to be in valid DICOM TM format ("[HH[MM[SS[.FFFFFF]]]]",
     *  "[HH[:MM[:SS[.FFFFFF]]]]" is also supported for reasons of backward compatibility).
     *  If this function fails the result variable 'formattedTime' is cleared automatically.
     *  @param dicomTime string value in DICOM TM format to be converted to ISO format
     *  @param formattedTime reference to string variable where the result is stored
     *  @param seconds add optional seconds (":SS") if OFTrue
     *  @param fraction add optional fractional part of a second (".FFFFFF") if OFTrue
     *   (requires parameter 'seconds' to be also OFTrue)
     *  @param createMissingPart if OFTrue create optional parts (seconds and/or fractional
     *   part of a seconds) if absent in the DICOM TM value
     *  @param supportOldFormat if OFTrue support old (prior V3.0) time format (see above)
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getISOFormattedTimeFromString(const OFString &dicomTime,
                                                     OFString &formattedTime,
                                                     const OFBool seconds = OFTrue,
                                                     const OFBool fraction = OFFalse,
                                                     const OFBool createMissingPart = OFFalse,
                                                     const OFBool supportOldFormat = OFTrue);

    /** get the specified DICOM time zone in number of hours format
     *  DICOM standard states that if the "Timezone Offset From UTC" attribute (0008,0201) is
     *  present it applies to all TM attributes in the object. This functions allows to convert
     *  the DICOM format ("&ZZZZ" where "&" is "+" or "-" and "ZZZZ" hours and minutes) to a
     *  floating point value, e.g. "+1.0" means plus one hour and "-2.5" minus two and a half
     *  hour, i.e. 2 hours and 30 minutes.
     *  The resulting 'timeZone' value can be used in conjuction with a OFTime object to convert
     *  the time to different time zones (e.g. to local time or UTC).
     *  @param dicomTimeZone string value in DICOM format ("&ZZZZ") to be converted
     *  @param timeZone reference to floating point variable where the resulting UTC offset is stored
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getTimeZoneFromString(const OFString &dicomTimeZone,
                                             double &timeZone);
};


#endif // DCVRTM_H


/*
** CVS/RCS Log:
** $Log: dcvrtm.h,v $
** Revision 1.17  2005/12/08 16:29:11  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.16  2004/07/01 12:28:25  meichel
** Introduced virtual clone method for DcmObject and derived classes.
**
** Revision 1.15  2002/12/06 12:49:19  joergr
** Enhanced "print()" function by re-working the implementation and replacing
** the boolean "showFullData" parameter by a more general integer flag.
** Added doc++ documentation.
** Made source code formatting more consistent with other modules/files.
**
** Revision 1.14  2002/04/25 09:58:07  joergr
** Removed getOFStringArray() implementation.
**
** Revision 1.13  2002/04/11 12:25:10  joergr
** Enhanced DICOM date, time and date/time classes. Added support for new
** standard date and time functions.
**
** Revision 1.12  2001/10/10 15:18:17  joergr
** Added new flag to date/time routines allowing to choose whether the old
** prior V3.0 format for the corresponding DICOM VRs is supported or not.
**
** Revision 1.11  2001/10/01 15:01:40  joergr
** Introduced new general purpose functions to get/set person names, date, time
** and date/time.
**
** Revision 1.10  2001/09/25 17:19:34  meichel
** Adapted dcmdata to class OFCondition
**
** Revision 1.9  2001/06/01 15:48:53  meichel
** Updated copyright header
**
** Revision 1.8  2000/03/08 16:26:26  meichel
** Updated copyright header.
**
** Revision 1.7  1999/03/31 09:25:08  meichel
** Updated copyright header in module dcmdata
**
** Revision 1.6  1998/11/12 16:47:56  meichel
** Implemented operator= for all classes derived from DcmObject.
**
** Revision 1.5  1997/09/11 15:13:17  hewett
** Modified getOFString method arguments by removing a default value
** for the pos argument.  By requiring the pos argument to be provided
** ensures that callers realise getOFString only gets one component of
** a multi-valued string.
**
** Revision 1.4  1997/08/29 08:32:45  andreas
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
** Revision 1.3  1996/01/05 13:23:10  andreas
** - changed to support new streaming facilities
** - more cleanups
** - merged read / write methods for block and file transfer
**
*/
