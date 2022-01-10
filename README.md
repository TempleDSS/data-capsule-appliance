# Data Capsule VM on Ubuntu

Data capsule virtual machine existed in redhat OS flavor which needed subscription. To make it cost effective and widely usable, Ansible playbooks for Ubuntu OS Data Capsule VMs have been written.

# Need for ubuntu based data capsule VMs:

1. Time needed to deploy the DCA guest setup on physical server took more than 2hours.
2. Jetstream cloud provides the VMs for academic research. Jetstream cloud has ubuntu and centos images. Hence in order to make data capsule VMs cloud compatible, Ansible playbooks have been written for ubuntu distribution.
3. Data capsule VMS are scalable by just updating the hosts in inventory file.
4. Complexity is reduced as the users are generally required to clone the repo and execute the playbook.
5. Reduction in infrastructure cost (physical server is $1500 -$7000 and additional electricity, air conditioning and space requirements).

# Ansible host server specification

Operating system: Ubuntu 20.04
Jetstream server specifications
Size: m1.small
CPU: 2
Mem: 4GB
Disk: 20GB

# Github urls

**Ubuntu data capsule vm**

https://github.com/AyishaT-Coder/DataCapsuleVMonUbuntu.git

# Clone the repo

git clone https://github.com/AyishaT-Coder/DataCapsuleVMonUbuntu.git

# Execute ansible playbook

ansible-playbook -k -i hosts site.yml 

# Original redhat repo is referenced from following github url

https://github.com/Data-to-Insight-Center/Data-Capsule-Appliance-Host
