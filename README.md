# Rumelhart1985

In 1985, Rumelhart and others introduced back propagation and gradient descent
as a method of training neural networks that have more than just
an input layer and an output layer.

This Java project implements the algorithm described in Rumelhart's paper.

# Installation and Use

Be sure Java 11 and Maven are installed.

Copy the root directory and everything below it onto your machine.

Run "mvn clean test", and the library will be compiled and tests run.

The main class of the library is Network.
A network is given a structure with "withStructure",
and fed training patterns (that must match the input and output structure
of the network) via "Network.learn()".

The NetworkTest.java file demonstrates proper (and improper) use.

# Technologies Used
Core Java.

# Acknowledgements
Learning internal representations by error propagation
DE Rumelhart, GE Hinton, RJ Williams - 1985 - apps.dtic.mil
