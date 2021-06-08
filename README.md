# Miniventure

#### Project Future
This project has unfortunately been discontinued. Not because I don't want to go forward with the idea, but because I didn't have the resources to complete my vision
for what I wanted this to be. I stopped working on it for a fairly long time.

Instead, I will be working on a game of a similar vein in unity, but this time with more finantial/art/etc support. However, due to the commercial nature of it, it will be closed source. I will most likely link to its webpage here when it gets one and when the game is moderately playable.

Thanks for the support, hopefully with this next iteration I can make it even better, and perhaps even finish it this time. :)

## In a nutshell
Put simply, *Miniventure* is a 2D, top down, survival sandbox adventure game. It's built around a few core mechanics:
- You scavenge your way through a chain of radically different islands, gathering resources, upgrading gear, and defeating bosses.
- The terrain is 100% malleable, and is yours to destroy, reform, or build up into a cool base.
- Designed for multiplayer, you can work with friends to take down a boss, show off your cool base, or do anything else you like.

### Build Instructions

If you want to build and run the code for yourself, it's super easy since this project uses gradle.
- Open up a terminal window in the main folder, where you see the build scripts, `gradlew` and `gradlew.bat`
- To run the code, simply type `./gradlew run` on Linux/Mac or `gradlew.bat run` on Windows.
- To build the jar file, type `./gradlew dist` on Linux/Mac or `gradlew.bat dist` on Windows. The file will end up in `build/libs/`.

## History
This started as a remake of [Minicraft Plus](https://github.com/chrisj42/minicraft-plus-revived), which is itself a mod of [Minicraft](https://github.com/shylor/miniventure), a Ludum Dare game jam game. As you may have noticed, that repository is where I got the name for this game.

I originally just wanted to remake Minicraft Plus with a fresh start; it was a messy code base to begin with, and that messiness was only compounded by the inexperience of the devs who worked on it (including myself) and used it to get better at coding, making a lot of mistakes in the process. I figured I'd make more progress starting over than trying to fix what I had to work with.

However, after a little while, Miniventure took a direction all its own, and I now fully intend to make and release it as an entirely independent game, with only basic similarities.

## Features
As of now, miniventure is still in alpha stage and probably lacks in actual gameplay, but the feature list continues to grow and change. Here's a list of the major features it has so far:

  - Multiplayer functionality, allowing many people to play in the same world. The server can be run in dedicated mode without a GUI.
  - Random island generation based on a custom variant of perlin noise, among other noise algorithms
  - World save/load with compatibility tracking
  - Sprite animations and smooth tile transitions
  - basic world interactions, breaking/placing tiles, hurting mobs
  - mob AI behaviors
  - an update-checker that tells you when there's a new update available, and gives you the link.
  - inventory/hotbar management
  - a crafting system (with mostly tools for now) that involves making held items and objects that you place straight into the world
  - chat system with commands 

## What's next?

Current development goals are focused around starting to flesh out the game in terms of features; there's currently only one island available so far, and no bosses yet, so there's a lot of content still to be added.

For upcoming features, you can expect new mobs, new items, new islands, more crafting progression, and of course bosses. 
Plans do exist for a storyline and perhaps a few more goals; it's all in the works. :)

If you're looking for the latest activity, be sure to check for other git branches; I do most of my development in one of those until a new release, when I finally push it to master.

### Some other things

You may notice that the art and visual style of the game is lacking... I'm no good at art, or UI design, so I've gotten the assets from various places like artist friends and publicly published game art. It probably doesn't look terribly amazing.
Given that, if there's anyone out there who'd like to help make the style more consistent, I'd really appreciate the help!

If you want to chat, you can find me on the [playminicraft discord server](https://discord.me/minicraft), Chris J#4288. I may make a server dedicated to miniventure at some point, but for now that's the base of operations.
