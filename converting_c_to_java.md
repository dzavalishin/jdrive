if( 0 != ...

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


