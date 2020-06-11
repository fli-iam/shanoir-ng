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
 *  Purpose: class DcmQuantHistogramItemList
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:01:49 $
 *  CVS/RCS Revision: $Revision: 1.3 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIQTHITL_H
#define DIQTHITL_H


#include "dcmtk/config/osconfig.h"
#include "dcmtk/ofstd/oflist.h"    /* for OFList */
#include "dcmtk/dcmimage/diqthitm.h"  /* for DcmQuantHistogramItem */


/** this is a helper class used by class DcmQuantColorHashTable.
 *  It maintains a list of DcmQuantHistogramItem objects.
 */
class DcmQuantHistogramItemList
{
public:

  /// constructor
  DcmQuantHistogramItemList();

  /// destructor. Destroys all objects pointed to by list.
  ~DcmQuantHistogramItemList();

  /** this method moves the contents of this list into the given array.
   *  The list becomes empty if the array is large enough to contain all list members.
   *  @param array array of pointers to DcmQuantHistogramItem
   *  @param counter When called, contains the index of the array element
   *    into which the first member of the list will be moved.  Must be < numcolors.
   *    Upon return, contains the array index of the last element moved + 1.
   *  @param numcolors number of elements in array
   */
  void moveto(DcmQuantHistogramItemPointer *array, unsigned long& counter, unsigned long numcolors);

  /** searches the list for an entry that equals the given pixel value.
   *  If found, the integer value assigned to that pixel is returned, otherwise returns -1.
   *  @param colorP pixel to lookup in list
   *  @return integer value for given color if found, -1 otherwise.
   */
  inline int lookup(const DcmQuantPixel& colorP)
  {
    first = list_.begin();
    while (first != last)
    {
      if ((*first)->equals(colorP)) return (*first)->getValue();
      ++first;
    }
    return -1;
  }

  /** adds the given pixel to the list.  If the pixel is already
   *  contained in the list, it's integer value (counter) is increased
   *  and 0 is returned.  Otherwise, a new entry with a counter of 1
   *  is created and 1 is returned.
   *  @param colorP pixel to add to the list
   *  @return 0 if pixel was already in list, 1 otherwise.
   */
  inline unsigned long add(const DcmQuantPixel& colorP)
  {
    first = list_.begin();
    while (first != last)
    {
      if ((*first)->equals(colorP))
      {
        (*first)->incValue();
        return 0;
      }
      ++first;
    }

    // not found in list, create new entry
    list_.push_front(new DcmQuantHistogramItem(colorP, 1));
    return 1;
  }

  /** inserts a new DcmQuantHistogramItem at the beginning of the list.
   *  @param colorP pixel value assigned to the new object in the list
   *  @param value integer value assigned to the new object in the list
   */
  inline void push_front(const DcmQuantPixel& colorP, int value)
  {
    list_.push_front(new DcmQuantHistogramItem(colorP, value));
  }

  /// returns current number of objects in the list
  inline size_t size() const
  {
    return list_.size();
  }

private:

  /// list of (pointers to) DcmQuantHistogramItem objects
  OFList<DcmQuantHistogramItem *> list_;

  /// temporary iterator used in various methods; declared here for efficiency reasons only.
  OFListIterator(DcmQuantHistogramItem *) first;

  /// constant iterator which always contains list_.end(); declared here for efficiency reasons only.
  OFListIterator(DcmQuantHistogramItem *) last;

};


/// typedef for a pointer to a DcmQuantHistogramItemList object
typedef DcmQuantHistogramItemList *DcmQuantHistogramItemListPointer;


#endif


/*
 * CVS/RCS Log:
 * $Log: diqthitl.h,v $
 * Revision 1.3  2005/12/08 16:01:49  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.2  2003/12/17 16:57:55  joergr
 * Renamed parameters/variables "list" to avoid name clash with STL class.
 *
 * Revision 1.1  2002/01/25 13:32:05  meichel
 * Initial release of new color quantization classes and
 *   the dcmquant tool in module dcmimage.
 *
 *
 */
