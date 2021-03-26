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
 *  Author:  Gerd Ehlers
 *
 *  Purpose: Print debug information
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:28:04 $
 *  CVS/RCS Revision: $Revision: 1.13 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCDEBUG_H
#define DCDEBUG_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */
#include "dcmtk/ofstd/ofstream.h"
#include "dcmtk/ofstd/ofglobal.h"

extern OFGlobal<int> DcmDebugLevel; /* default 0 */

#ifdef DEBUG

void DCM_dcmdata_debug_print(const char* text, ... );

// Set the debug level
#define SetDebugLevel(level) DcmDebugLevel.set(level);

// debug prints a debug message in param if lev <= DcmDebugLevel. param has the
// format of the printf parameters (with round brackets)!
#define DCM_dcmdataDebug(lev, param) \
  { \
    if ((lev) <= DcmDebugLevel.get()) \
    { \
      ofConsole.lockCerr() << __FILE__ << ", LINE " << __LINE__ << ":"; \
      DCM_dcmdata_debug_print param ; \
      ofConsole.unlockCerr(); \
    } \
  }

// Cdebug does the same as debug but only if a condition cond is OFTrue
#define DCM_dcmdataCDebug(lev, cond, param) \
  { \
    if ((lev) <= DcmDebugLevel.get() && (cond)) \
    { \
      ofConsole.lockCerr() << __FILE__ << ", LINE " << __LINE__ << ":"; \
      DCM_dcmdata_debug_print param ; \
      ofConsole.unlockCerr(); \
    } \
  }

#else  // DEBUG

#define SetDebugLevel(param)
#define DCM_dcmdataDebug(lev, param)
#define DCM_dcmdataCDebug(lev, cond, param)

#endif // DEBUG

#endif // DCDEBUG_H


/*
 * CVS/RCS Log:
 * $Log: dcdebug.h,v $
 * Revision 1.13  2005/12/08 16:28:04  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.12  2005/11/28 15:53:16  meichel
 * Renamed macros in dcdebug.h
 *
 * Revision 1.11  2004/01/16 14:06:32  joergr
 * Removed acknowledgements with e-mail addresses from CVS log.
 *
 * Revision 1.10  2002/04/16 13:41:43  joergr
 * Added configurable support for C++ ANSI standard includes (e.g. streams).
 *
 * Revision 1.9  2001/06/01 15:48:35  meichel
 * Updated copyright header
 *
 * Revision 1.8  2000/04/14 15:45:30  meichel
 * Dcmdata debug facility now uses ofConsole for output.
 *
 * Revision 1.7  2000/03/08 16:26:12  meichel
 * Updated copyright header.
 *
 * Revision 1.6  2000/03/03 14:05:22  meichel
 * Implemented library support for redirecting error messages into memory
 *   instead of printing them to stdout/stderr for GUI applications.
 *
 * Revision 1.5  1999/03/31 09:24:33  meichel
 * Updated copyright header in module dcmdata
 *
 *
 */
