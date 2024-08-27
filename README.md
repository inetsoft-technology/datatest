# DataTest Summary
Used for performing auto tests on various modules, all modules didn't install to local repository.

**Set 'root.dir' to your path on pom.xml**
## Compile and run test case on local
### Clean all project, then package commons to your local maven repository

```powershell
.\mvnw.cmd clean
.\mvnw.cmd package -pl commons
```
### Execute different project auto case
#### 1. The project only have one test model

```powershell
.\mvnw.cmd test -pl ws
```
#### 2. The project have multi test modes, for vsothers, you need set ${test.model}

```powershell
.\mvnw.cmd test -pl vsothers -Pvscalc
```
#### 3. Other commonly used commands

```powershell
.\mvnw.cmd clean -pl vsothers -Pvscalc
.\mvnw.cmd generate-test-resources -pl vsothers -Pvscalc -X 
.\mvnw.cmd package -DskipTest -pl ws
```

## Run test case on docker
@TODO