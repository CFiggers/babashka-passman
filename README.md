# babashka-passman

A simple Command Line password manager implemented using [Babashka](https://github.com/babashka/babashka), [sqlite3](https://www.sqlite.org/index.html) (via [this Babashka pod](https://github.com/babashka/pod-babashka-go-sqlite3)), and [stash](https://github.com/rorokimdim/stash) (also via Babashka pod). 

Started by following Daniel Amber's YouTube tutorial, [Create a password manager with Clojure using Babashka, sqlite, honeysql and stash](https://youtu.be/jm0RXmyjRJ8). 

I may update it myself with some additional features/safety (maybe tinkering with adding [cli4clj](https://github.com/ruedigergad/cli4clj)).

# Usage

The following assumes you are using Linux or some other Unix-like environment. Code blocks beginning with/containing a `$` are terminal commands.

1. Install [Babashka](https://github.com/babashka/babashka), [sqlite3](https://www.sqlite.org/index.html), and [stash](https://github.com/rorokimdim/stash).

2. Clone this repo using either `git` or the GitHub CLI:

``` Bash
$ git clone https://github.com/CFiggers/babashka-passman
```

or 

``` Bash
$ gh repo clone CFiggers/babashka-passman
```

3. Invoke `passman` using either Babashka (`bb`) or by running the Uberscript in `out/passman`.

```Bash
$ cd babashka-passman
$ bb -m passman.app
```

or 

```Bash
$ cd babashka-passman
$ ./out/passman
```

4. [Optional] Symlink the Uberscript from some directory on your `$PATH` to be able to use `passman` from anywhere.

```Bash
# This is probably your home directory, so /home/[user]
# ↓                              ↓
[...]/babashka-passman $ ln -s [...]/babashka-passman/out/passman [...]/bin/passman
```