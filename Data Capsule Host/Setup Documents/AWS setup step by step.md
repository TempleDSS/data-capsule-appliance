# AWS Setup step by step
### 1. Sign up for AWS account with email and password
https://portal.aws.amazon.com/billing/signup#/start

### 2. Log into AWS account using root credentials (email and password)
https://console.aws.amazon.com/

### 3. In the services tab, Select EC2 instances
Launch EC2 instances with the following specifications,

Instance type: t2.medium

Image ID: ami-01f87c43e618bf8f0

vCPU: 2

Memory: 4gb

EBS Volume: 20GB

Number of instances: 2

### 4. Configure the security group of host configuration instance for ssh connection

Source: IPs of host machines

Destination: IP of host configuration machine

Port: 22

### 5. Configure the security group of ansible host machines for ssh connection

Source: IP of host configuration machine

Destination: IP of host machine

Port: 22

### 6. Ansible host configuration machine setup: 
Install Ansible, python3 and python passlib module on Ansible host configuration machine. 
Ansible installation link: http://docs.ansible.com/ansible/latest/intro_installation.html

### 7. Ansible host setup: Install python3 and ssh on host machine.
</br>

### 8. Clone ubuntu based data capsule vm on Ansible based host configuration machine
Command: git clone https://github.com/TempleDSS/data-capsule-appliance.git

</br>

### 9. Add the IP address of the host machines in hosts file
</br>

### 10. Configure ssh keys between host configuration instance and host instances to ensure you don’t have to enter password each time to execute ansible playbook

</br> 

### 11. On host configuration machine, execute ansible playbook by using following command

```
ansible-playbook -k -i hosts site.yml\
```
