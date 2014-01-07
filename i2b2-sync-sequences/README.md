# About

Our infrastructure has multiple I2B2 enviroments that support development and proliferation, such as development, staging, and production.  Is it common to clone the data schemas for certain I2B2 project repositories from production to any of the other enviroments.  The current cloning process moves the schema tables and data, but leaves behind objects such as sequences.  With the sequences and data not in sync, the target I2B2 environment typically fails to operate properly.

This tool is used to get sequences in sync with the newly imported data from production by resetting the sequence to the largest (+1) value of the identity column that utilizes the sequence.


## Usage

To run from the repository, modify the connection properties in the local/i2b2-sync-sequences.props file, then call lein:

```
   lein with-profile local run
```

## License

Copyright © 2014 Health Sciences of South Carolina

Distributed under the Eclipse Public License either version 1.0.
