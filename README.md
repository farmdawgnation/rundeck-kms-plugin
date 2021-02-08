# Rundeck KMS Storage Converter Plugin

This plugin permits encrypting storage contents in Rundeck using an Amazon Key Management Service
Key. This enables encryption and decryption of secrets in Rundeck without the actual secret key
installed on the Rundeck server via the KMS API.

# Usage

## Requirements

This plugin requires Rundeck 3.3.9 or newer, as that's the version of the Rundeck APIs we're
built against.

## Installation

Binary versions of the plugin can be found on the Releases page of this repository. You can also
build it from source.

The full JAR is a bit hefty because we're required to ship the entire AWS SDK to get KMS support.
If I get interest in a version of the JAR that uses a _shared_ AWS SDK, I'm happy to provide one.
That would work better for folks who already package the AWS SDK in Rundeck's classpath in its
entirety.

## Configuration

The following in your `rundeck-config.properties` will configure this plugin:

```
rundeck.storage.converter.1.type=rundeck-kms-plugin
rundeck.storage.converter.1.path=keys
rundeck.storage.converter.1.config.keyArn=<ARN TO YOUR KEY>
rundeck.storage.converter.1.config.accessKeyId=<ACCESS KEY ID>
rundeck.storage.converter.1.config.secretAccessKey=<SECRET ACCESS KEY>
```

# About the Author

I'm Matt Farmer, a Staff Engineer at [Greenlight Financial Technology][gl] in
Platform Engineering.

[gl]: https://greenlightcard.com
