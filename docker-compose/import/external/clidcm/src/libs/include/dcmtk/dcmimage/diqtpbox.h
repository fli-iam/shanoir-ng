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
 *  Purpose: class DcmQuantPixelBoxArray
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:01:52 $
 *  CVS/RCS Revision: $Revision: 1.4 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIQTPBOX_H
#define DIQTPBOX_H


#include "dcmtk/config/osconfig.h"

#define INCLUDE_CSTDLIB
#define INCLUDE_CASSERT
#include "dcmtk/ofstd/ofstdinc.h"


/** helper structure for class DcmQuantPixelBoxArray.
 *  Each object of this class represents a pixel box used in the
 *  Median Cut algorithm.
 */
struct DcmQuantPixelBox
{
  /// index of the color histogram array where the colors represented by this box start
  int ind;

  /// the number of colors represented by this box
  int colors;

  /// the number of pixels represented by this box
  unsigned long sum;
};


/// typedef for a pointer to a DcmQuantPixelBox object
typedef DcmQuantPixelBox *DcmQuantPixelBoxPointer;


/** helper class for the Median Cut algorithm.  An object of
 *  this class maintains an array of pixel boxes which is used
 *  to determine a color look-up table of given size for a
 *  true color image.
 */
class DcmQuantPixelBoxArray
{
public:

  /** constructor. Creates an array with the given number of pixel boxes.
   *  @param entries number of entries in array, must be > 0.
   */
  DcmQuantPixelBoxArray(unsigned long entries);

  /// destructor
  ~DcmQuantPixelBoxArray();

  /** looks up an array element
   *  @param idx index, must be smaller than the number of entries
   *    with which this object was initialized
   *  @return non-const reference to DcmQuantPixelBox struct
   */
  inline DcmQuantPixelBox& operator[](size_t idx)
  {
#ifdef DEBUG
  	assert(idx < length);
#endif
  	return *(array[idx]);
  }

  /** sorts the given number of pixel boxes in the array,
   *  starting with the first array element.
   *  The boxes are sorted in decreasing order of the "sum" member.
   *  @param boxes number of boxes to sort, must be <= entries.
   */
  void sort(unsigned long boxes);

private:

  /// private undefined copy constructor
  DcmQuantPixelBoxArray(const DcmQuantPixelBoxArray& src);

  /// private undefined copy assignment operator
  DcmQuantPixelBoxArray& operator=(const DcmQuantPixelBoxArray& src);

  /// array of pixel boxes
  DcmQuantPixelBoxPointer *array;

  /// number of elements in array
  unsigned long length;
};


#endif


/*
 * CVS/RCS Log:
 * $Log: diqtpbox.h,v $
 * Revision 1.4  2005/12/08 16:01:52  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.3  2003/12/23 12:19:10  joergr
 * Updated copyright header.
 *
 * Revision 1.2  2002/11/27 14:16:56  meichel
 * Adapted module dcmimage to use of new header file ofstdinc.h
 *
 * Revision 1.1  2002/01/25 13:32:06  meichel
 * Initial release of new color quantization classes and
 *   the dcmquant tool in module dcmimage.
 *
 *
 */
