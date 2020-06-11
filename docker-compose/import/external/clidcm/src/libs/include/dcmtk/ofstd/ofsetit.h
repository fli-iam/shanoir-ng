/*
 *
 *  Copyright (C) 2003-2005, OFFIS
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
 *  Module:  ofstd
 *
 *  Author:  Thomas Wilkens
 *
 *  Purpose: Template class which represents an iterator class for corres-
 *           ponding set data structures in ofstd.
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:06:02 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/ofstd/include/dcmtk/ofstd/ofsetit.h,v $
 *  CVS/RCS Revision: $Revision: 1.2 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef OFSetIterator_h
#define OFSetIterator_h

#include "dcmtk/config/osconfig.h"
#include "dcmtk/ofstd/oftypes.h"
#include "dcmtk/ofstd/ofset.h"

/** This template class represents an iterator class for corresponding set data structures in ofstd.
 *  Note the following properties of this class:
 *   - even if the underlying set is unordered, it does have an internal order which can be used for
 *     iteration purposes
 *   - a reference to the set which shall be iterated over will be remembered inside an object of
 *     OFSetIterator<T>; the set will NOT be copied
 *   - it is possible to iterate from beginning to end or from end to beginning of the underlying set
 *     using Next() or Prev() (see below)
 *   - if an element from the set is removed during the iteration, the iteration process has to be
 *     cancelled and restarted
 *   - two OFSetIterators can be compared through operators == and !=; two iterators are considered
 *     to be identical, if and only if they operate on the exact same set (identical memory addresses)
 *     and they currently refer to the same element
 */
template <class T> class OFSetIterator
{
  protected:
    OFSet<T> &ofset;
    unsigned int pos;

  public:
      /** Constructor. The iterator will be set to the set's first element.
       *  @param ofsetv Set to which the created object shall be an iterator.
       */
    OFSetIterator( OFSet<T> &ofsetv )
        : ofset( ofsetv ), pos( 0 )
      {
      }

      /** Destructor.
       */
    virtual ~OFSetIterator()
      {
      }

      /** Resets the iterator to the set's first element.
       */
    void ResetBeginning()
      {
        pos = 0;
      }

      /** Resets the iterator to the set's last element.
       */
    void ResetEnd()
      {
        unsigned int num = ofset.NumberOfElements();

        if( num == 0 )
          pos = 0;
        else
          pos = num - 1;
      }

      /** Returns the current element.
       *  @return Pointer to the current original element in the set.
       */
    T *Object()
      {
        if( pos == ofset.NumberOfElements() )
          return( NULL );
        else
          return( &ofset[pos] );
      }

      /** Sets the iterator to the next element in the set.
       */
    void Next()
      {
        if( pos < ofset.NumberOfElements() )
          pos++;
      }

      /** Sets the iterator to the previous element in the set.
       */
    void Prev()
      {
        unsigned int num = ofset.NumberOfElements();

        if( pos == 0 || pos == num )
          pos = num;
        else
          pos--;
      }

      /** Determines if two iterators are identical.
       *  @param other Iterator which shall be compared with this.
       *  @return OFTrue if iterators are identical, OFFalse otherwise.
       */
    OFBool operator==( const OFSetIterator<T> &other ) const
      {
        // two iterators are considered to be identical, if and only if they operate on the
        // exact same set (identical addresses) and they currently refer to the same element
        if( &ofset == &other.ofset && pos == other.pos )
          return( OFTrue );
        else
          return( OFFalse );
      }

      /** Determines if two iterators are not identical.
       *  @param other Iterator which shall be compared with this.
       *  @return OFTrue if iterators are not identical, OFFalse otherwise.
       */
    OFBool operator!=( const OFSetIterator<T> &other ) const
      {
        return( !( *this == other ) );
      }
};

#endif

/*
** CVS/RCS Log:
** $Log: ofsetit.h,v $
** Revision 1.2  2005/12/08 16:06:02  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.1  2003/08/20 14:45:25  wilkens
** Added new class OFSetIterator, an iterator class for OFxxxSet data structures.
**
**
**
*/
