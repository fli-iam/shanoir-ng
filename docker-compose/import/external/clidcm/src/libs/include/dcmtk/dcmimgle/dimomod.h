/*
 *
 *  Copyright (C) 1996-2005, OFFIS
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
 *  Module:  dcmimgle
 *
 *  Author:  Joerg Riesmeier
 *
 *  Purpose: DicomMonochromeModality (Header)
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:47:52 $
 *  CVS/RCS Revision: $Revision: 1.14 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIMOMOD_H
#define DIMOMOD_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/ofstd/ofcast.h"

#include "dcmtk/dcmimgle/diluptab.h"
#include "dcmtk/dcmimgle/diobjcou.h"
#include "dcmtk/dcmimgle/diutils.h"


/*------------------------*
 *  forward declarations  *
 *------------------------*/

class DiDocument;
class DiInputPixel;


/*---------------------*
 *  class declaration  *
 *---------------------*/

/** Class to handle modality specific transformations (incl. modality LUT)
 */
class DiMonoModality
  : public DiObjectCounter
{

 public:

    /** constructor
     *
     ** @param  docu   pointer to dataset (encapsulated)
     *  @param  pixel  pointer to input pixel data
     */
    DiMonoModality(const DiDocument *docu,
                   DiInputPixel *pixel);

    /** constructor, rescale
     *
     ** @param  docu       pointer to dataset (encapsulated)
     *  @param  pixel      pointer to input pixel data
     *  @param  slope      rescale slope (<> 0)
     *  @param  intercept  rescale intercept
     */
    DiMonoModality(const DiDocument *docu,
                   DiInputPixel *pixel,
                   const double slope,
                   const double intercept);

    /** constructor, LUT
     *
     ** @param  docu         pointer to dataset (encapsulated)
     *  @param  pixel        pointer to input pixel data
     *  @param  data         element containing the modality LUT data
     *  @param  descriptor   element containing the modality LUT descriptor
     *  @param  explanation  element containing the modality LUT explanation (optional)
     */
    DiMonoModality(const DiDocument *docu,
                   DiInputPixel *pixel,
                   const DcmUnsignedShort &data,
                   const DcmUnsignedShort &descriptor,
                   const DcmLongString *explanation);

    /** constructor, no modality transform
     *
     ** @param  bits  number of bits per pixel
     */
    DiMonoModality(const int bits);

    /** destructor
     */
    virtual ~DiMonoModality();

    /** get integer representation
     *
     ** @return integer representation
     */
    inline EP_Representation getRepresentation() const
    {
        return Representation;
    }

    /** get minimum pixel value after modality transform
     *
     ** @return minimum pixel value
     */
    inline double getMinValue() const
    {
        return MinValue;
    }

    /** get maximum pixel value after modality transform
     *
     ** @return maximum pixel value
     */
    inline double getMaxValue() const
    {
        return MaxValue;
    }

    /** get number of bits describing the width of output data
     *
     ** @return number of bits
     */
    inline unsigned int getBits() const
    {
        return Bits;
    }

    /** get absolute (possible) minimum pixel value after modality transform
     *
     ** @return absolute minimum pixel value
     */
    inline double getAbsMinimum() const
    {
        return AbsMinimum;
    }

    /** get absolute (possible) maximum pixel value after modality transform
     *
     ** @return absolute maximum pixel value
     */
    inline double getAbsMaximum() const
    {
        return AbsMaximum;
    }

    /** get rescale intercept value
     *
     ** @return rescale intercept value
     */
    inline double getRescaleIntercept() const
    {
        return RescaleIntercept;
    }

    /** get rescale slope value
     *
     ** @return rescale slope value
     */
    inline double getRescaleSlope() const
    {
        return RescaleSlope;
    }

    /** get pointer to lookup table data
     *
     ** @return pointer to lookup table data or NULL if absent
     */
    inline const DiLookupTable *getTableData() const
    {
        return TableData;
    }

    /** get modality LUT explanation
     *
     ** @return modality LUT explanation or NULL if absent
     */
    inline const char *getExplanation() const
    {
        return (TableData != NULL) ? TableData->getExplanation() : OFstatic_cast(const char *, NULL);
    }

    /** check whether lookup table is present
     *
     ** @return true if lookup table is present, false otherwise
     */
    inline int hasLookupTable() const
    {
        return LookupTable;
    }

    /** check whether rescaling is present
     *
     ** @return true if rescaling is present, false otherwise
     */
    inline int hasRescaling() const
    {
        return Rescaling;
    }


 protected:

    /** initialize internal data structures and values
     *
     ** @param  docu   pointer to dataset (encapsulated)
     *  @param  pixel  pointer to input pixel data
     */
    int Init(const DiDocument *docu,
             DiInputPixel *pixel);

    /** check lookup table for validity (and possibly correct it)
     */
    void checkTable();

    /** check rescaling for validity (and possibly correct it)
     *
     ** @param  pixel  pointer to input pixel data
     */
    void checkRescaling(const DiInputPixel *pixel);


 private:

    /// integer representation
    EP_Representation Representation;

    /// minimum pixel value
    double MinValue;
    /// maximum pixel value
    double MaxValue;

    /// number of bits
    unsigned int Bits;
    /// absolute minimum pixel value
    double AbsMinimum;
    /// absolute maximum pixel value
    double AbsMaximum;

    /// rescale intercept
    double RescaleIntercept;
    /// rescale slope
    double RescaleSlope;

    /// status flag: lookup table present
    int LookupTable;
    /// status flag: rescaling present
    int Rescaling;

    /// pointer to modality lookup table
    DiLookupTable *TableData;

 // --- declarations to avoid compiler warnings

    DiMonoModality(const DiMonoModality &);
    DiMonoModality &operator=(const DiMonoModality &);
};


#endif


/*
 *
 * CVS/RCS Log:
 * $Log: dimomod.h,v $
 * Revision 1.14  2005/12/08 16:47:52  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.13  2003/12/08 18:28:56  joergr
 * Adapted type casts to new-style typecast operators defined in ofcast.h.
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols). Updated copyright header.
 *
 * Revision 1.12  2001/06/01 15:49:45  meichel
 * Updated copyright header
 *
 * Revision 1.11  2000/03/08 16:24:19  meichel
 * Updated copyright header.
 *
 * Revision 1.10  1999/10/06 13:40:25  joergr
 * Added Doc++ header comments.
 *
 * Revision 1.9  1999/09/17 12:26:25  joergr
 * iAdded/changed/completed DOC++ style comments in the header files.
 *
 * Revision 1.8  1999/05/31 12:35:15  joergr
 * Corrected bug concerning the conversion of color images to grayscale.
 *
 * Revision 1.7  1999/04/28 17:03:47  joergr
 * Added type casts to NULL pointers returned as 'const char *' to avoid
 * compiler warnings reported by gcc 2.7.2.1 (Linux).
 *
 * Revision 1.6  1999/03/24 17:20:12  joergr
 * Added/Modified comments and formatting.
 *
 * Revision 1.5  1999/02/03 17:30:00  joergr
 * Added member variable and related methods to store number of bits used for
 * pixel data.
 *
 * Revision 1.4  1998/12/22 14:31:01  joergr
 * Changed calculation of AbsMinimum/Maximum.
 * Removed member variable and method for isPotentiallySigned.
 *
 * Revision 1.3  1998/12/16 16:34:37  joergr
 * Added explanation string to LUT class (retrieved from dataset).
 *
 * Revision 1.2  1998/12/14 17:23:52  joergr
 * Added support for correct scaling of input/output values for grayscale
 * transformations.
 *
 * Revision 1.1  1998/11/27 15:25:15  joergr
 * Added copyright message.
 * Added constructors to use external modality transformations.
 *
 * Revision 1.5  1998/07/01 08:39:23  joergr
 * Minor changes to avoid compiler warnings (gcc 2.8.1 with additional
 * options), e.g. add copy constructors.
 *
 * Revision 1.4  1998/05/11 14:53:21  joergr
 * Added CVS/RCS header to each file.
 *
 *
 */
