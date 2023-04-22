# LogsLibraryApp

[![](https://jitpack.io/v/eladsabag/LogsLibraryApp.svg)](https://jitpack.io/#eladsabag/LogsLibraryApp)

## Setup
Step 1. Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
		maven { url 'https://jitpack.io' }
	}
}
```

Step 2. Add the dependency:
```
dependencies {
  implementation 'com.github.eladsabag:LogsLibraryApp:1:00:01'
}
```
## Usage

##### StepProgress Constructor
```java

// For Simple Logs Under LogsLibrary TAG -
        Logger.v("Test V");
        Logger.d("Test D");
        Logger.i("Test I");
        Logger.w("Test W");
        Logger.e("Test E");

```

##### Step 1. Add the following permissions to your manifest:
```java
// For Writing & Reading logs.txt file To Documents/Logs/logs.txt -

// For Android 9:
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

// For Android 10:
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

// Inside application tag:
android:requestLegacyExternalStorage="true"

// For Android 11:
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
        android:minSdkVersion="30"
        tools:ignore="ScopedStorage" />

// Inside application tag:
android:requestLegacyExternalStorage="true"
```
##### Step 2. Set your file and make some logs:
```java

Logger.setLogFile(this);

Logger.v("Test V");
Logger.d("Test D");
Logger.i("Test I");
Logger.w("Test W");
Logger.e("Test E");
```
##### Step 3. Read your logs programmatically(or search them manually under Documents/Logs/logs.txt):

ArrayList<String> readRes = Logger.readLogsFromFile(this);
if (readRes != null) {
  for (String readRe : readRes) {
    String text = main_LBL_logs.getText().toString() + readRe + "\n";
      main_LBL_logs.setText(text);
    }
}

## License

Copyright 2023 Elad Sabag

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
