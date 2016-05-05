# mobile_programming_server
This project is still work in progress:

The server for an app I write for a computer science course. The server should check a given directory and all its subdirectories for changes in the file and track those.
The changes are tracked via deltas (only add, remove and change for a file, nothing more specific).
If one or more files were changed since the last check a new revision is created with all changed files for this revision. If a client asks for a sync the server calculates which files have to be sinced based on the revision numbers of the client and the server (server is always the latest number, client can be any number smaller than that). The server then sends the files to the client (TODO here)
A client has to authenticate himself first if he wants access to the data from the server via a key that is created anew for each authentication. The Key is encrypted with a secret that both the server and the client now (basically a password)

