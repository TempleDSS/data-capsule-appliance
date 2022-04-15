pt-get install unzip
wget https://analytics.hathitrust.org/files/voyant.zip
unzip voyant.zip
mkdir /opt/applications
cp -r VoyantServer2_4-M2 /opt/applications/
chown -R $SSH_USERNAME /opt/applications/VoyantServer2_4-M2
mkdir -p /home/dcuser/Desktop
cp /opt/applications/VoyantServer2_4-M2/voyant.desktop /home/dcuser/Desktop/
chown -R $SSH_USERNAME /home/dcuser/Desktop/voyant.desktop
