# About

This is a simple tool to help load users and their respective roles into I2B2.  This was created in order to load a known group of users (15000)
into a new I2B2 installation with multiple projects, where the project(s) the user had access to is determined by the domain associated with the
email-like ID (in our case, a shibboleth ID).

## Usage

To use the project, you will need to:

* Create a source file with all your users in it.
** The file is tab delimited.
** The file contains 3 columns: UserID, First Name, Last Name
* Configure the src/resources/i2b2-user-load.props to point to the source file you are loading and the database you are loading to.
* Run the load process using the ``lein run`` command.

The process with inform you of progress through logging every 25th user saved.  Feel free to change as you wish.

## License

Copyright © 2013 Health Sciences of South Carolina

Distributed under the Eclipse Public License, the same as Clojure.
