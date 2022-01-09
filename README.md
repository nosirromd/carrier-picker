# My OpenMarket Coding Challenge Response
Studied Java then worked on Openmarket's coding challenge.

# Getting the code working

This project is built using Gradle. Most common IDEs allow the importing of gradle projects in such a way that provides
access to all their features. Gradle can be used with the `./gradlew` script for Unix based Operating Systems and
`./gradlew.bat` for Windows users. You will need the following commands to get this project working:
* `./gradlew init` - Ensures that the project Gradle build is complete.
* `./gradlew build` - Build the project and then run the unit tests.
* `./gradlew test` - Run the unit tests without building the project.

The unit tests are meant to be failing. Once you see failing unit tests you are ready to begin working on this test.

# Problem Outline: Pick a carrier

We want you to implement a class called CarrierPicker which decides which phone company to send a message to.

You will do this by writing two methods: `readCarrierRule` and `pickCarrier`.

`readCarrierRule` accepts rules saying how to choose a carrier, based on the
properties of a message.

`pickCarrier` chooses a carrier for a particular message by using the rules
that were passed in to `readCarrierRule`.

We would like this service to be as fast, stable and deterministic as possible (within reason).

We have provided some unit tests that match the behaviour we have outlined.
Feel free to add your own tests but do not edit the JsonCarrierMatcher class. Requirements will be considered as met when all
unit tests are passing. Meeting new requirements should not break the unit tests which define prior requirements.

# Phase 1: Matching messages to rules

We will provide rules in this form:
```
{"carrier" : "O2", "destinationAddress" : 44}
```
and messages in this form:
```
{"destinationAddress" : 447590490225, "sourceAddress" : 447932048491, "message" : "example"}
```
We expect CarrierPicker to
* Parse these rules and messages.
* If every field in the carrier rule is contained within a field of the message return a JSON object with carrier set to
that carrier.
* Otherwise return a JSON object with carrier set to `UNKNOWN`.

# Phase 2: Selecting the best carrier

We will update our rules to contain a price like so:
```
{"carrier" : "O2", "destinationAddress" : 44, "price", 10.0}
```
* If no price is given then set it to a default of `100.0`.
* If multiple rules match a message, select the message with the most matching fields.
* If multiple rules match with same number of fields, pick the rule with the most matching characters.
* If there are two rules that are equal on the above two points, return the one with the lowest price.

# Phase 3: Allowing custom selection criteria

We will finally update our carrier descriptions to contain a latency as shown:
```
{"carrier" : "OMTELECOM", "destinationAddress" : 22, "price" : 100.0, "latency" : 10.0}
```
and our message JSONs to contain latency and price multipliers like this example below:
```
{"destinationAddress" : 227261438701, "sourceAddress" : 89347589374, "message" : "hello", "latencyMultiplier" : 1.0, "priceMultiplier" : 1.0}
```
* Now instead of returning the carrier with the lowest price, return the carrier which minimizes the result of 
`(priceMultiplier * price) + (latencyMultiplier * latency)`
