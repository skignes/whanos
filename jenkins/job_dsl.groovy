folder("Whanos base images") {
    description("Whanos base images.")
}

folder("Projects") {
    description("Available projects.")
}

job("link-project") {
    parameters {
        stringParam("GIT_URL", null, 'Git repository url')
        stringParam("DISPLAY_NAME", null, "Display name for the job")
        // TODO: Add private ssh key
    }
    steps {
        dsl {
            text('''
job("Projects/$DISPLAY_NAME") {
    scm {
        git {
            remote {
                name("origin")
                url("$GIT_URL")
            }
        }
    }
    triggers {
        scm("* * * * *")
    }
    wrappers {
        preBuildCleanup()
    }
    steps {
        shell("echo \\"deploy\\"")
    }
}
            ''')
        }
    }
}
