# Jenkins image
FROM jenkins/jenkins:lts-jdk17

USER root

ARG DOCKER_GID=131

RUN apt-get update && \
    apt-get install -y neovim       \
    zsh build-essential curl        \
    apt-transport-https net-tools   \
    ca-certificates                 \
    gnupg lsb-release               \
    docker.io &&                    \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Install kubectl and helm
RUN curl -LO https://dl.k8s.io/release/v1.34.0/bin/linux/amd64/kubectl && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

RUN curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

RUN groupadd -g ${DOCKER_GID} docker_${DOCKER_GID}
RUN usermod -aG docker_${DOCKER_GID} jenkins

# Don't use the Setup Wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Uninstall all plugins
RUN rm -rf /var/jenkins_home/plugins/*

# Install Plugins
RUN jenkins-plugin-cli --plugins    \
    configuration-as-code:latest    \
    cloudbees-folder:latest         \
    credentials:latest              \
    github:latest                   \
    instance-identity:latest        \
    job-dsl:latest                  \
    workflow-job:latest             \
    workflow-cps:latest             \
    timestamper:latest              \
    script-security:latest          \
    structs:latest                  \
    role-strategy:latest            \
    dark-theme:latest               \
    ws-cleanup:latest

USER jenkins
