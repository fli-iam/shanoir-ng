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
 *  Purpose: Interface of class DcmSequenceOfItems
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:28:41 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcsequen.h,v $
 *  CVS/RCS Revision: $Revision: 1.32 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCSEQUEN_H
#define DCSEQUEN_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#include "dcmtk/ofstd/ofconsol.h"
#include "dcmtk/dcmdata/dcerror.h"
#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/dcmdata/dcobject.h"
#include "dcmtk/dcmdata/dcitem.h"
#include "dcmtk/dcmdata/dctag.h"
#include "dcmtk/dcmdata/dclist.h"
#include "dcmtk/dcmdata/dcstack.h"


//
// CLASS DcmSequenceOfItems
// A sequence has no explicit value. Therefore, it should be derived from
// DcmObject. Since a sequence is created in an (pseudo)-item and items collect
// sequences of elements the sequence Tag is derived from element.

class DcmSequenceOfItems : public DcmElement
{
protected:
    DcmList *itemList;
    OFBool lastItemComplete;
    Uint32 fStartPosition;

    virtual OFCondition readTagAndLength(DcmInputStream &inStream,            // inout
                                         const E_TransferSyntax xfer,         // in
                                         DcmTag &tag,                         // out
                                         Uint32 &length);                     // out

    virtual OFCondition makeSubObject(DcmObject *&subObject,
                                      const DcmTag &mewTag,
                                      const Uint32 newLength);

    OFCondition readSubItem(DcmInputStream &inStream,                         // inout
                            const DcmTag &newTag,                             // in
                            const Uint32 newLength,                           // in
                            const E_TransferSyntax xfer,                      // in
                            const E_GrpLenEncoding glenc,                     // in
                            const Uint32 maxReadLength = DCM_MaxReadLength);  // in

    virtual OFCondition searchSubFromHere(const DcmTagKey &tag,               // in
                                          DcmStack &resultStack,              // inout
                                          const OFBool searchIntoSub);        // in


public:
    DcmSequenceOfItems(const DcmTag &tag, const Uint32 len = 0, OFBool readAsUN = OFFalse);
    DcmSequenceOfItems(const DcmSequenceOfItems& oldSeq);
    virtual ~DcmSequenceOfItems();

    DcmSequenceOfItems &operator=(const DcmSequenceOfItems &obj);

    /** clone method
     *  @return deep copy of this object
     */
    virtual DcmObject *clone() const
    {
      return new DcmSequenceOfItems(*this);
    }

    virtual DcmEVR ident() const { return EVR_SQ; }
    virtual OFBool isLeaf() const { return OFFalse; }

    virtual void print(ostream &out,
                       const size_t flags = 0,
                       const int level = 0,
                       const char *pixelFileName = NULL,
                       size_t *pixelCounter = NULL);

    virtual unsigned long getVM() { return 1L; }

    virtual OFCondition computeGroupLengthAndPadding
                            (const E_GrpLenEncoding glenc,
                             const E_PaddingEncoding padenc = EPD_noChange,
                             const E_TransferSyntax xfer = EXS_Unknown,
                             const E_EncodingType enctype = EET_ExplicitLength,
                             const Uint32 padlen = 0,
                             const Uint32 subPadlen = 0,
                             Uint32 instanceLength = 0);

    virtual Uint32 calcElementLength(const E_TransferSyntax xfer,
                                     const E_EncodingType enctype);

    virtual Uint32 getLength(const E_TransferSyntax xfer = EXS_LittleEndianImplicit,
                             const E_EncodingType enctype = EET_UndefinedLength);

    virtual void transferInit();
    virtual void transferEnd();

    virtual OFBool canWriteXfer(const E_TransferSyntax oldXfer,
                                const E_TransferSyntax newXfer);

    virtual OFCondition read(DcmInputStream &inStream,
                             const E_TransferSyntax xfer,
                             const E_GrpLenEncoding glenc = EGL_noChange,
                             const Uint32 maxReadLength = DCM_MaxReadLength);

    virtual OFCondition write(DcmOutputStream &outStream,
                              const E_TransferSyntax oxfer,
                              const E_EncodingType enctype = EET_UndefinedLength);

    /** write object in XML format
     *  @param out output stream to which the XML document is written
     *  @param flags optional flag used to customize the output (see DCMTypes::XF_xxx)
     *  @return status, EC_Normal if successful, an error code otherwise
     */
    virtual OFCondition writeXML(ostream &out,
                                 const size_t flags = 0);

    /** special write method for creation of digital signatures
     */
    virtual OFCondition writeSignatureFormat(DcmOutputStream &outStream,
                                             const E_TransferSyntax oxfer,
                                             const E_EncodingType enctype = EET_UndefinedLength);

    /** returns true if the current object may be included in a digital signature
     *  @return true if signable, false otherwise
     */
    virtual OFBool isSignable() const;

    /** returns true if the object contains an element with Unknown VR at any nesting level
     *  @return true if the object contains an element with Unknown VR, false otherwise
     */
    virtual OFBool containsUnknownVR() const;

    virtual unsigned long card();

    virtual OFCondition prepend(DcmItem *item);
    virtual OFCondition insert(DcmItem *item,
                               unsigned long where = DCM_EndOfListIndex,
                               OFBool before = OFFalse);
    virtual OFCondition append(DcmItem *item);

    /** insert new item a current position.
     *  The current position is stored internally in the 'itemList' member variable.
     *  @param item new item to be inserted
     *  @param before flag indicating whether to insert the item before (OFFalse) or
     *    after (OFTrue) the current position
     *  @return status, EC_Normal upon success, an error code otherwise
     */
    virtual OFCondition insertAtCurrentPos(DcmItem *item,
                                           OFBool before = OFFalse);

    virtual DcmItem *getItem(const unsigned long num);
    virtual OFCondition nextObject(DcmStack &stack, const OFBool intoSub);
    virtual DcmObject *nextInContainer(const DcmObject *obj);
    virtual DcmItem *remove(const unsigned long num);
    virtual DcmItem *remove(DcmItem *item);
    virtual OFCondition clear();
    virtual OFCondition verify(const OFBool autocorrect = OFFalse);
    virtual OFCondition search(const DcmTagKey &xtag,             // in
                               DcmStack &resultStack,             // inout
                               E_SearchMode mode = ESM_fromHere,  // in
                               OFBool searchIntoSub = OFTrue);    // in
    virtual OFCondition searchErrors(DcmStack &resultStack);      // inout
    virtual OFCondition loadAllDataIntoMemory(void);

private:

  /* static helper method used in writeSignatureFormat().
   * This function resembles DcmObject::writeTagAndLength()
   * but only writes the tag, VR and reserved field.
   * @param outStream stream to write to
   * @param tag attribute tag
   * @param vr attribute VR as reported by getVR
   * @param oxfer output transfer syntax
   * @return EC_Normal if successful, an error code otherwise
   */
  static OFCondition writeTagAndVR(DcmOutputStream &outStream,
                                   const DcmTag &tag,
                                   DcmEVR vr,
                                   const E_TransferSyntax oxfer);

  /** true if this element has been instantiated while reading an UN element
   *  with undefined length
   */
  OFBool readAsUN_;

};


#endif // DCSEQUEN_H


/*
** CVS/RCS Log:
** $Log: dcsequen.h,v $
** Revision 1.32  2005/12/08 16:28:41  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.31  2005/05/10 15:27:14  meichel
** Added support for reading UN elements with undefined length according
**   to CP 246. The global flag dcmEnableCP246Support allows to revert to the
**   prior behaviour in which UN elements with undefined length were parsed
**   like a normal explicit VR SQ element.
**
** Revision 1.30  2004/07/01 12:28:25  meichel
** Introduced virtual clone method for DcmObject and derived classes.
**
** Revision 1.29  2003/08/08 13:29:13  joergr
** Added new method insertAtCurrentPos() which allows for a much more efficient
** insertion (avoids re-searching for the correct position).
**
** Revision 1.28  2002/12/06 12:49:13  joergr
** Enhanced "print()" function by re-working the implementation and replacing
** the boolean "showFullData" parameter by a more general integer flag.
** Added doc++ documentation.
** Made source code formatting more consistent with other modules/files.
**
** Revision 1.27  2002/08/27 16:55:39  meichel
** Initial release of new DICOM I/O stream classes that add support for stream
**   compression (deflated little endian explicit VR transfer syntax)
**
** Revision 1.26  2002/04/25 09:43:56  joergr
** Added support for XML output of DICOM objects.
**
** Revision 1.25  2001/11/19 15:23:10  meichel
** Cleaned up signature code to avoid some gcc warnings.
**
** Revision 1.24  2001/11/16 15:54:39  meichel
** Adapted digital signature code to final text of supplement 41.
**
** Revision 1.23  2001/09/25 17:19:28  meichel
** Adapted dcmdata to class OFCondition
**
** Revision 1.22  2001/06/01 15:48:43  meichel
** Updated copyright header
**
** Revision 1.21  2000/11/07 16:56:09  meichel
** Initial release of dcmsign module for DICOM Digital Signatures
**
** Revision 1.20  2000/04/14 15:31:33  meichel
** Removed default value from output stream passed to print() method.
**   Required for use in multi-thread environments.
**
** Revision 1.19  2000/03/08 16:26:17  meichel
** Updated copyright header.
**
** Revision 1.18  2000/03/03 14:05:25  meichel
** Implemented library support for redirecting error messages into memory
**   instead of printing them to stdout/stderr for GUI applications.
**
** Revision 1.17  2000/02/10 10:50:53  joergr
** Added new feature to dcmdump (enhanced print method of dcmdata): write
** pixel data/item value fields to raw files.
**
** Revision 1.16  1999/03/31 09:24:46  meichel
** Updated copyright header in module dcmdata
**
** Revision 1.15  1998/11/12 16:47:44  meichel
** Implemented operator= for all classes derived from DcmObject.
**
** Revision 1.14  1998/07/15 15:48:52  joergr
** Removed several compiler warnings reported by gcc 2.8.1 with
** additional options, e.g. missing copy constructors and assignment
** operators, initialization of member variables in the body of a
** constructor instead of the member initialization list, hiding of
** methods by use of identical names, uninitialized member variables,
** missing const declaration of char pointers. Replaced tabs by spaces.
**
** Revision 1.13  1997/07/21 08:25:10  andreas
** - Replace all boolean types (BOOLEAN, CTNBOOLEAN, DICOM_BOOL, BOOL)
**   with one unique boolean type OFBool.
**
** Revision 1.12  1997/07/07 07:42:05  andreas
** - Changed parameter type DcmTag & to DcmTagKey & in all search functions
**   in DcmItem, DcmSequenceOfItems, DcmDirectoryRecord and DcmObject
**
** Revision 1.11  1997/05/27 13:48:29  andreas
** - Add method canWriteXfer to class DcmObject and all derived classes.
**   This method checks whether it is possible to convert the original
**   transfer syntax to an new transfer syntax. The check is used in the
**   dcmconv utility to prohibit the change of a compressed transfer
**   syntax to a uncompressed.
**
** Revision 1.10  1997/05/16 08:23:48  andreas
** - Revised handling of GroupLength elements and support of
**   DataSetTrailingPadding elements. The enumeratio E_GrpLenEncoding
**   got additional enumeration values (for a description see dctypes.h).
**   addGroupLength and removeGroupLength methods are replaced by
**   computeGroupLengthAndPadding. To support Padding, the parameters of
**   element and sequence write functions changed.
** - Added a new method calcElementLength to calculate the length of an
**   element, item or sequence. For elements it returns the length of
**   tag, length field, vr field, and value length, for item and
**   sequences it returns the length of the whole item. sequence including
**   the Delimitation tag (if appropriate).  It can never return
**   UndefinedLength.
**
** Revision 1.9  1997/04/24 12:09:02  hewett
** Fixed DICOMDIR generation bug affecting ordering of
** patient/study/series/image records (item insertion into a sequence
** did produce the expected ordering).
**
** Revision 1.8  1996/08/05 08:45:28  andreas
** new print routine with additional parameters:
**         - print into files
**         - fix output length for elements
** corrected error in search routine with parameter ESM_fromStackTop
**
** Revision 1.7  1996/07/17 12:38:59  andreas
** new nextObject to iterate a DicomDataset, DicomFileFormat, Item, ...
**
** Revision 1.6  1996/01/29 13:38:14  andreas
** - new put method for every VR to put value as a string
** - better and unique print methods
**
** Revision 1.5  1996/01/24 09:34:56  andreas
** Support for 64 bit long
**
** Revision 1.4  1996/01/09 11:06:16  andreas
** New Support for Visual C++
** Correct problems with inconsistent const declarations
**
** Revision 1.3  1996/01/05 13:22:59  andreas
** - changed to support new streaming facilities
** - more cleanups
** - merged read / write methods for block and file transfer
**
*/
