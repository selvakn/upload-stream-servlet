Demo
====

Is it possible for java servlet to handle file upload as real streams?
Yes. this is a demo for the same.

`curl -v --limit-rate 4000k -X POST -F "file=@/Users/selva/Downloads/test.file" "http://localhost:8080"`
