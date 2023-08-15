cp $1 $1.orig 
sed -f sed.in $1.orig > $1.new 
mv $1.new $1
