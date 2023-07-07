#########################################################################
# Dicomifier.ws - Copyright (C) Universite de Strasbourg
# Distributed under the terms of the MIT license. Refer to the 
# LICENSE.txt file or to https://opensource.org/licenses/MIT for details.
#########################################################################

import werkzeug
import dicomifier_ws

werkzeug.run_simple(
    "0.0.0.0", 5000, dicomifier_ws.Application.instance(), 
    use_debugger=False, use_reloader=True)
