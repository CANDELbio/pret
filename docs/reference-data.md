# Generating reference data

Reference data must be generated from third party sources before CANDEL
databases are created. The guaranteed order starts with genes, as other
reference data processing uses the `all_hugos.edn` file created during
HGNC data processing.

## Generating genes data

Generate the genes data first as Proteins and Sequence Ontology features both depend on the `all_hugos.edn` file this produces.

From the [HGNC complete set archive](https://www.genenames.org/download/archive/), download the linked 'Current tab separated hgnc_complete_set file'.

From the HGNC ftp, download the Ensembl <-> Hugo mappings [file](ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc2ensembl_coords.csv.gz).

_NOTE_: A suitable replacement might be workable from the
[HGNC Custom Downloads page](https://www.genenames.org/download/custom/),
but not yet implemented.

Call the `org.candelbio.pret.bootstrap.reference-data.cli` with args:

```
./generate-reference-data --genes path/to/hgnc_complete_set.txt path/to/hgnc2ensembl_coords.csv 
```

to generate the reference data in transactable edn form.

## Generate proteins data

Make sure and generate `all_hugos.edn` in the genes step above first!

To generate the Uniprot derived Protein transaction data for CANDEL, download the Uniprot XML file
[uniprot_sprot.xml](https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/),
as well as the human ID mapping file
[HUMAN_9606_idmapping.dat](https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/HUMAN_9606_idmapping.dat.gz).

Generate protein data with:

```
./generate-reference-data --proteins path/to/uniprot_sprot.xml path/to/HUMAN_9606_idmapping.dat
```

## Generate cell types data

Navigate to the OBO Foundry cell type ontology
[home page](https://obofoundry.org/ontology/cl.html) and download `cl.obo`
from the Products table.

Generate the cell type transaction data with:

```
./generate-reference-data --cell-types path/to/cl.obo
```


## Generate sequence ontology features

Navigate to the Sequence Ontology page on the [OBO Foundry Site]( https://obofoundry.org/ontology/so.html).

Download `so.obo` from the Products table.

Generate sequence ontology data with:

```
./generate-reference-data --so-sequence-features path/to/so.obo
```

## Generate anatomic sites data

Download the GDC sample schema YAML file from github with
[this link](https://github.com/NCI-GDC/gdcdictionary/blob/develop/gdcdictionary/schemas/sample.yaml).

Generate anatomic site data with:

```
./generate-reference-data --anatomic-sites path/to/sample.yaml
```




