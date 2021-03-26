/*
 *
 *  Copyright (C) 1997-2005, OFFIS
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
 *  Purpose: Template class for administrating an unordered set of elements
 *           of an arbitrary type.
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/12 09:24:27 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/ofstd/include/dcmtk/ofstd/ofuoset.h,v $
 *  CVS/RCS Revision: $Revision: 1.6 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef OFUnorderedSet_h
#define OFUnorderedSet_h

#include "dcmtk/config/osconfig.h"
#include "dcmtk/ofstd/oftypes.h"
#include "dcmtk/ofstd/ofset.h"

/** This template class provides a data structure and operations for administrating an
 *  unordered set of elements of an arbitrary type. Note the following properties of this
 *  class:
 *  - an element which is inserted into the set will be copied
 *  - the datatype of the set's elements has to support operator== so that it is possible
 *    to find a certain element
 *  - it is allowed to insert identical elements into the set
 *  - if a user requires to remove a certain element and if there are several elements
 *    which are identical to this element, only one element will be removed from the set
 *  - after removing an element of a set, the indeces of the set's elements might have
 *    changed; hence, looping over all elements of a set must be restarted if an element
 *    was removed from the set within the loop
 *  - the order of the elements is arbitrary, but it will not be changed unless an element
 *    is removed from the set
 */
template <class T> class OFUnorderedSet : public OFSet<T>
{
  protected:

  public:
      /** Default constructor.
       */
    OFUnorderedSet()
        : OFSet<T>()
      {
      }


      /** Copy constructor.
       *  @param src Source object of which this will be a copy.
       */
    OFUnorderedSet( const OFUnorderedSet<T> &src )
        : OFSet<T>( src )
      {
      }


      /** Destructor.
       */
    virtual ~OFUnorderedSet()
      {
      }


      /** operator=.
       *  @param src Source object whose values will be assigned to this.
       *  @return Reference to this.
       */
    const OFUnorderedSet<T> &operator=( const OFUnorderedSet<T> &src )
      {
        if( this == &src )
          return( *this );

        OFSet<T>::operator=( src );

        return( *this );
      }


      /** Determines if two sets are identical.
       *  @param other Set which shall be compared with this.
       *  @return OFTrue if sets are identical, OFFalse otherwise.
       */
    virtual OFBool operator==( const OFUnorderedSet<T> &other ) const
      {
        // check if both sets contain the same
        // amount of items; if not, return OFFalse
        if( OFSet<T>::num != other.num )
          return( OFFalse );

        // initialize result with OFTrue
        OFBool result = OFTrue;

        // make a copy of this
        OFUnorderedSet<T> s = *this;

        // as long as result is OFTrue go through all items in other
        for( unsigned int i=0 ; i<other.num && result == OFTrue ; i++ )
        {
          // in case s contains the current item of other
          if( s.Contains( *other.items[i] ) )
          {
            // remove this item from s so that it will not be
            // considered again in a later call to s.Contains()
            s.Remove( *other.items[i] );
          }
          // in case s doesn't contain the current item of other the result is OFFalse
          else
            result = OFFalse;
        }

        // return result
        return( result );
      }


      /** Determines if two sets are not identical.
       *  @param other Set which shall be compared with this.
       *  @return OFTrue if sets are not identical, OFFalse otherwise.
       */
    virtual OFBool operator!=( const OFUnorderedSet<T> &other ) const
      {
        return( !( *this == other ) );
      }


      /** Inserts a new item into the set.
       *  @param item Item which shall be inserted into the set.
       */
    virtual void Insert( const T &item )
      {
        // if size equals num, we need more space
        if( OFSet<T>::size == OFSet<T>::num )
          Resize( OFSet<T>::size * 2 );

        // copy item
        T *newItem = new T( item );

        // insert copy into array
        OFSet<T>::items[OFSet<T>::num] = newItem;

        // increase counter
        OFSet<T>::num++;
      }


      /** Inserts all items of another set into this set.
       *  @param other set whose items shall be inserted into the set.
       */
    virtual void Insert( const OFUnorderedSet<T> &other )
      {
        // go through all items in other and insert each item into this
        for( unsigned int i=0 ; i<other.num ; i++ )
          Insert( *other.items[i] );
      }


      /** Removes one item from the set.
       *  @param item Item which shall be removed from the set.
       */
    virtual void Remove( const T &item )
      {
        // so far, nothing was deleted
        OFBool itemDeleted = OFFalse;

        // go through all items
        for( unsigned int i=0 ; i<OFSet<T>::num && !itemDeleted ; i++ )
        {
          // if current item is the one which shall be deleted
          if( *OFSet<T>::items[i] == item )
          {
            // delete item
            delete OFSet<T>::items[i];

            // and - so that there are no holes in the array - move the last
            // item to the array field from which the item was deleted;
            // only do so in case we did _not_ delete the last item
            if( i != OFSet<T>::num - 1 )
            {
              OFSet<T>::items[i] = OFSet<T>::items[OFSet<T>::num-1];
              OFSet<T>::items[OFSet<T>::num-1] = NULL;
            }
            else
              OFSet<T>::items[i] = NULL;

            // reduce counter
            OFSet<T>::num--;

            // remember that an item was deleted (so that always only one item will be deleted)
            itemDeleted = OFTrue;
          }
        }
      }


      /** Removes one item from the set.
       *  @param index Index of the item which shall be removed from the set.
       */
    virtual void RemoveByIndex( unsigned int index )
      {
        // do something only if the given index is not out of range
        if( index < OFSet<T>::num )
        {
          // delete item with given index
          delete OFSet<T>::items[index];

          // and - so that there are no holes in the array - move the last
          // item to the array field from which the item was deleted;
          // only do so in case we did _not_ delete the last item
          if( index != OFSet<T>::num - 1 )
          {
            OFSet<T>::items[index] = OFSet<T>::items[OFSet<T>::num-1];
            OFSet<T>::items[OFSet<T>::num-1] = NULL;
          }
          else
            OFSet<T>::items[index] = NULL;

          // reduce counter
          OFSet<T>::num--;
        }
      }


      /** Tries to find a given object in the set. In case the specified object could
       *  be found, a pointer to the corresponding element within the set is returned;
       *  in case the specified object could not be found, NULL will be returned.
       *  @param item Search pattern.
       *  @return Pointer to the corresponding element within the set or NULL.
       */
    virtual T *Find( const T &item ) const
      {
        unsigned int i;
        OFBool itemFound = OFFalse;

        for( i=0 ; i<OFSet<T>::num && !itemFound ; i++ )
        {
          if( *OFSet<T>::items[i] == item )
            itemFound = OFTrue;
        }

        if( itemFound )
          return( OFSet<T>::items[i-1] );
        else
          return( NULL );
      }


      /** Determines if a certain item is contained in the set.
       *  @param item - Item which shall be looked for.
       *  @return OFTrue, if item is contained in the set, OFFalse otherwise.
       */
    virtual OFBool Contains( const T &item ) const
      {
        OFBool itemFound = OFFalse;

        for( unsigned int i=0 ; i<OFSet<T>::num && !itemFound ; i++ )
        {
          if( *OFSet<T>::items[i] == item )
            itemFound = OFTrue;
        }

        return( itemFound );
      }


      /** Determines if this is an actual superset of other, i.e.
       *  if this completely contains other and furthermore has
       *  additional elements.
       *  @param other - Set which shall be compared with this.
       *  @return OFTrue if this is a superset of other, OFFalse otherwise.
       */
    virtual OFBool IsSupersetOf( const OFUnorderedSet<T> &other ) const
      {
        // if this contains less or the same amount of items than other, return OFFalse
        if( OFSet<T>::num <= other.num )
          return( OFFalse );

        // initialize result with OFTrue
        OFBool result = OFTrue;

        // make a copy of this
        OFUnorderedSet<T> s = *this;

        // as long as result is OFTrue go through all items in other
        for( unsigned int i=0 ; i<other.num && result == OFTrue ; i++ )
        {
          // in case s contains the current item of other
          if( s.Contains( *other.items[i] ) )
          {
            // remove this item from s so that it will not be
            // considered again in a later call to s.Contains()
            s.Remove( *other.items[i] );
          }
          // in case s does not contain the current item of other the result is OFFalse
          else
            result = OFFalse;
        }

        // return result
        return( result );
      }


      /** Determines if this is an actual subset of other, i.e.
       *  if this is completely contained in other and other
       *  furthermore has additional elements.
       *  @param other - Set which shall be compared with this.
       *  @return OFTrue if this is a subset of other, OFFalse otherwise.
       */
    virtual OFBool IsSubsetOf( const OFUnorderedSet<T> &other ) const
      {
        return( other.IsSupersetOf( *this ) );
      }


      /** Determines the union of the two sets this and other, i.e. the set
       *  containing all items which can be found either in this or in other,
       *  and returns the resulting new set.
       *  @param other Second parameter for union.
       *  @return New set.
       */
    OFUnorderedSet<T> Union( const OFUnorderedSet<T> &other ) const
      {
        // initialize result set
        OFUnorderedSet<T> resultSet = *this;

        // insert other set into result set
        resultSet.Insert( other );

        // return result set
        return( resultSet );
      }


      /** Determines the intersection of the two sets this and other, i.e. the set
       *  containing all items which can be found in both this and other, and
       *  returns the resulting new set.
       *  @param other Second parameter for intersection.
       *  @return New set.
       */
    OFUnorderedSet<T> Intersection( const OFUnorderedSet<T> &other ) const
      {
        // initialize result set
        OFUnorderedSet<T> resultSet;

        // make a copy of other
        OFUnorderedSet<T> s = other;

        // go through all items in this
        for( unsigned int i=0 ; i< OFSet<T>::num ; i++ )
        {
          // if s contains the current item
          if( s.Contains( *OFSet<T>::items[i] ) )
          {
            // insert the item into the result set
            resultSet.Insert( *OFSet<T>::items[i] );

            // and remove the item from s so that it will not be
            // considered again in a later call to s.Contains()
            s.Remove( *OFSet<T>::items[i] );
          }
        }

        // return result set
        return( resultSet );
      }


      /** Determines the difference this - other, i.e. the set containing all
       *  the items found in this but not in other, and returns the resulting
       *  new set.
       *  @param other Second parameter for difference.
       *  @return New set.
       */
    OFUnorderedSet<T> Difference( const OFUnorderedSet<T> &other ) const
      {
        // initialize result set
        OFUnorderedSet<T> resultSet;

        // make a copy of other
        OFUnorderedSet<T> s = other;

        // go through all items in this
        for( unsigned int i=0 ; i< OFSet<T>::num ; i++ )
        {
          // if s does not contain the current item
          if( !s.Contains( *OFSet<T>::items[i] ) )
          {
            // insert the item into the result set
            resultSet.Insert( *OFSet<T>::items[i] );
          }
          else
          {
            // else remove the item from s so that it will not be
            // considered again in a later call to s.Contains()
            s.Remove( *OFSet<T>::items[i] );
          }
        }

        // return result set
        return( resultSet );
      }


      /** Determines the symmetric difference of this and other, i.e. the set
       *  containing all the items which can be found either in this or in other
       *  but not in the intersection of this and other, and returns the resulting
       *  new set.
       *  @param other Second parameter for symmetric difference.
       *  @return New set.
       */
    OFUnorderedSet<T> SymmetricDifference( const OFUnorderedSet<T> &other ) const
      {
        // determine s1 = this - other
        OFUnorderedSet<T> s1 = (*this).Difference( other );

        // determine s2 = other - this
        OFUnorderedSet<T> s2 = other.Difference( *this );

        // determine the union of s1 and s2
        OFUnorderedSet<T> resultSet = s1.Union( s2 );

        // return result set
        return( resultSet );
      }
};

#endif

/*
** CVS/RCS Log:
** $Log: ofuoset.h,v $
** Revision 1.6  2005/12/12 09:24:27  meichel
** Added explicit references to parent template class, needed for
**   gcc 3.4.2 (MinGW port)
**
** Revision 1.5  2005/12/08 16:06:12  meichel
** Changed include path schema for all DCMTK header files
**
** Revision 1.4  2002/12/16 10:40:25  wilkens
** Removed superfluous implementation files and modified header and make files.
**
** Revision 1.3  2002/07/09 18:29:47  wilkens
** Added some more functionality.
**
**
*/
