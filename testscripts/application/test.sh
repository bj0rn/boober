#!/usr/bin/env bash
oc whoami -t || (echo "You must be logged into openshift" && exit 1)

token=$(oc whoami -t)

http --timeout 300 GET :8080/aurora/application/namespace/paas-utv/application/refapp Authorization:"bearer $token"
