#! /bin/sh

echo '[run lein check]'
lein check  

echo '[run lein kibit]'
lein kibit

echo '[run lein bikeshed]'
lein bikeshed

