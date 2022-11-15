# Validations performed by pret
This document summarizes all the validations that are performed by `pret`

## Validation of scalar attributes

|Attribute | Validation |
|----------|------------|
|`:measurement/cell-count`| >= 0 |
|`:measurement/tpm` |>= 0|
|`:measurement/fpkm` | >= 0|
|`:measurement/rpkm`| >= 0|
|`:measurement/rsem-raw-count`| >= 0|
|`:measurement/rsem-normalized-count`| >= 0|
|`:measurement/rsem-scaled-estimate`| >= 0|
|`:measurement/nanostring-count`| >= 0|
|`:measurement/vaf`| [0, 1]|
|`:measurement/percent-of-total-cells`| [0, 1]|
|`:measurement/percent-of-parent`| [0, 1]|
|`:measurement/percent-of-nuclei`| [0, 1]|
|`:measurement/percent-of-lymphocytes`| [0, 1]|
|`:measurement/percent-of-leukocytes`| [0, 1]|
|`:measurement/percent-of-live`| [0, 1]|
|`:measurement/live-percent`| [0, 1]|
|`:measurement/percent-of-singlets`| [0, 1]|
|`:measurement/a-allele-cn`| [-2, 50]|
|`:measurement/b-allele-cn`| [-2, 50]|
|`:measurement/baf-n`| [0, 1]|
|`:measurement/baf`| [0, 1]|
|`:measurement/t-alt-count` | >= 0 |
|`:measurement/t-ref-count` | >= 0 |
|`:measurement/t-depth` | >= 0 |
|`:measurement/n-alt-count` | >= 0 |
|`:measurement/n-ref-count` | >= 0 |
|`:measurement/n-depth` | >= 0 |
|`:measurement/cell-count` | >= 0 |
|`:measurement/singlets-count` | >= 0 |
|`:measurement/live-count` | >= 0 |
|`:measurement/lymphocyte-count` | >= 0 |
|`:measurement/leukocyte-count` | >= 0 |
|`:measurement/nuclei-count` | >= 0 |
|`:measurement/average-depth` | >= 0 |
|`:measurement/fraction-aligned-reads` | [0, 1]|
|`:measurement/read-count` | >= 0 |
|`:measurement/read-count-otu` | >= 0 |
|`:measurement/read-count-otu-rarefied` | >= 0 |
|`:measurement/tumor-purity`| [0, 1]|
|`:measurement/contamination` | [0, 1]|
|`:measurement/tss-score` | >= 0 |
|`:measurement/fraction-reads-in-peaks` | [0, 1]|
|`:measurement/reads-count` | >= 0 |
|`:measurement/total-reads` | >= 0 |
|`:measurement/cfdna-ng-mL` | >= 0 |
|`:measurement/pg-mL` | >= 0 |
|`:measurement/ng-mL` | >= 0 |
|`:measurement/U-L` | >= 0 |
|`:measurement/nanostring-signature-score` | >= 0 |
|`:measurement/chromvar-tf-binding-score` | >= 0 |
|`:measurement/absolute-cn` | >= 0 |
|`:measurement/tmb-indel` | >= 0 |
|`:measurement/tmb-snv` | >= 0 |
|`:measurement/tmb-total` | >= 0 |
|`:measurement/tcr-count` | >= 0 |
|`:measurement/tcr-clonality` | [0, 1]|
|`:measurement/tcr-frequency` | [0, 1]|
|`:clinical-observation/pfs`  | >= 0 |
|`:clinical-observation/dfi`  | >= 0 |
|`:clinical-observation/os`  | >= 0 |
|`:clinical-observation/ttf`  | >= 0 |
|`:clinical-observation/bmi`  | >= 0 |
|`:clinical-observation/ldh`  | >= 0 |
|`:clinical-observation/absolute-leukocyte-count`  | >= 0 |
|`:clinical-observation/absolute-monocyte-count`  | >= 0 |
|`:timepoint/relative-order`| > 0|
|`:timepoint/offset`|> 0|
|`:study-day/day`| >= 0|
|`:therapy/line`|> 0|
|`:therapy/order`|> 0|
|`:atac-peak/-log10pvalue`  | >= 0 |
|`:atac-peak/-log10qvalue`  | >= 0 |
|`:atac-peak/quality-score`  | >= 0 |
|`:atac-peak/summit-offset`  | >= 0 |
|`:subject/age`  | >= 0 |

## Validation of required attributes for entity kinds


|kind | required attributes|
|-----|--------------------|
|`sample`| `:sample/id`<br>`:sample/subject`|
|`dataset`|`:dataset/name`<br>`:dataset/samples`<br>`:dataset/assays`<br>`:dataset/timepoints`<br>`:dataset/subjects`|
|`assay`|`:assay/name`<br>`:assay/measurement-sets`<br>`:assay/technology`|
|`measurement-set`|`:measurement-set/name`<br>`:measurement-set/measurements`|
|`clinical-observation-set`|`:clinical-observation-set/name`<br>`:clinical-observation-set/clinical-observations`|
|`clinical-observation`|`:clinical-observation/subject`<br>`:clinical-observation/timepoint`|
|`timepoint`|`:timepoint/id`<br>`:timepoint/type`<br>If, and only if, part of a treatment regimen:<br>`:timepoint/relative-order`<br>`:timepoint/treatment-regimen`|
|`genomic-coordinate`|`:genomic-coordinate/id`<br>`:genomic-coordinate/assembly`<br>`:genomic-coordinate/contig`<br>`:genomic-coordinate/start`<br>`:genomic-coordinate/end`<br>`:genomic-coordinate/strand`|
|`variant`|`:variant/id`<br>`:variant/genomic-coordinates`<br>`:variant/ref-allele`<br>`:variant/alt-allele`|
|`subject`|`:subject/id`|
|`treatment-regimen`|`:treatment-regimen/name`|
|`cell-population`|`:cell-population/name`<br>`:cell-population/from-clustering`<br>If `from-clustering`is false:<br>`:cell-population/cell-type`|
|`therapy`|`:therapy/treatment-regimen`<br>`:therapy/order`|
|`cnv`|`:cnv/genomic-coordinates`|
|`tcr`| `:tcr/id`<br>At least one of the following:<br>`:tcr/alhpa-1`<br>`:tcr/beta`|
|`measurement`|`:measurement/sample` (see below for more details)|
|`drug-regimen`|`:drug-regimen/drug`|
|`clinical-trial`|`:clinical-trial/nct-number`|
|`atac-peak`|`:atac-peak/name`<br>`:atac-peak/genomic-coordinates`<br>`:atac-peak/jointly-called`<br>`:atac-peak/fixed-width`|
|`otu`|`:otu/id`<br>`:otu/kingdom`|
|`meddra-disease`|`:meddra-disease/preferred-name`<br>`:meddra-disease/synonyms`|
|`gdc-anatomic-site`|`:gdc-anatomic-site/name`|
|`ctcae-adverse-event`|`:ctcae-adverse-event/name`<br>`:ctcae-adverse-event/meddra-code`|
|`gene-product`|`:gene-product/id`<br>`:gene-product/gene`|
|`nanostring-signature`|`:nanostring-signature/name`<br>`:nanostring-signature/genes`|
|`study-day`|`:study-day/id`<br>`:study-day/day`<br>`:study-day/reference-event`|

## Validation of measurements

Additionally measurements are further validated based on their kind (which is determined by their target)

|measurement kind|required attributes|allowed optional attributes|
|----------------|-------------------|-------------------|
|cell population|`:measurement/sample`<br>`:measurement/cell-population`|`:measurement/region-of-interest`<br>`:measurement/epitope`<br>`:measurement/percent-of-total-cells`<br>`:measurement/median-channel-value`<br>`:measurement/cell-count`<br>`:measurement/percent-of-nuclei`<br>`:measurement/percent-of-lymphocytes`<br>`:measurement/percent-of-leukocytes`<br>`:measurement/percent-of-live`<br>`:measurement/percent-of-parent`<br>`:measurement/percent-of-singlets`|
|variant|`:measurement/sample`<br>`:measurement/variant`|`:measurement/t-alt-count`<br>`:measurement/t-ref-count`<br>`:measurement/t-depth`<br>`:measurement/n-alt-count`<br>`:measurement/n-ref-count`<br>`:measurement/n-depth`<br>`:measurement/vaf`|
|cnv|`:measurement/sample`<br>`:measurement/cnv`|`:measurement/a-allele-cn`<br>`:measurement/b-allele-cn`<br>`:measurement/segment-mean-lrr`<br>`:measurement/loh`<br>`:measurement/baf`<br>`:measurement/baf-n`<br>`:measurement/absolute-cn`|
|gene-product|`:measurement/sample`<br>`:measurement/gene-product`|`:measurement/tpm`<br>`:measurement/fpkm`<br>`:measurement/rsem-raw-count`<br>`:measurement/rsem-normalized-count`<br>`:measurement/rsem-scaled-estimate`<br>`:measurement/rpkm`<br>`:measurement/nanostring-count`<br>`:measurement/read-count`|
|epitope|`:measurement/sample`<br>`:measurement/epitope`|`:measurement/median-channel-value`<br>`:measurement/ng-mL`<br>`:measurement/U-L`<br>`:measurement/nanostring-count`|
|nanostring-signature|`:measurement/sample`<br>`:measurement/nanostring-signature`<br>`:measurement/nanostring-signature-score`||
|atac-peak|`:measurement/sample`<br>`:measurement/atac-peak`<br>`:measurement/reads-count`||
|tcr|`:measurement/sample`<br>`:measurement/tcr-frequency`<br>`:measurement/tcr-count`<br>`:measurement/tcr-v`<br>`:measurement/tcr-d`<br>`:measurement/tcr-j`||
|otu|`:measurement/sample`<br>`:measurement/otu`|`:measurement/read-count-otu`<br>`:measurement/read-count-otu-rarefied`||
|measurement witout target|`:measurement/sample`|`:measurement/tcr-clonality`<br>`:measurement/cell-count`<br>`:measurement/singlets-count`<br>`:measurement/live-count`<br>`:measurement/lymphocyte-count`<br>`:measurement/leukocyte-count`<br>`:measurement/nuclei-count`<br>`:measurement/live-percent`<br>`:measurement/average-depth`<br>`:measurement/fraction-aligned-reads`<br>`:measurement/total-reads`<br>`:measurement/tumor-purity`<br>`:measurement/contamination`<br>`:measurement/tss-score`<br>`:measurement/fraction-reads-in-peaks`<br>`:measurement/cfdna-ng-mL`<br>`:measurement/tmb-indel`<br>`:measurement/tmb-snv`<br>`:measurement/tmb-total`|

Additionally every measurement, irrespective of kind, must have at least one of these attributes

```
:measurement/percent-of-total-cells
:measurement/median-channel-value
:measurement/cell-count
:measurement/percent-of-parent
:measurement/percent-of-nuclei
:measurement/percent-of-lymphocytes
:measurement/percent-of-leukocytes
:measurement/percent-of-live
:measurement/percent-of-singlets
:measurement/live-percent
:measurement/t-alt-count
:measurement/t-ref-count
:measurement/t-depth
:measurement/n-alt-count
:measurement/n-ref-count
:measurement/n-depth
:measurement/vaf
:measurement/a-allele-cn
:measurement/b-allele-cn
:measurement/segment-mean-lrr
:measurement/loh
:measurement/baf
:measurement/baf-n
:measurement/tpm
:measurement/fpkm
:measurement/rsem-raw-count
:measurement/rsem-normalized-count
:measurement/rsem-scaled-estimate
:measurement/rpkm
:measurement/nanostring-count
:measurement/median-channel-value
:measurement/ng-mL
:measurement/U-L
:measurement/tcr-clonality
:measurement/tcr-count
:measurement/tcr-frequency
:measurement/cell-count
:measurement/singlets-count
:measurement/live-count
:measurement/lymphocyte-count
:measurement/leukocyte-count
:measurement/nuclei-count
:measurement/average-depth
:measurement/fraction-aligned-reads
:measurement/read-count
:measurement/tumor-purity
:measurement/contamination
:measurement/tss-score
:measurement/fraction-reads-in-peaks
:measurement/chromvar-tf-binding-score
:measurement/nanostring-signature-score
:measurement/cfdna-ng-mL
:measurement/tmb-indel
:measurement/tmb-snv
:measurement/tmb-total
:measurement/variant
:measurement/absolute-cn
:measurement/protein-array-log-intensity
:measurement/luminex-mfi
:measurement/total-reads
:measurement/read-count
:measurement/read-count-otu
:measurement/read-count-otu-rarefied
```

## Composite id validations

`pret` also validates that certain ids are obtained by concatenating other entity attributes with a specific separator

|id | attributes | separator|
|---|------------|----------|
|`:genomic-coordinate/id`|`:genomic-coordinate/assembly`<br>`:genomic-coordinate/contig`<br>`:genomic-coordinate/strand`<br>`:genomic-coordinate/start`<br>`:genomic-coordinate/end`| `:`|
|`:variant/id`|`:variant/genomic-coordinates`<br>`:variant/ref-allele`<br>`:variant/alt-allele`| `/`|

