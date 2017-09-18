# Install script for directory: C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle

# Set the install prefix
IF(NOT DEFINED CMAKE_INSTALL_PREFIX)
  SET(CMAKE_INSTALL_PREFIX "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/../dcmtk-3.5.4-win32-i386")
ENDIF(NOT DEFINED CMAKE_INSTALL_PREFIX)
STRING(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
IF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  IF(BUILD_TYPE)
    STRING(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  ELSE(BUILD_TYPE)
    SET(CMAKE_INSTALL_CONFIG_NAME "RELEASE")
  ENDIF(BUILD_TYPE)
  MESSAGE(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
ENDIF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)

# Set the component getting installed.
IF(NOT CMAKE_INSTALL_COMPONENT)
  IF(COMPONENT)
    MESSAGE(STATUS "Install component: \"${COMPONENT}\"")
    SET(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  ELSE(COMPONENT)
    SET(CMAKE_INSTALL_COMPONENT)
  ENDIF(COMPONENT)
ENDIF(NOT CMAKE_INSTALL_COMPONENT)

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  FILE(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/dcmtk/dcmimgle" TYPE FILE FILES
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dcmimage.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dibaslut.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diciefn.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dicielut.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dicrvfit.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/didislut.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/didispfn.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/didocu.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diflipt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/digsdfn.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/digsdlut.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diimage.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diinpx.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diinpxt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diluptab.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimo1img.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimo2img.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimocpt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimoflt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimoimg.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimoipxt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimomod.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimoopx.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimoopxt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimopx.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimopxt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimorot.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dimosct.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diobjcou.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diovdat.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diovlay.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diovlimg.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diovpln.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dipixel.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diplugin.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dipxrept.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diregbas.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/dirotat.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/discalet.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/displint.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/ditranst.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmimgle/include/dcmtk/dcmimgle/diutils.h"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

