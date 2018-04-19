#!/bin/bash

SSH_USERNAME=$1

# Removing the password for the DCUSER
cp /home/$SSH_USERNAME/uploads/dcuserSudoAccessRestrictions /etc/sudoers.d/dcuserSudoAccessRestrictions
chmod 0440 /etc/sudoers.d/dcuserSudoAccessRestrictions
passwd -d $SSH_USERNAME

shutdown -r now

