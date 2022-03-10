# Data Capsule Appliance Host

This projects provides the code for automating the Data Capsule Host and target deployment. 

# Data Capsule VM on Ubuntu ??

Previously Data capsule virtual machine existed in redhat OS flavor which needed lisenced subscription. To make it cost effective and widely usable, Ansible playbooks for Data Capsule VMs based on ubuntu OS have been written.

# Need for ubuntu based data capsule VMs:

1. Time needed to deploy the DCA guest setup on physical server took more than 2hours.
2. Jetstream cloud provides the VMs for academic research. Jetstream cloud has ubuntu and centos images. Hence in order to make data capsule VMs cloud compatible, Ansible playbooks have been written for ubuntu distribution.
3. Data capsule VMS are scalable by just updating the hosts in inventory file.
4. Complexity is reduced as the users are generally required to clone the repo and execute the playbook.
5. Reduction in infrastructure cost (physical server is $1500 -$7000 and additional electricity, air conditioning and space requirements).

## Getting Started

Clone this repo using 

```
git clone https://github.com/TempleDSS/data-capsule-appliance.git and cd into data Capsule Host directory.
```

### Prerequisites

#### Host Machine
You need to install Ansible in the machine that you are using for host provisioning. 
Refer to http://docs.ansible.com/ansible/latest/intro_installation.html for more details. 

Also Install the python and python passlib modules (http://passlib.readthedocs.io/en/stable/install.html) in the same machine. 

#### Target machine (Where DC would be deployed)

In the target machine, you need to have ssh and Ansible installed.

You need to have an account (<login_username>) to login in to the target machine with sudo access. 

### Deploying

1. Add the IP Addresses of the target machines that you need to provision in the hosts file. 

2. Then run the following command inside the cloned directory. Be sure to replace the <login_username> with your remote target machine username. 

```
ansible-playbook --user=<login_username> -k -i hosts site.yml --ask-become-pass
```

3. Then you will be prompted to enter two password as following. Enter the login remote user password for the first prompt and that user's
sudo password for the second prompt.

```
SSH password:<login_user_password>
SUDO password[defaults to SSH password]:<login_user_sudo_password>
```

This would start up the provisioning of the target machine! You should not see any errors in the terminal when this is being executed. 

4. After provisioning the machine, you need to add the iptable rules to your existing iptables(/etc/sysconfig/iptables) file. 
IPtable rules required for the Data Capsule will be available at /etc/sysconfig/iptables_dataCapsule in the provisioned host. 


### Result

The aforementioned steps would provision the target machine creating a DC hosting user with the default password (in group_vars/all file).

Then you can login into this user and deploy the Data Capsule or deploy the DC API. 

## Configurations

All the variables for the configurations can be found at the following file.
```
group_vars/all
```

