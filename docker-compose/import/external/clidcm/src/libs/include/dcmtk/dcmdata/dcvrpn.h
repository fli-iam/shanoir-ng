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
 *  Author:  Gerd Ehlers
 *
 *  Purpose: Interface of class DcmPersonName
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:29:05 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcvrpn.h,v $
 *  CVS/RCS Revision: $Revision: 1.17 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DCVRPN_H
#define DCVRPN_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#include "dcmtk/dcmdata/dcchrstr.h"


/** a class representing the DICOM value representation 'Person Name' (PN)
 */
class DcmPersonName
  : public DcmCharString
{

  public:

    /** constructor.
     *  Create new element from given tag and length.
     *  @param tag DICOM tag for the new element
     *  @param len value length for the new element
     */
    DcmPersonName(const DcmTag &tag,
                  const Uint32 len = 0);

    /** copy constructor
     *  @param old element to be copied
     */
    DcmPersonName(const DcmPersonName &old);

    /** destructor
     */
    virtual ~DcmPersonName();

    /** assignment operator
     *  @param obj element to be assigned/copied
     *  @return reference to this object
     */
    DcmPersonName &operator=(const DcmPersonName &obj);

    /** clone method
     *  @return deep copy of this object
     */
    virtual DcmObject *clone() const
    {
      return new DcmPersonName(*this);
    }

    /** get element type identifier
     *  @return type identifier of this class (EVR_PN)
     */
    virtual DcmEVR ident() const;

    /** get a copy of a particular string component
     *  @param stringVal variable in which the result value is stored
     *  @param pos index of the value in case of multi-valued elements (0..vm-1)
     *  @param normalize delete leading and trailing spaces if OFTrue
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition getOFString(OFString &stringVal,
                                    const unsigned long pos,
                                    OFBool normalize = OFTrue);

    /** get name components from the element value.
     *  The DICOM PN consists of up to three component groups separated by a "=". The
     *  supported format is "[CG0][=CG1][=CG2]" where the brackets enclose optional
     *  parts and CG0 is a single-byte character representation, CG1 an ideographic
     *  representation, and CG2 a phonetic representation of the name.
     *  Each component group may consist of up to five components separated by a "^".
     *  The format is "[lastName[^firstName[^middleName[^namePrefix[^nameSuffix]]]]";
     *  each component might be empty.
     *  If this function fails the result variables are cleared automatically. If the
     *  format is valid but does not comply with the above described scheme ("=" and "^")
     *  the full person name is returned in the 'lastName' variable.
     *  @param lastName reference to string variable where the "last name" is stored
     *  @param firstName reference to string variable where the "first name" is stored
     *  @param middleName reference to string variable where the "middle name" is stored
     *  @param namePrefix reference to string variable where the "name prefix" is stored
     *  @param nameSuffix reference to string variable where the "name suffix" is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @param componentGroup index of the component group (0..2) to be used, see above
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getNameComponents(OFString &lastName,
                                  OFString &firstName,
                                  OFString &middleName,
                                  OFString &namePrefix,
                                  OFString &nameSuffix,
                                  const unsigned long pos = 0,
                                  const unsigned int componentGroup = 0);

    /** get current element value as a formatted/readable name.
     *  The current element value is expected to be in DICOM PN format as described above.
     *  The output format is "[namePrefix][ firstName][ middleName][ lastName][, nameSuffix]";
     *  the delimiters (" " and ", ") are only inserted if required.
     *  If this function fails the result variable 'formattedName' is cleared automatically.
     *  @param formattedName reference to string variable where the result is stored
     *  @param pos index of the element component in case of value multiplicity (0..vm-1)
     *  @param componentGroup index of the component group (0..2) to be used, see above
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition getFormattedName(OFString &formattedName,
                                 const unsigned long pos = 0,
                                 const unsigned int componentGroup = 0);


    /** put element value from specified name components.
     *  The stored format is "[lastName[^firstName[^middleName[^namePrefix[^nameSuffix]]]]]",
     *  i.e. a DICOM Person Name (PN). Component groups are not (yet) supported.
     *  If this function fails the currently stored value is not modified.
     *  @param lastName reference to string variable where the "last name" is stored
     *  @param firstName reference to string variable where the "first name" is stored
     *  @param middleName reference to string variable where the "middle name" is stored
     *  @param namePrefix reference to string variable where the "name prefix" is stored
     *  @param nameSuffix reference to string variable where the "name suffix" is stored
     *  @return EC_Normal upon success, an error code otherwise
     */
    OFCondition putNameComponents(const OFString &lastName,
                                  const OFString &firstName,
                                  const OFString &middleName,
                                  const OFString &namePrefix,
                                  const OFString &nameSuffix);

    /* --- static helper functions --- */

    /** get name components from specified DICOM person name.
     *  The DICOM PN consists of up to three component groups separated by a "=". The
     *  supported format is "[CG0][=CG1][=CG2]" where the brackets enclose optional
     *  parts and CG0 is a single-byte character representation, CG1 an ideographic
     *  representation, and CG2 a phonetic representation of the name.
     *  Each component group may consist of up to five components separated by a "^".
     *  The format is "[lastName[^firstName[^middleName[^namePrefix[^nameSuffix]]]]";
     *  each component might be empty.
     *  If this function fails the result variables are cleared automatically. If the
     *  format is valid but does not comply with the above described scheme ("=" and "^")
     *  the full person name is returned in the 'lastName' variable.
     *  @param dicomName string value in DICOM PN format to be split into components
     *  @param lastName reference to string variable where the "last name" is stored
     *  @param firstName reference to string variable where the "first name" is stored
     *  @param middleName reference to string variable where the "middle name" is stored
     *  @param namePrefix reference to string variable where the "name prefix" is stored
     *  @param nameSuffix reference to string variable where the "name suffix" is stored
     *  @param componentGroup index of the component group (0..2) to be used, see above
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getNameComponentsFromString(const OFString &dicomName,
                                                   OFString &lastName,
                                                   OFString &firstName,
                                                   OFString &middleName,
                                                   OFString &namePrefix,
                                                   OFString &nameSuffix,
                                                   const unsigned int componentGroup = 0);

    /** get specified DICOM person name as a formatted/readable name.
     *  The specified 'dicomName' is expected to be in DICOM PN format as described above.
     *  The output format is "[namePrefix][ firstName][ middleName][ lastName][, nameSuffix]";
     *  the delimiters (" " and ", ") are only inserted if required.
     *  If this function fails the result variable 'formattedName' is cleared automatically.
     *  @param dicomName string value in DICOM PN format to be converted to readable format
     *  @param formattedName reference to string variable where the result is stored
     *  @param componentGroup index of the component group (0..2) to be used, see above
     *  @return EC_Normal upon success, an error code otherwise
     */
    static OFCondition getFormattedNameFromString(const OFString &dicomName,
                                                  OFString &formattedName,
                                                  const unsigned int componentGroup = 0);

    /** get formatted/readable name from specified name components.
     *  The output format is "[namePrefix][ firstName][ middleName][ lastName][, nameSuffix]";
     *  the delimiters (" " and ", ") are only inserted if required.
     *  If this function fails the result variable 'formattedName' is cleared automatically.
     *  @param lastName reference to string variable where the "last name" is stored
     *  @param firstName reference to string variable where the "first name" is stored
     *  @param middleName reference to string variable where the "middle name" is stored
     *  @param namePrefix reference to string variable where the "name prefix" is stored
     *  @param nameSuffix reference to string variable where the "name suffix" is stored
     *  @param formattedName reference to string variable where the result is stored
     *  @return always returns EC_Normal
     */
    static OFCondition getFormattedNameFromComponents(const OFString &lastName,
                                                      const OFString &firstName,
                                                      const OFString &middleName,
                                                      const OFString &namePrefix,
                                                      const OFString &nameSuffix,
                                                      OFString &formattedName);

    /** get DICOM Person Name (PN) from specified name components.
     *  The output format is "[lastName[^firstName[^middleName[^namePrefix[^nameSuffix]]]]]".
     *  Component groups are not (yet) supported.
     *  If this function fails the result variable 'dicomName' is cleared automatically.
     *  @param lastName reference to string variable where the "last name" is stored
     *  @param firstName reference to string variable where the "first name" is stored
     *  @param middleName reference to string variable where the "middle name" is stored
     *  @param namePrefix reference to string variable where the "name prefix" is stored
     *  @param nameSuffix reference to string variable where the "name suffix" is stored
     *  @param dicomName reference to string variable where the result is stored
     *  @return always returns EC_Normal
     */
    static OFCondition getStringFromNameComponents(const OFString &lastName,
                                                   const OFString &firstName,
                                                   const OFString &middleName,
                                                   const OFString &namePrefix,
                                                   const OFString &nameSuffix,
                                                   OFString &dicomName);
};


#endif // DCVRPN_H


/*
** CVS/RCS Log:
** $Log: dcvrpn.h,v $
** Revision 1.17  2005/12/08 16:29:05  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.16  2004/07/01 12:28:25  meichel
** Introduced virtual clone method for DcmObject and derived classes.
**
** Revision 1.15  2003/05/20 08:56:20  joergr
** Added methods and static functions to compose a DICOM Person Name from five
** name components.
**
** Revision 1.14  2002/12/06 12:49:17  joergr
** Enhanced "print()" function by re-working the implementation and replacing
** the boolean "showFullData" parameter by a more general integer flag.
** Added doc++ documentation.
** Made source code formatting more consistent with other modules/files.
**
** Revision 1.13  2002/04/25 09:56:19  joergr
** Removed getOFStringArray() implementation.
**
** Revision 1.12  2001/10/10 15:17:38  joergr
** Updated comments.
**
** Revision 1.11  2001/10/01 15:01:39  joergr
** Introduced new general purpose functions to get/set person names, date, time
** and date/time.
**
** Revision 1.10  2001/09/25 17:19:33  meichel
** Adapted dcmdata to class OFCondition
**
** Revision 1.9  2001/06/01 15:48:51  meichel
** Updated copyright header
**
** Revision 1.8  2000/03/08 16:26:25  meichel
** Updated copyright header.
**
** Revision 1.7  1999/03/31 09:25:04  meichel
** Updated copyright header in module dcmdata
**
** Revision 1.6  1998/11/12 16:47:52  meichel
** Implemented operator= for all classes derived from DcmObject.
**
** Revision 1.5  1997/09/11 15:13:16  hewett
** Modified getOFString method arguments by removing a default value
** for the pos argument.  By requiring the pos argument to be provided
** ensures that callers realise getOFString only gets one component of
** a multi-valued string.
**
** Revision 1.4  1997/08/29 08:32:43  andreas
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
** Revision 1.3  1996/01/05 13:23:08  andreas
** - changed to support new streaming facilities
** - more cleanups
** - merged read / write methods for block and file transfer
**
*/
