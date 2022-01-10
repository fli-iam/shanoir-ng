# Scripts

This folder contains scripts to process shanoir front files.

For now there are two scripts:

 - errorFix.py enables to find & fix 'private attribute' errors by parsing the angular error logs and replacing 'protected|private attributeName' by 'public attributeName' in conserned .ts files.
  - convertRoutes which enable to generate static routes from dynamic ones (which were used with the older angular version).