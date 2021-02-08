#!/bin/bash

set -e

docker build -t rundeck-kms-test-image test-image

docker run -it \
  -e AWS_ACCESS_KEY_ID \
  -e AWS_SECRET_ACCESS_KEY \
  -e AWS_REGION \
  -e RUNDECK_STORAGE_CONVERTER_1_TYPE=rundeck-kms-plugin \
  -e RUNDECK_STORAGE_CONVERTER_1_PATH=keys \
  -e RUNDECK_STORAGE_CONVERTER_1_CONFIG_KEYARN=$KMS_KEY_ARN \
  -p 4440:4440 \
  -v $(pwd)/lib/build/libs/rundeck-kms-plugin.jar:/home/rundeck/libext/rundeck-kms-plugin.jar \
  rundeck-kms-test-image
