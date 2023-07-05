#include "mydcmview.h"
#include "Serializers.hh"

//QApplication *app;
/*
bool DumpFile = false;
bool AcrNema = false;
bool RAWdump = false;
*/

std::string destdir = ".";
std::string keepAcq = "";

int main(int ac,  char* av[])
{
  //  app = new QApplication(ac, av);
  Dicomfile window;


  for (int i = 0; i <ac; ++i)
    {
      static const std::string dir="--dir";
      static const std::string acq="--keep-acq";
      if (dir == av[i] && i+1 < ac)
      {
          std::cout << "data will be saved in " << av[i+1] << std::endl;
          destdir = av[i+1];
          i++;
      }

      if (acq == av[i] && i+1 < ac)
      {
          std::cout << "Keeping only acquisition " << av[i+1] << std::endl;
          keepAcq = av[i+1];
          i++;
      }


  }

  if (ac != 1)
    window.loaddir(av[1]);
//   else
//     {
//       QString file = QFileDialog::getOpenFileName(NULL,
// 						  "Choose a file",
// 						  "/mnt/cdrom",
// 						  "Images (DICOMDIR dicomdir *)");
//       if (file.isEmpty()) return 0;
//       window.loaddcm(file.toLatin1());
//     }

  //  window.show();
  //  return app->exec();
  //  delete app;
  return 0;
}
