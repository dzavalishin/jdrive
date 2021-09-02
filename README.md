# NextTTD (JDrive)

This is an [OpenTTD](https://www.openttd.org/) port to Java.

[![Java CI](https://github.com/dzavalishin/jdrive/actions/workflows/ant-ci.yml/badge.svg)](https://github.com/dzavalishin/jdrive/actions/workflows/ant-ci.yml)

Current state: The game is basically playable - aircrafts, trains, ships and road vehicles are working. But Save/load is not completely tested.

You're welcome to take part in testing and/or development!

Screenshots:

![27 Aug 2021](https://raw.githubusercontent.com/dzavalishin/jdrive/master/docs/history/2021-08-27_18-20-18.png)

![23 Aug 2021](https://user-images.githubusercontent.com/11458393/130508122-ea062c84-1a82-4f90-ab91-c5e9f677639f.png)

## What for

OpenTTD is beautiful. But it still carries on most of the original TTD architectural solutions. Bit banging, obscure data structures and crazy assembler style encoding of tile state.

It's time to move on.

My goals:

  * Reduce code complexity and obscurity. Core functions code must be twice 

## Why Java

  * Portability for free. It just runs everywhere. Really.
  * Cleaner code. (Not yet, but I'm on my way:)
  * It is easier to build complex data structures in a language with GC.
  * Mature graphics, sound and midi subsystems

