# Add the required public key to the root user - this can be replaced at ../scripts/authorized_keys file
mkdir /root/.ssh
cp /home/$SSH_USERNAME/uploads/root_authorized_keys /root/.ssh/authorized_keys
chown -R root /root/.ssh
chmod 700 /root/.ssh
chmod 600 /root/.ssh/authorized_keys

#Create .ssh directory and authorized_keys file for DCUSER
mkdir /home/$SSH_USERNAME/.ssh
touch /home/$SSH_USERNAME/.ssh/authorized_keys
chown -R $SSH_USERNAME /home/$SSH_USERNAME/.ssh
chmod 700 /home/$SSH_USERNAME/.ssh
chmod 600 /home/$SSH_USERNAME/.ssh/authorized_keys
 
# enable logging of root user activity
cat  /home/$SSH_USERNAME/uploads/enableSyslogging >> /root/.bashrc

# adding user to the dialout group 
groupadd -g 20 dialout
usermod -a -G dialout $SSH_USERNAME

# Removing the password for the DCUSER
cp /home/$SSH_USERNAME/uploads/dcuserSudoAccessRestrictions /etc/sudoers.d/dcuserSudoAccessRestrictions
chmod 0440 /etc/sudoers.d/dcuserSudoAccessRestrictions
passwd -d $SSH_USERNAME

# Removing the UI password prompt
cp /home/$SSH_USERNAME/uploads/99-nouipassword.pkla /etc/polkit-1/localauthority/50-local.d/99-nouipassword.pkla

# Removing password authentication for SSH for the DC
sed -i -e 's/#PasswordAuthentication\syes/PasswordAuthentication no/g' /etc/ssh/sshd_config

# Remove Ubuntu upgrade prompt
sed -i 's/Prompt=.*/Prompt=never/g' /etc/update-manager/release-upgrades
