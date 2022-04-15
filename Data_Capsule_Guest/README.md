# Data Capsule Appliance Guest VM Creation

This project contains the artifacts to generate a Data Capsule(DC) using Packer (https://www.packer.io/).

## Getting Started

Let's create a DC image based on Ubuntu 16.04 LTS using Packer.

### Prerequisites

- Download and unzip [Packer](https://www.packer.io/downloads.html) and add it to your PATH.
- Install the qemu hypervisor (specifically, qemu-system-x86_64).

MacOS users will find it easiest to install qemu and Packer using using Homebrew:

```sh
brew install qemu --with-sdl
brew install packer
```

### Building the VM

1. Open a terminal.
2. Navigate to the cloned directory repo
3. Replace the contents of `uploads/root_authorized_keys` with your SSH public key(s), formatted as you would a typical authorized_keys file (one key per line). ([Guide: Generating an SSH key pair.](https://www.digitalocean.com/community/tutorials/how-to-set-up-ssh-keys--2))
4. Verify that packer is installed by running `packer --version`. If not, revisit the prerequisites section.
5. Build the template: `packer build ubuntu_vanilla.json`.

Packer will download the Ubuntu ISO, launch qemu, and start building your image. In desktop environments, qemu will launch and present you with the VM console, which you can monitor for progress. Once completed, the machine image is output to `./output-ubuntu1604/ubuntu1604`.

Builds can take over an hour, depending on your hardware. If you're concerned that it's stalled, set `PACKER_LOG=1` in your environment and rerun the build. ([Docs: Debugging Packer.](https://www.packer.io/docs/other/debugging.html))

### Troubleshooting

Errors tend to break down in one of three categories:

- Missing Dependencies: The build fails early, usually with a message describing why. This is painless — just install the missing package(s) and move on.
- Ubuntu Installation: It's unlikely for Ubuntu's preseed to fail outright, though it can take _very_ long (47min on a MacBook Pro/i7/16G RAM). It helps to use a desktop environment, as this allows you to see what's happening during installation.
- Ubuntu version issues: if Ubuntu version has been updated, please update the links in the ubuntu_vanilla.json file along with the iso name and the sha1 (checksum) value. 

## Configuration

The main configuration file is the ubuntu.json file. It contains the versions, ssh hostnames, passwords ..etc for the DC.

For further changes, you will have to refer to the individual scripts (in the scripts directory).

#### Validating Configurations

After changes to the ubuntu.json file, you can validate the configuration by running,

```sh
packer validate ubuntu_vanilla.json
```


#### Default Packages installed

- Anaconda (/opt/anaconda)
- Oracle JDK 8
- Pytables
- R
- SBT
- Scala
- Spark (/opt/spark)
- VoyantServer (/opt/VoyantServer)

#### System level packages:

- curl
- git
- htop
- iotop
- jq
- maven
- parallel
- pcregrep
- python-pip
- python3-pip
- zsh

#### Libraries installed for both python 2.7 and 3:

- csvkit
- dask
- GenSim
- htrc-feature-reader
- matplotlib
- nltk
- numpy
- pandas
- regex
- scipy
- theano
- toolz
- ujson

## Contributing

Please send Pull Requests for this repo for any changes required.
