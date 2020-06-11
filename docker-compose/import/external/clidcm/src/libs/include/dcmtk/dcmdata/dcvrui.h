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
 *  Purpose: Interface of class DcmUniqueIdentifier
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:29:12 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcvrui.h,v $
 *  CVS/RCS Revision: $Revision: 1.20 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCVRUI_H
#define DCVRUI_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#include "dcmtk/dcmdata/dcbytstr.h"


/** a class representing the DICOM value representation 'Unique Identifier' (UI)
 */
class DcmUniqueIdentifier
  : public DcmByteString
{

  public:

    /** constructor.
     *  Create new element from given tag and length.
     *  @param tag DICOM tag for the new element
     *  @param len value length for the new element
     */
    DcmUniqueIdentifier(const DcmTag &tag,
                        const Uint32 len = 0);

    /** copy constructor
     *  @param old element to be copied
     */
    DcmUniqueIdentifier(const DcmUniqueIdentifier &old);

    /** destructor
     */
    virtual ~DcmUniqueIdentifier();

    /** assignment operator
     *  @param obj element to be assigned/copied
     *  @return reference to this object
     */
    DcmUniqueIdentifier &operator=(const DcmUniqueIdentifier &obj);

    /** clone method
     *  @return deep copy of this object
     */
    virtual DcmObject *clone() const
    {
      return new DcmUniqueIdentifier(*this);
    }

    /** get element type identifier
     *  @return type identifier of this class (EVR_UI)
     */
    virtual DcmEVR ident() const;

    /** print element to a stream.
     *  The output format of the value is a backslash separated sequence of string
     *  components. In case of a single component the UID number is mapped to the
     *  corresponding UID name (using "dcmFindNameOfUID()") if available. A "=" is
     *  used as a prefix to distinguish the UID name from the UID number.
     *  NB: this mapping of UID names only works for single-valued strings.
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

    /** set element value from the given character string.
     *  If the string starts with a "=" the subsequent characters are interpreted as a
     *  UID name and mapped to the corresponding UID number (using "dcmFindUIDFromName()")
     *  if possible. Otherwise the leading "=" is removed.
     *  NB: this mapping of UID names only works for single-valued input strings.
     *  @param stringVal input character string (possibly multi-valued)
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition putString(const char *stringVal);


  protected:

    /** convert currently stored string value to internal representation.
     *  It removes any leading, embedded and trailing space character and recomputes
     *  the string length. This manipulation attempts to correct problems with
     *  incorrectly encoded UIDs which have been observed in some images.
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition makeMachineByteString();
};


#endif // DCVRUI_H


/*
** CVS/RCS Log:
** $Log: dcvrui.h,v $
** Revision 1.20  2005/12/08 16:29:12  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.19  2004/07/01 12:28:25  meichel
** Introduced virtual clone method for DcmObject and derived classes.
**
** Revision 1.18  2002/12/06 12:49:19  joergr
** Enhanced "print()" function by re-working the implementation and replacing
** the boolean "showFullData" parameter by a more general integer flag.
** Added doc++ documentation.
** Made source code formatting more consistent with other modules/files.
**
** Revision 1.17  2002/04/25 10:01:21  joergr
** Made makeMachineByteString() virtual to avoid ambiguities.
**
** Revision 1.16  2001/09/25 17:19:35  meichel
** Adapted dcmdata to class OFCondition
**
** Revision 1.15  2001/06/01 15:48:53  meichel
** Updated copyright header
**
** Revision 1.14  2000/04/14 15:31:35  meichel
** Removed default value from output stream passed to print() method.
**   Required for use in multi-thread environments.
**
** Revision 1.13  2000/03/08 16:26:27  meichel
** Updated copyright header.
**
** Revision 1.12  2000/03/03 14:05:28  meichel
** Implemented library support for redirecting error messages into memory
**   instead of printing them to stdout/stderr for GUI applications.
**
** Revision 1.11  2000/02/10 10:50:56  joergr
** Added new feature to dcmdump (enhanced print method of dcmdata): write
** pixel data/item value fields to raw files.
**
** Revision 1.10  1999/03/31 09:25:09  meichel
** Updated copyright header in module dcmdata
**
** Revision 1.9  1998/11/12 16:47:56  meichel
** Implemented operator= for all classes derived from DcmObject.
**
** Revision 1.8  1997/07/21 08:25:16  andreas
** - Replace all boolean types (BOOLEAN, CTNBOOLEAN, DICOM_BOOL, BOOL)
**   with one unique boolean type OFBool.
**
** Revision 1.7  1997/04/18 08:13:33  andreas
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
** Revision 1.6  1996/08/05 08:45:38  andreas
** new print routine with additional parameters:
**         - print into files
**         - fix output length for elements
** corrected error in search routine with parameter ESM_fromStackTop
**
** Revision 1.5  1996/05/30 17:19:22  hewett
** Added a makeMachineByteString() method to strip and trailing whitespace
** from a UID.
**
** Revision 1.4  1996/01/29 13:38:18  andreas
** - new put method for every VR to put value as a string
** - better and unique print methods
**
** Revision 1.3  1996/01/05 13:23:10  andreas
** - changed to support new streaming facilities
** - more cleanups
** - merged read / write methods for block and file transfer
**
**
**
*/

