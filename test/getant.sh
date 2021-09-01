# get missing ant-junitlauncher.jar 
cd
wget --no-check-certificate https://dlcdn.apache.org//ant/binaries/apache-ant-1.10.11-bin.tar.gz
tar xvf apache-ant-1.10.11-bin.tar.gz
mkdir .ant
ln -s ~/apache-ant-1.10.11/lib -t ~/.ant
