# umls-graph-api

To start using the API, you'll first need to create the database from your UMLS download. To create the database, create a directory called "neo4j" in your project directory. Then run the RelReader class with one argument -- a pointer to the directory in the UMLS download with all the .RRF files. On my system it is at ~/mnt/r/temp/2015AB/META/.

Next, run TestGraphFunctions as a junit test and make sure the tests pass. Assuming everything worked, then you can inspect this file to see a few sample use cases.

The database only indexes the ISA relationship from the MRREL.RRF file, so it is only good for hypernym/hyponym relations. It is also restricted to the same set of Type Unique Identifiers (TUIs) that the Apache cTAKES dictionary. With these restrictions, the database is still 45 MB. While this is not unreasonable, in its intended use case as a feature source for machine learning systems it will get hit a lot and needs to be very fast.
