Podbase
=======

Podbase is an image database designed specifically to cater to the concerns of a scientific lab that uses images (such as microscopy) as a primary form of data collection. At it's core, the functionality of Podbase is simple, store custom metadata fields along with the images.

Beyond that core functionality, Podbase strives to priovide a set of features that makes browsing, analyzing, and organizing these images easier.


Setup
=====

A sample application.conf file is available under conf/application.conf.example. Copy this file to application.conf and then configure it to your needs.
Important pieces to configure include:
* Database Configuration (including when the create/update/drop configuration)
* SMTP Server
* Secret key
* HTTP port
