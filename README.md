# Data Capsule Appliance Host

This projects provides the code for automating the Data Capsule Host deployment. 

## Getting Started

Clone this repo using 

```
git clone https://github.com/Data-to-Insight-Center/Data-Capsule-Appliance-Host.git
```

### Prerequisites

You need to install Ansible in the machine that you are using for host provisioning. Refer to http://docs.ansible.com/ansible/latest/intro_installation.html for more details. 

In the host machine, you need to have ssh and python installed (for Ansible).

You need to have an account to login in to the target machine with sudo access. 

### Deploying

1. Add the IP Addresses of the hosts that you need to provision in to the hosts file. 

2. Change the username that will be used to login into the host provisioning machine at site.yml file. The default value is root. 

```
user: <login_username>
```

3. Then run 

```
ansible-playbook -k -i hosts site.yml
```

from the cloned directory. You'll have to provide the root password when prompted. 
