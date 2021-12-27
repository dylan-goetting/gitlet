# Gitlet Design Document

**Name**: Dylan Goetting

## Classes and Data Structures
Commit
* Time field
* Message field
* Parent string field
* Parent2 string field (usually null)
* make hashing instance method
* hashmap(string-string) representing the files

Repo
* Has a staging/removing area field (hashmap of strings -> strings)
*Has a Commit area field (hashmap of strings -> commits)
*Master field
*Head field

## Algorithms
Init:
* New repo object
* Make a .gitlet metadata folder to store things in
* Initializes head and master pointers
* Makes initial commit, adds it to the commit map
* Checks if there is already a repo there and errors if there is

Checkout
* Controls where the head pointer is

Commit
* First clones the head commit, changes metadata
* Adds new commit object to the commit area
* Advances the head pointer / master pointer
* Clear staging area

Add
* Move file to staging area
* Read the file’s blob and add it to blobs

Log
* Start at the head pointer, display its info in a certain format
* Recurse backwards by looking at the parent of that and go all the way until the initial commit
  
RM
* Takes whatever file is specified and adds it tot he remove staging area hashmap. Then when commit is called it all the files that are in this hashmap are no longer being tracked in commits going forward

## Persistence
* Save/write different parts of the repo’s data into parts of the gitlet directory after doing anything.
* Before doing things, read the objects from the directory and use them for any modifications
* Have a fridge and freezer, freezer is data that will never be changed, like commits, and fridge will be things to work on in progress, like staging area

* Commit
* Time field
* Message field
* Parent string field
* Parent2 string field (usually null)
* make hashing instance method
* hashmap(string-string) representing the files

* Repo
* Has a staging/removing area field (hashmap of strings -> strings)
  *Has a Commit area field (hashmap of strings -> commits)
  *Master field
  *Head field

* Init:
* New repo object
* Make a .gitlet metadata folder to store things in
* Initializes head and master pointers
* Makes initial commit, adds it to the commit map
* Checks if there is already a repo there and errors if there is
Checkout
* Controls where the head pointer is
Commit
* First clones the head commit, changes metadata
* Adds new commit object to the commit area
* Advances the head pointer / master pointer
* Clear staging area
Add
* Move file to staging area
* Read the file’s blob and add it to blobs
Log
* Start at the head pointer, display its info in a certain format
* Recurse backwards by looking at the parent of that and go all the way until the initial commit
  RM
  Takes whatever file is specified and adds it tot he remove staging area hashmap. Then when commit is called it all the files that are in this hashmap are no longer being tracked in commits going forward
* Save/write different parts of the repo’s data into parts of the gitlet directory after doing anything.
* Before doing things, read the objects from the directory and use them for any modifications
* Have a fridge and freezer, freezer is data that will never be changed, like commits, and fridge will be things to work on in progress, like staging area



