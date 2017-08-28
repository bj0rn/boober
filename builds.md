Builds
==

Development flows has builds already. Some values are Hardcoded into the ADC like
 - builder name and version
 - baseImage name and version
 - triggerStrategy: Both
 - test: false

 The following premutations are needed:

  - development: triggerStategy: none, test: false always
	- snapshot release: same as devlopment
	- semantic release with no blackBox test: both triggers
	- semantic release with blackbox test: 2 bc, Jenkinsfile has triggers. 

Create one bc per branch of code. name-branchname	


The bc currently in the templates directory defaults to an ImageStream, We also need to build to Docker Image. 

In the future it is also needed to be able to accept binary builds for development type builds. Or should we ONLY accept binary builds for development?


