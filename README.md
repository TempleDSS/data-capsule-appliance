# Data Capsule Appliance Host

This projects provides the code for automating the Data Capsule Host deployment. 

## Getting Started

Clone this repo using 

```
git clone https://github.com/Data-to-Insight-Center/Data-Capsule-Appliance-Host.git
```

### Prerequisites

You need to install Ansible in the machine that you are using for host provisioning. Refer to http://docs.ansible.com/ansible/latest/intro_installation.html for more details. 

### Deploying

First add the IP Addresses of the hosts that you need to provision in to the hosts file. 

Then, after installing Ansible, you can run 

```
ansible-playbook -k -i hosts site.yml
```

from the cloned directory. You'll have to provide the root password when prompted. 
