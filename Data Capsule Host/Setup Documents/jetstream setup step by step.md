# Jetstream Setup step by step
### 1. Login to Jetstream cloud using your credentials
https://use.jetstream-cloud.org

### 2. Launch 2 Jetstream VMs with following specifications
Image: Ubuntu 20.04 Devel and Docker v1.8

CPU: 2

Memory: 4GB

Disk Space: 20GB

Ansible host setup: Install Ansible, python3 and python passlib module on Ansible host machine. Ansible installation link:
http://docs.ansible.com/ansible/latest/intro_installation.html

### 4. Ansible target setup: Install python3 and ssh on target machines.
</br>

### 5. Clone ubuntu based data capsule vm on Ansible host machine
```
git clone https://github.com/TempleDSS/data-capsule-appliance.git
```

### 6. Add the IP address of the target machines in hosts file
</br>

### 7. Configure ssh keys between host and target machines to ensure you don’t have to enter password each time to execute ansible playbook
</br> 

### 8. On ansible host machine, execute ansible playbook by using following command
```
Command: ansible-playbook -k -i hosts site.yml
```