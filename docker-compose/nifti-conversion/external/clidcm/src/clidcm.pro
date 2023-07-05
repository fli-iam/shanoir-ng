# #####################################################################
TEMPLATE = app
TARGET =
DEPENDPATH += .
INCLUDEPATH += .

QT -= gui
CONFIG -= gui
LIBS += -Llibs/

DEFINES += VISTAL_USE_PROPERTIES

LIBS += -ldcmimage -ldcmimgle -ldcmdata -ldcmjpeg -lijg8 -lijg12 -lijg16 -lofstd
LIBS += -lmat -lProperties -lgis -lz -lbz2 


win32-g++ {

INCLUDEPATH += libs/include/ \
                C:\soft\Vistal\Sources\Vistal\Image3d\ \
                C:\soft\Vistal\Sources\Vistal\GIS\ \
                C:\soft\Vistal\Sources\Vistal\Voxel\ \
                C:\soft\Vistal\Sources\Vistal\Stats\ \
                C:\soft\Vistal\Sources\Vistal\Gauss\ \
                C:\soft\Vistal\Sources\Vistal\Recalage\ \
                C:\soft\Vistal\Sources\VistalPlus\Properties\ \
                C:\soft\Vistal\Sources\VistalPlus\DTI\ \
                C:\soft\Vistal\Sources\Vistal\Matrice\ \
                C:\soft\boost_1_42_0\boost_1_42_0\ C:\soft\GnuWin32\include

LIBS +=  -LC:\soft\Vistal\bin\lib -LC:\soft\GnuWin32\lib
LIBS += -lnetapi32 -lwsock32 -lws2_32 -lwsock32
}

linux-g++ {


DEFINES += HAVE_CONFIG_H

INCLUDEPATH +=  /home/aferial/Vistal/Vistal/Sources/Vistal/Image3d/ \
                /home/aferial/Vistal/Vistal/Sources/Vistal/GIS/ \
                /home/aferial/Vistal/Vistal/Sources/Vistal/Voxel/ \
                /home/aferial/Vistal/Vistal/Sources/Vistal/Stats/ \
                /home/aferial/Vistal/Vistal/Sources/Vistal/Gauss/ \
                /home/aferial/Vistal/Vistal/Sources/Vistal/Recalage/ \
                /home/aferial/Vistal/Vistal/Sources/VistalPlus/Properties/ \
                /home/aferial/Vistal/Vistal/Sources/VistalPlus/DTI/ \
                /home/aferial/Vistal/Vistal/Sources/Vistal/Matrice/ 



LIBS += -L/home/aferial/Vistal/build/lib/
LIBS += -ltiff -lpng 
}


HEADERS     = mydcmview.h
SOURCES     = main.cpp

