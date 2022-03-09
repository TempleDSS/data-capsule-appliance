# Go to temp directory
cd /tmp 

# You can change what anaconda version you want at 
# https://repo.continuum.io/archive/
wget https://repo.anaconda.com/archive/Anaconda3-5.1.0-Linux-x86_64.sh 

bash Anaconda3-5.1.0-Linux-x86_64.sh -b -p /opt/anaconda
chown -R $SSH_USERNAME /opt/anaconda

cp $USER_HOME_DIR/uploads/anaconda.sh /etc/profile.d/anaconda.sh
source /etc/profile.d/anaconda.sh

#cleanup
rm Anaconda3-5.1.0-Linux-x86_64.sh

#setup virtual env
conda create -y --name python3_env python=3
conda create -y --name python2_env python=2 

