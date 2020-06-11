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
 *  Purpose: DicomMonochromeScaleTemplate (Header)
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:47:58 $
 *  CVS/RCS Revision: $Revision: 1.14 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIMOSCT_H
#define DIMOSCT_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/ofstd/ofcast.h"

#include "dcmtk/dcmimgle/dimopxt.h"
#include "dcmtk/dcmimgle/discalet.h"


/*---------------------*
 *  class declaration  *
 *---------------------*/

/** Template class to scale monochrome images (on pixel data level).
 */
template<class T>
class DiMonoScaleTemplate
  : public DiMonoPixelTemplate<T>,
    protected DiScaleTemplate<T>
{

 public:

    /** constructor
     *
     ** @param  pixel        pointer to intermediate pixel representation
     *  @param  columns      width of source image
     *  @param  rows         height of source image
     *  @param  left_pos     left coordinate of clipping area
     *  @param  top_pos      top coordinate of clipping area
     *  @param  src_cols     width of clipping area
     *  @param  src_rows     height of clipping area
     *  @param  dest_cols    width of destination image (scaled image)
     *  @param  dest_rows    height of destination image
     *  @param  frames       number of frames
     *  @param  interpolate  use of interpolation when scaling
     *  @param  pvalue       value possibly used for regions outside the image boundaries
     */
    DiMonoScaleTemplate(const DiMonoPixel *pixel,
                        const Uint16 columns,
                        const Uint16 rows,
                        const signed long left_pos,
                        const signed long top_pos,
                        const Uint16 src_cols,
                        const Uint16 src_rows,
                        const Uint16 dest_cols,
                        const Uint16 dest_rows,
                        const Uint32 frames,
                        const int interpolate,
                        const Uint16 pvalue)
      : DiMonoPixelTemplate<T>(pixel, OFstatic_cast(unsigned long, dest_cols) * OFstatic_cast(unsigned long, dest_rows) * frames),
        DiScaleTemplate<T>(1, columns, rows, left_pos, top_pos, src_cols, src_rows, dest_cols, dest_rows, frames)
    {
        if ((pixel != NULL) && (pixel->getCount() > 0))
        {
            if (pixel->getCount() == OFstatic_cast(unsigned long, columns) * OFstatic_cast(unsigned long, rows) * frames)
            {
                scale(OFstatic_cast(const T *, pixel->getData()), pixel->getBits(), interpolate, pvalue);
                this->determineMinMax();
            } else {
                if (DicomImageClass::checkDebugLevel(DicomImageClass::DL_Warnings))
                {
                   ofConsole.lockCerr() << "WARNING: could not scale image ... corrupted data." << endl;
                   ofConsole.unlockCerr();
                }
            }
        }
    }

    /** destructor
     */
    virtual ~DiMonoScaleTemplate()
    {
    }


 private:

    /** scale pixel data
     *
     ** @param  pixel        pointer to pixel data to be scaled
     *  @param  bits         bit depth of pixel data
     *  @param  interpolate  use of interpolation when scaling
     *  @param  pvalue       value possibly used for regions outside the image boundaries
     */
    inline void scale(const T *pixel,
                      const unsigned int bits,
                      const int interpolate,
                      const Uint16 pvalue)
    {
        if (pixel != NULL)
        {
            this->Data = new T[this->getCount()];
            if (this->Data != NULL)
            {
                const T value = OFstatic_cast(T, OFstatic_cast(double, DicomImageClass::maxval(bits)) *
                    OFstatic_cast(double, pvalue) / OFstatic_cast(double, DicomImageClass::maxval(WIDTH_OF_PVALUES)));
                scaleData(&pixel, &this->Data, interpolate, value);
             }
        }
    }
};


#endif


/*
 *
 * CVS/RCS Log:
 * $Log: dimosct.h,v $
 * Revision 1.14  2005/12/08 16:47:58  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.13  2004/04/21 10:00:36  meichel
 * Minor modifications for compilation with gcc 3.4.0
 *
 * Revision 1.12  2003/12/09 10:04:45  joergr
 * Adapted type casts to new-style typecast operators defined in ofcast.h.
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols). Updated copyright header.
 *
 * Revision 1.11  2002/12/09 13:32:54  joergr
 * Renamed parameter/local variable to avoid name clashes with global
 * declaration left and/or right (used for as iostream manipulators).
 *
 * Revision 1.10  2001/06/01 15:49:48  meichel
 * Updated copyright header
 *
 * Revision 1.9  2000/09/12 10:04:45  joergr
 * Corrected bug: wrong parameter for attribute search routine led to crashes
 * when multiple pixel data attributes were contained in the dataset (e.g.
 * IconImageSequence). Added new checking routines to avoid crashes when
 * processing corrupted image data.
 *
 * Revision 1.8  2000/03/08 16:24:21  meichel
 * Updated copyright header.
 *
 * Revision 1.7  1999/09/17 12:43:24  joergr
 * Added/changed/completed DOC++ style comments in the header files.
 *
 * Revision 1.6  1999/09/07 09:49:39  joergr
 * Removed register variable declaration to avoid compiler warnings.
 *
 * Revision 1.5  1999/08/25 16:41:54  joergr
 * Added new feature: Allow clipping region to be outside the image
 * (overlapping).
 *
 * Revision 1.4  1999/03/24 17:20:18  joergr
 * Added/Modified comments and formatting.
 *
 * Revision 1.3  1999/02/11 16:41:10  joergr
 * Corrected some typos and formatting.
 *
 * Revision 1.2  1998/12/16 16:36:11  joergr
 * *** empty log message ***
 *
 * Revision 1.1  1998/11/27 15:39:32  joergr
 * Added copyright message.
 * Combined clipping and scaling methods.
 *
 * Revision 1.4  1998/05/11 14:53:24  joergr
 * Added CVS/RCS header to each file.
 *
 *
 */
