# NextTTD (JDrive)

This is an [OpenTTD](https://www.openttd.org/) port to Java.

[![Java CI](https://github.com/dzavalishin/jdrive/actions/workflows/ant-ci.yml/badge.svg)](https://github.com/dzavalishin/jdrive/actions/workflows/ant-ci.yml)

Current state: The game is basically playable - aircrafts, trains, ships and road vehicles are working. But Save/load is not completely tested.

You're welcome to take part in testing and/or development!

## News

  * AI is operational
  * Language packs support restored
  * Sounds/MIDI music are working 


Screenshots:

![27 Aug 2021](https://raw.githubusercontent.com/dzavalishin/jdrive/master/docs/history/2021-08-27_18-20-18.png)

![23 Aug 2021](https://user-images.githubusercontent.com/11458393/130508122-ea062c84-1a82-4f90-ab91-c5e9f677639f.png)

## What for

OpenTTD is beautiful. But it still carries on most of the original TTD architectural solutions. Bit banging, obscure data structures and crazy assembler style encoding of tile state.

It's time to move on.

My goals:

  * Reduce code complexity and obscurity. Core functions code must be twice shorter in terms of byte count.
  * No more bit fields and C/asm style polymorphism. Make code more OO and, where possible, functional.
  * Make game save automatic. No manual enumeration of fields to save.  

As a result game code must be really easy to understand and modify.

Example:

Obscure and long:
```c
FOR_ALL_INDUSTRIES(ind) {
	if (ind->xy != 0 && (cargo_type == ind->accepts_cargo[0] || cargo_type
			 == ind->accepts_cargo[1] || cargo_type == ind->accepts_cargo[2]) &&
			 ind->produced_cargo[0] != CT_INVALID &&
			 ind->produced_cargo[0] != cargo_type &&
			 (t = DistanceManhattan(ind->xy, xy)) < 2 * u) 
	{
		...
	}
}
```

Easy to understand and short:
```java
Industry.forEach( ind ->
{			
	if (ind.isValid() && ind.acceptsCargo(cargo_type) 
			&& !ind.producesCargo(cargo_type)
			&& (t = Map.DistanceManhattan(ind.xy, xy)) < 2 * u[0]) 
	{
		...
	}
});
```

One more example. Code:

```c
static bool AnyTownExists(void)
{
	const Town* t;

	FOR_ALL_TOWNS(t) {
		if (t->xy != 0) return true;
	}
	return false;
}
```
Becomes:
```java
public static boolean anyTownExist()
{
	return stream().anyMatch( t -> t.isValid() );
}
```



## Why Java

* Portability for free. It just runs everywhere. Really.
* Cleaner code. (Not yet, but I'm on my way:)
* It is easier to build complex data structures in a language with GC.
* Mature graphics, sound and midi subsystems




## Build and run

* just run ```mvnw clean install``` to generate the executable
* then ```java -jar target\jdrive-1.0.0-SNAPSHOT.jar```
