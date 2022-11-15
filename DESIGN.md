# Design manifesto

`pret` is a programmable ETL tool for Datomic that is *schema-agnostic*. Given a set of input files in tabular format and a configuration file, `pret` prepares transaction data that can be imported into a Datomic database. Besides working with tabular data, `pret` can also transact *literal* data, i.e. data that is already specified in map form in the configuration file.

## Identity

In order to allow for transaction data to be independent of ordering, and for entities to be composed from different transactions, `pret` creates synthetic unique ids for (almost) all the entities that are created as part of an import job.

Synthetic ids are created from user-specified ids that are unique only within a given scope (e.g. a dataset, or a measurement-set). `pret` creates uids by stringing togeher the ids of an entity, and the ids of all the parent scopes of the entity

## Metamodel

The functionality of `pret` relies on a metamodel that is stored with the database schema and that specifies:
- the *kind* of entities that exist in the schema, and which attributes determine the *kind* of an entity
- the *kind* of entities that can be referenced from a `:db.type/ref` attribute
- the parent scope of each entity *kind* (this is used in the generation of synthetic ids)

## Validation

The validation code in `pret` is the only component that is tied to a specific schema (i.e. it is not *schema-agnostic*). Validation on scalar attributes (e.g. numeric ranges) are performed during the `prepare` step when entity data is created. Validation that are related to how multiple attributes compose into an entity, as well as relationship between entities with specific attributes, are performed once all the data has been transacted in the database. This is necessary because a single entity can be assembled from many independent transactions as part of the import job.

## Dataset access control

In order to limit access to specific datasets `candelabra` creates databases on demand that only contain the subset of data that a user has access to. Subsequent queries are then issued against this derived database. The implementation of this system relies on having (in a separate database), an ordered history of all the transactions that constitute the production database. The derived databases are then created by replaying the parts of that history that include datasets that the user has access to. Reference data is always included in derived databases, even when the dataset that the reference data was created from is not.

Alternative implementations of access control were discussed but ruled out at least for now. These include:
- Clause injection: injecting into every query clauses that constrain the result set to only include data that is derived from datasets the user has access to. This was ruled out due to the potential complication of determining exactly which clauses should be injected for any possible arbitrary query
- Dataset provenance attribute: adding to every entity an attribute specifying which dataset the entity belongs to, and using this to facilitate clause injection. This was ruled out becuase it would increase the size of the data, particularly for measurement entities

## Datomic cloud vs on-prem



