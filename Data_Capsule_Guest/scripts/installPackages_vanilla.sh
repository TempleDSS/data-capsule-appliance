#update and upgrade ubuntu
apt-get -y update
#apt-get -y upgrade

#generic ubuntu packages
apt-get install -y git maven parallel curl htop iotop jq pcregrep zsh python-setuptools python-dev python3-setuptools

#python 2.7 packages
easy_install pip
pip install numpy scipy matplotlib pandas nltk wget docopt

#python 3 packages
easy_install3 pip
pip3 install numpy scipy matplotlib pandas nltk wget docopt

#fixing the terminal LANG issue
locale-gen
localectl set-locale LANG="en_US.UTF-8"

# install nfs-mount client
apt-get install -y nfs-common
