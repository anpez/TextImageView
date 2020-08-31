# TextImageView
An ImageView subclass that draws one or more spannable, rotable, scalable texts on top of the image.

[ ![Download](https://api.bintray.com/packages/anpez/maven/textimageview/images/download.svg) ](https://bintray.com/anpez/maven/textimageview/_latestVersion)

[ ![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-TextImageView-green.svg?style=true)](https://android-arsenal.com/details/1/3017)

![Snapshot](https://raw.githubusercontent.com/ANPez/TextImageView/master/snapshot.gif)

Provided that this is an ImageView subclass, you can use Picasso, Glide, or any Image loader that you like to load the image into TextImageView.

## New in 4.0
Rewritten in kotlin+androidx

## Requirements
Android 4.0, API 14

## Usage
### Gradle dependency

```groovy
dependencies {
  implementation 'com.antonionicolaspina:textimageview:4.0'
}
```

### Sample layout

```xml
<com.antonionicolaspina.textimageview.TextImageView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:text="my text"
    android:textSize="30sp"
    android:textColor="#ff0000"
    app:tiv_panEnabled="true"
    app:tiv_scaleEnabled="true"
    app:tiv_rotationEnabled="true"
    android:src="@drawable/sample"/>
```

## License
    Copyright 2020 YaikoStudio

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
