* if( 0 != ...

 if(0 != (cmd & Cmd.CMD_AUTO))

byte -> int

* clever use of ,

```
( bonus += 10, age > 10 ) ||
( bonus += 20, age > 5 ) ||
( bonus += 40, age > 2 ) ||
( bonus += 100, true )
```

* idiotic use of ,

```
		//if (!tile.IsTileType(TileTypes.MP_RAILWAY) || ((dir = 0, tile.getMap().m5 != 1) && (dir = 1, _tile.getMap().m5 != 2)))
		//	return Cmd.return_cmd_error(Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK);

		if (!tile.IsTileType(TileTypes.MP_RAILWAY))
			return Cmd.return_cmd_error(Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK);

		if(tile.getMap().m5 == 1)
			dir = 0;
		else if(_tile.getMap().m5 == 2)
			dir = 1;
		else
			return Cmd.return_cmd_error(Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK);

```



* simple pointers -> one el arrays

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



* unsigneds

Integer. and Long. have operations needed


* и одним движением брови уничтожил вю мою кунсткамеру

 allocations - draw lists / semi/automatic variables and per-draw allocator


* boolean value used as int - is boolean->int value standard?

* int used as boolean


* java decl does not create object itself = bunch of null ptr accesses


* unsigned problems - negatives where they'r not assumed to be and vice versa

a lot of 16 bit integers in original game, all become unsigneds when load from data files

