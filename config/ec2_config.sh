#!/bin/bash
apt-get update -y

# Install AWS Command Line Utilities
apt-get install -y awscli

# Install the base packages
R -e 'install.packages("dplyr")'
R -e 'install.packages("ggplot2")'
R -e 'install.packages("lubridate")'

# Install java 8 (see http://aws-labs.com/ubuntu-install-java-8/)
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
sudo apt-get -y install oracle-java8-installer
sudo apt-get -y install oracle-java8-set-default

# Install Scipy
sudo apt-get -y install build-dep python-numpy python-scipy python-matplotlib ipython ipython-notebook python-pandas python-sympy python-nose 

# Install pip (see https://www.saltycrane.com/blog/2010/02/how-install-pip-ubuntu/)
sudo apt-get -y install python-pip python-dev build-essential 
sudo pip install --upgrade pip 
sudo pip install --upgrade virtualenv

# Install slycot
sudo pip install slycot

# Generate the file structure
mkdir -p /Users/ftubilla/Documents
chown ubuntu /Users/ftubilla/Documents
mkdir -p /Users/ftubilla/Documents/setups_io/inputs
mkdir -p /Users/ftubilla/Documents/setups_io/archive

# TODO
# mkdir output if not present in the java code
# copy python and config to the jar in the pom file