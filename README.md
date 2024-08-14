# DataTest Summary
Used for performing auto tests on various modules
## Compile and run test case on local
### Building all project
```powershell
.\mvnw.cmd clean install
```
### Execute different project auto case
#### 1. the project only have one test model
```powershell
.\mvnw.cmd test -pl ws
```
#### 2. the project have multi test modes, for vsothers, you need set ${test.model}
```powershell
.\mvnw.cmd test -pl vscalc -Pvscalc
```

## Run test case on docker
@TODO