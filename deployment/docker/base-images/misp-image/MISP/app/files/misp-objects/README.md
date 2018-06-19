# misp-objects

[![Build Status](https://travis-ci.org/MISP/misp-objects.svg?branch=master)](https://travis-ci.org/MISP/misp-objects)

MISP objects to be used in MISP (2.4.80 (TBC)) system and can be used by other information sharing tool. MISP objects
are in addition to MISP attributes to allow advanced combinations of attributes. The creation of these objects
and their associated attributes are based on real cyber security use-cases and existing practices in information sharing.

Feel free to propose your own MISP objects to be included in MISP. The system is similar to the [misp-taxonomies](https://github.com/MISP/misp-taxonomies) where anyone can contribute their own objects to be included in MISP without modifying software.

## Format of MISP objects

~~~~json
{
        "name": "domain|ip",
        "meta-category": "network",
        "description": "A domain and IP address seen as a tuple in a specific time frame.",
        "version": 1,
        "attributes" :
        {
                "ip": {
                        "misp-attribute": "ip-dst",
                        "ui-priority": 1,
                        "categories": ["Network activity","External analysis"]
                },
                "domain": {
                        "misp-attribute": "domain",
                        "ui-priority": 1,
                        "categories": ["Network activity","External analysis"]
                },
                "first-seen": {
                        "misp-attribute": "datetime",
                        "ui-priority": 0
                },
                "last-seen": {
                        "misp-attribute": "datetime",
                        "ui-priority": 0
                }

        },
        "required": ["ip","domain"]
}
~~~~

A MISP object is described in a simple JSON file containing the following element.

* **name** is the name of the your object.
* **meta-category** is the category where the object falls into. (file, network, financial, misc, internal)
* **description** is a summary of the object description.
* **version** is the version number as a decimal value.
* **required** is an array containing the minimal required attributes to describe the object.
* **requiredOneOf** is an array containing the attributes where at least one need to be present to describe the object.
* **attributes** contains another JSON object listing all the attributes composing the object.

Each attribute must contain a reference **misp-attribute** to reference an existing attribute definition in MISP.
An array **categories** shall be used to described in which categories the attribute is. The **ui-priority**
describes the usage frequency of an attribute. This helps to only display the most frequently used attributes and
allowing advanced users to show all the attributes depending of their configuration. An optional **multiple** field
shall be set to true if multiple elements of the same key can be used in the object. An optional **values_list**
where this list of value can be selected as a value for an attribute. An optional **sane_default** where this list of value recommend
potential a sane default for an attribute. An optional **disable_correlation** boolean field to suggest the disabling of correlation
for a specific attribute.

## Existing MISP objects

* [objects/ail-leak](objects/ail-leak/definition.json) -  information leak object as defined by the [AIL Analysis Information Leak framework](https://www.github.com/CIRCL/AIL-framework).
* [objects/cookie](objects/cookie/definition.json) - A cookie object describes an HTTP cookie including its use in malicious cases.
* [objects/ddos](objects/ddos/definition.json) - DDoS object describes a current DDoS activity from a specific or/and to a specific target.
* [objects/domain-ip](objects/domain-ip/definition.json) - A domain and IP address seen as a tuple in a specific time frame.
* [objects/elf](objects/elf/definition.json) - Object describing an Executable and Linkable Format (ELF).
* [objects/elf-section](objects/elf-section/definition.json) - Object describing a section of an Executable and Linkable Format (ELF).
* [objects/email](objects/email/definition.json) - An email object.
* [objects/file](objects/file/definition.json) - File object describing a file with meta-information.
* [objects/geolocation](objects/geolocation/definition.json) - A geolocation object to describe a location.
* [objects/ip-port](objects/ip-port/definition.json) - An IP address and a port seen as a tuple (or as a triple) in a specific time frame.
* [objects/macho](objects/macho/definition.json) - Object describing a Mach object file format.
* [objects/macho-section](objects/macho-section/definition.hson) - Object describing a section of a Mach object file format.
* [objects/passive-dns](objects/passive-dns/definition.json) - Passive DNS records as expressed in [draft-dulaunoy-dnsop-passive-dns-cof-01](https://tools.ietf.org/html/draft-dulaunoy-dnsop-passive-dns-cof-01).
* [objects/pe](objects/pe/definition.json) - Portable Executable (PE) object.
* [objects/pe-section](objects/pe-section/definition.json)  - Portable Executable (PE) object - section description.
* [objects/phone](objects/phone/definition.json) - A phone or mobile phone object.
* [objects/registry-key](objects/registry-key/definition.json) - A registry-key object.
* [objects/r2graphity](objects/r2graphity/definition.json) - Indicators extracted from binary files using radare2 and graphml.
* [objects/tor-node](objects/tor-node/definition.json) - Tor node description which are part of the Tor network at a time. 
* [objects/vulnerability](objects/vulnerability/definition.json) - Vulnerability object to describe software or hardware vulnerability as described in a CVE.
* [objects/url](objects/url/definition.json) - url object describes an url along with its normalized field (e.g. using faup parsing library) and its metadata.
* [objects/whois](objects/whois/definition.json) - Whois records information for a domain name.
* [objects/x509](objects/x509/definition.json) - x509 object describing a X.509 certificate.

## MISP objects relationships

The MISP object model is open and allows user to use their own relationships. MISP provides a list of default relationships that can be used if you plan to share your events with other MISP communities.

- [relationships](relationships/definition.json) - list of predefined default relationships which can be used to link MISP objects together and explain the context of the relationship.

## How to contribute MISP objects?

Fork the project, create a new directory in the [objects directory](objects/) matching your object name. Objects must be composed
of existing MISP attributes. If you are missing a specific attributes, feel free to open an issue in the [MISP project](https://www.github.com/MISP/MISP).

We recommend to add a **text** attribute in a object to allow users to add comments or correlating text.

If the unparsed object can be included, a **raw-base64** attribute can be used in the object to import the whole object.

When the object is created, pull a request on this project. We usually merge the objects if it fits existing use-cases.

## What are the advantages of MISP objects versus existing standards?

MISP objects are dynamically used objects that are contributed by users of MISP (the threat sharing platform) or other information sharing platforms.

The aim is to allow a dynamic update of objects definition in operational distributed sharing systems like MISP. Security threats and their related indicators are quite dynamic, standardized formats are quite static and new indicators require a significant time before being standardized.

The MISP objects model allows to add new combined indicators format based on their usage without changing the underlying code base of MISP or other threat sharing platform using it. The definition of the objects can be then propagated along with the indicators itself.

## License

~~~~

Copyright (C) 2016-2017 Andras Iklody
Copyright (C) 2016-2017 Alexandre Dulaunoy
Copyright (C) 2016-2017 CIRCL - Computer Incident Response Center Luxembourg

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

~~~~
