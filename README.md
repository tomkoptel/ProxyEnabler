# NOT PUBLISHED CAN BE BUILD ONLY FROM SOURCES

# Plugin
Plugin to enable httpProxy.

```
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.olderwold.proxyenabler")
}
```

 Just pass `-PprojectAllowHttpProxy=true` while building.
 ```
 ./gradlew :app:assembleDebug -PprojectAllowHttpProxy=true
 ```
 
