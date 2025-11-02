folder("Whanos base images") {
  description("Whanos base images.")
}

folder("Projects") {
  description("Available projects.")
}

job('/link-project') {
    parameters{
        stringParam('GIT_URL', null, 'Git URL')
        stringParam('DISPLAY_NAME', null, 'Display Name')
        credentialsParam('CREDENTIAL') {
            type('com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey')
            description('SSH key to clone repository')
        }
    }
    steps {
        dsl('''
job("/Projects/$DISPLAY_NAME") {
    scm {
        git {
            remote {
                name('origin')
                url("$GIT_URL")
                credentials("$CREDENTIAL")
            }
        }
    }
    triggers {
        scm('* * * * *')
    }
    wrappers {
        preBuildCleanup()
    }
    steps {
        shell('/opt/jenkins/deploy.sh')
    }
}'''.stripIndent())
    }
}

def languages = [
  [id: "c", name: "whanos-c"],
  [id: "java", name: "whanos-java"],
  [id: "javascript", name: "whanos-javascript"],
  [id: "python", name: "whanos-python"],
  [id: "befunge", name: "whanos-befunge"]
]

languages.each { lang ->
        job("Whanos base images/${lang.name}") {
                description("Build the base image for ${lang.id} language")
                steps {
                        shell("""
set -e pipefail
echo "Building base image ${lang.name}"

if [ ! -f /opt/jenkins/images/${lang.id}/Dockerfile.base ]; then
  echo "Dockerfile.base for ${lang.id} is missing"
  exit 1
fi

docker build -t ${lang.name}:latest - < /opt/jenkins/images/${lang.id}/Dockerfile.base
docker tag ${lang.name}:latest 192.168.0.56:5000/${lang.name}:latest
docker push 192.168.0.56:5000/${lang.name}:latest
""")
                }
                wrappers { timestamps() }
        }
}

pipelineJob('Whanos base images/Build all base images') {
    description('Trigger all Whanos base images builds.')

    definition {
        cps {
            sandbox(false)
            script('''\
    import jenkins.model.Jenkins

    def imgs = [ 'whanos-befunge', 'whanos-c', 'whanos-java', 'whanos-javascript', 'whanos-python' ]

    def jobs = [:]

    for (int i = 0; i < imgs.size(); ++i) {
        def img = imgs[i]
        def jobFullName = "Whanos base images/${img}"
        jobs[img] = {
            def instance = Jenkins.instance.getItemByFullName(jobFullName)

            if (instance == null) {
                throw new hudson.AbortException("Job ${jobFullName} not found")
            }

            def build = instance.scheduleBuild2(0)

            if (build == null) {
                throw new hudson.AbortException("Failed to schedule ${jobFullName}")
            }

            def finished_build = build.get()
            if (finished_build == null) {
                throw new hudson.AbortException("Failed to get build for ${jobFullName}")
            }

            def result = finished_build.getResult()
            if (result == null || result.toString() != 'SUCCESS') {
                throw new hudson.AbortException("${jobFullName} finished with status: ${result}")
            }
        }
    }

    parallel jobs
''')
        }
    }
}
