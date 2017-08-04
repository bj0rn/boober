#!/usr/bin/env bash
oc whoami -t || (echo "You must be logged into openshift" && exit 1)

token=$(oc whoami -t)


#http --timeout 300 GET :8080/aurora/application/namespace/paas-utv/application/refapp/health Authorization:"bearer $token"
http --timeout 300 GET http://boober-paas-bjarte-dev.utv.paas.skead.no/aurora/application/namespace/paas-bjarte-dev/application/referanse/health Authorization:"bearer $token"
