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
 *  Purpose: DcmOutputFileStream and related classes,
 *    implements streamed output to files.
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:28:26 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcostrmf.h,v $
 *  CVS/RCS Revision: $Revision: 1.4 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCOSTRMF_H
#define DCOSTRMF_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/dcmdata/dcostrma.h"

#define INCLUDE_CSTDIO
#include "dcmtk/ofstd/ofstdinc.h"


/** consumer class that stores data in a plain file.
 */
class DcmFileConsumer: public DcmConsumer
{
public:
  /** constructor
   *  @param filename name of file to be created, must not be NULL or empty
   */
  DcmFileConsumer(const char *filename);

  /** constructor
   *  @param file structure, file must already be open for writing
   */
  DcmFileConsumer(FILE *file);

  /// destructor
  virtual ~DcmFileConsumer();

  /** returns the status of the consumer. Unless the status is good,
   *  the consumer will not permit any operation.
   *  @return status, true if good
   */
  virtual OFBool good() const;

  /** returns the status of the consumer as an OFCondition object.
   *  Unless the status is good, the consumer will not permit any operation.
   *  @return status, EC_Normal if good
   */
  virtual OFCondition status() const;

  /** returns true if the consumer is flushed, i.e. has no more data
   *  pending in it's internal state that needs to be flushed before
   *  the stream is closed.
   *  @return true if consumer is flushed, false otherwise
   */
  virtual OFBool isFlushed() const;

  /** returns the minimum number of bytes that can be written with the
   *  next call to write(). The DcmObject write methods rely on avail
   *  to return a value > 0 if there is no I/O suspension since certain
   *  data such as tag and length are only written "en bloc", i.e. all
   *  or nothing.
   *  @return minimum of space available in consumer
   */
  virtual Uint32 avail() const;

  /** processes as many bytes as possible from the given input block.
   *  @param buf pointer to memory block, must not be NULL
   *  @param buflen length of memory block
   *  @return number of bytes actually processed. 
   */
  virtual Uint32 write(const void *buf, Uint32 buflen);

  /** instructs the consumer to flush its internal content until
   *  either the consumer becomes "flushed" or I/O suspension occurs.
   *  After a call to flush(), a call to write() will produce undefined
   *  behaviour.
   */
  virtual void flush();

private:

  /// private unimplemented copy constructor
  DcmFileConsumer(const DcmFileConsumer&);

  /// private unimplemented copy assignment operator
  DcmFileConsumer& operator=(const DcmFileConsumer&);

  /// the file we're actually writing to
  FILE *file_;

  /// status
  OFCondition status_;
};


/** output stream that writes into a plain file
 */
class DcmOutputFileStream: public DcmOutputStream
{
public:
  /** constructor
   *  @param filename name of file to be created, must not be NULL or empty
   */
  DcmOutputFileStream(const char *filename);

  /** constructor
   *  @param file structure, file must already be open for writing
   */
  DcmOutputFileStream(FILE *file);

  /// destructor
  virtual ~DcmOutputFileStream();

private:

  /// private unimplemented copy constructor
  DcmOutputFileStream(const DcmOutputFileStream&);

  /// private unimplemented copy assignment operator
  DcmOutputFileStream& operator=(const DcmOutputFileStream&);

  /// the final consumer of the filter chain
  DcmFileConsumer consumer_;

};


#endif

/*
 * CVS/RCS Log:
 * $Log: dcostrmf.h,v $
 * Revision 1.4  2005/12/08 16:28:26  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.3  2003/11/07 13:49:08  meichel
 * Added constructor taking an open FILE* instead of a file name string
 *
 * Revision 1.2  2002/11/27 12:07:22  meichel
 * Adapted module dcmdata to use of new header file ofstdinc.h
 *
 * Revision 1.1  2002/08/27 16:55:37  meichel
 * Initial release of new DICOM I/O stream classes that add support for stream
 *   compression (deflated little endian explicit VR transfer syntax)
 *
 *
 */
