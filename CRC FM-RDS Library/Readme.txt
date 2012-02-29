(For the most recent information, please visit our website: http://mmbtools.crc.ca/content/view/53/33/)

Integrate the CRC FM-RDS Library in your Android Application

Perequisites
- You are using Eclipse with the Android SDK plugin
- You have a working Android project

The CRC FM-RDS Library for Android is an archive enclosing:
- libfmrds.so, a dynamic library to access the radio chip and to decode RDS
- libfmrds.jar, a Java archive containing the ca.gc.crc.libfmrds package, that you can import into your project to start implementing FM-RDS functionalities
- /doc folder, a javadoc to document all the functionnality that this library provides

Procedure to Integrate this Library in your Android Project

1) Download and decompress the "CRC FM-RDS Library.zip" archive into your project's root ($PROJ_ROOT).

2) Copy and paste the libfmrds.so file in the following location: $PROJ_ROOT/libs/armeabi/<here>
Only then it will be included in the APK of your application, which is installed on your device in order to run the application. If the .so file is stored elsewhere in your project structure, it will not be included in the APK package, resulting in linker errors when running the application. When cleaning your project, make sure that the .so file remains in /libs/armeabi (in some configurations it was getting deleted/cleaned for no apparent reason).

3) In Eclipse, go in your project properties (Project-> Properties->Java Build Path->Libraries) and click "Add JARs...". Select the JAR file in the folder $PROJ_ROOT/CRC FM-RDS Library that you just decompressed.

4) Expand the JAR item in the menu (click on the "plus" sign) and set the javadoc location to the /doc folder in $PROJ_ROOT/CRC FM-RDS Library.

5) In your own Android application's code, you can now import ca.gc.crc.libfmrds.FMinterface and  instantiate one FMinterface object. All the functionalities are accessible via the FMinterface object. You are now ready to use our FM-RDS radio library!
