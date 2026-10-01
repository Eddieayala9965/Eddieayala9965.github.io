---
title: "Enhancement One: Software Design and Engineering"
---

# Enhancement One: Software Design and Engineering

[View the enhanced DatabaseHelper.java](weighttracker/app/src/main/java/com/example/weighttracker_ayala/DatabaseHelper.java)

WeightTracker is an Android app I built in Java for CS 360, Mobile Architecture and Programming, as Project Three. It tracks weight entries. It has account login, local storage with SQLite, and an SMS notification that sends when a user reaches their goal weight.

I chose it because it's a full working app with several layers. It has a user interface with a RecyclerView and Material components, a database layer I wrote by hand, and it uses Android's SMS permission and messaging APIs. That makes it one artifact that can show more than one skill.

This enhancement fixed the two main software design problems I found in my code review. First, almost every method in the database helper repeated the same code to open and close the database connection. I moved that into two reusable helper methods, executeWritable and executeReadable, which also make sure the connection always closes. Second, passwords were stored in plaintext. I replaced that with salted PBKDF2 password hashing, using only the built-in Java crypto libraries and no outside dependencies. Each user gets a random salt, and login compares hashes in constant time instead of doing a plain string match.

This covers two course outcomes. The connection refactor covers the software engineering and design outcome, and the password hashing covers the security outcome.

The hardest part was a compatibility problem I didn't plan for. The SHA-256 version of PBKDF2 isn't available on the app's minimum supported Android version, and it fails on those devices without a clear error. I had to research it, choose the SHA-1 version instead, and write down why in the code. SHA-1's known weaknesses are about collisions, which don't apply to how it's used inside PBKDF2, so it's still safe here.
