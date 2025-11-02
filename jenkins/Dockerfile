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

ARG TARGETARCH
RUN set -eux; \
    if [ -n "${TARGETARCH:-}" ]; then \
        KUB_ARCH=${TARGETARCH}; \
    else \
        case "$(uname -m)" in \
            x86_64) KUB_ARCH=amd64 ;; \
            aarch64|arm64) KUB_ARCH=arm64 ;; \
            armv7l) KUB_ARCH=arm ;; \
            i386|i686) KUB_ARCH=386 ;; \
            ppc64le) KUB_ARCH=ppc64le ;; \
            s390x) KUB_ARCH=s390x ;; \
            *) echo "Unsupported architecture: $(uname -m)" >&2; exit 1 ;; \
        esac; \
    fi; \
    echo "Installing kubectl for arch=${KUB_ARCH}"; \
    KUB_VERSION="v1.34.0"; \
    KUB_URL="https://dl.k8s.io/release/${KUB_VERSION}/bin/linux/${KUB_ARCH}/kubectl"; \
    curl -fsSLo /tmp/kubectl "$KUB_URL"; \
    install -o root -g root -m 0755 /tmp/kubectl /usr/local/bin/kubectl; \
    rm -f /tmp/kubectl

RUN curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

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
