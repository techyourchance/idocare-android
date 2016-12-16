#!/bin/bash

if [ ! -d "$(pwd)/bin" ]; then
    echo "Error: couldn't locate ./bin directory"
    exit 1;
fi

echo "$(pwd)"

IDOCARE_BIN_PATH="$(pwd)/bin"
export PATH=$PATH:$IDOCARE_BIN_PATH
