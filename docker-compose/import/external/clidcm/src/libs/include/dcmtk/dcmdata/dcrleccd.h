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
 *  Author:  Marco Eichelberg
 *
 *  Purpose: decoder codec class for RLE
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:28:33 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcrleccd.h,v $
 *  CVS/RCS Revision: $Revision: 1.3 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCRLECCD_H
#define DCRLECCD_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/ofstd/oftypes.h"
#include "dcmtk/dcmdata/dccodec.h"  /* for class DcmCodec */

/** decoder class for RLE.
 *  This class only supports decompression, it neither implements
 *  encoding nor transcoding.
 */
class DcmRLECodecDecoder: public DcmCodec
{
public:  
 
  /// default constructor
  DcmRLECodecDecoder();

  /// destructor
  virtual ~DcmRLECodecDecoder();

  /** decompresses the given pixel sequence and
   *  stores the result in the given uncompressedPixelData element.
   *  @param fromRepParam current representation parameter of compressed data, may be NULL
   *  @param pixSeq compressed pixel sequence
   *  @param uncompressedPixelData uncompressed pixel data stored in this element
   *  @param cp codec parameters for this codec
   *  @param objStack stack pointing to the location of the pixel data
   *    element in the current dataset.
   *  @return EC_Normal if successful, an error code otherwise.
   */
  virtual OFCondition decode(
    const DcmRepresentationParameter * fromRepParam,
    DcmPixelSequence * pixSeq,
    DcmPolymorphOBOW& uncompressedPixelData,
    const DcmCodecParameter * cp,
    const DcmStack& objStack) const;

  /** compresses the given uncompressed DICOM image and stores
   *  the result in the given pixSeq element.
   *  @param pixelData pointer to the uncompressed image data in OW format
   *    and local byte order
   *  @param length of the pixel data field in bytes
   *  @param toRepParam representation parameter describing the desired
   *    compressed representation (e.g. JPEG quality)
   *  @param pixSeq compressed pixel sequence (pointer to new DcmPixelSequence object
   *    allocated on heap) returned in this parameter upon success.   
   *  @param cp codec parameters for this codec
   *  @param objStack stack pointing to the location of the pixel data
   *    element in the current dataset.
   *  @return EC_Normal if successful, an error code otherwise.
   */
  virtual OFCondition encode(
    const Uint16 * pixelData,
    const Uint32 length,
    const DcmRepresentationParameter * toRepParam,
    DcmPixelSequence * & pixSeq,
    const DcmCodecParameter *cp,
    DcmStack & objStack) const;

  /** transcodes (re-compresses) the given compressed DICOM image and stores
   *  the result in the given toPixSeq element.
   *  @param fromRepType current transfer syntax of the compressed image
   *  @param fromRepParam current representation parameter of compressed data, may be NULL
   *  @param fromPixSeq compressed pixel sequence
   *  @param toRepParam representation parameter describing the desired
   *    new compressed representation (e.g. JPEG quality)
   *  @param toPixSeq compressed pixel sequence (pointer to new DcmPixelSequence object
   *    allocated on heap) returned in this parameter upon success.   
   *  @param cp codec parameters for this codec
   *  @param objStack stack pointing to the location of the pixel data
   *    element in the current dataset.
   *  @return EC_Normal if successful, an error code otherwise.
   */
  virtual OFCondition encode(
    const E_TransferSyntax fromRepType,
    const DcmRepresentationParameter * fromRepParam,
    DcmPixelSequence * fromPixSeq,
    const DcmRepresentationParameter * toRepParam,
    DcmPixelSequence * & toPixSeq,
    const DcmCodecParameter * cp,
    DcmStack & objStack) const;

  /** checks if this codec is able to convert from the
   *  given current transfer syntax to the given new
   *  transfer syntax
   *  @param oldRepType current transfer syntax
   *  @param newRepType desired new transfer syntax
   *  @return true if transformation is supported by this codec, false otherwise.
   */
  virtual OFBool canChangeCoding(
    const E_TransferSyntax oldRepType,
    const E_TransferSyntax newRepType) const;

private:

  /// private undefined copy constructor
  DcmRLECodecDecoder(const DcmRLECodecDecoder&);
  
  /// private undefined copy assignment operator
  DcmRLECodecDecoder& operator=(const DcmRLECodecDecoder&);

};

#endif

/*
 * CVS/RCS Log
 * $Log: dcrleccd.h,v $
 * Revision 1.3  2005/12/08 16:28:33  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.2  2003/03/21 13:06:46  meichel
 * Minor code purifications for warnings reported by MSVC in Level 4
 *
 * Revision 1.1  2002/06/06 14:52:34  meichel
 * Initial release of the new RLE codec classes
 *   and the dcmcrle/dcmdrle tools in module dcmdata
 *
 *
 */
