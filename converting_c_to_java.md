* if( 0 != ...

 if(0 != (cmd & Cmd.CMD_AUTO))

byte -> int

clever use of ,

```
( bonus += 10, age > 10 ) ||
( bonus += 20, age > 5 ) ||
( bonus += 40, age > 2 ) ||
( bonus += 100, true )
```


simple pointers -> one el arrays

```
modify( int[] x )
{
  x[0]++;
}
```


* Pointer or allocated local object

```
NPFFindStationOrTileData fstd;
...
NPFFillWithOrderData(&fstd, v);
```
must be

```
NPFFindStationOrTileData fstd = new NPFFindStationOrTileData();
...
NPFFillWithOrderData(fstd, v);
```


* No enums

if( flags & FLG_USED ) does not work on Java enums
use constants and ints

* no macros

```
BitOps.SB(tile.getMap().m2, 0, 4, new_ground);
```

to


```
tile.getMap().m2 = BitOps.RETSB(tile.getMap().m2, 0, 4, new_ground);
```


* bad replacements

'finalruct' was 'construct' initially




---
SignalState
Track
TrackBits
Trackdir
TrackdirBits


