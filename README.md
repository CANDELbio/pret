# pret Overview

`pret` is a programmable ETL pipeline for the CANDEL database. The input to `pret` is a collection of input data in flat tsv files, and a specification (in [EDN](https://github.com/edn-format/edn) format) of how this data maps to the CANDEL schema. Using this input, `pret` will:
1. parse the data according to the specification
2. validate the data against a set of rules
3. transact the data into the CANDEL database

Therefore `pret` is programmed through data (the EDN specification) and allows you to import new dataset into CANDEL without having to interact with the database directly. Your job as a user is to provide the EDN specification that describes how your dataset maps to the CANDEL schema, and `pret` will do the rest.

## Environment setup and installation

`pret` will use environment variables / Java system properties for environment configuration:


| Envar | systemProperty | Description | Default |
| --- | --- | --- | --- |
| `CANDEL_BASE_URI` | n/a | Base datomic URI for CANDEL databases | nil |
| `CANDEL_AWS_REGION` | `candel.awsRegion` | AWS Region | "us-east-1" |
| `CANDEL_DDB_TABLE` | `candel.ddbTable` | DDB Table containing CANDEL Datomic DB | "candel-prod" |
| `CANDEL_REFERENCE_DATA_BUCKET` | `candel.referenceDataBucket` | S3 bucket for reference data | "pret-processed-reference-data-prod" |
| `CANDEL_MATRIX_BUCKET` | `candel.matrixBucket` | S3 bucket for matrix files | "candel-matrix" |
| `CANDEL_MATRIX_DIR` | `candel.matrixDir` | Local dir for matrix files when using "file" | "matrix-store" |
| `CANDEL_MATRIX_BACKEND` | `candel.matrixBackend` | Storage medium for matrix files (file or s3) | "s3" |

see the `org.candelbio.pret.db.config` namespace

AWS permissions should be configured at the environment level for use of `pret`

### Prerequisites

`pret` requires the following installed:
1. Java version 1.8 or 1.9. See [OpenJDK](https://openjdk.java.net/install/) for installation instructions.

## Building

See the `DEVELOPMENT.md` file for details on building the pret jar.

## Running

Invoke `pret` in the directory you downloaded the `pret.jar` file with:

`./pret` in linux or macOS

`pretw.bat` in Windows

This will echo the command line usage options

### Example usage

The following example would provision a test database as specified in `~/repos/pret/example-data/datomic.conf.edn`, prepare data as specified in `~/repos/pret-datasets/tcga/config.edn` to the working directory `~/data/tcga-import/tmp-working`, and then transact the data into the database provisioned in the `provision` task.

```./pret provision --datomic-config ~/repos/pret/example-data/datomic.conf.edn```

```./pret prepare --import-config ~/repos/pret-datasets/tcga/config.edn --working-directory ~/data/tcga-import/tmp-working```

```./pret transact --datomic-config ~/repos/pret/example-data/datomic.conf.edn --working-directory ~/data/tcga-import/tmp-working```

# Developer Notes

For developer notes see [DEVELOPMENT.md](DEVELOPMENT.md).
