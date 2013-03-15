#! /bin/sh

echo '[run lein check ...]'
lein check  

echo '[run lein kibit ...]'
lein kibit

echo '[lein eastwood ...]'
lein eastwood

echo '[run lein bikeshed ...]'
lein bikeshed
