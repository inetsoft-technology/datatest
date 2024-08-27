pipeline {
    agent any
    tools {
        jdk('java21')
    }
    options {
        ws('/inetsoft/styleBI')
    }
    parameters {
         choice(name: 'STAGE_TO_RUN', choices: ['all', 'vscalc', 'vscrosstab', 'vsfreehand', 'vspara', 'otherassembly'], description: 'Select the stage to run')
    }

    stages {
        /*stage('compile source code') {
            when {
                expression { return params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('stylebi') {
                    echo 'update and compile source code'
                    sh 'git fetch origin'
                    sh 'git reset --hard origin/$(git rev-parse --abbrev-ref HEAD)'
                    sh './mvnw package -DskipTests "-Pcommunity,enterprise"'
                }
            }
        }*/
        stage('init test env') {
            when {
                expression { return params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('test/datatest') {
                    sh 'git fetch origin'
                    sh 'git reset --hard origin/$(git rev-parse --abbrev-ref HEAD)'
                    sh 'chmod -R 777 /inetsoft/styleBI/test/datatest'
                    sh './mvnw package -pl commons'
                    sh './mvnw clean -pl vsothers'
                }
            }
        }

        stage('check vscalc') {
            when {
                expression { return params.STAGE_TO_RUN == 'vscalc' || params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('test/datatest') {
                    echo 'execute vscalc test'
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                         sh './mvnw test -pl vsothers -Pvscalc'
                    }
                }

            }
        }

        stage('check vscrosstab') {
            when {
                expression { return params.STAGE_TO_RUN == 'vscrosstab' || params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('test/datatest') {
                    echo 'execute vscrosstab test'
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                         sh './mvnw test -pl vsothers -Pvscrosstab'
                    }
                }
            }
        }

        stage('check vsfreehand') {
            when {
                expression { return params.STAGE_TO_RUN == 'vsfreehand' || params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('test/datatest') {
                    echo 'execute vsfreehand test'
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        sh './mvnw test -pl vsothers -Pvsfreehand'
                    }
                }
            }
        }
        stage('check otherassembly') {
            when {
                expression { return params.STAGE_TO_RUN == 'otherassembly' || params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('test/datatest') {
                    echo 'execute vsfreehand test'
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        sh './mvnw test -pl vsothers -Potherassembly'
                    }

                }
            }
        }

        stage('check vspara') {
            when {
                expression { return params.STAGE_TO_RUN == 'vspara' || params.STAGE_TO_RUN == 'all' }
            }
            steps {
                dir('test/datatest') {
                    echo 'execute vsfreehand test'
                    sh './mvnw test -pl vsothers -Pvspara'
                }
            }
        }
    }

     post {
        always {
            junit checksName: 'VSOthers Test', keepLongStdio: true, testResults: 'test/datatest/vsothers/target/surefire-reports/*.xml'
        }
    }
}