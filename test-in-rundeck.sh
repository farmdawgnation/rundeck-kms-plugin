#!/bin/bash

docker run -it \
  -e AWS_ACCESS_KEY_ID \
  -e AWS_SECRET_ACCESS_KEY \
  -e AWS_REGION \
  -p 4440:4440 \
  -v $(pwd)/lib/build/libs/rundeck-kms-plugin.jar:/home/rundeck/libext/rundeck-kms-plugin.jar \
  rundeck/rundeck:3.3.9
