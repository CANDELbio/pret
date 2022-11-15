# Developer Guide

This doc details steps necessary to successfully run, build, and deploy pret from this repo.

## Build version info edn file

Pret requires a version info edn file to run. Build it with:

```
make version-info
```

## Dependencies

This project depends on `datomic-pro`. to download this dependency, this project is configured to use the private
Maven repo at `my.datomic.com`. From your [my.datomic account](https://my.datomic.com/account) you can get your
Maven repo credentials. These will need to go into your `~/.m2/settings.xml` file, added as an entry to the
`servers`:

Notes: the XML header is required, and PASSWORD is the download key available on the my.datomic.com page, not the login password.
```
<servers>
  ...
  <server>
    <id>my.datomic.com</id>
    <username>USERNAME</username>
    <password>PASSWORD</password>
  </server>
</servers>
```

## Clojure Tools/Deps Notes

Note: for all following steps, the quickest sanity check on whether or not the Clojure Tools environment is configured correctly is running `deps` or starting a repl, via:

```clojure -Mdeps```

or:

```clojure```

### Installation

Not all packages will give you the latest version of Clojure or configure JVM, etc. for you. Your best bet is to follow the install instructions for your platform as described in the official
[Clojure docs](https://clojure.org/guides/getting_started).

### Private Repo access

Most of the pain points in using pret from the repo are around configuring access to private git repos via Clojure deps/tools gitlibs.
This has been improved on the latest version of gitlibs and is propagating to Clojure core package deployments, so many of these pain
points should improve. In the mean time, they're documented for access.

#### SSH-Agent

On many systems, you need an `ssh-agent` process up for private git repo access. This can be configured via *nix command line via:

```eval $(ssh-agent -s) && ssh-add -k ~/.ssh/id_rsa```

#### Limited JGit support

Clojure uses (but is phasing out) JGit which is a java implemented version of Git which does not support all private/public key encryption schemes.
The historical catch-all topic (w/workarounds) is [here](https://clojure.atlassian.net/browse/TDEPS-91?jql=text%20~%20%22JGit%22).

#### Cached gitlibs

If all else fails, you can copy a working cached gitlibs from another user. By default, Clojure caches all local git repos used in project builds to:

```
~/.gitlibs
```

## Re-caching the Schema

`pret` uses a cached schema for local operations (such as `prepare`). You should update the cached schema anytime you make changes to
the `schema.edn`, `metamodel.edn`, or `enums.edn` files. This can be done with the follow command line invocation:


```
clojure -M -m org.candelbio.pret.db.schema.cache
```

## Running Pret CLI

To call the pret CLI from the local environment in an arg for arg parity with the deployed version of the pret package, there are two convenience wrappers:

```
./pret
./pret-dev
```

These can be invoked as the pret CLI is, e.g.:

```
./pret-dev prepare --import-config ~/azure-datasets/template/config.edn --working-directory ~/wds/template/
```

etc.

If you need to troubleshoot JVM args, etc. you can use the command invocation in these scripts as a starting point.

## Building and deploying

There are make targets for all builds and deploys, e.g.:

```
make uberjar
```

```
make package-prod
```