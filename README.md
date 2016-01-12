# TextImageView
An ImageView subclass that draws a spannable text on top of the image

[ ![Download](https://api.bintray.com/packages/anpez/maven/textimageview/images/download.svg) ](https://bintray.com/anpez/maven/textimageview/_latestVersion)

[ ![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-TextImageView-green.svg?style=true)](https://android-arsenal.com/details/1/3017)

![Snapshot](https://raw.githubusercontent.com/ANPez/TextImageView/master/snapshot.gif)

## Requirements
Android 1.0, API 1

## Usage
### Gradle dependency

```groovy
dependencies {
  compile 'com.antonionicolaspina:textimageview:1.0'
}
```

### Sample layout

```xml
<com.antonionicolaspina.textimageview.TextImageView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:text="my text"
    app:textSize="30sp"
    app:textColor="#ff0000"
    app:panEnabled="true"
    app:clampTextMode="textInside"
    android:src="@drawable/sample"/>
```

## License
    Copyright 2016 Antonio Nicolás Pina

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
