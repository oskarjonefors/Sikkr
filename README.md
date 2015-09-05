**Sikkr for Android**
=====
An Android app for communicating safely in traffic.
This app was developed as a project in the course DAT255 - Software Engineering Project at Chalmers University of Technology.

**Getting Started**
=====
The project is divided into a client and a server component. The Android client is located in the <i>sikkr</i> directory in the repo, and the server is located in the <i>server</i> directory. The <i>servertest</i> directory is currently obsolete, but it was crucial in the development of the server.

**Voice message server**
=====
To run your own server you have to edit the constant SERVER_IP in edu.chalmers.sikkr.backend.util.ServerInterface to match your ip. You also have to open the ports specified there, by default port 1123 and port 1124. The group will try to keep the default server running for a couple of months while the project is being graded.

The server can be built and run with the command:
```
gradle:run
```
**SDK Targets**
=====
 - Minimum SDK: **19**
 - Target SDK: **21**
