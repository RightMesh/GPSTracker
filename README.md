# GPS Tracker [![Build Status](https://travis-ci.com/RightMesh/GPSTracker.svg?branch=master)](https://travis-ci.com/RightMesh/GPSTracker)

## What is this?

GPS Tracker is a simple Android application that illustrates how RightMesh devices interact with their associated [SuperPeer](https://medium.com/rightmesh/rightmesh-roadmap-multiple-superpeers-implementation-plan-and-progress-e637be9d53fb).

Users simply accept the Location Service permission prompts and move to several locations so that the SuperPeer can receive the location updates.

## How do I build it?

GPS Tracker is built in Android Studio, and should be able to be opened once this repo has been cloned. Note that you will have to sign up for a RightMesh developer account in order to download our library and license verification Gradle plugin - please check out [https://rightmesh.io/developers](https://rightmesh.io/developers) for more information.

If you want to change GPSTracker configuration (RightMesh port, SuperPeer information), you might need to revise the value of `buildConfigField` in `build.gradle`

## What is RightMesh?

RightMesh is an SDK that is trying change the paradigm from “Always Connected to the Internet” and let everyone simply be “Always Connected” - to people, to devices, to our communities, to what matters in our world. RightMesh connects smartphones even when the Internet and mobile data can’t. [Check out our website for more details!](https://www.rightmesh.io)
