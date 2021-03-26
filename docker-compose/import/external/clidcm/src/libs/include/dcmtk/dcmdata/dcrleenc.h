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
 *  Module:  dcmdata
 *
 *  Author:  Marco Eichelberg
 *
 *  Purpose: RLE compressor
 *
 *  Last Update:      $Author: onken $
 *  Update Date:      $Date: 2005/12/16 09:04:47 $
 *  CVS/RCS Revision: $Revision: 1.12 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCRLEENC_H
#define DCRLEENC_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/ofstd/oflist.h"   /* for class OFList<> */

#define INCLUDE_CSTRING
#include "dcmtk/ofstd/ofstdinc.h"

#define DcmRLEEncoder_BLOCKSIZE 16384


/** abstract class that defines an interface through which
 *  encoder classes (such as DcmRLEEncoder) may export their
 *  encoded data
 */
class DcmEncoderOutputStream
{
public:
  /** write the given buffer into the output stream
   *  @param buf pointer to buffer
   *  @param bufsize number of bytes in buffer
   */
  virtual void write(const unsigned char *buf, size_t bufsize) =0;

  /** Virtual Desctructor
   */
  virtual ~DcmEncoderOutputStream() {}

};


/** this class implements an RLE compressor conforming to the DICOM standard.
 *  The class is loosely based on an implementation by Phil Norman.
 */
class DcmRLEEncoder
{
public:

  /** default constructor
   *  @param doPad if true, RLE codec will pad output data to even number of bytes
   */
  DcmRLEEncoder(int doPad)
  : fail_(0)
  , pad_(doPad)
  , currentBlock_(new unsigned char[DcmRLEEncoder_BLOCKSIZE])
  , offset_(0)
  , blockList_()
  , RLE_buff_(new unsigned char[132])
  , RLE_prev_(-1)
  , RLE_pcount_(0)
  , RLE_bindex_(1)
  {
    if ((! RLE_buff_)||(! currentBlock_)) fail_ = 1;
    else RLE_buff_[0] = 0;
  }

  /// destructor
  ~DcmRLEEncoder()
  {
    delete[] currentBlock_;
    delete[] RLE_buff_;
    OFListIterator(unsigned char *) first = blockList_.begin();
    OFListIterator(unsigned char *) last = blockList_.end();
    while (first != last)
    {
        delete[] *first;
        first = blockList_.erase(first);
    }
  }

  /** this method adds one byte to the byte stream to be compressed
   *  with the RLE compressor.
   *  @param ch byte to be added
   */
  inline void add(unsigned char ch)
  {
    if (! fail_) // if fail_ is true, just ignore input
    {
      // if the current byte equals the last byte read
      // (which is initialized with the "impossible" value -1),
      // just increase the repeat counter
      if (OFstatic_cast(int, ch) == RLE_prev_) RLE_pcount_++;
      else
      {
          // byte is different from last byte read.
          // flush replicate run if necessary
          switch (RLE_pcount_)
          {
            case 0:
              // happens only after construction or flush()
              break;
            case 2:
              // two bytes in repeat buffer. Convert to literal run
              RLE_buff_[RLE_bindex_++] = OFstatic_cast(unsigned char, RLE_prev_);
              // no break. Fall-through into next case statement is intended.
            case 1:
              // one (or two) bytes in repeat buffer. Convert to literal run
              RLE_buff_[RLE_bindex_++] = OFstatic_cast(unsigned char, RLE_prev_);
              break;
            default:
              // more than two bytes in repeat buffer. Convert to replicate run
              if (RLE_bindex_ > 1)
              {
                  // there is a literal run in the buffer that must be flushed
                  // before the replicate run.  Flush literal run now.
                  RLE_buff_[0] = OFstatic_cast(unsigned char, RLE_bindex_-2);
                  move(RLE_bindex_);
              }
              // this is the byte value for the repeat run
              RLE_buff_[1] = OFstatic_cast(unsigned char, RLE_prev_);
              // write as many repeat runs as necessary
              for (; RLE_pcount_>0; RLE_pcount_-=128)
              {
                  // different PackBit schemes exist. The original from which
                  // this code is derived used 0x80 | (RLE_pcount_ - 1)
                  // to represent replicate runs.
                  // DICOM instead uses 257 - RLE_pcount_
                  if (RLE_pcount_ > 128) RLE_buff_[0] = 0x81;
                    else RLE_buff_[0] = OFstatic_cast(unsigned char, 257 - RLE_pcount_);
                  move(2);
              }
              // now the buffer is guaranteed to be empty
              RLE_buff_[0] = 0;
              RLE_bindex_ = 1;
              break;
          }

          // if we have 128 or more bytes in the literal run, flush buffer
          if (RLE_bindex_ > 129)
          {
              RLE_buff_[0] = 127;
              move(129);
              RLE_bindex_ -= 128;
              if (RLE_bindex_ > 1)
                  RLE_buff_[1] = RLE_buff_[129];
              if (RLE_bindex_ > 2)
                  RLE_buff_[2] = RLE_buff_[130];
          }

          // current byte is stored in RLE_prev_, RLE_pcount_ is 1.
          RLE_prev_ = ch;
          RLE_pcount_ = 1;
      }
    }
  }

  /** this method adds a block of bytes to the byte stream to be
   *  compressed with the RLE compressor.
   *  @param buf buffer to be added
   *  @param bufcount number of bytes in buffer
   */
  inline void add(const unsigned char *buf, size_t bufcount)
  {
    if (buf)
    {
      while (bufcount--) add(*buf++);
    }
  }

  /** this method finalizes the compressed RLE stream, i.e. flushes all
   *  pending literal or repeat runs. This method can be called at any
   *  time; however, it must be called before size() or write()
   *  can be used.  Intermediate calls should be avoided since they
   *  possibly decrease the compression ratio.
   */
  inline void flush()
  {
    if (! fail_) // if fail_ is true, do nothing
    {
      // if there are max 1 bytes in the repeat counter, convert to literal run
      if (RLE_pcount_ < 2)
      {
        for (; RLE_pcount_>0; --RLE_pcount_) RLE_buff_[RLE_bindex_++] = OFstatic_cast(unsigned char, RLE_prev_);
      }

      // if we have 128 or more bytes in the literal run, flush buffer
      if (RLE_bindex_ > 129)
      {
          RLE_buff_[0] = 127;
          move(129);
          RLE_bindex_ -= 128;
          if (RLE_bindex_ > 1)
              RLE_buff_[1] = RLE_buff_[129];
          if (RLE_bindex_ > 2)
              RLE_buff_[2] = RLE_buff_[130];
      }

      // if there is still a literal run in the buffer, flush literal run
      if (RLE_bindex_ > 1)
      {
          RLE_buff_[0] = OFstatic_cast(unsigned char, RLE_bindex_-2);
          move(RLE_bindex_);
      }

      // if there is a remaining repeat run, flush this one as well
      if (RLE_pcount_ >= 2)
      {
          RLE_buff_[1] = OFstatic_cast(unsigned char, RLE_prev_);
          // write as many repeat runs as necessary
          for (; RLE_pcount_>0; RLE_pcount_-=128)
          {
            // different PackBit schemes exist. The original from which
            // this code is derived used 0x80 | (RLE_pcount_ - 1)
            // to represent replicate runs.
            // DICOM instead uses 257 - RLE_pcount_
            if (RLE_pcount_ > 128) RLE_buff_[0] = 0x81;
              else RLE_buff_[0] = OFstatic_cast(unsigned char, 257 - RLE_pcount_);
            move(2);
          }
      }

      // now the buffer is guaranteed to be empty, re-initialize
      RLE_buff_[0] = 0;
      RLE_prev_ = -1;
      RLE_pcount_ = 0;
      RLE_bindex_ = 1;
    }
  }

  /** returns the size of compressed RLE stream in bytes.
   *  The size is guaranteed to be an even number of bytes (padded
   *  with a trailing zero byte as required by DICOM if necessary).
   *  This method may only be called after flush() has been executed
   *  to finalize the compressed stream.
   *  @return size of compressed stream, in bytes
   */
  inline size_t size() const
  {
    size_t result = blockList_.size() * DcmRLEEncoder_BLOCKSIZE + offset_;
    if (pad_ && (result & 1)) result++; // enforce even number of bytes
    return result;
  }

  /** returns true if the RLE compressor has run out of memory.  In this case,
   *  no output has been created.
   */
  inline OFBool fail() const
  {
    if (fail_) return OFTrue; else return OFFalse;
  }

  /** copies the compressed RLE byte stream into a target array of at least
   *  size() bytes.
   *  @param target pointer to array of at least size() bytes, must not be NULL.
   */
  inline void write(void *target) const
  {
    if ((!fail_) && target)
    {
      unsigned char *current = NULL;
      unsigned char *target8 = OFstatic_cast(unsigned char *, target);
      OFListConstIterator(unsigned char *) first = blockList_.begin();
      OFListConstIterator(unsigned char *) last = blockList_.end();
      while (first != last)
      {
        current = *first;
        memcpy(target8, current, DcmRLEEncoder_BLOCKSIZE);
        target8 += DcmRLEEncoder_BLOCKSIZE;
        ++first;
      }
      if (offset_ > 0)
      {
        memcpy(target8, currentBlock_, offset_);
      }

      // pad to even number of bytes if necessary
      if (pad_ && ((blockList_.size() * DcmRLEEncoder_BLOCKSIZE + offset_) & 1))
      {
        target8 += offset_;
        *target8 = 0;
      }
    }
  }

  /** copies the compressed RLE byte stream into an
   *  output stream
   *  @param os output stream
   */
  inline void write(DcmEncoderOutputStream& os) const
  {
    if (!fail_)
    {
      OFListConstIterator(unsigned char *) first = blockList_.begin();
      OFListConstIterator(unsigned char *) last = blockList_.end();
      while (first != last)
      {
        os.write(*first, DcmRLEEncoder_BLOCKSIZE);
        ++first;
      }
      if (offset_ > 0)
      {
        os.write(currentBlock_, offset_);
      }

      // pad to even number of bytes if necessary
      if (pad_ && ((blockList_.size() * DcmRLEEncoder_BLOCKSIZE + offset_) & 1))
      {
        unsigned char c = 0;
        os.write(&c, 1);
      }
    }
  }

private:

  /// private undefined copy constructor
  DcmRLEEncoder(const DcmRLEEncoder&);

  /// private undefined copy assignment operator
  DcmRLEEncoder& operator=(const DcmRLEEncoder&);

  /** this method moves the given number of bytes from buff_
   *  to currentBlock_ and "flushes" currentBlock_ to
   *  blockList_ if necessary.
   *  @param numberOfBytes number of bytes to copy
   */
  inline void move(size_t numberOfBytes)
  {
    size_t i=0;
    while (i < numberOfBytes)
    {
      if (offset_ == DcmRLEEncoder_BLOCKSIZE)
      {
        blockList_.push_back(currentBlock_);
        currentBlock_ = new unsigned char[DcmRLEEncoder_BLOCKSIZE];
        offset_ = 0;
        if (! currentBlock_) // out of memory
        {
          fail_ = 1;
          break;    // exit while loop
        }
      }
      currentBlock_[offset_++] = RLE_buff_[i++];
    }
  }

  /* member variables */

  /** this flag indicates a failure of the RLE codec.  Once a failure is
   *  flagged, the codec will consume all input and not produce any more
   *  output.  A failure status can only be caused by an out-of-memory
   *  condition.
   */
  int fail_;

  /** this flag indicates whether the RLE codec must pad encoded
   *  data to an even number of bytes (as required by DICOM).
   *  True if padding is required, false otherwise
   */
  int pad_;

  /** this member points to a block of size DcmRLEEncoder_BLOCKSIZE
   *  (unless fail_ is true). This is the current block of data to
   *  which the RLE stream is written
   */
  unsigned char *currentBlock_;

  /** contains the number of bytes already written the the memory
   *  block pointed to by currentBlock_.  Value is always less than
   *  DcmRLEEncoder_BLOCKSIZE.
   */
  size_t offset_;

  /** this member contains a list of memory blocks of size DcmRLEEncoder_BLOCKSIZE
   *  which already have been filled with encoded RLE data.
   *  The current block (pointed to by currentBlock_) is not contained in this list.
   */
  OFList<unsigned char *> blockList_;

  /** this member points to a buffer of 132 bytes that is used by the RLE
   *  encoding algorithm.
   */
  unsigned char *RLE_buff_;

  /** value of the last byte fed to the RLE compressor.  This byte is not yet
   *  stored in the RLE_buff_ buffer.
   *  Type is int because this allows an "impossible" -1 as default value
   */
  int RLE_prev_;

  /** repeat counter, for RLE compressor
   *  may temporarily become negative, guaranteed to be >= 0 between method calls.
   */
  int RLE_pcount_;

  /** index of next unused byte in RLE_buff_.
   */
  unsigned int RLE_bindex_;

};

#endif


/*
 * CVS/RCS Log
 * $Log: dcrleenc.h,v $
 * Revision 1.12  2005/12/16 09:04:47  onken
 * - Added virtual (dummy) destructor to avoid compiler warnings
 *
 * Revision 1.11  2005/12/08 16:28:38  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.10  2004/01/16 14:06:20  joergr
 * Removed acknowledgements with e-mail addresses from CVS log.
 *
 * Revision 1.9  2003/08/14 09:00:56  meichel
 * Adapted type casts to new-style typecast operators defined in ofcast.h
 *
 * Revision 1.8  2003/06/12 18:21:24  joergr
 * Modified code to use const_iterators where appropriate (required for STL).
 *
 * Revision 1.7  2003/06/12 13:32:59  joergr
 * Fixed inconsistent API documentation reported by Doxygen.
 *
 * Revision 1.6  2003/03/21 13:06:46  meichel
 * Minor code purifications for warnings reported by MSVC in Level 4
 *
 * Revision 1.5  2002/11/27 12:07:22  meichel
 * Adapted module dcmdata to use of new header file ofstdinc.h
 *
 * Revision 1.4  2002/07/18 12:16:52  joergr
 * Replaced return statement by break in a while loop of an inline function (not
 * supported by Sun CC 2.0.1).
 *
 * Revision 1.3  2002/07/08 07:02:50  meichel
 * RLE codec now includes <string.h>, needed for memcpy on Win32
 *
 * Revision 1.2  2002/06/27 15:15:42  meichel
 * Modified RLE encoder to make it usable for other purposes than
 *   DICOM encoding as well (e.g. PostScript, TIFF)
 *
 * Revision 1.1  2002/06/06 14:52:37  meichel
 * Initial release of the new RLE codec classes
 *   and the dcmcrle/dcmdrle tools in module dcmdata
 *
 *
 */
