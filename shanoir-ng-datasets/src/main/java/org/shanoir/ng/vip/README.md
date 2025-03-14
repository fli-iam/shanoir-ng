# VIP execution directory and object details

A short description for all VIP directories in Shanoir :

* Execution : an object corresponding to a VIP pipeline execution. It's relative to some inputs and outputs, and has others data as the execution date. Usually created at VIP HTTP response receipt.
* Execution monitoring : an object representing VIP execution while VIP HTTP response is not received. It's also used for gathering multiple execution in one reference frame.
* Output : a directory usefull for manipulating VIP execution outputs
* Path : a directory used no by Shanoir but by VIP's Carmin API, to allow resources/dataset download into their environnement (precisely, used by the job environnement in the HTC cluster).
* Pipeline : an  object corresponding to the available pipeline in the VIP servers.
* Processing resource : an object relative to the archive (containing datasets) sent to VIP for executions.
* Shared : Miscellaneous resources.

# Tips

Some tips :

* Execution and execution monitoring are structured in a really stranged way. Execution monitoring are extending execution (in their class definition, not in their usage). When Shanoir's back receives a VIP execution request : 
  * it creates 1st an execution monitoring with an partial execution as child.
  * when a VIP response is received, instead of completing the existing execution with outputs an other datas, a new one is created (without any parent/child relation), and the old one is removed (with its child)