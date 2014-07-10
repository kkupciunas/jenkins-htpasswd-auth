Jenkins htpasswd Plugin
=======================

This plugin allows Jenkins (http://jenkins-ci.org) to use Apache *htpasswd* 
file as an alternative access control security realm.

Why? I did it just because I needed user access integration with existing
services that used htpasswd-based access control.

Features
========

Supported password hashes:
 - MD5 ($apr1$)
 - SHA1 ({SHA})
 - CRYPT (Unix crypt)

Bcrypt is not yet supported.

There are no plans to support plain text passwords.


Installation
============

Maven (http://maven.apache.org/) is required to build plugin package.

Run
	`mvn clean package`
to create plugin .hpi file

Pick up *target/htpasswd-auth.hpi* and install it either by
 * placing it to the $JENKINS_HOME/plugins directory
 * or use Advanced tab in Jenkins plugin management console to upload hpi file

It is necessary to restart Jenkins for the plugin to get activated.

Usage
=====

Pretty much straightforward -
  `Jenkins -> Configure Global Security -> Enable security`

then there should be **htpasswd** option under `Access Control -> Security Realm` choices.

Specify the location of htpasswd file and you're done. Please note, that there is no UI
to manage htpasswd file itself, you still have to maintain it using htpasswd utility or
whatever other means you used to do it before.


TODO
====
 - bcrypt support (htpasswd uses $2y$ salt revision, jbcrypt doesn't like it..)
 - htgroup support (parsing code is there, just need to plug it into SecurityRealm)
 - multiple htpasswd file (combined) support (undecided on that one yet)


Development
===========

Compile & package
-----------------
	`mvn clean package`

Run 
---
	`mvn hpi:run -Djetty.port=9090`

Debug
-----
```
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
mvn hpi:run -Djetty.port=9090
```

License
-------

  The MIT License
 
  Copyright (c) 2014, Kestutis Kupciunas (aka kesha)
 
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 

