#!/bin/bash

# Copyright 2013 University of Michigan
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

SCRIPT_DIR=$(cd $(dirname $0); pwd)
. $SCRIPT_DIR/capsules.cfg

kvm-ok 2>&1 >>/dev/null

if [[ $? -ne 0 || ! -e /dev/kvm ]]; then
  echo "Starting KVM"
  modprobe kvm_intel
fi

kvm-ok 2>&1 >>/dev/null

if [[ $? -ne 0 || ! -e /dev/kvm ]]; then
  echo "Error: Failed to start kvm"
  exit 1
fi

chown root:kvm /dev/kvm
chmod 660 /dev/kvm

exit 0
