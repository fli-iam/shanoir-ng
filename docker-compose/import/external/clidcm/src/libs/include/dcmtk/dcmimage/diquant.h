/*
 *
 *  Copyright (C) 2002-2005, OFFIS
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
 *  Module:  dcmimage
 *
 *  Author:  Marco Eichelberg
 *
 *  Purpose: class DcmQuantFloydSteinberg
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:01:56 $
 *  CVS/RCS Revision: $Revision: 1.5 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIQUANT_H
#define DIQUANT_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/ofstd/ofcond.h"    /* for OFCondition */
#include "dcmtk/dcmdata/dctypes.h"   /* for Uint32 etc. */
#include "dcmtk/dcmimage/diqttype.h"  /* for enums */
#include "dcmtk/ofstd/ofstring.h"   /* for class OFString */


class DicomImage;
class DcmItem;
class DcmTagKey;


class DcmQuant
{
public:

  /** converts the given color image into a palette color image.
   *  All frames of the image are converted.  The converted result
   *  is written as a complete Image Pixel module to the given
   *  target item.
   *  @param sourceImage DICOM color image
   *  @param target target item to which the palette color image is written
   *  @param writeAsOW if true, the LUT Data attributes are encoded as OW instead
   *    US.  LUT Data is always written as OW if numberOfColors is 65536.
   *  @param write16BitEntries if true, LUT data is encoded with 16 bits per entry
   *  @param floydSteinberg determines whether or not to use Floyd-Steinberg
   *    error diffusion during creation of the palette color image
   *  @param numberOfColors desired number of colors in the color palette.
   *    Valid range is [2..65536].
   *  @param description description string suitable for use as
   *    Derivation Description returned in this parameter
   *  @param verbose verbose mode, print information to console
   *  @param largeType algorithm used for determining the largest dimension
   *    in the Median Cut algorithm
   *  @param repType algorithm for choosing a representative color for each
   *    box in the Median Cut algorithm
   *  @return EC_Normal if successful, an error code otherwise.
   */
  static OFCondition createPaletteColorImage(
    DicomImage& sourceImage,
    DcmItem& target,
    OFBool writeAsOW,
    OFBool write16BitEntries,
    OFBool floydSteinberg,
    Uint32 numberOfColors,
    OFString& description,
    OFBool verbose,
    DcmLargestDimensionType largeType = DcmLargestDimensionType_default,
    DcmRepresentativeColorType repType = DcmRepresentativeColorType_default);

  /** create Derivation Description. If a derivation description
   *  already exists, the old text is appended to the new text.
   *  @param dataset dataset to be modified
   *  @param description description string.
   *  @return EC_Normal if successful, an error code otherwise
   */
  static OFCondition updateDerivationDescription(DcmItem *dataset, const char *description);

};


#endif


/*
 * CVS/RCS Log:
 * $Log: diquant.h,v $
 * Revision 1.5  2005/12/08 16:01:56  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.4  2004/08/24 14:55:24  meichel
 * Removed duplicate code
 *
 * Revision 1.3  2003/12/23 12:21:36  joergr
 * Updated copyright header.
 *
 * Revision 1.2  2003/07/04 13:25:40  meichel
 * Replaced forward declarations for OFString with explicit includes,
 *   needed when compiling with HAVE_STD_STRING
 *
 * Revision 1.1  2002/01/25 13:32:08  meichel
 * Initial release of new color quantization classes and
 *   the dcmquant tool in module dcmimage.
 *
 *
 */
