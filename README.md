# Description

Implementation of aggreplay using bcel.

Assumptions:
 * Assumes deterministic thread execution, order of object and thread creation should be the same
 * Able to instrument the code which causes the bug. If it is in some library that cannot be accessed, we are unable to log their shared memory access

TODO:
 - [ ] Keep track of unique id for shared objects per thread -> by order of creation. -> data needed per thread (Do by tonight??)
 - [x] Keep track of unique id for threads -> by order of creation
 - [ ] Record the read execution
 - [ ] Record write execution
 - [ ] Store the variables to keep track of in a class to be populated by some form of file

Problems faced:
 * Instrumentation of Thread -> need to enable retransformable classes + add Can-RetransformClasses to manifest
 * Unique id for threads for consistency across runs -> use of a tree (for effeciency, do by reverse)
 * Tracing of read-write instructions, cannot do phase by phase, because the read instrumentation might add putfield instructions
 	* Might be a non issue
 * Tracing can accidentally trace items within the package -> use a filter to filter out all under the package name
 * Cannot add fields to classes already loaded -> should only add fields for static classes
 	* not really needed if we can make use of the existing object reference and what jvm guarantees (which it sadly doesn't guarantee uniquenesse)
 * Constant pool stores the Fieldrefs, have to loop through and add the string to constant pool, to pass into storage.

 
Some thoughts:
 * Local variables should not have any problems, as they can only be read iff they are final
   * Therefore fields are the more impt parts.