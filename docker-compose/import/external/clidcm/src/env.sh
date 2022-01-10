#!/bin/sh
##
## env.sh
## Login : <nwiestda@pallium.irisa.fr>
## Started on  Tue Apr 11 17:47:53 2006 Nicolas Wiest-Daessle
## $Id$
## 
## Copyright (C) 2006 Nicolas Wiest-Daessle
## This program is free software; you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 2 of the License, or
## (at your option) any later version.
## 
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU General Public License for more details.
## 
## You should have received a copy of the GNU General Public License
## along with this program; if not, write to the Free Software
## Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
##

export DCMDICTPATH=~/work/dicom/dicom.dic:~/work/dicom/private.dic
export QTDIR=/local/qt-4/
export PATH=$QTDIR/bin:$PATH
export LD_LIBRARY_PATH=$QTDIR/lib:$LD_LIBRARY_PATH
#export QMAKESPEC=/udd/medical/soft/QT/QT4/qt-4-build/mkspecs/linux-g++

