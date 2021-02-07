# Rundeck KMS Storage Converter Plugin

This plugin permits encrypting storage contents in Rundeck using an Amazon Key Management Service
Key. This enables encryption and decryption of secrets in Rundeck without the actual secret key
installed on the Rundeck server via the KMS API.

## Requirements

This plugin requires Rundeck 3.3.9 or newer, as that's the version of the Rundeck APIs we're
built against. This plugin also assumes Java 11 or newer because it's 2021, and we should all
be running a modern version of Java. Also, it makes me type less and I'm doing this for free. :P

## Installation

Binary versions of the plugin can be found on the Releases page of this repository. You can also
build it from source.