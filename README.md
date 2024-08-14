# DataTest Summary
Used for performing auto tests on various modules
## Compile and run test case on local
### Building all project

```powershell
.\mvnw.cmd clean install
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
.\mvnw.cmd package -pl commons
```

## Run test case on docker
@TODO