This branch is hacked to work on Datomic Cloud. 

Eventually, it should be merged into the main branch.

# Configuration

Built into the code for now, see https://github.com/Candelbio/pret/blob/cloudy/src/org/candelbio/pret/db.clj#L40

# Credentials

Uses AWS credentials, which must have appropriate permissions. Uses the default profile, but you can change that by editing the configuration.

# Usage

<wd> is a local working directory
<config> is a standard Pret config file
<db> is a database name

```
./pret-dev request-db --import-config <config> --database <db>
./pret-dev prepare --import-config <config> --working-directory <wd>
./pret-dev transact --import-config <config> --working-directory <wd> --database <db>
```


# Administration

The `list-dbs` and `delete-db` command should work.

There are some other curation functions in db.clj, not yet hooked to cli. db/print-db-stats is particularly useful.

