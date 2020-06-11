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
 *  Author:  Gerd Ehlers, Andreas Barth
 *
 *  Purpose: Interface of class DcmByteString
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:27:59 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcbytstr.h,v $
 *  CVS/RCS Revision: $Revision: 1.30 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DCBYTSTR_H
#define DCBYTSTR_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#include "dcmtk/ofstd/ofconsol.h"
#include "dcmtk/dcmdata/dcerror.h"
#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/dcmdata/dcelem.h"
#include "dcmtk/ofstd/ofstring.h"


/** base class for all DICOM value representations storing a character string
 */
class DcmByteString
  : public DcmElement
{

    /// internal type used to specify the current string representation
    enum E_StringMode
    {
        /// string has internal representation (no padding)
        DCM_MachineString,
        /// string has DICOM representation (even length)
        DCM_DicomString,
        /// string has unknown representation (maybe multiple padding chars?)
        DCM_UnknownString
    };


 public:

    /** constructor.
     *  Create new element from given tag and length.
     *  @param tag DICOM tag for the new element
     *  @param len value length for the new element
     */
    DcmByteString(const DcmTag &tag,
                  const Uint32 len = 0);

    /** copy constructor
     *  @param old element to be copied
     */
    DcmByteString(const DcmByteString &old);

    /** destructor
     */
    virtual ~DcmByteString();

    /** assignment operator
     *  @param obj element to be assigned/copied
     *  @return reference to this object
     */
    DcmByteString& operator=(const DcmByteString& obj);

    /** clone method
     *  @return deep copy of this object
     */
    virtual DcmObject *clone() const
    {
      return new DcmByteString(*this);
    }

    /** get element type identifier
     *  @return type identifier of this class (EVR_UNKNOWN)
     */
    virtual DcmEVR ident() const;

    /** clear the currently stored value
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition clear();

    /** get value multiplicity
     *  @return number of string components (separated by a backslash)
     */
    virtual unsigned long getVM();

    /** get length of the stored value.
     *  Trailing spaces (padding characters) are ignored for the "real" length.
     *  @return number of characters stored for the string value
     */
    Uint32 getRealLength();

    /** get DICOM length of the stored value.
     *  The string value is padded if required.  Therefore, the returned length
     *  always has an even value.
     *  @param xfer not used
     *  @param enctype not used
     *  @return number of characters stored in DICOM representation
     */
    virtual Uint32 getLength(const E_TransferSyntax xfer = EXS_LittleEndianImplicit,
                             const E_EncodingType enctype = EET_UndefinedLength);

    /** print element to a stream.
     *  The output format of the value is a backslash separated sequence of string
     *  components (if any).
     *  @param out output stream
     *  @param flags optional flag used to customize the output (see DCMTypes::PF_xxx)
     *  @param level current level of nested items. Used for indentation.
     *  @param pixelFileName not used
     *  @param pixelCounter not used
     */
    virtual void print(ostream &out,
                       const size_t flags = 0,
                       const int level = 0,
                       const char *pixelFileName = NULL,
                       size_t *pixelCounter = NULL);

    /** write data element to a stream
     *  @param outStream output stream
     *  @param writeXfer transfer syntax used to write the data
     *  @param encodingType flag, specifying the encoding with undefined or explicit length
     */
    virtual OFCondition write(DcmOutputStream &outStream,
                              const E_TransferSyntax writeXfer,
                              const E_EncodingType encodingType = EET_UndefinedLength);

    /** write data element to a stream as required for the creation of digital signatures
     *  @param outStream output stream
     *  @param writeXfer transfer syntax used to write the data
     *  @param encodingType flag, specifying the encoding with undefined or explicit length
     */
    virtual OFCondition writeSignatureFormat(DcmOutputStream &outStream,
                                             const E_TransferSyntax writeXfer,
                                             const E_EncodingType encodingType = EET_UndefinedLength);

    /** get a copy of a particular string component
     *  @param stringVal variable in which the result value is stored
     *  @param pos index of the value in case of multi-valued elements (0..vm-1)
     *  @param normalize not used since string normalization depends on value representation
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition getOFString(OFString &stringVal,
                                    const unsigned long pos,
                                    OFBool normalize = OFTrue);

    /** get a pointer to the current string value.
     *  This includes all string components and separators. NB: this method does
     *  not copy the stored value.
     *  @param stringVal reference to the pointer variable
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition getString(char *&stringVal);

    /** set element value from the given character string
     *  @param stringVal input character string (possibly multi-valued)
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition putString(const char *stringVal);

    /** set element value from the given character string.
     *  @param stringVal input character string (possibly multi-valued)
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition putOFStringArray(const OFString &stringVal);

    /** check the currently stored string value.
     *  Checks every string component for the maximum length specified for the particular
     *  value representation.
     *  @param autocorrect correct value and value component length if OFTrue
     *  @return status, EC_Normal if value length is correct, an error code otherwise
     */
    virtual OFCondition verify(const OFBool autocorrect = OFFalse);


 protected:

    /** create a new value field (string buffer) of the previously defined size
     *  (member variable 'Length'). Also handles odd value length by allocating
     *  extra space for the padding character.
     *  This method is used by derived classes only.
     *  @return pointer to the newly created value field
     */
    virtual Uint8 *newValueField();

    /** method is called after the element value has been loaded.
     *  Can be used to correct the value before it is used for the first time.
     */
    virtual void postLoadValue();

    /** convert currently stored string value to internal representation.
     *  It removes any trailing space character and recomputes the string length.
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition makeMachineByteString();

    /** convert currently stored string value to DICOM representation.
     *  It removes trailing spaces apart from a possibly required single padding
     *  character (in case of odd string length).
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    OFCondition makeDicomByteString();

    /** get a copy of the current string value.
     *  This includes all string components and separators.
     *  @param stringVal variable in which the result is stored
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    OFCondition getStringValue(OFString &stringVal);

    /// padding character used to adjust odd value length (space)
    char paddingChar;
    /// maximum number of characters for each string component
    Uint32 maxLength;


 private:

    /// number of characters of the internal string representation
    Uint32 realLength;
    /// current representation of the string value
    E_StringMode fStringMode;
};


/** @name string normalization flags.
 *  These flags can be used with normalizeString() to specify the extent of normalization.
 */
//@{

/// delete trailing spaces
const OFBool DELETE_TRAILING = OFTrue;
/// delete leading spaces
const OFBool DELETE_LEADING = OFTrue;
/// handle string as multi-valued (components separated by a backslash)
const OFBool MULTIPART = OFTrue;

//@}


/* Function to get part out of a String for VM > 1 */

/** extract particular component from a string value.
 *  String components are expected to be separated by a backslash character ('\').
 *  @param result reference to the result variable
 *  @param orgStr input string value
 *  @param pos index of the string component to be extracted (0..vm-1)
 *  @return status, EC_Normal if successful, an error code otherwise
 */
OFCondition getStringPart(OFString &result,
                          const char *orgStr,
                          const unsigned long pos);


/** normalize the given string value, i.e. remove leading and/or trailing spaces
 *  @param string input and output string value to be normalized
 *  @param multiPart handle string as multi-valued if OFTrue
 *  @param leading delete leading spaces if OFTrue
 *  @param trailing delete trailing spaces if OFTrue
 */
void normalizeString(OFString &string,
                     const OFBool multiPart,
                     const OFBool leading,
                     const OFBool trailing);


#endif // DCBYTSTR_H


/*
** CVS/RCS Log:
** $Log: dcbytstr.h,v $
** Revision 1.30  2005/12/08 16:27:59  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.29  2004/07/01 12:28:25  meichel
** Introduced virtual clone method for DcmObject and derived classes.
**
** Revision 1.28  2003/07/04 13:25:35  meichel
** Replaced forward declarations for OFString with explicit includes,
**   needed when compiling with HAVE_STD_STRING
**
** Revision 1.27  2003/06/12 13:35:54  joergr
** Fixed inconsistent API documentation reported by Doxygen.
**
** Revision 1.26  2002/12/10 17:41:22  meichel
** Removed typedef to avoid warnings on various compilers
**
** Revision 1.25  2002/12/06 12:49:07  joergr
** Enhanced "print()" function by re-working the implementation and replacing
** the boolean "showFullData" parameter by a more general integer flag.
** Added doc++ documentation.
** Made source code formatting more consistent with other modules/files.
**
** Revision 1.24  2002/08/27 16:55:30  meichel
** Initial release of new DICOM I/O stream classes that add support for stream
**   compression (deflated little endian explicit VR transfer syntax)
**
** Revision 1.23  2002/04/25 10:05:14  joergr
** Removed getOFStringArray() implementation.
** Made makeMachineByteString() virtual to avoid ambiguities.
**
** Revision 1.22  2001/09/25 17:19:24  meichel
** Adapted dcmdata to class OFCondition
**
** Revision 1.21  2001/06/01 15:48:33  meichel
** Updated copyright header
**
** Revision 1.20  2000/11/07 16:56:05  meichel
** Initial release of dcmsign module for DICOM Digital Signatures
**
** Revision 1.19  2000/04/14 15:31:31  meichel
** Removed default value from output stream passed to print() method.
**   Required for use in multi-thread environments.
**
** Revision 1.18  2000/03/08 16:26:11  meichel
** Updated copyright header.
**
** Revision 1.17  2000/03/03 14:05:22  meichel
** Implemented library support for redirecting error messages into memory
**   instead of printing them to stdout/stderr for GUI applications.
**
** Revision 1.16  2000/02/10 10:50:49  joergr
** Added new feature to dcmdump (enhanced print method of dcmdata): write
** pixel data/item value fields to raw files.
**
** Revision 1.15  1999/03/31 09:24:30  meichel
** Updated copyright header in module dcmdata
**
** Revision 1.14  1998/11/12 16:47:36  meichel
** Implemented operator= for all classes derived from DcmObject.
**
** Revision 1.13  1997/09/11 15:13:09  hewett
** Modified getOFString method arguments by removing a default value
** for the pos argument.  By requiring the pos argument to be provided
** ensures that callers realise getOFString only gets one component of
** a multi-valued string.
**
** Revision 1.12  1997/08/29 13:11:38  andreas
** Corrected Bug in getOFStringArray Implementation
**
** Revision 1.11  1997/08/29 08:32:37  andreas
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
** Revision 1.10  1997/07/21 08:25:05  andreas
** - Replace all boolean types (BOOLEAN, CTNBOOLEAN, DICOM_BOOL, BOOL)
**   with one unique boolean type OFBool.
**
** Revision 1.9  1997/05/16 08:31:19  andreas
** - Revised handling of GroupLength elements and support of
**   DataSetTrailingPadding elements. The enumeratio E_GrpLenEncoding
**   got additional enumeration values (for a description see dctypes.h).
**   addGroupLength and removeGroupLength methods are replaced by
**   computeGroupLengthAndPadding. To support Padding, the parameters of
**   element and sequence write functions changed.
**
** Revision 1.8  1997/04/18 08:13:28  andreas
** - The put/get-methods for all VRs did not conform to the C++-Standard
**   draft. Some Compilers (e.g. SUN-C++ Compiler, Metroworks
**   CodeWarrier, etc.) create many warnings concerning the hiding of
**   overloaded get methods in all derived classes of DcmElement.
**   So the interface of all value representation classes in the
**   library are changed rapidly, e.g.
**   OFCondition get(Uint16 & value, const unsigned long pos);
**   becomes
**   OFCondition getUint16(Uint16 & value, const unsigned long pos);
**   All (retired) "returntype get(...)" methods are deleted.
**   For more information see dcmdata/include/dcelem.h
**
** Revision 1.7  1996/08/05 08:45:15  andreas
** new print routine with additional parameters:
**         - print into files
**         - fix output length for elements
** corrected error in search routine with parameter ESM_fromStackTop
**
** Revision 1.6  1996/03/12 15:26:52  hewett
** Removed get method for unsigned char*
**
** Revision 1.5  1996/01/29 13:38:11  andreas
** - new put method for every VR to put value as a string
** - better and unique print methods
**
** Revision 1.4  1996/01/09 11:06:13  andreas
** New Support for Visual C++
** Correct problems with inconsistent const declarations
**
** Revision 1.3  1996/01/05 13:22:52  andreas
** - changed to support new streaming facilities
** - more cleanups
** - merged read / write methods for block and file transfer
**
*/
